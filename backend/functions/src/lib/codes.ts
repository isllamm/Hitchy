// Unique short codes: package references, Fixed Ride invites, referral codes (BUILD_PLAN 5.3).
import { randomInt } from "node:crypto";
import { Timestamp, type Transaction } from "firebase-admin/firestore";
import type { CodeDoc } from "../schemas/docs.js";
import { db } from "./app.js";
import { now } from "./clock.js";

// No look-alikes: no 0, O, 1 or I.
export const CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
export const CODE_LENGTH = 6;
const MAX_TRIES = 5;

export type CodeType = CodeDoc["type"];

export function randomCode(): string {
  let code = "";
  for (let i = 0; i < CODE_LENGTH; i++) {
    code += CODE_ALPHABET.charAt(randomInt(CODE_ALPHABET.length));
  }
  return code;
}

// Package references look like "HT-7K3QX9"; invite and referral codes have no prefix.
export function formatCode(type: CodeType, code: string): string {
  return type === "reference" ? `HT-${code}` : code;
}

// Reserves a unique code by creating codes/{code} in the caller's transaction, retrying on a
// collision. It reads and then writes, so call it after the caller's reads and before its writes.
export async function createUniqueCode(
  tx: Transaction,
  type: CodeType,
  ownerId: string,
  refId: string,
): Promise<string> {
  for (let attempt = 0; attempt < MAX_TRIES; attempt++) {
    const code = formatCode(type, randomCode());
    const ref = db.collection("codes").doc(code);
    const taken = await tx.get(ref);
    if (!taken.exists) {
      const doc: CodeDoc = { type, ownerId, refId, createdAt: Timestamp.fromDate(now()) };
      tx.create(ref, doc);
      return code;
    }
  }
  throw new Error(`No free ${type} code after ${String(MAX_TRIES)} tries`);
}
