// P4-B03: syncPublicProfile against the emulators (BUILD_PLAN 8.3).
import { Timestamp } from "firebase-admin/firestore";
import { afterAll, afterEach, beforeAll, describe, expect, it } from "vitest";
import { db } from "../../src/lib/app.js";
import { resetClock, setClock } from "../../src/lib/clock.js";
import { getProfile } from "../../src/repos/profiles.js";
import type { ProfileDoc } from "../../src/schemas/docs.js";
import { syncPublicProfileCore } from "../../src/triggers/syncPublicProfile.js";
import { FIXED_NOW, userDoc } from "../helpers/docs.js";

const UID_CORE = "p4b03-user-core";
const UID_TRIGGER = "p4b03-user-trigger";

async function waitForProfile(
  uid: string,
  predicate: (doc: ProfileDoc | null) => boolean,
  timeoutMs = 5000,
): Promise<ProfileDoc | null> {
  const deadline = Date.now() + timeoutMs;
  while (Date.now() < deadline) {
    const doc = await getProfile(uid);
    if (predicate(doc)) return doc;
    await new Promise((resolve) => setTimeout(resolve, 100));
  }
  return await getProfile(uid);
}

describe("syncPublicProfile", () => {
  beforeAll(() => {
    setClock(() => FIXED_NOW);
  });

  afterAll(() => {
    resetClock();
  });

  afterEach(async () => {
    await db.collection("profiles").doc(UID_CORE).delete();
    await db.collection("users").doc(UID_TRIGGER).delete();
    await db.collection("profiles").doc(UID_TRIGGER).delete();
  });

  it("creates a public profile when a user is created in Firestore (trigger)", async () => {
    const initialUser = userDoc({
      firstName: "Ramy",
      photoPath: `avatars/${UID_TRIGGER}.jpg`,
    });
    await db.collection("users").doc(UID_TRIGGER).set(initialUser);

    const profile = await waitForProfile(UID_TRIGGER, (doc) => doc !== null);
    expect(profile).not.toBeNull();
    expect(profile?.firstName).toBe("Ramy");
    expect(profile?.photoPath).toBe(`avatars/${UID_TRIGGER}.jpg`);
    expect(profile?.hostRating).toEqual({ avg: null, count: 0 });
    expect(profile?.riderRating).toEqual({ avg: null, count: 0 });
    expect(profile?.hostTrips).toBe(0);
    expect(profile?.verified).toBe(false);
    expect(profile?.hostTier).toBe("bronze");
    expect(profile?.riderTier).toBe("bronze");
  });

  it("copies ratings (rounded to 1 decimal), trips, verified flag, and tiers", async () => {
    const u = userDoc({
      firstName: "Nour",
      photoPath: null,
      rider: {
        tier: "silver",
        ratingSum: 14,
        ratingCount: 3, // 14 / 3 = 4.666... -> 4.7
        tripsCompleted: 12,
        unpaidFees: 0,
        lateCancels: 0,
      },
      host: {
        status: "approved",
        verified: true,
        approvedAt: Timestamp.fromDate(FIXED_NOW),
        freeUntil: null,
        freeEndingNotified: false,
        credits: { regular: 10, fixed: 0 },
        tier: "gold",
        ratingSum: 49,
        ratingCount: 10, // 49 / 10 = 4.9
        tripsCompleted: 25,
        lateCancels: 0,
        hostLateCount: 0,
      },
    });

    const wrote = await syncPublicProfileCore(UID_CORE, u, FIXED_NOW);
    expect(wrote).toBe(true);

    const profile = await getProfile(UID_CORE);
    expect(profile).toEqual({
      schemaVersion: 1,
      firstName: "Nour",
      photoPath: null,
      hostRating: { avg: 4.9, count: 10 },
      hostTrips: 25,
      riderRating: { avg: 4.7, count: 3 },
      verified: true,
      hostTier: "gold",
      riderTier: "silver",
      updatedAt: Timestamp.fromDate(FIXED_NOW),
    });
  });

  it("does not rewrite the profile on an unrelated user change (e.g. workHours)", async () => {
    const baseUser = userDoc({
      firstName: "Ramy",
      workHours: { start: "08:00", end: "16:00" },
    });

    const firstWrite = await syncPublicProfileCore(UID_CORE, baseUser, FIXED_NOW);
    expect(firstWrite).toBe(true);

    const later = new Date("2026-11-03T12:00:00.000Z");
    const unrelatedChange = {
      ...baseUser,
      workHours: { start: "09:30", end: "18:00" },
      activeRole: "rider" as const,
      companyOther: "Test Co",
      updatedAt: Timestamp.fromDate(later),
    };

    const secondWrite = await syncPublicProfileCore(UID_CORE, unrelatedChange, later);
    expect(secondWrite).toBe(false);

    const profile = await getProfile(UID_CORE);
    expect(profile?.updatedAt.toDate()).toEqual(FIXED_NOW);
  });

  it("deletes the public profile when the user document is deleted", async () => {
    const u = userDoc({ firstName: "Ramy" });
    await syncPublicProfileCore(UID_CORE, u, FIXED_NOW);
    expect(await getProfile(UID_CORE)).not.toBeNull();

    const deleted = await syncPublicProfileCore(UID_CORE, null, FIXED_NOW);
    expect(deleted).toBe(true);
    expect(await getProfile(UID_CORE)).toBeNull();

    // Deleting again when already absent is a no-op
    const deletedAgain = await syncPublicProfileCore(UID_CORE, null, FIXED_NOW);
    expect(deletedAgain).toBe(false);
  });
});
