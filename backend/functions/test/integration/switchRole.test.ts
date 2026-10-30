// P4-B02: switchRole against the emulators (BUILD_PLAN 8.3).
import { Timestamp } from "firebase-admin/firestore";
import { afterAll, beforeAll, describe, expect, it } from "vitest";
import { switchRoleCore } from "../../src/handlers/profile/switchRole.js";
import { db } from "../../src/lib/app.js";
import { resetClock, setClock } from "../../src/lib/clock.js";
import type { CallCtx } from "../../src/lib/ctx.js";
import type { UserDoc } from "../../src/schemas/docs.js";
import { FIXED_NOW, userDoc } from "../helpers/docs.js";

const UID_APPROVED_HOST = "p4b02-user-approved";
const UID_PENDING_HOST = "p4b02-user-pending";
const UID_PLAIN_RIDER = "p4b02-user-rider";

function ctxFor(uid: string, user: UserDoc | null = null): CallCtx {
  return { uid, phone: "test-phone", isAdmin: false, user, now: FIXED_NOW };
}

describe("switchRole", () => {
  beforeAll(async () => {
    setClock(() => FIXED_NOW);

    // 1. Approved host, currently in rider mode
    const approvedUser = userDoc({
      activeRole: "rider",
      host: {
        status: "approved",
        verified: true,
        approvedAt: Timestamp.fromDate(FIXED_NOW),
        freeUntil: null,
        freeEndingNotified: false,
        credits: { regular: 10, fixed: 0 },
        tier: "bronze",
        ratingSum: 0,
        ratingCount: 0,
        tripsCompleted: 0,
        lateCancels: 0,
        hostLateCount: 0,
      },
    });
    await db.collection("users").doc(UID_APPROVED_HOST).set(approvedUser);

    // 2. Pending host
    const pendingUser = userDoc({
      activeRole: "rider",
      host: {
        status: "pending",
        verified: false,
        approvedAt: null,
        freeUntil: null,
        freeEndingNotified: false,
        credits: { regular: 0, fixed: 0 },
        tier: "bronze",
        ratingSum: 0,
        ratingCount: 0,
        tripsCompleted: 0,
        lateCancels: 0,
        hostLateCount: 0,
      },
    });
    await db.collection("users").doc(UID_PENDING_HOST).set(pendingUser);

    // 3. Plain rider (host.status: "none")
    const plainRider = userDoc({
      activeRole: "rider",
      host: {
        status: "none",
        verified: false,
        approvedAt: null,
        freeUntil: null,
        freeEndingNotified: false,
        credits: { regular: 0, fixed: 0 },
        tier: "bronze",
        ratingSum: 0,
        ratingCount: 0,
        tripsCompleted: 0,
        lateCancels: 0,
        hostLateCount: 0,
      },
    });
    await db.collection("users").doc(UID_PLAIN_RIDER).set(plainRider);
  });

  afterAll(async () => {
    resetClock();
    await db.collection("users").doc(UID_APPROVED_HOST).delete();
    await db.collection("users").doc(UID_PENDING_HOST).delete();
    await db.collection("users").doc(UID_PLAIN_RIDER).delete();
  });

  it("switches an approved host from rider to host and back", async () => {
    const ctx = ctxFor(UID_APPROVED_HOST);

    // Switch to host
    const toHost = await switchRoleCore({ role: "host" }, ctx);
    expect(toHost).toEqual({ activeRole: "host" });

    let snap = await db.collection("users").doc(UID_APPROVED_HOST).get();
    expect((snap.data() as UserDoc).activeRole).toBe("host");

    // Switch back to rider
    const toRider = await switchRoleCore({ role: "rider" }, ctx);
    expect(toRider).toEqual({ activeRole: "rider" });

    snap = await db.collection("users").doc(UID_APPROVED_HOST).get();
    expect((snap.data() as UserDoc).activeRole).toBe("rider");
  });

  it("refuses role switch to host when host status is not approved", async () => {
    // Plain rider (status none)
    await expect(switchRoleCore({ role: "host" }, ctxFor(UID_PLAIN_RIDER))).rejects.toMatchObject({
      reason: "HOST_NOT_APPROVED",
    });

    // Pending host
    await expect(switchRoleCore({ role: "host" }, ctxFor(UID_PENDING_HOST))).rejects.toMatchObject({
      reason: "HOST_NOT_APPROVED",
    });
  });

  it("allows switching to rider even if host is not approved", async () => {
    const out = await switchRoleCore({ role: "rider" }, ctxFor(UID_PLAIN_RIDER));
    expect(out).toEqual({ activeRole: "rider" });
  });

  it("fails with PROFILE_REQUIRED if user doc does not exist", async () => {
    await expect(
      switchRoleCore({ role: "rider" }, ctxFor("p4b02-nonexistent")),
    ).rejects.toMatchObject({
      reason: "PROFILE_REQUIRED",
    });
  });
});
