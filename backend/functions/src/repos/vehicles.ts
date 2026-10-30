// All Firestore reads and writes for users/{uid}/vehicles (BUILD_PLAN 4.2, 6.2).
import type { Transaction } from "firebase-admin/firestore";
import type { VehicleDoc } from "../schemas/docs.js";
import { db } from "../lib/app.js";

function ref(uid: string, vehicleId: string) {
  return db.collection("users").doc(uid).collection("vehicles").doc(vehicleId);
}

// The vehicle, or null when this user has no vehicle with that id.
export async function getVehicle(
  uid: string,
  vehicleId: string,
  tx?: Transaction,
): Promise<VehicleDoc | null> {
  const docRef = ref(uid, vehicleId);
  const snap = await (tx ? tx.get(docRef) : docRef.get());
  return snap.exists ? (snap.data() as VehicleDoc) : null;
}
