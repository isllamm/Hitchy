// All Firestore reads for routes/{fromZone}__{toZone} (BUILD_PLAN 4.2, 6.4).
import type { Transaction } from "firebase-admin/firestore";
import type { RouteDoc } from "../schemas/docs.js";
import { db } from "../lib/app.js";

export function routeId(fromZone: string, toZone: string): string {
  return `${fromZone}__${toZone}`;
}

export async function getRoute(
  fromZone: string,
  toZone: string,
  tx?: Transaction,
): Promise<RouteDoc | null> {
  const docRef = db.collection("routes").doc(routeId(fromZone, toZone));
  const snap = await (tx ? tx.get(docRef) : docRef.get());
  return snap.exists ? (snap.data() as RouteDoc) : null;
}
