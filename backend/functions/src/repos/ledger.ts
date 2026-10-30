// All Firestore writes for users/{uid}/ledger (BUILD_PLAN 4.2, 6.2): every credit change.
import { Timestamp, type Transaction } from "firebase-admin/firestore";
import type { LedgerEntry } from "../schemas/docs.js";
import { db } from "../lib/app.js";

export function addLedgerEntry(
  tx: Transaction,
  uid: string,
  entry: Omit<LedgerEntry, "createdAt">,
  at: Date,
): void {
  const row: LedgerEntry = { ...entry, createdAt: Timestamp.fromDate(at) };
  tx.set(db.collection("users").doc(uid).collection("ledger").doc(), row);
}
