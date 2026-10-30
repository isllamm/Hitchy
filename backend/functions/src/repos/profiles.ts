// All Firestore reads for profiles/{uid}, the public card (BUILD_PLAN 4.2, 6.2).
import type { Transaction } from "firebase-admin/firestore";
import type { ProfileDoc } from "../schemas/docs.js";
import { db } from "../lib/app.js";

export async function getProfile(uid: string, tx?: Transaction): Promise<ProfileDoc | null> {
  const ref = db.collection("profiles").doc(uid);
  const snap = await (tx ? tx.get(ref) : ref.get());
  return snap.exists ? (snap.data() as ProfileDoc) : null;
}
