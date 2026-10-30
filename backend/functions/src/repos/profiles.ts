// All Firestore reads and writes for profiles/{uid} (BUILD_PLAN 4.2, 6.2).
import type { Transaction } from "firebase-admin/firestore";
import { db } from "../lib/app.js";
import type { ProfileDoc } from "../schemas/docs.js";

export async function getProfile(uid: string, tx?: Transaction): Promise<ProfileDoc | null> {
  const ref = db.collection("profiles").doc(uid);
  const snap = await (tx ? tx.get(ref) : ref.get());
  return snap.exists ? (snap.data() as ProfileDoc) : null;
}

export async function setProfile(uid: string, doc: ProfileDoc): Promise<void> {
  await db.collection("profiles").doc(uid).set(doc);
}

export async function deleteProfile(uid: string): Promise<void> {
  await db.collection("profiles").doc(uid).delete();
}
