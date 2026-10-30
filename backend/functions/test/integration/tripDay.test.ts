// P3-B04: the trip day against the emulators (BUILD_PLAN 8.7 with the free wait of 8.16).
// The happy path plus every money, permission and input check (plan 2.2 #8).
import { Timestamp } from "firebase-admin/firestore";
import { afterAll, beforeAll, beforeEach, describe, expect, it } from "vitest";
import { cancelBookingCore } from "../../src/handlers/booking/cancelBooking.js";
import { requestSeatsCore } from "../../src/handlers/booking/requestSeats.js";
import { respondToRequestCore } from "../../src/handlers/booking/respondToRequest.js";
import { arriveAtStopCore } from "../../src/handlers/trip/arriveAtStop.js";
import { checkInCore } from "../../src/handlers/trip/checkIn.js";
import { finishRideHandler } from "../../src/handlers/trip/finishRide.js";
import { markBoardedCore } from "../../src/handlers/trip/markBoarded.js";
import { reportLateCore } from "../../src/handlers/trip/reportLate.js";
import { startRideCore } from "../../src/handlers/trip/startRide.js";
import { db } from "../../src/lib/app.js";
import { resetClock, setClock } from "../../src/lib/clock.js";
import { clearConfigCache } from "../../src/lib/config.js";
import type { CallCtx } from "../../src/lib/ctx.js";
import type {
  BookingDoc,
  FeeDoc,
  LedgerEntry,
  RideDoc,
  UserDoc,
  WalletDoc,
} from "../../src/schemas/docs.js";
import { runSeed } from "../../src/seed/seed.js";
import { FIXED_NOW, limitsConfig, pricingConfig, rideDoc, userDoc } from "../helpers/docs.js";

const HOST = "p3b04-host";
const HOST2 = "p3b04-host2";
const RIDER = "p3b04-rider";
const RIDER2 = "p3b04-rider2";
const STRANGER = "p3b04-stranger";

const ZONE_A = "p3b04-a";
const ZONE_B = "p3b04-b";
const ZONE_C = "p3b04-c";

const DEPART = new Date("2026-11-02T06:30:00Z"); // 08:30 Cairo
const START_AT = new Date("2026-11-02T06:10:00Z"); // inside the 30-minute start window
const ARRIVED_AT = new Date("2026-11-02T06:15:00Z"); // the owner taps "I'm here"
const GRACE_OVER_AT = new Date("2026-11-02T06:21:00Z"); // 6 minutes later, past the 5 free ones
const NO_SHOW_AT = new Date("2026-11-02T06:31:00Z"); // 16 minutes later, past noShowMinutes

const STOP_AT = new Date("2026-11-02T06:45:00Z");

let requestIdCounter = 0;
function nextRequestId(): string {
  requestIdCounter += 1;
  return `d0000000-0000-4000-8000-${String(requestIdCounter).padStart(12, "0")}`;
}

function ctxFor(uid: string, user: UserDoc, at: Date = FIXED_NOW): CallCtx {
  return { uid, phone: null, isAdmin: false, user, now: at };
}

function riderDoc(overrides: Partial<UserDoc> = {}): UserDoc {
  const base = userDoc({ activeRole: "rider", host: { ...userDoc().host, status: "none" } });
  return { ...base, ...overrides };
}

const stop = {
  id: "s1",
  zone: ZONE_B,
  pointId: "pt1",
  pointName: { ar: "نقطة 1", en: "Point 1" },
  point: { lat: 30.0, lng: 31.0 },
  at: Timestamp.fromDate(STOP_AT),
};

async function putRide(id: string, overrides: Partial<RideDoc> = {}): Promise<void> {
  await db.doc(`rides/${id}`).set(
    rideDoc({
      hostId: HOST,
      fromZone: ZONE_A,
      toZone: ZONE_C,
      pickupZones: [ZONE_A, ZONE_B],
      stops: [stop],
      departAt: Timestamp.fromDate(DEPART),
      ...overrides,
    }),
  );
}

async function expectReason(promise: Promise<unknown>, reason: string): Promise<void> {
  await expect(promise).rejects.toMatchObject({ reason });
}

async function deleteCollection(path: string): Promise<void> {
  const snap = await db.collection(path).get();
  await Promise.all(snap.docs.map((d) => db.recursiveDelete(d.ref)));
}

async function bookingOf(rideId: string, riderUid: string): Promise<BookingDoc> {
  return (await db.doc(`rides/${rideId}/bookings/${riderUid}`).get()).data() as BookingDoc;
}

async function rideOf(rideId: string): Promise<RideDoc> {
  return (await db.doc(`rides/${rideId}`).get()).data() as RideDoc;
}

async function userOf(uid: string): Promise<UserDoc> {
  return (await db.doc(`users/${uid}`).get()).data() as UserDoc;
}

// Books a rider onto the ride and has the owner accept, at the chosen pickup.
async function book(
  rideId: string,
  riderUid: string,
  pickupId = "start",
  seats = 1,
): Promise<void> {
  await requestSeatsCore(
    { rideId, seats, pickupId, requestId: nextRequestId() },
    ctxFor(riderUid, riderDoc()),
  );
  await respondToRequestCore({ rideId, riderUid, accept: true }, ctxFor(HOST, userDoc()));
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
  }
  await db
    .doc(`routes/${ZONE_A}__${ZONE_C}`)
    .set({ fromZone: ZONE_A, toZone: ZONE_C, distanceKm: 55, usualMinutes: 65 });
});

beforeEach(async () => {
  await db.doc("config/pricing").set(pricingConfig());
  await db.doc("config/limits").set(limitsConfig());
  clearConfigCache();

  await deleteCollection("rides");
  await deleteCollection("idempotency");
  await deleteCollection("fees");

  // The owner is past the free month with credits to spend.
  await db.doc(`users/${HOST}`).set(userDoc({ firstName: "Owner" }));
  await db.doc(`users/${HOST2}`).set(userDoc({ firstName: "Owner2" }));
  for (const uid of [RIDER, RIDER2, STRANGER]) {
    await db.recursiveDelete(db.doc(`users/${uid}`));
    await db.doc(`users/${uid}`).set(riderDoc({ firstName: "Rider" }));
    const wallet: WalletDoc = { balance: 1000, updatedAt: Timestamp.fromDate(FIXED_NOW) };
    await db.doc(`wallets/${uid}`).set(wallet);
  }
  await db.recursiveDelete(db.collection(`users/${HOST}/ledger`));
});

afterAll(async () => {
  resetClock();
  await deleteCollection("rides");
  await deleteCollection("idempotency");
  await deleteCollection("fees");
  for (const uid of [HOST, HOST2, RIDER, RIDER2, STRANGER]) {
    await db.recursiveDelete(db.doc(`users/${uid}`));
    await db.recursiveDelete(db.doc(`wallets/${uid}`));
  }
  for (const zone of [ZONE_A, ZONE_B, ZONE_C]) {
    await db.recursiveDelete(db.doc(`zones/${zone}`));
  }
  await db.doc(`routes/${ZONE_A}__${ZONE_C}`).delete();
  await runSeed({ project: "demo-hitchy", silent: true });
});

describe("startRide", () => {
  it("starts the ride inside the start window", async () => {
    await putRide("ride-1");
    await startRideCore({ rideId: "ride-1" }, ctxFor(HOST, userDoc(), START_AT));

    const ride = await rideOf("ride-1");
    expect(ride.status).toBe("started");
    expect(ride.startedAt?.toDate().toISOString()).toBe(START_AT.toISOString());
  });

  it("refuses anyone who is not the owner", async () => {
    await putRide("ride-1");
    await expectReason(
      startRideCore({ rideId: "ride-1" }, ctxFor(HOST2, userDoc(), START_AT)),
      "NOT_FOUND",
    );
  });

  it("refuses a ride that is not open and one outside the start window", async () => {
    await putRide("ride-1");
    await startRideCore({ rideId: "ride-1" }, ctxFor(HOST, userDoc(), START_AT));
    await expectReason(
      startRideCore({ rideId: "ride-1" }, ctxFor(HOST, userDoc(), START_AT)),
      "RIDE_NOT_OPEN",
    );

    await putRide("ride-2");
    // An hour before departure is outside the 30-minute window.
    await expectReason(
      startRideCore({ rideId: "ride-2" }, ctxFor(HOST, userDoc(), FIXED_NOW)),
      "START_WINDOW_CLOSED",
    );
  });
});

describe("arriveAtStop", () => {
  it("starts the free wait for the riders at that pickup only", async () => {
    await putRide("ride-1", { seatsTotal: 3, seatsLeft: 3 });
    await book("ride-1", RIDER, "start");
    await book("ride-1", RIDER2, "s1");
    await startRideCore({ rideId: "ride-1" }, ctxFor(HOST, userDoc(), START_AT));

    const out = await arriveAtStopCore(
      { rideId: "ride-1", pickupId: "start" },
      ctxFor(HOST, userDoc(), ARRIVED_AT),
    );

    expect(out.bookings).toBe(1);
    expect((await bookingOf("ride-1", RIDER)).hostArrivedAt?.toDate().toISOString()).toBe(
      ARRIVED_AT.toISOString(),
    );
    expect((await bookingOf("ride-1", RIDER2)).hostArrivedAt).toBeNull();
  });

  it("keeps the first arrival time when the owner taps again", async () => {
    await putRide("ride-1");
    await book("ride-1", RIDER, "start");
    await startRideCore({ rideId: "ride-1" }, ctxFor(HOST, userDoc(), START_AT));
    await arriveAtStopCore(
      { rideId: "ride-1", pickupId: "start" },
      ctxFor(HOST, userDoc(), ARRIVED_AT),
    );

    const again = await arriveAtStopCore(
      { rideId: "ride-1", pickupId: "start" },
      ctxFor(HOST, userDoc(), GRACE_OVER_AT),
    );

    expect(again.bookings).toBe(0);
    expect((await bookingOf("ride-1", RIDER)).hostArrivedAt?.toDate().toISOString()).toBe(
      ARRIVED_AT.toISOString(),
    );
  });

  it("refuses a stranger, a ride that has not started and a pickup not on the ride", async () => {
    await putRide("ride-1");
    await book("ride-1", RIDER, "start");

    await expectReason(
      arriveAtStopCore(
        { rideId: "ride-1", pickupId: "start" },
        ctxFor(HOST, userDoc(), ARRIVED_AT),
      ),
      "RIDE_NOT_STARTED",
    );

    await startRideCore({ rideId: "ride-1" }, ctxFor(HOST, userDoc(), START_AT));
    await expectReason(
      arriveAtStopCore(
        { rideId: "ride-1", pickupId: "start" },
        ctxFor(HOST2, userDoc(), ARRIVED_AT),
      ),
      "NOT_FOUND",
    );
    await expectReason(
      arriveAtStopCore({ rideId: "ride-1", pickupId: "s3" }, ctxFor(HOST, userDoc(), ARRIVED_AT)),
      "PICKUP_NOT_ON_RIDE",
    );
  });
});

describe("checkIn", () => {
  it("stores the server's time and the distance to the rider's own pickup", async () => {
    await putRide("ride-1");
    await book("ride-1", RIDER, "start");
    await startRideCore({ rideId: "ride-1" }, ctxFor(HOST, userDoc(), START_AT));

    const out = await checkInCore(
      { rideId: "ride-1", lat: 30.0, lng: 31.0, accuracyM: 10 },
      ctxFor(RIDER, riderDoc(), ARRIVED_AT),
    );

    expect(out.distanceM).toBe(0);
    const booking = await bookingOf("ride-1", RIDER);
    expect(booking.checkedInAt?.toDate().toISOString()).toBe(ARRIVED_AT.toISOString());
    expect(booking.checkInDistanceM).toBe(0);
  });

  it("measures the distance to a stop, not to the ride's start point", async () => {
    await putRide("ride-1", {
      fromPoint: { lat: 31.0, lng: 32.0 }, // far from the stop
    });
    await book("ride-1", RIDER, "s1");
    await startRideCore({ rideId: "ride-1" }, ctxFor(HOST, userDoc(), START_AT));

    const out = await checkInCore(
      { rideId: "ride-1", lat: 30.0, lng: 31.0, accuracyM: 10 },
      ctxFor(RIDER, riderDoc(), ARRIVED_AT),
    );
    expect(out.distanceM).toBe(0);
  });

  it("refuses a rider without an accepted booking and a check-in after the window", async () => {
    await putRide("ride-1");
    await startRideCore({ rideId: "ride-1" }, ctxFor(HOST, userDoc(), START_AT));
    await expectReason(
      checkInCore(
        { rideId: "ride-1", lat: 30.0, lng: 31.0, accuracyM: 10 },
        ctxFor(STRANGER, riderDoc(), ARRIVED_AT),
      ),
      "BOOKING_NOT_FOUND",
    );

    await putRide("ride-2");
    await book("ride-2", RIDER, "start");
    await startRideCore({ rideId: "ride-2" }, ctxFor(HOST, userDoc(), START_AT));
    // The window closes 15 minutes after this booking's pickup time of 06:30Z.
    await expectReason(
      checkInCore(
        { rideId: "ride-2", lat: 30.0, lng: 31.0, accuracyM: 10 },
        ctxFor(RIDER, riderDoc(), new Date("2026-11-02T06:46:00Z")),
      ),
      "CHECK_IN_CLOSED",
    );
  });
});

describe("markBoarded", () => {
  it("lets the owner board a rider and a rider board themselves", async () => {
    await putRide("ride-1", { seatsTotal: 3, seatsLeft: 3 });
    await book("ride-1", RIDER, "start");
    await book("ride-1", RIDER2, "start");
    await startRideCore({ rideId: "ride-1" }, ctxFor(HOST, userDoc(), START_AT));

    await markBoardedCore(
      { rideId: "ride-1", riderUid: RIDER },
      ctxFor(HOST, userDoc(), ARRIVED_AT),
    );
    await markBoardedCore(
      { rideId: "ride-1", riderUid: RIDER2 },
      ctxFor(RIDER2, riderDoc(), ARRIVED_AT),
    );

    expect((await bookingOf("ride-1", RIDER)).status).toBe("boarded");
    expect((await bookingOf("ride-1", RIDER2)).boardedAt).not.toBeNull();
  });

  it("refuses a stranger, a ride that has not started and a booking that is not accepted", async () => {
    await putRide("ride-1");
    await book("ride-1", RIDER, "start");

    await expectReason(
      markBoardedCore({ rideId: "ride-1", riderUid: RIDER }, ctxFor(HOST, userDoc(), ARRIVED_AT)),
      "RIDE_NOT_STARTED",
    );

    await startRideCore({ rideId: "ride-1" }, ctxFor(HOST, userDoc(), START_AT));
    await expectReason(
      markBoardedCore(
        { rideId: "ride-1", riderUid: RIDER },
        ctxFor(STRANGER, riderDoc(), ARRIVED_AT),
      ),
      "NOT_ON_RIDE",
    );
    await expectReason(
      markBoardedCore(
        { rideId: "ride-1", riderUid: STRANGER },
        ctxFor(HOST, userDoc(), ARRIVED_AT),
      ),
      "BOOKING_NOT_FOUND",
    );
  });
});

describe("reportLate", () => {
  async function onRide(pickupId = "start"): Promise<void> {
    await putRide("ride-1");
    await book("ride-1", RIDER, pickupId);
    await startRideCore({ rideId: "ride-1" }, ctxFor(HOST, userDoc(), START_AT));
  }

  it("records a 10 EGP fee once the free wait is over", async () => {
    await onRide();
    await arriveAtStopCore(
      { rideId: "ride-1", pickupId: "start" },
      ctxFor(HOST, userDoc(), ARRIVED_AT),
    );

    const out = await reportLateCore(
      { rideId: "ride-1", riderUid: RIDER, kind: "late" },
      ctxFor(HOST, userDoc(), GRACE_OVER_AT),
    );

    const fee = (await db.doc(`fees/${out.feeId}`).get()).data() as FeeDoc;
    expect(fee.amount).toBe(10);
    expect(fee.reason).toBe("late");
    expect(fee.status).toBe("unpaid");
    expect(fee.riderUid).toBe(RIDER);
    expect(fee.hostId).toBe(HOST);
    expect(fee.evidence.reportedAt.toDate().toISOString()).toBe(GRACE_OVER_AT.toISOString());

    expect((await bookingOf("ride-1", RIDER)).lateFeeId).toBe(out.feeId);
    expect((await userOf(RIDER)).rider.unpaidFees).toBe(1);
  });

  it("blocks the rider's next request until the fee is settled", async () => {
    await onRide();
    await arriveAtStopCore(
      { rideId: "ride-1", pickupId: "start" },
      ctxFor(HOST, userDoc(), ARRIVED_AT),
    );
    await reportLateCore(
      { rideId: "ride-1", riderUid: RIDER, kind: "late" },
      ctxFor(HOST, userDoc(), GRACE_OVER_AT),
    );

    await putRide("ride-2", {
      hostId: HOST2,
      departAt: Timestamp.fromDate(new Date("2026-11-02T09:30:00Z")),
    });
    await expectReason(
      requestSeatsCore(
        { rideId: "ride-2", seats: 1, pickupId: "start", requestId: nextRequestId() },
        ctxFor(RIDER, riderDoc()),
      ),
      "UNPAID_FEE",
    );
  });

  it("protects a rider who checked in at the pickup inside the free wait", async () => {
    await onRide();
    await arriveAtStopCore(
      { rideId: "ride-1", pickupId: "start" },
      ctxFor(HOST, userDoc(), ARRIVED_AT),
    );
    await checkInCore(
      { rideId: "ride-1", lat: 30.0, lng: 31.0, accuracyM: 10 },
      ctxFor(RIDER, riderDoc(), ARRIVED_AT),
    );

    await expectReason(
      reportLateCore(
        { rideId: "ride-1", riderUid: RIDER, kind: "late" },
        ctxFor(HOST, userDoc(), GRACE_OVER_AT),
      ),
      "RIDER_WAS_ON_TIME",
    );
  });

  it("still charges a rider who checked in far from the pickup", async () => {
    await onRide();
    await arriveAtStopCore(
      { rideId: "ride-1", pickupId: "start" },
      ctxFor(HOST, userDoc(), ARRIVED_AT),
    );
    // About 1.5 km away, well past checkInRadiusM of 150.
    await checkInCore(
      { rideId: "ride-1", lat: 30.013, lng: 31.0, accuracyM: 10 },
      ctxFor(RIDER, riderDoc(), ARRIVED_AT),
    );

    const out = await reportLateCore(
      { rideId: "ride-1", riderUid: RIDER, kind: "late" },
      ctxFor(HOST, userDoc(), GRACE_OVER_AT),
    );
    expect(out.feeId).toBeTruthy();
  });

  it('needs the owner to tap "I\'m here" first, and the wait to be over', async () => {
    await onRide();
    await expectReason(
      reportLateCore(
        { rideId: "ride-1", riderUid: RIDER, kind: "late" },
        ctxFor(HOST, userDoc(), GRACE_OVER_AT),
      ),
      "GRACE_NOT_OVER",
    );

    await arriveAtStopCore(
      { rideId: "ride-1", pickupId: "start" },
      ctxFor(HOST, userDoc(), ARRIVED_AT),
    );
    // Only 2 minutes into the 5 free ones.
    await expectReason(
      reportLateCore(
        { rideId: "ride-1", riderUid: RIDER, kind: "late" },
        ctxFor(HOST, userDoc(), new Date("2026-11-02T06:17:00Z")),
      ),
      "GRACE_NOT_OVER",
    );
  });

  it("records one fee per booking", async () => {
    await onRide();
    await arriveAtStopCore(
      { rideId: "ride-1", pickupId: "start" },
      ctxFor(HOST, userDoc(), ARRIVED_AT),
    );
    await reportLateCore(
      { rideId: "ride-1", riderUid: RIDER, kind: "late" },
      ctxFor(HOST, userDoc(), GRACE_OVER_AT),
    );
    await expectReason(
      reportLateCore(
        { rideId: "ride-1", riderUid: RIDER, kind: "late" },
        ctxFor(HOST, userDoc(), GRACE_OVER_AT),
      ),
      "FEE_EXISTS",
    );
    expect((await userOf(RIDER)).rider.unpaidFees).toBe(1);
  });

  it("refuses anyone who is not the owner, and a rider with no booking", async () => {
    await onRide();
    await arriveAtStopCore(
      { rideId: "ride-1", pickupId: "start" },
      ctxFor(HOST, userDoc(), ARRIVED_AT),
    );
    await expectReason(
      reportLateCore(
        { rideId: "ride-1", riderUid: RIDER, kind: "late" },
        ctxFor(HOST2, userDoc(), GRACE_OVER_AT),
      ),
      "NOT_FOUND",
    );
    await expectReason(
      reportLateCore(
        { rideId: "ride-1", riderUid: STRANGER, kind: "late" },
        ctxFor(HOST, userDoc(), GRACE_OVER_AT),
      ),
      "BOOKING_NOT_FOUND",
    );
  });

  it("marks a no-show once the longer wait is over", async () => {
    await onRide();
    await arriveAtStopCore(
      { rideId: "ride-1", pickupId: "start" },
      ctxFor(HOST, userDoc(), ARRIVED_AT),
    );

    // Still inside noShowMinutes of 15.
    await expectReason(
      reportLateCore(
        { rideId: "ride-1", riderUid: RIDER, kind: "no_show" },
        ctxFor(HOST, userDoc(), GRACE_OVER_AT),
      ),
      "GRACE_NOT_OVER",
    );

    const out = await reportLateCore(
      { rideId: "ride-1", riderUid: RIDER, kind: "no_show" },
      ctxFor(HOST, userDoc(), NO_SHOW_AT),
    );

    const fee = (await db.doc(`fees/${out.feeId}`).get()).data() as FeeDoc;
    expect(fee.reason).toBe("no_show");
    expect((await bookingOf("ride-1", RIDER)).status).toBe("no_show");
  });
});

describe("finishRide", () => {
  async function ridingWith(riders: string[]): Promise<void> {
    await putRide("ride-1", { seatsTotal: 3, seatsLeft: 3 });
    for (const rider of riders) {
      await book("ride-1", rider, "start");
    }
    await startRideCore({ rideId: "ride-1" }, ctxFor(HOST, userDoc(), START_AT));
  }

  it("completes the boarded riders, no-shows the rest and takes one credit", async () => {
    await ridingWith([RIDER, RIDER2]);
    await markBoardedCore(
      { rideId: "ride-1", riderUid: RIDER },
      ctxFor(HOST, userDoc(), ARRIVED_AT),
    );

    const out = await finishRideHandler({ rideId: "ride-1" }, ctxFor(HOST, userDoc(), NO_SHOW_AT));

    expect(out.completedBookings).toBe(1);
    expect(out.creditUsed).toBe(true);
    expect((await bookingOf("ride-1", RIDER)).status).toBe("completed");
    expect((await bookingOf("ride-1", RIDER2)).status).toBe("no_show");

    const ride = await rideOf("ride-1");
    expect(ride.status).toBe("finished");
    expect(ride.creditConsumed).toBe(true);
    expect(ride.autoFinished).toBe(false);

    // One credit gone, with a ledger row to show it (R3).
    expect((await userOf(HOST)).host.credits.regular).toBe(9);
    const ledger = await db.collection(`users/${HOST}/ledger`).get();
    expect(ledger.size).toBe(1);
    const entry = ledger.docs[0]?.data() as LedgerEntry;
    expect(entry).toMatchObject({
      delta: -1,
      bucket: "regular",
      reason: "trip",
      refId: "ride-1",
      balanceAfter: 9,
    });
  });

  it("takes no credit when nobody boarded", async () => {
    await ridingWith([RIDER]);
    const out = await finishRideHandler({ rideId: "ride-1" }, ctxFor(HOST, userDoc(), NO_SHOW_AT));

    expect(out.completedBookings).toBe(0);
    expect(out.creditUsed).toBe(false);
    expect((await userOf(HOST)).host.credits.regular).toBe(10);
    expect((await db.collection(`users/${HOST}/ledger`).get()).size).toBe(0);
  });

  it("takes no credit inside the free month", async () => {
    await db.doc(`users/${HOST}`).set(
      userDoc({
        host: {
          ...userDoc().host,
          freeUntil: Timestamp.fromDate(new Date("2026-11-28T00:00:00Z")),
        },
      }),
    );
    await ridingWith([RIDER]);
    await markBoardedCore(
      { rideId: "ride-1", riderUid: RIDER },
      ctxFor(HOST, userDoc(), ARRIVED_AT),
    );

    const out = await finishRideHandler({ rideId: "ride-1" }, ctxFor(HOST, userDoc(), NO_SHOW_AT));
    expect(out.completedBookings).toBe(1);
    expect(out.creditUsed).toBe(false);
    expect((await userOf(HOST)).host.credits.regular).toBe(10);
  });

  it("takes exactly one credit even when the owner taps End trip twice", async () => {
    await ridingWith([RIDER]);
    await markBoardedCore(
      { rideId: "ride-1", riderUid: RIDER },
      ctxFor(HOST, userDoc(), ARRIVED_AT),
    );
    await finishRideHandler({ rideId: "ride-1" }, ctxFor(HOST, userDoc(), NO_SHOW_AT));

    await expectReason(
      finishRideHandler({ rideId: "ride-1" }, ctxFor(HOST, userDoc(), NO_SHOW_AT)),
      "RIDE_NOT_STARTED",
    );
    expect((await userOf(HOST)).host.credits.regular).toBe(9);
    expect((await db.collection(`users/${HOST}/ledger`).get()).size).toBe(1);
  });

  it("refuses anyone who is not the owner and a ride that never started", async () => {
    await ridingWith([RIDER]);
    await expectReason(
      finishRideHandler({ rideId: "ride-1" }, ctxFor(HOST2, userDoc(), NO_SHOW_AT)),
      "NOT_FOUND",
    );

    await putRide("ride-2");
    await expectReason(
      finishRideHandler({ rideId: "ride-2" }, ctxFor(HOST, userDoc(), NO_SHOW_AT)),
      "RIDE_NOT_STARTED",
    );
  });

  it("leaves a cancelled booking alone", async () => {
    // The rider cancels before the trip starts; after that cancelling is refused.
    await putRide("ride-1", { seatsTotal: 3, seatsLeft: 3 });
    await book("ride-1", RIDER, "start");
    await book("ride-1", RIDER2, "start");
    await cancelBookingCore(
      { rideId: "ride-1", reason: "plans_changed" },
      ctxFor(RIDER2, riderDoc(), FIXED_NOW),
    );
    await startRideCore({ rideId: "ride-1" }, ctxFor(HOST, userDoc(), START_AT));
    await markBoardedCore(
      { rideId: "ride-1", riderUid: RIDER },
      ctxFor(HOST, userDoc(), ARRIVED_AT),
    );

    await finishRideHandler({ rideId: "ride-1" }, ctxFor(HOST, userDoc(), NO_SHOW_AT));
    expect((await bookingOf("ride-1", RIDER2)).status).toBe("cancelled_by_rider");
  });
});
