// Callables that create things take a requestId and run once per user action (BUILD_PLAN 5.4).
import { Timestamp, type Transaction } from "firebase-admin/firestore";
import type { IdempotencyDoc } from "../schemas/docs.js";
import { db } from "./app.js";
import { now } from "./clock.js";

// A Firestore TTL policy on expireAt deletes the records (P0-H12).
const KEEP_MS = 24 * 60 * 60 * 1000;

// Runs `work` in a transaction and stores its result in idempotency/{uid}_{requestId} in the
// same transaction; a repeated call returns the stored result. The result must be plain data
// Firestore can store (no undefined values).
export function withIdempotency<T>(
  uid: string,
  requestId: string,
  work: (tx: Transaction) => Promise<T>,
): Promise<T> {
  const ref = db.collection("idempotency").doc(`${uid}_${requestId}`);
  return db.runTransaction(async (tx) => {
    const done = await tx.get(ref);
    if (done.exists) return (done.data() as IdempotencyDoc).result as T;
    const result = await work(tx);
    const record: IdempotencyDoc = {
      result,
      expireAt: Timestamp.fromMillis(now().getTime() + KEEP_MS),
    };
    tx.set(ref, record);
    return result;
  });
}
