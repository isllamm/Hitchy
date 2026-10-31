// P3-B06: onBookingChanged and the inbox (BUILD_PLAN 8.6, 8.15) against the emulators.
// FCM is not emulated, so the push is replaced by a fake and the inbox is what we assert on.
import { Timestamp } from "firebase-admin/firestore";
import { afterAll, afterEach, beforeAll, beforeEach, describe, expect, it } from "vitest";
import { requestSeatsCore } from "../../src/handlers/booking/requestSeats.js";
import { respondToRequestCore } from "../../src/handlers/booking/respondToRequest.js";
import { cancelBookingCore } from "../../src/handlers/booking/cancelBooking.js";
import { confirmRidePaymentCore } from "../../src/handlers/trip/confirmRidePayment.js";
import { finishRideHandler } from "../../src/handlers/trip/finishRide.js";
import { markBoardedCore } from "../../src/handlers/trip/markBoarded.js";
import { startRideCore } from "../../src/handlers/trip/startRide.js";
import { db } from "../../src/lib/app.js";
import { resetClock, setClock } from "../../src/lib/clock.js";
import { clearConfigCache } from "../../src/lib/config.js";
import type { CallCtx } from "../../src/lib/ctx.js";
import { resetPushSender, setPushSender } from "../../src/lib/notify.js";
import { noticeFor, onBookingChangedCore } from "../../src/triggers/onBookingChanged.js";
import type {
  BookingDoc,
  DeviceDoc,
  NotificationDoc,
  RideDoc,
  UserDoc,
  WalletDoc,
} from "../../src/schemas/docs.js";
import { runSeed } from "../../src/seed/seed.js";
import {
  FIXED_NOW,
  limitsConfig,
  pricingConfig,
  profileDoc,
  rideDoc,
  userDoc,
} from "../helpers/docs.js";

const HOST = "p3b06-host";
const RIDER = "p3b06-rider";

const ZONE_A = "p3b06-a";
const ZONE_C = "p3b06-c";

const DEPART = new Date("2026-11-02T06:30:00Z");
const START_AT = new Date("2026-11-02T06:10:00Z");
const FINISH_AT = new Date("2026-11-02T07:40:00Z");

let requestIdCounter = 0;
function nextRequestId(): string {
  requestIdCounter += 1;
  return `f0000000-0000-4000-8000-${String(requestIdCounter).padStart(12, "0")}`;
}

function ctxFor(uid: string, user: UserDoc, at: Date = FIXED_NOW): CallCtx {
  return { uid, phone: null, isAdmin: false, user, now: at };
}

function riderDoc(overrides: Partial<UserDoc> = {}): UserDoc {
  const base = userDoc({ activeRole: "rider", host: { ...userDoc().host, status: "none" } });
  return { ...base, ...overrides };
}

function bookingFixture(overrides: Partial<BookingDoc> = {}): BookingDoc {
  const at = Timestamp.fromDate(FIXED_NOW);
  return {
    schemaVersion: 1,
    createdAt: at,
    updatedAt: at,
    rideId: "ride-1",
    riderUid: RIDER,
    hostId: HOST,
    departAt: Timestamp.fromDate(DEPART),
    status: "requested",
    seats: 2,
    price: 56,
    total: 112,
    pickupId: "start",
    pickupPointName: { ar: "نقطة", en: "Point" },
    pickupPoint: { lat: 30.0, lng: 31.0 },
    pickupAt: Timestamp.fromDate(DEPART),
    expiresAt: Timestamp.fromDate(new Date("2026-11-02T06:00:00Z")),
    respondedAt: null,
    checkedInAt: null,
    checkInDistanceM: null,
    hostArrivedAt: null,
    boardedAt: null,
    lateFeeId: null,
    paymentConfirmedAt: null,
    verifiedTogether: false,
    rewarded: false,
    lateCancel: false,
    cancelledAt: null,
    cancelReason: null,
    ...overrides,
  };
}

async function inboxOf(uid: string): Promise<NotificationDoc[]> {
  const snap = await db.collection(`users/${uid}/notifications`).get();
  return snap.docs.map((d) => d.data() as NotificationDoc);
}

async function clearInboxes(): Promise<void> {
  for (const uid of [HOST, RIDER]) {
    await db.recursiveDelete(db.collection(`users/${uid}/notifications`));
  }
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

async function deleteCollection(path: string): Promise<void> {
  const snap = await db.collection(path).get();
  await Promise.all(snap.docs.map((d) => db.recursiveDelete(d.ref)));
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
  // A push that always works, so a test only fails for inbox reasons.
  setPushSender(() => Promise.resolve({ responses: [], failureCount: 0 }));

  await db.doc("config/pricing").set(pricingConfig());
  await db.doc("config/limits").set(limitsConfig());
  clearConfigCache();

  await deleteCollection("rides");
  await deleteCollection("idempotency");

  await db.doc(`users/${HOST}`).set(userDoc({ firstName: "Omar" }));
  await db.doc(`users/${RIDER}`).set(riderDoc({ firstName: "Mona" }));
  await db.doc(`profiles/${HOST}`).set(profileDoc({ firstName: "Omar" }));
  await db.doc(`profiles/${RIDER}`).set(profileDoc({ firstName: "Mona" }));

  const wallet: WalletDoc = { balance: 1000, updatedAt: Timestamp.fromDate(FIXED_NOW) };
  await db.doc(`wallets/${RIDER}`).set(wallet);
  await db.doc(`wallets/${HOST}`).set({ balance: 0, updatedAt: wallet.updatedAt });

  await clearInboxes();
});

afterEach(() => {
  resetPushSender();
});

afterAll(async () => {
  resetClock();
  resetPushSender();
  await deleteCollection("rides");
  await deleteCollection("idempotency");
  for (const uid of [HOST, RIDER]) {
    await db.recursiveDelete(db.doc(`users/${uid}`));
    await db.doc(`profiles/${uid}`).delete();
    await db.recursiveDelete(db.doc(`wallets/${uid}`));
  }
  for (const zone of [ZONE_A, ZONE_C]) {
    await db.recursiveDelete(db.doc(`zones/${zone}`));
  }
  await db.doc(`routes/${ZONE_A}__${ZONE_C}`).delete();
  await runSeed({ project: "demo-hitchy", silent: true });
});

describe("noticeFor", () => {
  it("maps every change in the table of 8.6", () => {
    const req = bookingFixture({ status: "requested" });
    const booked = bookingFixture({ status: "booked" });

    expect(noticeFor(null, req)).toEqual({ to: "host", type: "booking_requested" });
    expect(noticeFor(req, booked)).toEqual({ to: "rider", type: "booking_accepted" });
    expect(noticeFor(req, bookingFixture({ status: "declined" }))).toEqual({
      to: "rider",
      type: "booking_declined",
    });
    expect(noticeFor(req, bookingFixture({ status: "expired" }))).toEqual({
      to: "rider",
      type: "request_expired",
    });
    expect(noticeFor(req, bookingFixture({ status: "cancelled_by_rider" }))).toEqual({
      to: "host",
      type: "booking_cancelled",
    });
    expect(noticeFor(booked, bookingFixture({ status: "cancelled_by_rider" }))).toEqual({
      to: "host",
      type: "booking_cancelled",
    });
    expect(noticeFor(req, bookingFixture({ status: "cancelled_by_host" }))).toEqual({
      to: "rider",
      type: "ride_cancelled",
    });
    expect(noticeFor(booked, bookingFixture({ status: "cancelled_by_host" }))).toEqual({
      to: "rider",
      type: "ride_cancelled",
    });
  });

  it("says nothing for a change that is not in the table", () => {
    const booked = bookingFixture({ status: "booked" });

    // Boarding, completing and no-shows are silent here.
    expect(noticeFor(booked, bookingFixture({ status: "boarded" }))).toBeNull();
    expect(
      noticeFor(bookingFixture({ status: "boarded" }), bookingFixture({ status: "completed" })),
    ).toBeNull();
    expect(noticeFor(booked, bookingFixture({ status: "no_show" }))).toBeNull();

    // A write that changes no status, and a deleted booking.
    expect(noticeFor(booked, booked)).toBeNull();
    expect(noticeFor(booked, null)).toBeNull();
    expect(noticeFor(null, null)).toBeNull();

    // A booking cannot go straight to booked without being requested first.
    expect(noticeFor(bookingFixture({ status: "declined" }), booked)).toBeNull();
  });
});

describe("the inbox", () => {
  it("tells the owner about a new request, with the rider's first name and seats", async () => {
    await onBookingChangedCore(null, bookingFixture({ status: "requested" }));

    const host = await inboxOf(HOST);
    expect(host).toHaveLength(1);
    expect(host[0]?.type).toBe("booking_requested");
    expect(host[0]?.title.en).toBe("Mona asked for 2 seats");
    expect(host[0]?.body.en).toBe("Open the ride to accept or decline.");
    expect(host[0]?.title.ar).toContain("Mona");
    expect(host[0]?.deepLink).toBe("hitchy://ride/ride-1");
    expect(host[0]?.readAt).toBeNull();
    expect(await inboxOf(RIDER)).toHaveLength(0);
  });

  it("keeps both languages and an expiry 30 days out [DECISION-27]", async () => {
    await onBookingChangedCore(
      bookingFixture({ status: "requested" }),
      bookingFixture({ status: "booked" }),
    );

    const rider = await inboxOf(RIDER);
    expect(rider).toHaveLength(1);
    expect(rider[0]?.title.en).toBe("Omar accepted your request");
    expect(rider[0]?.body.en).toBe("Your seat is confirmed. See you at the pickup point.");
    expect(rider[0]?.title.ar.length).toBeGreaterThan(0);

    const created = rider[0]?.createdAt.toDate().getTime() ?? 0;
    const expires = rider[0]?.expireAt.toDate().getTime() ?? 0;
    expect((expires - created) / (24 * 60 * 60 * 1000)).toBe(30);
  });

  it("writes the inbox even when the push fails", async () => {
    // A device token exists and FCM refuses the whole send, as it does on the emulator.
    const device: DeviceDoc = {
      fcmToken: "fake-token",
      platform: "android",
      appVersion: "0.1.0",
      language: "en",
      updatedAt: Timestamp.fromDate(FIXED_NOW),
    };
    await db.doc(`users/${HOST}/devices/install-1`).set(device);
    setPushSender(() => Promise.reject(new Error("FCM is not available")));

    await onBookingChangedCore(null, bookingFixture({ status: "requested" }));

    expect(await inboxOf(HOST)).toHaveLength(1);
    await db.doc(`users/${HOST}/devices/install-1`).delete();
  });

  it("sends nothing for a silent change", async () => {
    await onBookingChangedCore(
      bookingFixture({ status: "booked" }),
      bookingFixture({ status: "boarded" }),
    );
    expect(await inboxOf(HOST)).toHaveLength(0);
    expect(await inboxOf(RIDER)).toHaveLength(0);
  });
});

describe("the demo cycle's messages", () => {
  it("tells the owner of the request and the rider of the acceptance", async () => {
    await putRide("ride-1");
    await requestSeatsCore(
      { rideId: "ride-1", seats: 1, pickupId: "start", requestId: nextRequestId() },
      ctxFor(RIDER, riderDoc()),
    );
    // The trigger runs on the written document, as the Functions emulator would run it.
    await onBookingChangedCore(
      null,
      (await db.doc(`rides/ride-1/bookings/${RIDER}`).get()).data() as BookingDoc,
    );
    expect((await inboxOf(HOST))[0]?.type).toBe("booking_requested");

    const before = (await db.doc(`rides/ride-1/bookings/${RIDER}`).get()).data() as BookingDoc;
    await respondToRequestCore(
      { rideId: "ride-1", riderUid: RIDER, accept: true },
      ctxFor(HOST, userDoc()),
    );
    await onBookingChangedCore(
      before,
      (await db.doc(`rides/ride-1/bookings/${RIDER}`).get()).data() as BookingDoc,
    );
    expect((await inboxOf(RIDER))[0]?.type).toBe("booking_accepted");
  });

  it("tells the owner when the rider cancels", async () => {
    await putRide("ride-1");
    await requestSeatsCore(
      { rideId: "ride-1", seats: 1, pickupId: "start", requestId: nextRequestId() },
      ctxFor(RIDER, riderDoc()),
    );
    const before = (await db.doc(`rides/ride-1/bookings/${RIDER}`).get()).data() as BookingDoc;
    await cancelBookingCore(
      { rideId: "ride-1", reason: "plans_changed" },
      ctxFor(RIDER, riderDoc()),
    );
    await onBookingChangedCore(
      before,
      (await db.doc(`rides/ride-1/bookings/${RIDER}`).get()).data() as BookingDoc,
    );

    const host = await inboxOf(HOST);
    expect(host.map((n) => n.type)).toContain("booking_cancelled");
  });

  it("tells the rider their payment is confirmed, with a link to the receipt", async () => {
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
    await markBoardedCore({ rideId: "ride-1", riderUid: RIDER }, ctxFor(HOST, userDoc(), START_AT));
    await finishRideHandler({ rideId: "ride-1" }, ctxFor(HOST, userDoc(), FINISH_AT));
    await clearInboxes();

    await confirmRidePaymentCore(
      { rideId: "ride-1", requestId: nextRequestId() },
      ctxFor(HOST, userDoc(), FINISH_AT),
    );

    const rider = await inboxOf(RIDER);
    expect(rider).toHaveLength(1);
    expect(rider[0]?.type).toBe("payment_confirmed");
    expect(rider[0]?.title.en).toBe("Omar confirmed your payment");
    expect(rider[0]?.deepLink).toBe("hitchy://receipt/ride-1");
  });
});
