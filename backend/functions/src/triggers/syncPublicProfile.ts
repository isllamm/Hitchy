// P4-B03: syncPublicProfile (BUILD_PLAN 8.3).
import { Timestamp } from "firebase-admin/firestore";
import { onDocumentWritten } from "firebase-functions/firestore";
import { REGION } from "../lib/app.js";
import { now } from "../lib/clock.js";
import { deleteProfile, getProfile, setProfile } from "../repos/profiles.js";
import type { ProfileDoc, UserDoc } from "../schemas/docs.js";

function round1(val: number): number {
  return Math.round(val * 10) / 10;
}

export async function syncPublicProfileCore(
  uid: string,
  user: UserDoc | null,
  nowDate: Date = now(),
): Promise<boolean> {
  const existing = await getProfile(uid);

  if (!user) {
    if (existing) {
      await deleteProfile(uid);
      return true;
    }
    return false;
  }

  const hostCount = user.host.ratingCount;
  const hostAvg = hostCount > 0 ? round1(user.host.ratingSum / hostCount) : null;
  const riderCount = user.rider.ratingCount;
  const riderAvg = riderCount > 0 ? round1(user.rider.ratingSum / riderCount) : null;

  if (
    existing &&
    existing.firstName === user.firstName &&
    existing.photoPath === user.photoPath &&
    existing.hostTrips === user.host.tripsCompleted &&
    existing.verified === user.host.verified &&
    existing.hostTier === user.host.tier &&
    existing.riderTier === user.rider.tier &&
    existing.hostRating.count === hostCount &&
    existing.hostRating.avg === hostAvg &&
    existing.riderRating.count === riderCount &&
    existing.riderRating.avg === riderAvg
  ) {
    // Nothing relevant changed (e.g. workHours, phone, savedPlaces) - skip write
    return false;
  }

  const profileDoc: ProfileDoc = {
    schemaVersion: 1,
    firstName: user.firstName,
    photoPath: user.photoPath,
    hostRating: { avg: hostAvg, count: hostCount },
    hostTrips: user.host.tripsCompleted,
    riderRating: { avg: riderAvg, count: riderCount },
    verified: user.host.verified,
    hostTier: user.host.tier,
    riderTier: user.rider.tier,
    updatedAt: Timestamp.fromDate(nowDate),
  };

  await setProfile(uid, profileDoc);
  return true;
}

export const syncPublicProfile = onDocumentWritten(
  {
    document: "users/{uid}",
    region: REGION,
    memory: "256MiB",
  },
  async (event) => {
    const uid = event.params.uid;
    const after = event.data?.after;
    const user = after?.exists ? (after.data() as UserDoc) : null;
    await syncPublicProfileCore(uid, user, now());
  },
);
