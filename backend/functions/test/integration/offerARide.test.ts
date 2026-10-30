// P3-B02: quotePrice and createRide against the emulators (BUILD_PLAN 8.5 and the stops of 8.16).
// Covers the happy path plus every money, permission and input check (plan 2.2 #8).
import { Timestamp } from "firebase-admin/firestore";
import { afterAll, afterEach, beforeAll, beforeEach, describe, expect, it } from "vitest";
import { createRideCore } from "../../src/handlers/rides/createRide.js";
import { quotePriceCore } from "../../src/handlers/rides/quotePrice.js";
import { db } from "../../src/lib/app.js";
import { resetClock, setClock } from "../../src/lib/clock.js";
import { AppFailure } from "../../src/lib/errors.js";
import { clearConfigCache } from "../../src/lib/config.js";
import type { CallCtx } from "../../src/lib/ctx.js";
import type { RideDoc, UserDoc } from "../../src/schemas/docs.js";
import type { CreateRideInput } from "../../src/schemas/inputs.js";
import { FIXED_NOW, limitsConfig, pricingConfig, userDoc, vehicleDoc } from "../helpers/docs.js";
import { runSeed } from "../../src/seed/seed.js";

const HOST = "p3b02-host";
const HOST_WOMAN = "p3b02-host-woman";
const HOST_PENDING = "p3b02-host-pending";

const FROM_ZONE = "p3b02-from";
const TO_ZONE = "p3b02-to";
const STOP_ZONE = "p3b02-stop";
const CLOSED_ZONE = "p3b02-closed";

const CAR = "p3b02-car";
const CAR_PENDING = "p3b02-car-pending";
const SCOOTER = "p3b02-scooter";

// 08:30 Cairo on the same day: 60 minutes after FIXED_NOW and inside the 07:00–10:00 peak window.
const DEPART_AT = "2026-11-02T06:30:00.000Z";
const STOP_AT = "2026-11-02T06:45:00.000Z";

let requestIdCounter = 0;
function nextRequestId(): string {
  requestIdCounter += 1;
  return `b0000000-0000-4000-8000-${String(requestIdCounter).padStart(12, "0")}`;
}

function ctxFor(uid: string, user: UserDoc): CallCtx {
  return { uid, phone: null, isAdmin: false, user, now: FIXED_NOW };
}

function createInput(overrides: Partial<CreateRideInput> = {}): CreateRideInput {
  return {
    vehicleId: CAR,
    fromZone: FROM_ZONE,
    fromPointId: "pt1",
    toZone: TO_ZONE,
    toPointId: "pt1",
    stops: [],
    direction: "to_work",
    departAt: DEPART_AT,
    seats: 3,
    pricePerSeat: 56,
    womenOnly: false,
    requestId: nextRequestId(),
    ...overrides,
  };
}

async function expectReason(promise: Promise<unknown>, reason: string): Promise<void> {
  await expect(promise).rejects.toMatchObject({ reason });
}

async function deleteCollection(path: string): Promise<void> {
  const snap = await db.collection(path).get();
  await Promise.all(snap.docs.map((d) => d.ref.delete()));
}

beforeAll(async () => {
  setClock(() => FIXED_NOW);

  // Reference data. The zones and points are fake ids used only by this file.
  await db
    .doc(`zones/${FROM_ZONE}`)
    .set({ name: { ar: "من", en: "From" }, active: true, order: 1 });
  await db.doc(`zones/${TO_ZONE}`).set({ name: { ar: "إلى", en: "To" }, active: true, order: 2 });
  await db
    .doc(`zones/${STOP_ZONE}`)
    .set({ name: { ar: "وقفة", en: "Stop" }, active: true, order: 3 });
  await db
    .doc(`zones/${CLOSED_ZONE}`)
    .set({ name: { ar: "مقفول", en: "Closed" }, active: false, order: 4 });

  for (const zone of [FROM_ZONE, TO_ZONE, STOP_ZONE, CLOSED_ZONE]) {
    await db
      .doc(`zones/${zone}/points/pt1`)
      .set({ name: { ar: "نقطة 1", en: "Point 1" }, lat: 30.0, lng: 31.0, active: true });
    await db
      .doc(`zones/${zone}/points/off`)
      .set({ name: { ar: "نقطة مقفولة", en: "Point off" }, lat: 30.1, lng: 31.1, active: false });
  }

  await db.doc(`routes/${FROM_ZONE}__${TO_ZONE}`).set({
    fromZone: FROM_ZONE,
    toZone: TO_ZONE,
    distanceKm: 55,
    usualMinutes: 65,
  });

  // Hosts and vehicles.
  await db.doc(`users/${HOST}`).set(userDoc());
  await db.doc(`users/${HOST_WOMAN}`).set(userDoc({ gender: "female" }));
  await db
    .doc(`users/${HOST_PENDING}`)
    .set(userDoc({ host: { ...userDoc().host, status: "pending" } }));

  for (const uid of [HOST, HOST_WOMAN]) {
    await db.doc(`users/${uid}/vehicles/${CAR}`).set(vehicleDoc());
    await db.doc(`users/${uid}/vehicles/${CAR_PENDING}`).set(vehicleDoc({ status: "pending" }));
    await db
      .doc(`users/${uid}/vehicles/${SCOOTER}`)
      .set(vehicleDoc({ type: "scooter", maxSeats: 1 }));
  }
});

beforeEach(async () => {
  // The plan's own numbers, not the looser demo seed (ADR 0003 row 81).
  await db.doc("config/pricing").set(pricingConfig());
  await db.doc("config/limits").set(limitsConfig());
  clearConfigCache();
  await deleteCollection("rides");
  await deleteCollection("idempotency");
});

afterEach(() => {
  clearConfigCache();
});

afterAll(async () => {
  resetClock();
  await deleteCollection("rides");
  await deleteCollection("idempotency");
  for (const uid of [HOST, HOST_WOMAN, HOST_PENDING]) {
    await db.recursiveDelete(db.doc(`users/${uid}`));
  }
  for (const zone of [FROM_ZONE, TO_ZONE, STOP_ZONE, CLOSED_ZONE]) {
    await db.recursiveDelete(db.doc(`zones/${zone}`));
  }
  await db.doc(`routes/${FROM_ZONE}__${TO_ZONE}`).delete();
  // Restore the seed so later emulator tests see fresh seed data.
  await runSeed({ project: "demo-hitchy", silent: true });
});

describe("quotePrice", () => {
  it("returns the golden peak row for 55 km by car", async () => {
    const user = userDoc();
    const out = await quotePriceCore(
      { vehicleId: CAR, fromZone: FROM_ZONE, toZone: TO_ZONE, departAt: DEPART_AT, seats: 3 },
      ctxFor(HOST, user),
    );

    // BUILD_PLAN 8.5 golden table, peak row, 3 seats.
    expect(out.min).toBe(42);
    expect(out.recommended).toBe(56);
    expect(out.max).toBe(67);
    expect(out.trafficLevel).toBe("peak");
    expect(out.distanceKm).toBe(55);
    expect(out.configVersion).toBe(1);
  });

  it("refuses a host who is not approved", async () => {
    const pending = userDoc({ host: { ...userDoc().host, status: "pending" } });
    await expectReason(
      quotePriceCore(
        { vehicleId: CAR, fromZone: FROM_ZONE, toZone: TO_ZONE, departAt: DEPART_AT, seats: 3 },
        ctxFor(HOST_PENDING, pending),
      ),
      "HOST_NOT_APPROVED",
    );
  });

  it("refuses a vehicle that is not the caller's", async () => {
    await expectReason(
      quotePriceCore(
        {
          vehicleId: "someone-elses-car",
          fromZone: FROM_ZONE,
          toZone: TO_ZONE,
          departAt: DEPART_AT,
          seats: 3,
        },
        ctxFor(HOST, userDoc()),
      ),
      "NOT_FOUND",
    );
  });

  it("refuses a scooter while scooters are disabled", async () => {
    await expectReason(
      quotePriceCore(
        { vehicleId: SCOOTER, fromZone: FROM_ZONE, toZone: TO_ZONE, departAt: DEPART_AT, seats: 1 },
        ctxFor(HOST, userDoc()),
      ),
      "SCOOTER_DISABLED",
    );
  });

  it("refuses a route we do not serve", async () => {
    await expectReason(
      quotePriceCore(
        { vehicleId: CAR, fromZone: TO_ZONE, toZone: FROM_ZONE, departAt: DEPART_AT, seats: 3 },
        ctxFor(HOST, userDoc()),
      ),
      "ROUTE_NOT_SUPPORTED",
    );
  });

  it("refuses more seats than the vehicle has", async () => {
    await db.doc(`users/${HOST}/vehicles/two-seater`).set(vehicleDoc({ maxSeats: 2 }));
    await expectReason(
      quotePriceCore(
        {
          vehicleId: "two-seater",
          fromZone: FROM_ZONE,
          toZone: TO_ZONE,
          departAt: DEPART_AT,
          seats: 3,
        },
        ctxFor(HOST, userDoc()),
      ),
      "SEATS_INVALID",
    );
    await db.doc(`users/${HOST}/vehicles/two-seater`).delete();
  });
});

describe("createRide", () => {
  it("writes the ride with the vehicle, the points, the price range and the stops", async () => {
    const out = await createRideCore(
      createInput({
        stops: [{ zone: STOP_ZONE, pointId: "pt1", at: STOP_AT }],
      }),
      ctxFor(HOST, userDoc()),
    );

    const snap = await db.doc(`rides/${out.rideId}`).get();
    expect(snap.exists).toBe(true);
    const ride = snap.data() as RideDoc;

    expect(ride.hostId).toBe(HOST);
    expect(ride.status).toBe("open");
    expect(ride.seatsTotal).toBe(3);
    expect(ride.seatsLeft).toBe(3);
    expect(ride.bookingsCount).toBe(0);
    expect(ride.creditConsumed).toBe(false);
    expect(ride.fixedRideId).toBeNull();
    expect(ride.dateKey).toBe("2026-11-02");

    // Copies taken at posting time.
    expect(ride.vehicle.plate).toBe("TEST-PLATE");
    expect(ride.fromPointName.en).toBe("Point 1");
    expect(ride.fromPoint).toEqual({ lat: 30.0, lng: 31.0 });
    expect(ride.priceRange).toMatchObject({
      min: 42,
      recommended: 56,
      max: 67,
      trafficLevel: "peak",
      distanceKm: 55,
      configVersion: 1,
    });

    // v1.2 stops and the zones search uses.
    expect(ride.stops).toHaveLength(1);
    expect(ride.stops[0]?.id).toBe("s1");
    expect(ride.stops[0]?.zone).toBe(STOP_ZONE);
    expect(ride.stops[0]?.pointName.en).toBe("Point 1");
    expect(ride.stops[0]?.point).toEqual({ lat: 30.0, lng: 31.0 });
    expect(ride.stops[0]?.at.toDate().toISOString()).toBe(STOP_AT);
    expect(ride.pickupZones).toEqual([FROM_ZONE, STOP_ZONE]);
  });

  it("numbers up to three stops in order and lists each zone once", async () => {
    const out = await createRideCore(
      createInput({
        stops: [
          { zone: STOP_ZONE, pointId: "pt1", at: "2026-11-02T06:40:00.000Z" },
          { zone: FROM_ZONE, pointId: "pt1", at: "2026-11-02T06:50:00.000Z" },
          { zone: STOP_ZONE, pointId: "pt1", at: "2026-11-02T07:00:00.000Z" },
        ],
      }),
      ctxFor(HOST, userDoc()),
    );

    const ride = (await db.doc(`rides/${out.rideId}`).get()).data() as RideDoc;
    expect(ride.stops.map((s) => s.id)).toEqual(["s1", "s2", "s3"]);
    expect(ride.pickupZones).toEqual([FROM_ZONE, STOP_ZONE]);
  });

  it("runs once for the same requestId", async () => {
    const input = createInput();
    const first = await createRideCore(input, ctxFor(HOST, userDoc()));
    const second = await createRideCore(input, ctxFor(HOST, userDoc()));

    expect(second.rideId).toBe(first.rideId);
    const rides = await db.collection("rides").get();
    expect(rides.size).toBe(1);
  });

  it("refuses a host who is not approved", async () => {
    const pending = userDoc({ host: { ...userDoc().host, status: "pending" } });
    await expectReason(
      createRideCore(createInput(), ctxFor(HOST_PENDING, pending)),
      "HOST_NOT_APPROVED",
    );
  });

  it("refuses a vehicle that is not approved or not the caller's", async () => {
    await expectReason(
      createRideCore(createInput({ vehicleId: CAR_PENDING }), ctxFor(HOST, userDoc())),
      "VEHICLE_NOT_APPROVED",
    );
    await expectReason(
      createRideCore(createInput({ vehicleId: "no-such-car" }), ctxFor(HOST, userDoc())),
      "VEHICLE_NOT_APPROVED",
    );
  });

  it("refuses a scooter while scooters are disabled", async () => {
    await expectReason(
      createRideCore(
        createInput({ vehicleId: SCOOTER, seats: 1, pricePerSeat: 127 }),
        ctxFor(HOST, userDoc()),
      ),
      "SCOOTER_DISABLED",
    );
  });

  it("refuses an unknown route or a point that is not active", async () => {
    await expectReason(
      createRideCore(
        createInput({ fromZone: TO_ZONE, toZone: FROM_ZONE }),
        ctxFor(HOST, userDoc()),
      ),
      "ROUTE_NOT_SUPPORTED",
    );
    await expectReason(
      createRideCore(createInput({ fromPointId: "off" }), ctxFor(HOST, userDoc())),
      "ROUTE_NOT_SUPPORTED",
    );
    await expectReason(
      createRideCore(createInput({ toPointId: "off" }), ctxFor(HOST, userDoc())),
      "ROUTE_NOT_SUPPORTED",
    );
  });

  it("refuses stops that break [DECISION-28]", async () => {
    const cases: { name: string; stops: CreateRideInput["stops"] }[] = [
      {
        name: "a point that is not active",
        stops: [{ zone: STOP_ZONE, pointId: "off", at: STOP_AT }],
      },
      {
        name: "a zone that is not active",
        stops: [{ zone: CLOSED_ZONE, pointId: "pt1", at: STOP_AT }],
      },
      {
        name: "a time before departure",
        stops: [{ zone: STOP_ZONE, pointId: "pt1", at: "2026-11-02T06:20:00.000Z" }],
      },
      {
        name: "a time less than a minute after departure",
        stops: [{ zone: STOP_ZONE, pointId: "pt1", at: "2026-11-02T06:30:30.000Z" }],
      },
      {
        name: "two stops out of order",
        stops: [
          { zone: STOP_ZONE, pointId: "pt1", at: "2026-11-02T06:50:00.000Z" },
          { zone: STOP_ZONE, pointId: "pt1", at: "2026-11-02T06:40:00.000Z" },
        ],
      },
      {
        name: "two stops less than a minute apart",
        stops: [
          { zone: STOP_ZONE, pointId: "pt1", at: "2026-11-02T06:40:00.000Z" },
          { zone: STOP_ZONE, pointId: "pt1", at: "2026-11-02T06:40:30.000Z" },
        ],
      },
      {
        name: "a time past the route's usual minutes",
        stops: [{ zone: STOP_ZONE, pointId: "pt1", at: "2026-11-02T07:35:00.000Z" }],
      },
    ];

    for (const { name, stops } of cases) {
      const result = await createRideCore(createInput({ stops }), ctxFor(HOST, userDoc())).then(
        () => null,
        (e: unknown) => (e instanceof AppFailure ? e.reason : null),
      );
      expect(result, `Expected STOPS_INVALID for ${name}`).toBe("STOPS_INVALID");
    }
  });

  it("refuses a departure too soon or too far ahead", async () => {
    await expectReason(
      createRideCore(
        createInput({ departAt: "2026-11-02T05:40:00.000Z" }),
        ctxFor(HOST, userDoc()),
      ),
      "DEPART_TOO_SOON",
    );
    await expectReason(
      createRideCore(
        createInput({ departAt: "2026-11-20T06:30:00.000Z" }),
        ctxFor(HOST, userDoc()),
      ),
      "DEPART_TOO_FAR",
    );
  });

  it("refuses more seats than the vehicle has", async () => {
    await db.doc(`users/${HOST}/vehicles/two-seater`).set(vehicleDoc({ maxSeats: 2 }));
    await expectReason(
      createRideCore(createInput({ vehicleId: "two-seater", seats: 3 }), ctxFor(HOST, userDoc())),
      "SEATS_INVALID",
    );
    await db.doc(`users/${HOST}/vehicles/two-seater`).delete();
  });

  it("refuses a price outside the server's own range", async () => {
    await expectReason(
      createRideCore(createInput({ pricePerSeat: 41 }), ctxFor(HOST, userDoc())),
      "PRICE_OUT_OF_RANGE",
    );
    await expectReason(
      createRideCore(createInput({ pricePerSeat: 70 }), ctxFor(HOST, userDoc())),
      "PRICE_OUT_OF_RANGE",
    );

    // The ends of the range are allowed.
    await createRideCore(createInput({ pricePerSeat: 42 }), ctxFor(HOST, userDoc()));
    await deleteCollection("rides");
    await createRideCore(createInput({ pricePerSeat: 67 }), ctxFor(HOST, userDoc()));
  });

  it("lets only a woman host post a Women-only ride", async () => {
    await expectReason(
      createRideCore(createInput({ womenOnly: true }), ctxFor(HOST, userDoc())),
      "WOMEN_ONLY_HOST",
    );

    const woman = userDoc({ gender: "female" });
    const out = await createRideCore(createInput({ womenOnly: true }), ctxFor(HOST_WOMAN, woman));
    const ride = (await db.doc(`rides/${out.rideId}`).get()).data() as RideDoc;
    expect(ride.womenOnly).toBe(true);
  });

  it("allows two rides a day in opposite directions and refuses a third or a repeat direction", async () => {
    await createRideCore(createInput(), ctxFor(HOST, userDoc()));

    // Same direction again, far enough away in time to pass the gap check.
    await expectReason(
      createRideCore(
        createInput({ departAt: "2026-11-02T10:30:00.000Z", pricePerSeat: 46 }),
        ctxFor(HOST, userDoc()),
      ),
      "DAILY_RIDE_LIMIT",
    );

    // The other direction is fine.
    await createRideCore(
      createInput({
        direction: "to_home",
        departAt: "2026-11-02T14:30:00.000Z",
        pricePerSeat: 46,
      }),
      ctxFor(HOST, userDoc()),
    );

    // A third ride that day is refused whatever its direction. 17:30Z is 19:30 Cairo, so the
    // traffic level is offPeak and 46 EGP sits inside that range too.
    await expectReason(
      createRideCore(
        createInput({ departAt: "2026-11-02T17:30:00.000Z", pricePerSeat: 46 }),
        ctxFor(HOST, userDoc()),
      ),
      "DAILY_RIDE_LIMIT",
    );
  });

  it("refuses a ride inside rideGapMinutes of another live ride", async () => {
    await createRideCore(createInput(), ctxFor(HOST, userDoc()));
    await expectReason(
      createRideCore(
        createInput({
          direction: "to_home",
          departAt: "2026-11-02T07:30:00.000Z",
          pricePerSeat: 46,
        }),
        ctxFor(HOST, userDoc()),
      ),
      "RIDE_TIME_CONFLICT",
    );
  });

  it("needs a credit for every live ride once the free month is over", async () => {
    const noCredits = userDoc({
      host: { ...userDoc().host, credits: { regular: 0, fixed: 0 }, freeUntil: null },
    });
    await db.doc(`users/${HOST}`).set(noCredits);
    await expectReason(createRideCore(createInput(), ctxFor(HOST, noCredits)), "NO_CREDITS");

    // One credit covers one live ride, and the second is refused.
    const oneCredit = userDoc({
      host: { ...userDoc().host, credits: { regular: 1, fixed: 0 }, freeUntil: null },
    });
    await db.doc(`users/${HOST}`).set(oneCredit);
    await createRideCore(createInput(), ctxFor(HOST, oneCredit));
    await expectReason(
      createRideCore(
        createInput({
          direction: "to_home",
          departAt: "2026-11-02T14:30:00.000Z",
          pricePerSeat: 46,
        }),
        ctxFor(HOST, oneCredit),
      ),
      "NO_CREDITS",
    );

    // Inside the free month no credit is needed.
    const free = userDoc({
      host: {
        ...userDoc().host,
        credits: { regular: 0, fixed: 0 },
        freeUntil: Timestamp.fromDate(new Date("2026-11-28T00:00:00Z")),
      },
    });
    await db.doc(`users/${HOST}`).set(free);
    await deleteCollection("rides");
    await createRideCore(createInput(), ctxFor(HOST, free));

    await db.doc(`users/${HOST}`).set(userDoc());
  });
});
