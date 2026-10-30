// All Firestore writes for fees/{feeId} (BUILD_PLAN 4.2, 6.6).
import { Timestamp, type DocumentReference, type Transaction } from "firebase-admin/firestore";
import type { FeeDoc } from "../schemas/docs.js";
import { db } from "../lib/app.js";

export function newFeeRef(): DocumentReference {
  return db.collection("fees").doc();
}

export async function getFee(feeId: string, tx?: Transaction): Promise<FeeDoc | null> {
  const ref = db.collection("fees").doc(feeId);
  const snap = await (tx ? tx.get(ref) : ref.get());
  return snap.exists ? (snap.data() as FeeDoc) : null;
}

export interface NewFee {
  rideId: string;
  riderUid: string;
  hostId: string;
  amount: number;
  reason: FeeDoc["reason"];
  evidence: FeeDoc["evidence"];
}

export function writeFee(tx: Transaction, ref: DocumentReference, fee: NewFee, at: Date): void {
  const createdAt = Timestamp.fromDate(at);
  const doc: FeeDoc = {
    schemaVersion: 1,
    createdAt,
    updatedAt: createdAt,
    rideId: fee.rideId,
    riderUid: fee.riderUid,
    hostId: fee.hostId,
    amount: fee.amount,
    reason: fee.reason,
    status: "unpaid",
    evidence: fee.evidence,
    dispute: null,
    resolvedBy: null,
  };
  tx.set(ref, doc);
}
