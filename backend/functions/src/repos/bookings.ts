// All Firestore reads and writes for rides/{rideId}/bookings/{riderUid} (BUILD_PLAN 4.2, 6.5).
import type { DocumentReference, Transaction } from "firebase-admin/firestore";
import type { BookingDoc, BookingStatus } from "../schemas/docs.js";
import { db } from "../lib/app.js";

// A booking's id is the rider's uid: one booking per rider per ride (5.3).
export function bookingRef(rideId: string, riderUid: string): DocumentReference {
  return db.collection("rides").doc(rideId).collection("bookings").doc(riderUid);
}

export async function getBooking(
  rideId: string,
  riderUid: string,
  tx?: Transaction,
): Promise<BookingDoc | null> {
  const ref = bookingRef(rideId, riderUid);
  const snap = await (tx ? tx.get(ref) : ref.get());
  return snap.exists ? (snap.data() as BookingDoc) : null;
}

export async function listBookings(rideId: string, tx?: Transaction): Promise<BookingDoc[]> {
  const query = db.collection("rides").doc(rideId).collection("bookings");
  const snap = await (tx ? tx.get(query) : query.get());
  return snap.docs.map((d) => d.data() as BookingDoc);
}

/**
 * Every booking of one rider, across rides, read inside the transaction. requestSeats needs it
 * twice at once: for the time conflict (check 9) and for the wallet hold (`[DECISION-29]`).
 * The collection-group index on riderUid comes from section 10.
 */
export async function riderBookings(riderUid: string, tx: Transaction): Promise<BookingDoc[]> {
  const snap = await tx.get(db.collectionGroup("bookings").where("riderUid", "==", riderUid));
  return snap.docs.map((d) => d.data() as BookingDoc);
}

// A booking still holding seats, a wallet amount or the rider's time (6.5).
export const LIVE_BOOKING_STATUSES: readonly BookingStatus[] = ["requested", "booked", "boarded"];

// The statuses whose fare the rider still owes, used by the wallet hold [DECISION-29].
export const OWED_BOOKING_STATUSES: readonly BookingStatus[] = [
  "requested",
  "booked",
  "boarded",
  "completed",
];

export function writeBooking(tx: Transaction, ref: DocumentReference, booking: BookingDoc): void {
  tx.set(ref, booking);
}

export function updateBooking(
  tx: Transaction,
  ref: DocumentReference,
  fields: Partial<BookingDoc>,
): void {
  tx.update(ref, fields);
}
