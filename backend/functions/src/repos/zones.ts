// All Firestore reads for zones and their pickup points (BUILD_PLAN 4.2, 6.4).
import type { Transaction } from "firebase-admin/firestore";
import type { PointDoc, ZoneDoc } from "../schemas/docs.js";
import { db } from "../lib/app.js";

export async function getZone(zoneId: string, tx?: Transaction): Promise<ZoneDoc | null> {
  const docRef = db.collection("zones").doc(zoneId);
  const snap = await (tx ? tx.get(docRef) : docRef.get());
  return snap.exists ? (snap.data() as ZoneDoc) : null;
}

export async function getPoint(
  zoneId: string,
  pointId: string,
  tx?: Transaction,
): Promise<PointDoc | null> {
  const docRef = db.collection("zones").doc(zoneId).collection("points").doc(pointId);
  const snap = await (tx ? tx.get(docRef) : docRef.get());
  return snap.exists ? (snap.data() as PointDoc) : null;
}

// A pickup point counts only when both the zone and the point are active.
export async function getActivePoint(
  zoneId: string,
  pointId: string,
  tx?: Transaction,
): Promise<PointDoc | null> {
  const zone = await getZone(zoneId, tx);
  if (!zone?.active) return null;
  const point = await getPoint(zoneId, pointId, tx);
  return point?.active ? point : null;
}
