// Helpers for the security rules tests (test/rules). They run on the emulators started by
// `npm test`, in project demo-hitchy. Test data uses fake ids and no personal data.
import { readFileSync } from "node:fs";
import {
  initializeTestEnvironment,
  type RulesTestContext,
  type RulesTestEnvironment,
} from "@firebase/rules-unit-testing";
import { doc, setDoc, Timestamp, type DocumentData, type Firestore } from "firebase/firestore";
import type { FirebaseStorage } from "firebase/storage";

export const PROJECT_ID = "demo-hitchy";

// Fake uids used across the rules tests.
export const ADMIN = "admin1";
export const HOST = "host1";
export const RIDER = "rider1";
export const RIDER2 = "rider2";
export const STRANGER = "stranger1";

export function firestoreRulesEnv(): Promise<RulesTestEnvironment> {
  return initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules: readFileSync(new URL("../../../firestore.rules", import.meta.url), "utf8"),
    },
  });
}

export function storageRulesEnv(): Promise<RulesTestEnvironment> {
  return initializeTestEnvironment({
    projectId: PROJECT_ID,
    storage: {
      rules: readFileSync(new URL("../../../storage.rules", import.meta.url), "utf8"),
    },
    firestore: {
      rules: readFileSync(new URL("../../../firestore.rules", import.meta.url), "utf8"),
    },
  });
}

// The context's Firestore works with the modular API; its type is the compat one.
function fs(ctx: RulesTestContext): Firestore {
  return ctx.firestore() as unknown as Firestore;
}

function st(ctx: RulesTestContext): FirebaseStorage {
  return ctx.storage();
}

export function anon(env: RulesTestEnvironment): Firestore {
  return fs(env.unauthenticatedContext());
}

export function asUser(env: RulesTestEnvironment, uid: string): Firestore {
  return fs(env.authenticatedContext(uid));
}

export function asAdmin(env: RulesTestEnvironment): Firestore {
  return fs(env.authenticatedContext(ADMIN, { admin: true }));
}

export function storageAnon(env: RulesTestEnvironment): FirebaseStorage {
  return st(env.unauthenticatedContext());
}

export function storageAsUser(env: RulesTestEnvironment, uid: string): FirebaseStorage {
  return st(env.authenticatedContext(uid));
}

export function storageAsAdmin(env: RulesTestEnvironment): FirebaseStorage {
  return st(env.authenticatedContext(ADMIN, { admin: true }));
}

// Writes documents with the rules off, as the functions (Admin SDK) would.
export async function seed(
  env: RulesTestEnvironment,
  docs: Record<string, DocumentData>,
): Promise<void> {
  await env.withSecurityRulesDisabled(async (ctx) => {
    const db = fs(ctx);
    await Promise.all(Object.entries(docs).map(([path, data]) => setDoc(doc(db, path), data)));
  });
}

export const someTime = Timestamp.fromDate(new Date("2026-11-02T05:30:00Z"));

// A complete users/{uid} document (section 6.2) with fake values.
export function userDoc(overrides: DocumentData = {}): DocumentData {
  return {
    schemaVersion: 1,
    createdAt: someTime,
    updatedAt: someTime,
    phone: "test-phone",
    firstName: "Test",
    lastName: "User",
    gender: "female",
    companyId: null,
    companyOther: null,
    workHours: null,
    language: "en",
    photoPath: null,
    emergencyContact: null,
    savedPlaces: [],
    activeRole: "rider",
    referralCode: "TESTCODE",
    referredBy: null,
    referralRewarded: false,
    blockedUids: [],
    suspended: false,
    rider: {
      tier: "bronze",
      ratingSum: 0,
      ratingCount: 0,
      tripsCompleted: 0,
      unpaidFees: 0,
      lateCancels: 0,
    },
    host: {
      status: "none",
      verified: false,
      approvedAt: null,
      freeUntil: null,
      freeEndingNotified: false,
      credits: { regular: 0, fixed: 0 },
      tier: "bronze",
      ratingSum: 0,
      ratingCount: 0,
      tripsCompleted: 0,
      lateCancels: 0,
      hostLateCount: 0,
    },
    points: { balance: 0 },
    ...overrides,
  };
}
