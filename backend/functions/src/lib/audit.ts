// Every admin change leaves an adminActions entry (BUILD_PLAN 6.8).
import { Timestamp, type Transaction } from "firebase-admin/firestore";
import type { AdminActionDoc } from "../schemas/docs.js";
import { db } from "./app.js";
import { now } from "./clock.js";

export interface AdminAction {
  adminUid: string;
  action: string;
  targetUid?: string | null;
  refId?: string | null;
  note?: string | null;
}

// Writes inside the caller's transaction when one is given; returns the entry id.
export async function logAdminAction(entry: AdminAction, tx?: Transaction): Promise<string> {
  const ref = db.collection("adminActions").doc();
  const doc: AdminActionDoc = {
    adminUid: entry.adminUid,
    action: entry.action,
    targetUid: entry.targetUid ?? null,
    refId: entry.refId ?? null,
    note: entry.note ?? null,
    createdAt: Timestamp.fromDate(now()),
  };
  if (tx) {
    tx.set(ref, doc);
  } else {
    await ref.set(doc);
  }
  return ref.id;
}
