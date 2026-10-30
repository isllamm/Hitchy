// All Firestore reads and writes for wallets/{uid} and its entries (BUILD_PLAN 6.6, v1.2).
import { Timestamp, type DocumentReference, type Transaction } from "firebase-admin/firestore";
import type { WalletDoc, WalletEntryDoc, WalletEntryReason } from "../schemas/docs.js";
import { db } from "../lib/app.js";

export function walletRef(uid: string): DocumentReference {
  return db.collection("wallets").doc(uid);
}

/**
 * The wallet, or a zero balance when the user has none yet. Every user is seeded with a wallet;
 * a missing one still reads as 0 so a balance check never crashes on an older account.
 */
export async function getBalance(uid: string, tx?: Transaction): Promise<number> {
  const ref = walletRef(uid);
  const snap = await (tx ? tx.get(ref) : ref.get());
  return snap.exists ? (snap.data() as WalletDoc).balance : 0;
}

// Moves money and records why, in the caller's transaction. Returns the new balance.
export function addToWallet(
  tx: Transaction,
  uid: string,
  delta: number,
  entry: { reason: WalletEntryReason; refId: string | null; by: string },
  currentBalance: number,
  at: Date,
): number {
  const balanceAfter = currentBalance + delta;
  const updatedAt = Timestamp.fromDate(at);

  const wallet: WalletDoc = { balance: balanceAfter, updatedAt };
  tx.set(walletRef(uid), wallet);

  const row: WalletEntryDoc = {
    delta,
    reason: entry.reason,
    refId: entry.refId,
    balanceAfter,
    by: entry.by,
    createdAt: updatedAt,
  };
  tx.set(walletRef(uid).collection("entries").doc(), row);

  return balanceAfter;
}
