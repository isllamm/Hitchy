// P3-B03: searchRides, requestSeats, respondToRequest and cancelBooking against the emulators
// (BUILD_PLAN 8.6 with the v1.2 changes of 8.16). The happy path plus every money, permission
// and input check (plan 2.2 #8).
import { Timestamp } from "firebase-admin/firestore";
import { afterAll, beforeAll, beforeEach, describe, expect, it } from "vitest";
import { cancelBookingCore } from "../../src/handlers/booking/cancelBooking.js";
import { requestSeatsCore } from "../../src/handlers/booking/requestSeats.js";
import { respondToRequestCore } from "../../src/handlers/booking/respondToRequest.js";
import { searchRidesCore } from "../../src/handlers/booking/searchRides.js";
import { db } from "../../src/lib/app.js";
import { resetClock, setClock } from "../../src/lib/clock.js";
import { clearConfigCache } from "../../src/lib/config.js";
import type { CallCtx } from "../../src/lib/ctx.js";
import type { BookingDoc, RideDoc, UserDoc, WalletDoc } from "../../src/schemas/docs.js";
import type { SearchRidesInput } from "../../src/schemas/inputs.js";
import { runSeed } from "../../src/seed/seed.js";
import {
  FIXED_NOW,
  limitsConfig,
  pricingConfig,
  profileDoc,
  rideDoc,
  userDoc,
} from "../helpers/docs.js";

const HOST = "p3b03-host";
const HOST2 = "p3b03-host2";
const RIDER = "p3b03-rider";
const RIDER_WOMAN = "p3b03-rider-woman";

const ZONE_A = "p3b03-a"; // the rider's zone and the usual start
const ZONE_B = "p3b03-b"; // a stop zone
const ZONE_C = "p3b03-c"; // the destination

const DEPART = new Date("2026-11-02T06:30:00Z"); // 08:30 Cairo, 60 minutes after FIXED_NOW
const SEARCH_DATE = "2026-11-02";
const SEARCH_TIME = "08:30"; // Cairo

let requestIdCounter = 0;
function nextRequestId(): string {
  requestIdCounter += 1;
  return `c0000000-0000-4000-8000-${String(requestIdCounter).padStart(12, "0")}`;
}

function ctxFor(uid: string, user: UserDoc, at: Date = FIXED_NOW): CallCtx {
  return { uid, phone: null, isAdmin: false, user, now: at };
}

function searchInput(overrides: Partial<SearchRidesInput> = {}): SearchRidesInput {
  return {
    fromZone: ZONE_A,
    toZone: ZONE_C,
    fromPointId: "pt1",
    date: SEARCH_DATE,
    time: SEARCH_TIME,
    seats: 1,
    sort: "recommended",
    timeBand: "any",
    maxPrice: null,
    minRating: null,
    verifiedOnly: false,
    womenOnly: false,
    ...overrides,
  };
}

function riderDoc(overrides: Partial<UserDoc> = {}): UserDoc {
  const base = userDoc({ activeRole: "rider", host: { ...userDoc().host, status: "none" } });
  return { ...base, ...overrides };
}

async function setWallet(uid: string, balance: number): Promise<void> {
  const wallet: WalletDoc = { balance, updatedAt: Timestamp.fromDate(FIXED_NOW) };
  await db.doc(`wallets/${uid}`).set(wallet);
}

async function putRide(id: string, overrides: Partial<RideDoc> = {}): Promise<string> {
  await db.doc(`rides/${id}`).set(
    rideDoc({
      hostId: HOST,
      fromZone: ZONE_A,
      toZone: ZONE_C,
      pickupZones: [ZONE_A],
      departAt: Timestamp.fromDate(DEPART),
      ...overrides,
    }),
  );
  return id;
}

async function expectReason(promise: Promise<unknown>, reason: string): Promise<void> {
  await expect(promise).rejects.toMatchObject({ reason });
}

async function deleteCollection(path: string): Promise<void> {
  const snap = await db.collection(path).get();
  await Promise.all(snap.docs.map((d) => db.recursiveDelete(d.ref)));
}

async function bookingOf(rideId: string, riderUid: string): Promise<BookingDoc> {
  const snap = await db.doc(`rides/${rideId}/bookings/${riderUid}`).get();
  expect(snap.exists, `Expected a booking for ${riderUid} on ${rideId}`).toBe(true);
  return snap.data() as BookingDoc;
}

async function rideOf(rideId: string): Promise<RideDoc> {
  return (await db.doc(`rides/${rideId}`).get()).data() as RideDoc;
}

beforeAll(async () => {
  setClock(() => FIXED_NOW);

  for (const [zone, order] of [
    [ZONE_A, 1],
    [ZONE_B, 2],
    [ZONE_C, 3],
  ] as const) {
    await db
      .doc(`zones/${zone}`)
      .set({ name: { ar: "منطقة", en: `Zone ${String(order)}` }, active: true, order });
    await db
      .doc(`zones/${zone}/points/pt1`)
      .set({ name: { ar: "نقطة 1", en: "Point 1" }, lat: 30.0, lng: 31.0, active: true });
    await db
      .doc(`zones/${zone}/points/pt2`)
      .set({ name: { ar: "نقطة 2", en: "Point 2" }, lat: 30.05, lng: 31.05, active: true });
  }

  for (const [from, to] of [
    [ZONE_A, ZONE_C],
    [ZONE_B, ZONE_C],
  ] as const) {
    await db
      .doc(`routes/${from}__${to}`)
      .set({ fromZone: from, toZone: to, distanceKm: 55, usualMinutes: 65 });
  }
});

beforeEach(async () => {
  await db.doc("config/pricing").set(pricingConfig());
  await db.doc("config/limits").set(limitsConfig());
  clearConfigCache();

  await deleteCollection("rides");
  await deleteCollection("idempotency");

  await db.doc(`users/${HOST}`).set(userDoc({ firstName: "Owner" }));
  await db.doc(`users/${HOST2}`).set(userDoc({ firstName: "Owner2" }));
  await db.doc(`users/${RIDER}`).set(riderDoc({ firstName: "Rider" }));
  await db.doc(`users/${RIDER_WOMAN}`).set(riderDoc({ firstName: "RiderW", gender: "female" }));

  await db.doc(`profiles/${HOST}`).set(profileDoc({ firstName: "Owner" }));
  await db.doc(`profiles/${HOST2}`).set(profileDoc({ firstName: "Owner2" }));

  for (const uid of [RIDER, RIDER_WOMAN]) {
    await setWallet(uid, 1000);
  }
  await setWallet(HOST, 0);
});

afterAll(async () => {
  resetClock();
  await deleteCollection("rides");
  await deleteCollection("idempotency");
  for (const uid of [HOST, HOST2, RIDER, RIDER_WOMAN]) {
    await db.recursiveDelete(db.doc(`users/${uid}`));
    await db.doc(`profiles/${uid}`).delete();
    await db.recursiveDelete(db.doc(`wallets/${uid}`));
  }
  for (const zone of [ZONE_A, ZONE_B, ZONE_C]) {
    await db.recursiveDelete(db.doc(`zones/${zone}`));
  }
  await db.doc(`routes/${ZONE_A}__${ZONE_C}`).delete();
  await db.doc(`routes/${ZONE_B}__${ZONE_C}`).delete();
  await runSeed({ project: "demo-hitchy", silent: true });
});

describe("searchRides", () => {
  it("returns the ride with its card, pickup and walk distance", async () => {
    await putRide("ride-1");

    const out = await searchRidesCore(RIDER, searchInput());

    expect(out.rides).toHaveLength(1);
    const card = out.rides[0];
    expect(card?.rideId).toBe("ride-1");
    expect(card?.pricePerSeat).toBe(56);
    expect(card?.fairPrice).toBe(true);
    expect(card?.seatsLeft).toBe(3);
    expect(card?.departAt).toBe("2026-11-02T06:30:00.000Z");
    expect(card?.arriveAt).toBe("2026-11-02T07:35:00.000Z"); // departAt + 65 usual minutes
    expect(card?.pickup.id).toBe("start");
    expect(card?.pickup.at).toBe("2026-11-02T06:30:00.000Z");
    expect(card?.pickup.walkM).toBe(0); // the rider's point is the pickup point
    expect(card?.walkM).toBe(0);
    expect(card?.bestMatch).toBe(true);
    expect(card?.host.uid).toBe(HOST);
    expect(card?.host.firstName).toBe("Owner");
    expect(card?.host.sameCompany).toBe(false);
  });

  it("never puts a plate or a phone number in a card", async () => {
    await putRide("ride-1");
    const out = await searchRidesCore(RIDER, searchInput());
    const text = JSON.stringify(out);
    expect(text).not.toContain("TEST-PLATE");
    expect(text).not.toContain("test-phone");
  });

  it("finds a ride by a stop in the rider's zone (v1.2)", async () => {
    // Leaves zone C's neighbour A, stops in B at 08:40 Cairo, ends in C.
    await putRide("ride-stop", {
      fromZone: ZONE_A,
      pickupZones: [ZONE_A, ZONE_B],
      stops: [
        {
          id: "s1",
          zone: ZONE_B,
          pointId: "pt1",
          pointName: { ar: "نقطة 1", en: "Point 1" },
          point: { lat: 30.0, lng: 31.0 },
          at: Timestamp.fromDate(new Date("2026-11-02T06:40:00Z")),
        },
      ],
    });

    const out = await searchRidesCore(RIDER, searchInput({ fromZone: ZONE_B, time: "08:40" }));

    expect(out.rides).toHaveLength(1);
    expect(out.rides[0]?.pickup.id).toBe("s1");
    expect(out.rides[0]?.pickup.at).toBe("2026-11-02T06:40:00.000Z");
  });

  it("keeps a ride that left before the window but picks the rider up inside it", async () => {
    // Departs 07:10 Cairo, 80 minutes before the rider's 08:30, so departAt is outside the
    // 60-minute window; its stop at 08:20 is inside it.
    await putRide("ride-early", {
      departAt: Timestamp.fromDate(new Date("2026-11-02T05:10:00Z")),
      pickupZones: [ZONE_A, ZONE_B],
      stops: [
        {
          id: "s1",
          zone: ZONE_B,
          pointId: "pt1",
          pointName: { ar: "نقطة 1", en: "Point 1" },
          point: { lat: 30.0, lng: 31.0 },
          at: Timestamp.fromDate(new Date("2026-11-02T06:20:00Z")),
        },
      ],
    });

    const out = await searchRidesCore(RIDER, searchInput({ fromZone: ZONE_B }));
    expect(out.rides).toHaveLength(1);
    expect(out.rides[0]?.pickup.id).toBe("s1");
  });

  it("drops a ride whose pickup is outside the rider's window", async () => {
    await putRide("ride-late", {
      departAt: Timestamp.fromDate(new Date("2026-11-02T07:45:00Z")), // 09:45 Cairo
    });
    const out = await searchRidesCore(RIDER, searchInput());
    expect(out.rides).toHaveLength(0);
  });

  it("drops the caller's own ride, a full ride and a ride with too few seats", async () => {
    await putRide("ride-own", { hostId: RIDER });
    await putRide("ride-full", { seatsLeft: 0, status: "full" });
    await putRide("ride-one-seat", { seatsLeft: 1 });

    const out = await searchRidesCore(RIDER, searchInput({ seats: 2 }));
    expect(out.rides).toHaveLength(0);
  });

  it("keeps Women-only rides away from men and hides other rides when asked for", async () => {
    await putRide("ride-women", { womenOnly: true });
    await putRide("ride-open");

    const asMan = await searchRidesCore(RIDER, searchInput());
    expect(asMan.rides.map((r) => r.rideId)).toEqual(["ride-open"]);

    const asWoman = await searchRidesCore(RIDER_WOMAN, searchInput());
    expect(asWoman.rides.map((r) => r.rideId).sort()).toEqual(["ride-open", "ride-women"]);

    const onlyWomen = await searchRidesCore(RIDER_WOMAN, searchInput({ womenOnly: true }));
    expect(onlyWomen.rides.map((r) => r.rideId)).toEqual(["ride-women"]);
  });

  it("drops a suspended owner and anyone blocked either way", async () => {
    await putRide("ride-1");

    await db.doc(`users/${HOST}`).set(userDoc({ suspended: true }));
    expect((await searchRidesCore(RIDER, searchInput())).rides).toHaveLength(0);

    await db.doc(`users/${HOST}`).set(userDoc());
    await db.doc(`users/${RIDER}`).set(riderDoc({ blockedUids: [HOST] }));
    expect((await searchRidesCore(RIDER, searchInput())).rides).toHaveLength(0);

    await db.doc(`users/${RIDER}`).set(riderDoc());
    await db.doc(`users/${HOST}`).set(userDoc({ blockedUids: [RIDER] }));
    expect((await searchRidesCore(RIDER, searchInput())).rides).toHaveLength(0);
  });

  it("applies the Verified only, maximum price and minimum rating filters", async () => {
    await putRide("ride-plain");
    await putRide("ride-good", { hostId: HOST2, pricePerSeat: 45 });

    await db.doc(`profiles/${HOST2}`).set(
      profileDoc({
        firstName: "Owner2",
        verified: true,
        hostRating: { avg: 4.9, count: 12 },
      }),
    );

    const verified = await searchRidesCore(RIDER, searchInput({ verifiedOnly: true }));
    expect(verified.rides.map((r) => r.rideId)).toEqual(["ride-good"]);

    const cheap = await searchRidesCore(RIDER, searchInput({ maxPrice: 50 }));
    expect(cheap.rides.map((r) => r.rideId)).toEqual(["ride-good"]);

    const rated = await searchRidesCore(RIDER, searchInput({ minRating: 4.8 }));
    expect(rated.rides.map((r) => r.rideId)).toEqual(["ride-good"]);
  });

  it("splits the window with the time chips", async () => {
    await putRide("ride-before", {
      departAt: Timestamp.fromDate(new Date("2026-11-02T06:00:00Z")), // 08:00 Cairo
    });
    await putRide("ride-soon", { hostId: HOST2 }); // 08:30 Cairo, the picked time
    await putRide("ride-later", {
      hostId: HOST2,
      departAt: Timestamp.fromDate(new Date("2026-11-02T07:20:00Z")), // 09:20 Cairo
    });

    const before = await searchRidesCore(RIDER, searchInput({ timeBand: "before" }));
    expect(before.rides.map((r) => r.rideId)).toEqual(["ride-before"]);

    const firstHour = await searchRidesCore(RIDER, searchInput({ timeBand: "first_hour" }));
    expect(firstHour.rides.map((r) => r.rideId).sort()).toEqual(["ride-later", "ride-soon"]);

    const any = await searchRidesCore(RIDER, searchInput({ timeBand: "any" }));
    expect(any.rides).toHaveLength(3);
  });

  it("sorts by earliest, by cheapest and by the ranking", async () => {
    await putRide("ride-cheap-late", {
      pricePerSeat: 42,
      departAt: Timestamp.fromDate(new Date("2026-11-02T07:00:00Z")),
    });
    await putRide("ride-dear-early", {
      hostId: HOST2,
      pricePerSeat: 67,
      departAt: Timestamp.fromDate(new Date("2026-11-02T06:10:00Z")),
    });

    const earliest = await searchRidesCore(RIDER, searchInput({ sort: "earliest" }));
    expect(earliest.rides.map((r) => r.rideId)).toEqual(["ride-dear-early", "ride-cheap-late"]);
    expect(earliest.rides.every((r) => !r.bestMatch)).toBe(true);

    const cheapest = await searchRidesCore(RIDER, searchInput({ sort: "cheapest" }));
    expect(cheapest.rides.map((r) => r.rideId)).toEqual(["ride-cheap-late", "ride-dear-early"]);

    // Recommended puts the Verified owner with a good rating first, and marks it best match.
    await db
      .doc(`profiles/${HOST2}`)
      .set(profileDoc({ firstName: "Owner2", verified: true, hostRating: { avg: 5, count: 20 } }));
    const recommended = await searchRidesCore(RIDER, searchInput({ sort: "recommended" }));
    expect(recommended.rides[0]?.rideId).toBe("ride-dear-early");
    expect(recommended.rides[0]?.bestMatch).toBe(true);
    expect(recommended.rides[1]?.bestMatch).toBe(false);
  });

  it("marks a ride with the same company without naming it", async () => {
    await db.doc(`users/${HOST}`).set(userDoc({ companyId: "acme" }));
    await db.doc(`users/${RIDER}`).set(riderDoc({ companyId: "acme" }));
    await putRide("ride-1", { hostCompanyId: "acme" });

    const out = await searchRidesCore(RIDER, searchInput());
    expect(out.rides[0]?.host.sameCompany).toBe(true);
    expect(JSON.stringify(out)).not.toContain("acme");
  });
});

describe("requestSeats", () => {
  const input = (overrides: Partial<Parameters<typeof requestSeatsCore>[0]> = {}) => ({
    rideId: "ride-1",
    seats: 1,
    pickupId: "start",
    requestId: nextRequestId(),
    ...overrides,
  });

  it("holds the seats and copies the pickup onto the booking", async () => {
    await putRide("ride-1");

    const out = await requestSeatsCore(input({ seats: 2 }), ctxFor(RIDER, riderDoc()));

    expect(out.booking.status).toBe("requested");
    expect(out.booking.seats).toBe(2);
    expect(out.booking.price).toBe(56);
    expect(out.booking.total).toBe(112);
    expect(out.ride.rideId).toBe("ride-1");

    const booking = await bookingOf("ride-1", RIDER);
    expect(booking.hostId).toBe(HOST);
    expect(booking.pickupId).toBe("start");
    expect(booking.pickupPointName.en).toBe("Point");
    expect(booking.pickupAt.toDate().toISOString()).toBe("2026-11-02T06:30:00.000Z");
    expect(booking.paymentConfirmedAt).toBeNull();
    expect(booking.hostArrivedAt).toBeNull();

    // The seats are held while the owner decides (R20).
    expect((await rideOf("ride-1")).seatsLeft).toBe(1);
  });

  it("marks the ride full when the last seat is held", async () => {
    await putRide("ride-1", { seatsLeft: 1, seatsTotal: 1 });
    await requestSeatsCore(input(), ctxFor(RIDER, riderDoc()));

    const ride = await rideOf("ride-1");
    expect(ride.seatsLeft).toBe(0);
    expect(ride.status).toBe("full");
  });

  it("gives the owner 30 minutes to answer, but never past the booking cutoff", async () => {
    // Far from departure the 30-minute deadline wins: 05:30Z + 30 minutes.
    await putRide("ride-1");
    const far = await requestSeatsCore(input(), ctxFor(RIDER, riderDoc()));
    expect(far.booking.expiresAt).toBe("2026-11-02T06:00:00.000Z");

    // Close to departure the cutoff wins: 15 minutes before a 06:00Z departure [DECISION-21].
    // A second rider asks, because these two rides are inside one rider's rideGapMinutes.
    await putRide("ride-2", {
      hostId: HOST2,
      departAt: Timestamp.fromDate(new Date("2026-11-02T06:00:00Z")),
    });
    const near = await requestSeatsCore(
      input({ rideId: "ride-2" }),
      ctxFor(RIDER_WOMAN, riderDoc({ gender: "female" })),
    );
    expect(near.booking.expiresAt).toBe("2026-11-02T05:45:00.000Z");
  });

  it("runs once for the same requestId", async () => {
    await putRide("ride-1");
    const same = input();
    const first = await requestSeatsCore(same, ctxFor(RIDER, riderDoc()));
    const second = await requestSeatsCore(same, ctxFor(RIDER, riderDoc()));

    expect(second.booking.expiresAt).toBe(first.booking.expiresAt);
    expect((await rideOf("ride-1")).seatsLeft).toBe(2);
  });

  it("refuses an unknown ride and a ride that is not open", async () => {
    await expectReason(
      requestSeatsCore(input({ rideId: "nope" }), ctxFor(RIDER, riderDoc())),
      "NOT_FOUND",
    );
    await putRide("ride-1", { status: "started" });
    await expectReason(requestSeatsCore(input(), ctxFor(RIDER, riderDoc())), "RIDE_NOT_OPEN");
  });

  it("refuses a pickup that is not on the ride", async () => {
    await putRide("ride-1");
    await expectReason(
      requestSeatsCore(input({ pickupId: "s2" }), ctxFor(RIDER, riderDoc())),
      "PICKUP_NOT_ON_RIDE",
    );
  });

  it("refuses a request inside the booking cutoff", async () => {
    await putRide("ride-1", {
      departAt: Timestamp.fromDate(new Date("2026-11-02T05:40:00Z")), // 10 minutes away
    });
    await expectReason(requestSeatsCore(input(), ctxFor(RIDER, riderDoc())), "TOO_LATE_TO_BOOK");
  });

  it("refuses the owner's own ride", async () => {
    await putRide("ride-1");
    await expectReason(requestSeatsCore(input(), ctxFor(HOST, userDoc())), "OWN_RIDE");
  });

  it("hides a block behind RIDE_NOT_OPEN, either way round", async () => {
    await putRide("ride-1");

    await db.doc(`users/${RIDER}`).set(riderDoc({ blockedUids: [HOST] }));
    await expectReason(
      requestSeatsCore(input(), ctxFor(RIDER, riderDoc({ blockedUids: [HOST] }))),
      "RIDE_NOT_OPEN",
    );

    await db.doc(`users/${RIDER}`).set(riderDoc());
    await db.doc(`users/${HOST}`).set(userDoc({ blockedUids: [RIDER] }));
    await expectReason(requestSeatsCore(input(), ctxFor(RIDER, riderDoc())), "RIDE_NOT_OPEN");
  });

  it("refuses more seats than are left", async () => {
    await putRide("ride-1", { seatsLeft: 1 });
    await expectReason(
      requestSeatsCore(input({ seats: 2 }), ctxFor(RIDER, riderDoc())),
      "RIDE_FULL",
    );
  });

  it("refuses a second request on the same ride but allows one after a decline", async () => {
    await putRide("ride-1");
    await requestSeatsCore(input(), ctxFor(RIDER, riderDoc()));
    await expectReason(requestSeatsCore(input(), ctxFor(RIDER, riderDoc())), "ALREADY_BOOKED");

    await respondToRequestCore(
      { rideId: "ride-1", riderUid: RIDER, accept: false },
      ctxFor(HOST, userDoc()),
    );
    const again = await requestSeatsCore(input(), ctxFor(RIDER, riderDoc()));
    expect(again.booking.status).toBe("requested");
  });

  it("refuses a rider with an unpaid fee", async () => {
    await putRide("ride-1");
    await db.doc(`users/${RIDER}`).set(riderDoc({ rider: { ...riderDoc().rider, unpaidFees: 1 } }));
    await expectReason(requestSeatsCore(input(), ctxFor(RIDER, riderDoc())), "UNPAID_FEE");
  });

  it("refuses a request the wallet cannot cover, counting what is already owed", async () => {
    await putRide("ride-1");
    await setWallet(RIDER, 100);

    // 2 seats at 56 is 112, more than the 100 in the wallet.
    await expectReason(
      requestSeatsCore(input({ seats: 2 }), ctxFor(RIDER, riderDoc())),
      "INSUFFICIENT_BALANCE",
    );

    // One seat fits, and then a second ride's seat no longer does [DECISION-29].
    await requestSeatsCore(input(), ctxFor(RIDER, riderDoc()));
    await putRide("ride-2", {
      hostId: HOST2,
      departAt: Timestamp.fromDate(new Date("2026-11-02T09:30:00Z")),
    });
    await expectReason(
      requestSeatsCore(input({ rideId: "ride-2" }), ctxFor(RIDER, riderDoc())),
      "INSUFFICIENT_BALANCE",
    );
  });

  it("keeps a man off a Women-only ride", async () => {
    await putRide("ride-1", { womenOnly: true });
    await expectReason(requestSeatsCore(input(), ctxFor(RIDER, riderDoc())), "WOMEN_ONLY");

    const out = await requestSeatsCore(
      input(),
      ctxFor(RIDER_WOMAN, riderDoc({ gender: "female" })),
    );
    expect(out.booking.status).toBe("requested");
  });

  it("refuses two bookings inside rideGapMinutes of each other", async () => {
    await putRide("ride-1");
    await putRide("ride-2", {
      hostId: HOST2,
      departAt: Timestamp.fromDate(new Date("2026-11-02T07:00:00Z")), // 30 minutes later
    });

    await requestSeatsCore(input(), ctxFor(RIDER, riderDoc()));
    await expectReason(
      requestSeatsCore(input({ rideId: "ride-2" }), ctxFor(RIDER, riderDoc())),
      "TIME_CONFLICT",
    );
  });
});

describe("respondToRequest", () => {
  async function request(rideId = "ride-1", seats = 1): Promise<void> {
    await requestSeatsCore(
      { rideId, seats, pickupId: "start", requestId: nextRequestId() },
      ctxFor(RIDER, riderDoc()),
    );
  }

  it("accepts a request and counts the booking", async () => {
    await putRide("ride-1");
    await request();

    const out = await respondToRequestCore(
      { rideId: "ride-1", riderUid: RIDER, accept: true },
      ctxFor(HOST, userDoc()),
    );

    expect(out.status).toBe("booked");
    const booking = await bookingOf("ride-1", RIDER);
    expect(booking.status).toBe("booked");
    expect(booking.respondedAt).not.toBeNull();
    expect((await rideOf("ride-1")).bookingsCount).toBe(1);
  });

  it("declines a request, gives the seats back and reopens a full ride", async () => {
    await putRide("ride-1", { seatsLeft: 1, seatsTotal: 1 });
    await request();
    expect((await rideOf("ride-1")).status).toBe("full");

    const out = await respondToRequestCore(
      { rideId: "ride-1", riderUid: RIDER, accept: false },
      ctxFor(HOST, userDoc()),
    );

    expect(out.status).toBe("declined");
    const ride = await rideOf("ride-1");
    expect(ride.seatsLeft).toBe(1);
    expect(ride.status).toBe("open");
    expect(ride.bookingsCount).toBe(0);
  });

  it("refuses anyone who is not the ride's owner", async () => {
    await putRide("ride-1");
    await request();
    await expectReason(
      respondToRequestCore(
        { rideId: "ride-1", riderUid: RIDER, accept: true },
        ctxFor(HOST2, userDoc()),
      ),
      "NOT_FOUND",
    );
  });

  it("refuses a request that was already answered, or has run out of time", async () => {
    await putRide("ride-1");
    await request();
    await respondToRequestCore(
      { rideId: "ride-1", riderUid: RIDER, accept: true },
      ctxFor(HOST, userDoc()),
    );
    await expectReason(
      respondToRequestCore(
        { rideId: "ride-1", riderUid: RIDER, accept: true },
        ctxFor(HOST, userDoc()),
      ),
      "REQUEST_NOT_PENDING",
    );

    await putRide("ride-2", {
      hostId: HOST,
      departAt: Timestamp.fromDate(new Date("2026-11-02T09:30:00Z")),
    });
    await requestSeatsCore(
      { rideId: "ride-2", seats: 1, pickupId: "start", requestId: nextRequestId() },
      ctxFor(RIDER, riderDoc()),
    );
    // An hour later the 30-minute deadline has passed.
    await expectReason(
      respondToRequestCore(
        { rideId: "ride-2", riderUid: RIDER, accept: true },
        ctxFor(HOST, userDoc(), new Date("2026-11-02T06:40:00Z")),
      ),
      "REQUEST_EXPIRED",
    );
  });

  it("refuses a booking that does not exist", async () => {
    await putRide("ride-1");
    await expectReason(
      respondToRequestCore(
        { rideId: "ride-1", riderUid: RIDER, accept: true },
        ctxFor(HOST, userDoc()),
      ),
      "BOOKING_NOT_FOUND",
    );
  });
});

describe("cancelBooking", () => {
  async function requestAndMaybeAccept(accept: boolean): Promise<void> {
    await requestSeatsCore(
      { rideId: "ride-1", seats: 2, pickupId: "start", requestId: nextRequestId() },
      ctxFor(RIDER, riderDoc()),
    );
    if (accept) {
      await respondToRequestCore(
        { rideId: "ride-1", riderUid: RIDER, accept: true },
        ctxFor(HOST, userDoc()),
      );
    }
  }

  it("cancels a waiting request for free and gives every seat back", async () => {
    await putRide("ride-1");
    await requestAndMaybeAccept(false);

    const out = await cancelBookingCore(
      { rideId: "ride-1", reason: "plans_changed" },
      ctxFor(RIDER, riderDoc()),
    );

    expect(out.lateCancel).toBe(false);
    const booking = await bookingOf("ride-1", RIDER);
    expect(booking.status).toBe("cancelled_by_rider");
    expect(booking.cancelReason).toBe("plans_changed");
    expect((await rideOf("ride-1")).seatsLeft).toBe(3);
  });

  it("does not flag a late cancel outside the free window", async () => {
    // Departure is 3 hours away, well outside the 60-minute free-cancel window.
    await putRide("ride-1", {
      departAt: Timestamp.fromDate(new Date("2026-11-02T08:30:00Z")),
    });
    await requestAndMaybeAccept(true);

    const out = await cancelBookingCore(
      { rideId: "ride-1", reason: "found_another_ride" },
      ctxFor(RIDER, riderDoc()),
    );

    expect(out.lateCancel).toBe(false);
    const ride = await rideOf("ride-1");
    expect(ride.seatsLeft).toBe(3);
    expect(ride.bookingsCount).toBe(0);
    const rider = (await db.doc(`users/${RIDER}`).get()).data() as UserDoc;
    expect(rider.rider.lateCancels).toBe(0);
  });

  it("flags a late cancel inside the free window and counts it against the rider", async () => {
    await putRide("ride-1");
    await requestAndMaybeAccept(true);

    // 06:00Z is 30 minutes before the 06:30Z departure, inside freeCancelMinutes of 60.
    const out = await cancelBookingCore(
      { rideId: "ride-1", reason: "plans_changed" },
      ctxFor(RIDER, riderDoc(), new Date("2026-11-02T06:00:00Z")),
    );

    expect(out.lateCancel).toBe(true);
    expect((await bookingOf("ride-1", RIDER)).lateCancel).toBe(true);
    const rider = (await db.doc(`users/${RIDER}`).get()).data() as UserDoc;
    expect(rider.rider.lateCancels).toBe(1);
  });

  it("refuses the owner-is-late reason until trip presence exists", async () => {
    await putRide("ride-1");
    await requestAndMaybeAccept(true);
    await expectReason(
      cancelBookingCore({ rideId: "ride-1", reason: "host_late" }, ctxFor(RIDER, riderDoc())),
      "HOST_NOT_LATE",
    );
  });

  it("refuses a booking that is not there or already cancelled", async () => {
    await putRide("ride-1");
    await expectReason(
      cancelBookingCore({ rideId: "ride-1", reason: "other" }, ctxFor(RIDER, riderDoc())),
      "BOOKING_NOT_FOUND",
    );

    await requestAndMaybeAccept(false);
    await cancelBookingCore({ rideId: "ride-1", reason: "other" }, ctxFor(RIDER, riderDoc()));
    await expectReason(
      cancelBookingCore({ rideId: "ride-1", reason: "other" }, ctxFor(RIDER, riderDoc())),
      "BOOKING_NOT_FOUND",
    );
  });

  it("refuses cancelling an accepted booking once the ride has started", async () => {
    await putRide("ride-1");
    await requestAndMaybeAccept(true);
    await db.doc("rides/ride-1").update({ status: "started" });

    await expectReason(
      cancelBookingCore({ rideId: "ride-1", reason: "plans_changed" }, ctxFor(RIDER, riderDoc())),
      "RIDE_HAS_STARTED",
    );
  });
});
