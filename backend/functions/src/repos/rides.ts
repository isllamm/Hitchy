// All Firestore reads and writes for rides/{rideId} (BUILD_PLAN 4.2, 6.5).
import type { DocumentReference, Transaction } from "firebase-admin/firestore";
import type { RideDoc, RideStatus } from "../schemas/docs.js";
import { db } from "../lib/app.js";

export interface RideWithId extends RideDoc {
  rideId: string;
}

function collection() {
  return db.collection("rides");
}

export function newRideRef(): DocumentReference {
  return collection().doc();
}

export async function getRide(rideId: string, tx?: Transaction): Promise<RideDoc | null> {
  const docRef = collection().doc(rideId);
  const snap = await (tx ? tx.get(docRef) : docRef.get());
  return snap.exists ? (snap.data() as RideDoc) : null;
}

/**
 * Every ride of one host, read inside the transaction. createRide needs three views of this
 * list at once (the daily limit, the time gap and the open rides holding credits), so it reads
 * it once and filters in memory. A host has at most a couple of rides a day; P6-B02 narrows the
 * query if that ever stops being true.
 */
export async function hostRides(hostId: string, tx: Transaction): Promise<RideWithId[]> {
  const snap = await tx.get(collection().where("hostId", "==", hostId));
  return snap.docs.map((d) => ({ rideId: d.id, ...(d.data() as RideDoc) }));
}

export function writeRide(tx: Transaction, ref: DocumentReference, ride: RideDoc): void {
  tx.set(ref, ride);
}

export function updateRide(tx: Transaction, rideId: string, fields: Partial<RideDoc>): void {
  tx.update(collection().doc(rideId), fields);
}

export interface SearchQuery {
  pickupZone: string;
  toZone: string;
  departFrom: Date;
  departTo: Date;
  limit: number;
}

/**
 * Open rides that pick up in the rider's zone (v1.2: `pickupZones array-contains`, so a ride is
 * found from any zone it stops in). The composite index is in firestore.indexes.json.
 */
export async function searchOpenRides(q: SearchQuery): Promise<RideWithId[]> {
  const snap = await collection()
    .where("pickupZones", "array-contains", q.pickupZone)
    .where("toZone", "==", q.toZone)
    .where("status", "==", "open")
    .where("departAt", ">=", q.departFrom)
    .where("departAt", "<=", q.departTo)
    .orderBy("departAt")
    .limit(q.limit)
    .get();

  return snap.docs.map((d) => ({ rideId: d.id, ...(d.data() as RideDoc) }));
}

// The statuses that still hold a seat, a credit or a slot in the host's day (6.5).
export const LIVE_RIDE_STATUSES: readonly RideStatus[] = ["open", "full", "started"];
