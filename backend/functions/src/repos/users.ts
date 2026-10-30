// All Firestore reads and writes for users/{uid} (BUILD_PLAN 4.2, 6.2).
import type { Transaction } from "firebase-admin/firestore";
import type { UserDoc } from "../schemas/docs.js";
import { db } from "../lib/app.js";

export async function getUser(uid: string, tx?: Transaction): Promise<UserDoc | null> {
  const docRef = db.collection("users").doc(uid);
  const snap = await (tx ? tx.get(docRef) : docRef.get());
  return snap.exists ? (snap.data() as UserDoc) : null;
}
