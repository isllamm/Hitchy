// P3-B05: confirmRidePayment and topUpWallet against the emulators (BUILD_PLAN 8.16, v1.2).
// Money is never "happy path only" (plan 3.9 note 4): every balance, every entry, every refusal.
import { Timestamp } from "firebase-admin/firestore";
import { afterAll, beforeAll, beforeEach, describe, expect, it } from "vitest";
import { requestSeatsCore } from "../../src/handlers/booking/requestSeats.js";
import { respondToRequestCore } from "../../src/handlers/booking/respondToRequest.js";
import { topUpWalletCore } from "../../src/handlers/packages/topUpWallet.js";
import { arriveAtStopCore } from "../../src/handlers/trip/arriveAtStop.js";
import { confirmRidePaymentCore } from "../../src/handlers/trip/confirmRidePayment.js";
import { finishRideHandler } from "../../src/handlers/trip/finishRide.js";
import { markBoardedCore } from "../../src/handlers/trip/markBoarded.js";
import { reportLateCore } from "../../src/handlers/trip/reportLate.js";
import { startRideCore } from "../../src/handlers/trip/startRide.js";
import { db } from "../../src/lib/app.js";
import { resetClock, setClock } from "../../src/lib/clock.js";
import { clearConfigCache } from "../../src/lib/config.js";
import { buildCtx, type CallCtx } from "../../src/lib/ctx.js";
import type {
  AdminActionDoc,
  BookingDoc,
  FeeDoc,
  RideDoc,
  UserDoc,
  WalletDoc,
  WalletEntryDoc,
} from "../../src/schemas/docs.js";
import { runSeed } from "../../src/seed/seed.js";
import { FIXED_NOW, limitsConfig, pricingConfig, rideDoc, userDoc } from "../helpers/docs.js";

const ADMIN = "p3b05-admin";
const HOST = "p3b05-host";
const HOST2 = "p3b05-host2";
const RIDER = "p3b05-rider";
const RIDER2 = "p3b05-rider2";

const ZONE_A = "p3b05-a";
const ZONE_C = "p3b05-c";

const DEPART = new Date("2026-11-02T06:30:00Z");
const START_AT = new Date("2026-11-02T06:10:00Z");
const ARRIVED_AT = new Date("2026-11-02T06:15:00Z");
const GRACE_OVER_AT = new Date("2026-11-02T06:21:00Z");
const FINISH_AT = new Date("2026-11-02T07:40:00Z");

let requestIdCounter = 0;
function nextRequestId(): string {
  requestIdCounter += 1;
  return `e0000000-0000-4000-8000-${String(requestIdCounter).padStart(12, "0")}`;
}

function ctxFor(uid: string, user: UserDoc | null, at: Date = FIXED_NOW): CallCtx {
  return { uid, phone: null, isAdmin: uid === ADMIN, user, now: at };
}

function riderDoc(overrides: Partial<UserDoc> = {}): UserDoc {
  const base = userDoc({ activeRole: "rider", host: { ...userDoc().host, status: "none" } });
  return { ...base, ...overrides };
}

// Sets an opening balance the way the seed does: the balance plus the one `seed` entry that
// accounts for it, so "a balance is the sum of its entries" holds from the start.
async function setWallet(uid: string, balance: number): Promise<void> {
  await db.recursiveDelete(db.collection(`wallets/${uid}/entries`));
  const at = Timestamp.fromDate(FIXED_NOW);
  const wallet: WalletDoc = { balance, updatedAt: at };
  await db.doc(`wallets/${uid}`).set(wallet);
  const seedEntry: WalletEntryDoc = {
    delta: balance,
    reason: "seed",
    refId: null,
    balanceAfter: balance,
    by: "seed",
    createdAt: at,
  };
  await db.doc(`wallets/${uid}/entries/seed`).set(seedEntry);
}

async function balanceOf(uid: string): Promise<number> {
  const snap = await db.doc(`wallets/${uid}`).get();
  return snap.exists ? (snap.data() as WalletDoc).balance : 0;
}

async function entriesOf(uid: string): Promise<WalletEntryDoc[]> {
  const snap = await db.collection(`wallets/${uid}/entries`).orderBy("createdAt").get();
  return snap.docs.map((d) => d.data() as WalletEntryDoc);
}

// Entries written in the same transaction share a timestamp, so pick by reason, not by position.
async function entriesByReason(
  uid: string,
  reason: WalletEntryDoc["reason"],
): Promise<WalletEntryDoc[]> {
  return (await entriesOf(uid)).filter((e) => e.reason === reason);
}

// The invariant the plan asks for by name: a balance is the sum of its entries.
async function expectBalanceMatchesEntries(uid: string): Promise<void> {
  const entries = await entriesOf(uid);
  const sum = entries.reduce((acc, e) => acc + e.delta, 0);
  expect(sum, `wallets/${uid} balance should equal the sum of its entries`).toBe(
    await balanceOf(uid),
  );
}

async function putRide(id: string, overrides: Partial<RideDoc> = {}): Promise<void> {
  await db.doc(`rides/${id}`).set(
    rideDoc({
      hostId: HOST,
      fromZone: ZONE_A,
      toZone: ZONE_C,
      pickupZones: [ZONE_A],
      departAt: Timestamp.fromDate(DEPART),
      seatsTotal: 3,
      seatsLeft: 3,
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

/** Books riders, runs the trip and finishes it, so the bookings end up `completed`. */
async function rideTo(
  rideId: string,
  riders: { uid: string; seats: number }[],
  opts: { boardAll?: boolean } = {},
): Promise<void> {
  await putRide(rideId);
  for (const rider of riders) {
    await requestSeatsCore(
      { rideId, seats: rider.seats, pickupId: "start", requestId: nextRequestId() },
      ctxFor(rider.uid, riderDoc()),
    );
    await respondToRequestCore(
      { rideId, riderUid: rider.uid, accept: true },
      ctxFor(HOST, userDoc()),
    );
  }
  await startRideCore({ rideId }, ctxFor(HOST, userDoc(), START_AT));
  if (opts.boardAll !== false) {
    for (const rider of riders) {
      await markBoardedCore({ rideId, riderUid: rider.uid }, ctxFor(HOST, userDoc(), ARRIVED_AT));
    }
  }
  await finishRideHandler({ rideId }, ctxFor(HOST, userDoc(), FINISH_AT));
}

beforeAll(async () => {
  setClock(() => FIXED_NOW);

  for (const [zone, order] of [
    [ZONE_A, 1],
    [ZONE_C, 2],
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
  await deleteCollection("adminActions");

  await db.doc(`users/${HOST}`).set(userDoc({ firstName: "Owner" }));
  await db.doc(`users/${HOST2}`).set(userDoc({ firstName: "Owner2" }));
  await db.doc(`users/${ADMIN}`).set(riderDoc({ firstName: "Admin" }));
  for (const uid of [RIDER, RIDER2]) {
    await db.doc(`users/${uid}`).set(riderDoc({ firstName: "Rider" }));
  }

  await setWallet(RIDER, 300);
  await setWallet(RIDER2, 300);
  await setWallet(HOST, 0);
  await setWallet(HOST2, 0);
  await setWallet(ADMIN, 0);
});

afterAll(async () => {
  resetClock();
  await deleteCollection("rides");
  await deleteCollection("idempotency");
  await deleteCollection("fees");
  await deleteCollection("adminActions");
  for (const uid of [ADMIN, HOST, HOST2, RIDER, RIDER2]) {
    await db.recursiveDelete(db.doc(`users/${uid}`));
    await db.recursiveDelete(db.doc(`wallets/${uid}`));
  }
  for (const zone of [ZONE_A, ZONE_C]) {
    await db.recursiveDelete(db.doc(`zones/${zone}`));
  }
  await db.doc(`routes/${ZONE_A}__${ZONE_C}`).delete();
  await runSeed({ project: "demo-hitchy", silent: true });
});

describe("confirmRidePayment", () => {
  it("moves one rider's fare from their wallet to the owner's", async () => {
    await rideTo("ride-1", [{ uid: RIDER, seats: 1 }]);

    const out = await confirmRidePaymentCore(
      { rideId: "ride-1", requestId: nextRequestId() },
      ctxFor(HOST, userDoc(), FINISH_AT),
    );

    expect(out).toEqual({ paidBookings: 1, total: 56 });
    expect(await balanceOf(RIDER)).toBe(244); // 300 − 56
    expect(await balanceOf(HOST)).toBe(56);

    const riderEntry = (await entriesByReason(RIDER, "ride_payment")).at(0);
    expect(riderEntry).toMatchObject({
      delta: -56,
      reason: "ride_payment",
      refId: "ride-1",
      balanceAfter: 244,
      by: HOST,
    });
    const ownerEntry = (await entriesByReason(HOST, "ride_earning")).at(0);
    expect(ownerEntry).toMatchObject({
      delta: 56,
      reason: "ride_earning",
      refId: "ride-1",
      balanceAfter: 56,
      by: HOST,
    });

    expect((await bookingOf("ride-1", RIDER)).paymentConfirmedAt).not.toBeNull();
    await expectBalanceMatchesEntries(RIDER);
    await expectBalanceMatchesEntries(HOST);
  });

  it("pays every rider of the ride in one call, seats and all", async () => {
    await rideTo("ride-1", [
      { uid: RIDER, seats: 2 },
      { uid: RIDER2, seats: 1 },
    ]);

    const out = await confirmRidePaymentCore(
      { rideId: "ride-1", requestId: nextRequestId() },
      ctxFor(HOST, userDoc(), FINISH_AT),
    );

    // 2 seats at 56 plus 1 seat at 56.
    expect(out).toEqual({ paidBookings: 2, total: 168 });
    expect(await balanceOf(RIDER)).toBe(188); // 300 − 112
    expect(await balanceOf(RIDER2)).toBe(244); // 300 − 56
    expect(await balanceOf(HOST)).toBe(168);

    // The owner has one entry per booking, and they add up as they go.
    const earnings = await entriesByReason(HOST, "ride_earning");
    expect(earnings).toHaveLength(2); // one per booking
    expect(earnings.map((e) => e.balanceAfter).sort((a, b) => a - b)).toEqual([112, 168]);

    await expectBalanceMatchesEntries(RIDER);
    await expectBalanceMatchesEntries(RIDER2);
    await expectBalanceMatchesEntries(HOST);
  });

  it("collects a reported late fee with the fare and settles the fee", async () => {
    await putRide("ride-1");
    await requestSeatsCore(
      { rideId: "ride-1", seats: 1, pickupId: "start", requestId: nextRequestId() },
      ctxFor(RIDER, riderDoc()),
    );
    await respondToRequestCore(
      { rideId: "ride-1", riderUid: RIDER, accept: true },
      ctxFor(HOST, userDoc()),
    );
    await startRideCore({ rideId: "ride-1" }, ctxFor(HOST, userDoc(), START_AT));
    await arriveAtStopCore(
      { rideId: "ride-1", pickupId: "start" },
      ctxFor(HOST, userDoc(), ARRIVED_AT),
    );
    const fee = await reportLateCore(
      { rideId: "ride-1", riderUid: RIDER, kind: "late" },
      ctxFor(HOST, userDoc(), GRACE_OVER_AT),
    );
    await markBoardedCore(
      { rideId: "ride-1", riderUid: RIDER },
      ctxFor(HOST, userDoc(), GRACE_OVER_AT),
    );
    await finishRideHandler({ rideId: "ride-1" }, ctxFor(HOST, userDoc(), FINISH_AT));

    const out = await confirmRidePaymentCore(
      { rideId: "ride-1", requestId: nextRequestId() },
      ctxFor(HOST, userDoc(), FINISH_AT),
    );

    // 56 for the seat plus the 10 EGP fee.
    expect(out).toEqual({ paidBookings: 1, total: 66 });
    expect(await balanceOf(RIDER)).toBe(234);
    expect(await balanceOf(HOST)).toBe(66);

    const settled = (await db.doc(`fees/${fee.feeId}`).get()).data() as FeeDoc;
    expect(settled.status).toBe("paid");
    const rider = (await db.doc(`users/${RIDER}`).get()).data() as UserDoc;
    expect(rider.rider.unpaidFees).toBe(0);
    await expectBalanceMatchesEntries(RIDER);
  });

  it("leaves a disputed fee for an admin to settle", async () => {
    await putRide("ride-1");
    await requestSeatsCore(
      { rideId: "ride-1", seats: 1, pickupId: "start", requestId: nextRequestId() },
      ctxFor(RIDER, riderDoc()),
    );
    await respondToRequestCore(
      { rideId: "ride-1", riderUid: RIDER, accept: true },
      ctxFor(HOST, userDoc()),
    );
    await startRideCore({ rideId: "ride-1" }, ctxFor(HOST, userDoc(), START_AT));
    await arriveAtStopCore(
      { rideId: "ride-1", pickupId: "start" },
      ctxFor(HOST, userDoc(), ARRIVED_AT),
    );
    const fee = await reportLateCore(
      { rideId: "ride-1", riderUid: RIDER, kind: "late" },
      ctxFor(HOST, userDoc(), GRACE_OVER_AT),
    );
    await db.doc(`fees/${fee.feeId}`).update({ status: "disputed" });
    await markBoardedCore(
      { rideId: "ride-1", riderUid: RIDER },
      ctxFor(HOST, userDoc(), GRACE_OVER_AT),
    );
    await finishRideHandler({ rideId: "ride-1" }, ctxFor(HOST, userDoc(), FINISH_AT));

    const out = await confirmRidePaymentCore(
      { rideId: "ride-1", requestId: nextRequestId() },
      ctxFor(HOST, userDoc(), FINISH_AT),
    );

    // The fare only: the disputed 10 EGP waits for resolveFee.
    expect(out).toEqual({ paidBookings: 1, total: 56 });
    expect(await balanceOf(RIDER)).toBe(244);
    expect(((await db.doc(`fees/${fee.feeId}`).get()).data() as FeeDoc).status).toBe("disputed");
  });

  it("pays once for the same requestId", async () => {
    await rideTo("ride-1", [{ uid: RIDER, seats: 1 }]);
    const requestId = nextRequestId();

    const first = await confirmRidePaymentCore(
      { rideId: "ride-1", requestId },
      ctxFor(HOST, userDoc(), FINISH_AT),
    );
    const second = await confirmRidePaymentCore(
      { rideId: "ride-1", requestId },
      ctxFor(HOST, userDoc(), FINISH_AT),
    );

    expect(second).toEqual(first);
    expect(await balanceOf(RIDER)).toBe(244);
    expect(await balanceOf(HOST)).toBe(56);
    expect(await entriesOf(HOST)).toHaveLength(2); // the opening row plus one earning
  });

  it("refuses a second Confirm Payment on the same ride", async () => {
    await rideTo("ride-1", [{ uid: RIDER, seats: 1 }]);
    await confirmRidePaymentCore(
      { rideId: "ride-1", requestId: nextRequestId() },
      ctxFor(HOST, userDoc(), FINISH_AT),
    );

    await expectReason(
      confirmRidePaymentCore(
        { rideId: "ride-1", requestId: nextRequestId() },
        ctxFor(HOST, userDoc(), FINISH_AT),
      ),
      "PAYMENT_ALREADY_CONFIRMED",
    );
    expect(await balanceOf(HOST)).toBe(56);
  });

  it("refuses anyone who is not the ride's owner", async () => {
    await rideTo("ride-1", [{ uid: RIDER, seats: 1 }]);
    await expectReason(
      confirmRidePaymentCore(
        { rideId: "ride-1", requestId: nextRequestId() },
        ctxFor(HOST2, userDoc(), FINISH_AT),
      ),
      "NOT_FOUND",
    );
    expect(await balanceOf(RIDER)).toBe(300);
  });

  it("refuses a ride that has not finished", async () => {
    await putRide("ride-1");
    await requestSeatsCore(
      { rideId: "ride-1", seats: 1, pickupId: "start", requestId: nextRequestId() },
      ctxFor(RIDER, riderDoc()),
    );
    await respondToRequestCore(
      { rideId: "ride-1", riderUid: RIDER, accept: true },
      ctxFor(HOST, userDoc()),
    );

    await expectReason(
      confirmRidePaymentCore(
        { rideId: "ride-1", requestId: nextRequestId() },
        ctxFor(HOST, userDoc(), FINISH_AT),
      ),
      "RIDE_NOT_FINISHED",
    );

    await startRideCore({ rideId: "ride-1" }, ctxFor(HOST, userDoc(), START_AT));
    await expectReason(
      confirmRidePaymentCore(
        { rideId: "ride-1", requestId: nextRequestId() },
        ctxFor(HOST, userDoc(), FINISH_AT),
      ),
      "RIDE_NOT_FINISHED",
    );
  });

  it("pays nothing for a rider who never boarded", async () => {
    await rideTo("ride-1", [{ uid: RIDER, seats: 1 }], { boardAll: false });

    await expectReason(
      confirmRidePaymentCore(
        { rideId: "ride-1", requestId: nextRequestId() },
        ctxFor(HOST, userDoc(), FINISH_AT),
      ),
      "PAYMENT_ALREADY_CONFIRMED",
    );
    expect(await balanceOf(RIDER)).toBe(300);
    expect(await balanceOf(HOST)).toBe(0);
  });

  it("lets a late fee push a balance below 0, which then blocks the next request", async () => {
    // Just enough for the fare, nothing for the fee [DECISION-30].
    await setWallet(RIDER, 56);
    await putRide("ride-1");
    await requestSeatsCore(
      { rideId: "ride-1", seats: 1, pickupId: "start", requestId: nextRequestId() },
      ctxFor(RIDER, riderDoc()),
    );
    await respondToRequestCore(
      { rideId: "ride-1", riderUid: RIDER, accept: true },
      ctxFor(HOST, userDoc()),
    );
    await startRideCore({ rideId: "ride-1" }, ctxFor(HOST, userDoc(), START_AT));
    await arriveAtStopCore(
      { rideId: "ride-1", pickupId: "start" },
      ctxFor(HOST, userDoc(), ARRIVED_AT),
    );
    await reportLateCore(
      { rideId: "ride-1", riderUid: RIDER, kind: "late" },
      ctxFor(HOST, userDoc(), GRACE_OVER_AT),
    );
    await markBoardedCore(
      { rideId: "ride-1", riderUid: RIDER },
      ctxFor(HOST, userDoc(), GRACE_OVER_AT),
    );
    await finishRideHandler({ rideId: "ride-1" }, ctxFor(HOST, userDoc(), FINISH_AT));

    await confirmRidePaymentCore(
      { rideId: "ride-1", requestId: nextRequestId() },
      ctxFor(HOST, userDoc(), FINISH_AT),
    );

    expect(await balanceOf(RIDER)).toBe(-10);
    expect(await balanceOf(HOST)).toBe(66);
    await expectBalanceMatchesEntries(RIDER);

    // A negative balance cannot cover anything, so the next request is refused.
    await putRide("ride-2", {
      hostId: HOST2,
      departAt: Timestamp.fromDate(new Date("2026-11-02T09:30:00Z")),
    });
    await expectReason(
      requestSeatsCore(
        { rideId: "ride-2", seats: 1, pickupId: "start", requestId: nextRequestId() },
        ctxFor(RIDER, riderDoc()),
      ),
      "INSUFFICIENT_BALANCE",
    );
  });
});

describe("topUpWallet", () => {
  it("adds balance, records an entry and audits the admin", async () => {
    const requestId = nextRequestId();
    const out = await topUpWalletCore({ uid: RIDER, amount: 250, requestId }, ctxFor(ADMIN, null));

    expect(out).toEqual({ balance: 550 }); // 300 + 250
    expect(await balanceOf(RIDER)).toBe(550);

    const entry = (await entriesByReason(RIDER, "top_up")).at(0);
    expect(entry).toMatchObject({
      delta: 250,
      reason: "top_up",
      refId: requestId,
      balanceAfter: 550,
      by: ADMIN,
    });
    await expectBalanceMatchesEntries(RIDER);

    const audit = await db.collection("adminActions").get();
    expect(audit.size).toBe(1);
    expect(audit.docs[0]?.data() as AdminActionDoc).toMatchObject({
      adminUid: ADMIN,
      action: "top_up_wallet",
      targetUid: RIDER,
      refId: requestId,
    });
  });

  it("tops up once for the same requestId", async () => {
    const requestId = nextRequestId();
    await topUpWalletCore({ uid: RIDER, amount: 100, requestId }, ctxFor(ADMIN, null));
    const second = await topUpWalletCore(
      { uid: RIDER, amount: 100, requestId },
      ctxFor(ADMIN, null),
    );

    expect(second).toEqual({ balance: 400 });
    expect(await balanceOf(RIDER)).toBe(400);
    expect(await entriesOf(RIDER)).toHaveLength(2); // the opening row plus one top-up
  });

  it("creates the wallet when the user has none yet", async () => {
    await db.recursiveDelete(db.doc(`wallets/${RIDER2}`));
    const out = await topUpWalletCore(
      { uid: RIDER2, amount: 500, requestId: nextRequestId() },
      ctxFor(ADMIN, null),
    );

    expect(out).toEqual({ balance: 500 });
    await expectBalanceMatchesEntries(RIDER2);
  });

  it("refuses a user who does not exist", async () => {
    await expectReason(
      topUpWalletCore(
        { uid: "p3b05-nobody", amount: 100, requestId: nextRequestId() },
        ctxFor(ADMIN, null),
      ),
      "NOT_FOUND",
    );
  });

  it("refuses a caller without the admin claim", async () => {
    // topUpWallet is built with { admin: true }, and buildCtx is where that is enforced.
    const asRider = {
      auth: { uid: RIDER, token: {} },
      app: undefined,
      data: {},
      rawRequest: {},
    } as unknown as Parameters<typeof buildCtx>[0];

    await expectReason(
      buildCtx(asRider, { admin: true, profile: false, allowSuspended: true }),
      "PERMISSION_DENIED",
    );

    const asAdmin = {
      auth: { uid: ADMIN, token: { admin: true } },
      app: undefined,
      data: {},
      rawRequest: {},
    } as unknown as Parameters<typeof buildCtx>[0];

    const ctx = await buildCtx(asAdmin, { admin: true, profile: false, allowSuspended: true });
    expect(ctx.isAdmin).toBe(true);
  });
});
