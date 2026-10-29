import { getApps, initializeApp } from "firebase-admin/app";
import { getAuth } from "firebase-admin/auth";
import { getFirestore } from "firebase-admin/firestore";
import { afterAll, beforeAll, describe, expect, it } from "vitest";
import type { AdminsDoc } from "../../src/schemas/docs.js";
import { setAdmin, validateSetAdminTarget } from "../../src/scripts/setAdmin.js";
import { runSeed } from "../../src/seed/seed.js";

const PROJECT_ID = "demo-hitchy";

beforeAll(() => {
  process.env["FIRESTORE_EMULATOR_HOST"] = "127.0.0.1:8080";
  process.env["FIREBASE_AUTH_EMULATOR_HOST"] = "127.0.0.1:9099";
  if (!getApps().some((a) => a.name === "test-setadmin")) {
    initializeApp({ projectId: PROJECT_ID }, "test-setadmin");
  }
});

afterAll(async () => {
  // Restore seed state so any subsequent emulator tests see fresh seed data
  await runSeed({ project: "demo-hitchy", silent: true });
});

describe("validateSetAdminTarget", () => {
  it("defaults to demo-hitchy emulator when project is omitted or demo-hitchy", () => {
    expect(validateSetAdminTarget(undefined)).toEqual({
      isDev: false,
      isProd: false,
      isEmulator: true,
      targetProject: "demo-hitchy",
    });
    expect(validateSetAdminTarget("demo-hitchy")).toEqual({
      isDev: false,
      isProd: false,
      isEmulator: true,
      targetProject: "demo-hitchy",
    });
    expect(validateSetAdminTarget("local")).toEqual({
      isDev: false,
      isProd: false,
      isEmulator: true,
      targetProject: "demo-hitchy",
    });
  });

  it("refuses unknown project names", () => {
    expect(() => validateSetAdminTarget("staging")).toThrow(/Invalid project: "staging"/);
    expect(() => validateSetAdminTarget("other-cloud")).toThrow(/Invalid project: "other-cloud"/);
  });

  it("refuses dev project when emulator environment variables are set", () => {
    process.env["FIRESTORE_EMULATOR_HOST"] = "127.0.0.1:8080";
    expect(() => validateSetAdminTarget("dev")).toThrow(
      /Refusing to run set-admin on dev project with emulator environment variables set/,
    );
    expect(() => validateSetAdminTarget("hitchy-dev")).toThrow(
      /Refusing to run set-admin on dev project with emulator environment variables set/,
    );
  });

  it("accepts dev project when emulator environment variables are unset", () => {
    const savedFirestore = process.env["FIRESTORE_EMULATOR_HOST"];
    const savedAuth = process.env["FIREBASE_AUTH_EMULATOR_HOST"];
    delete process.env["FIRESTORE_EMULATOR_HOST"];
    delete process.env["FIREBASE_AUTH_EMULATOR_HOST"];

    try {
      expect(validateSetAdminTarget("dev")).toEqual({
        isDev: true,
        isProd: false,
        isEmulator: false,
        targetProject: "hitchy-dev",
      });
      expect(validateSetAdminTarget("hitchy-dev")).toEqual({
        isDev: true,
        isProd: false,
        isEmulator: false,
        targetProject: "hitchy-dev",
      });
    } finally {
      process.env["FIRESTORE_EMULATOR_HOST"] = savedFirestore;
      process.env["FIREBASE_AUTH_EMULATOR_HOST"] = savedAuth;
    }
  });

  it("refuses prod project when --confirm flag is missing", () => {
    expect(() => validateSetAdminTarget("prod", false)).toThrow(
      /Modifying admin status on prod requires --confirm flag/,
    );
    expect(() => validateSetAdminTarget("hitchy-prod")).toThrow(
      /Modifying admin status on prod requires --confirm flag/,
    );
  });

  it("refuses prod project with --confirm when emulator env vars are set", () => {
    process.env["FIRESTORE_EMULATOR_HOST"] = "127.0.0.1:8080";
    expect(() => validateSetAdminTarget("prod", true)).toThrow(
      /Refusing to run set-admin on prod project with emulator environment variables set/,
    );
  });

  it("accepts prod project when --confirm is true and emulator env vars are unset", () => {
    const savedFirestore = process.env["FIRESTORE_EMULATOR_HOST"];
    const savedAuth = process.env["FIREBASE_AUTH_EMULATOR_HOST"];
    delete process.env["FIRESTORE_EMULATOR_HOST"];
    delete process.env["FIREBASE_AUTH_EMULATOR_HOST"];

    try {
      expect(validateSetAdminTarget("prod", true)).toEqual({
        isDev: false,
        isProd: true,
        isEmulator: false,
        targetProject: "hitchy-prod",
      });
      expect(validateSetAdminTarget("hitchy-prod", true)).toEqual({
        isDev: false,
        isProd: true,
        isEmulator: false,
        targetProject: "hitchy-prod",
      });
    } finally {
      process.env["FIRESTORE_EMULATOR_HOST"] = savedFirestore;
      process.env["FIREBASE_AUTH_EMULATOR_HOST"] = savedAuth;
    }
  });
});

describe("setAdmin operations on emulator", () => {
  it("fails with a clear error when --uid is missing or whitespace", async () => {
    await expect(setAdmin({ uid: "", silent: true })).rejects.toThrow(
      /Missing required argument: --uid <uid>/,
    );
    await expect(setAdmin({ uid: "   ", silent: true })).rejects.toThrow(
      /Missing required argument: --uid <uid>/,
    );
  });

  it("fails with a clear error if user is not found in Firebase Auth", async () => {
    await expect(
      setAdmin({
        uid: "non-existent-uid-99999",
        project: "demo-hitchy",
        silent: true,
      }),
    ).rejects.toThrow(/User with UID "non-existent-uid-99999" not found in Firebase Auth\./);
  });

  it("adds admin claim, updates config/admins.uids, keeps other claims, and is idempotent", async () => {
    const app = getApps().find((a) => a.name === "test-setadmin");
    if (!app) {
      throw new Error("Admin app not found");
    }
    const auth = getAuth(app);
    const db = getFirestore(app);

    const testUid = "test-setadmin-user-1";

    // Setup user in Auth with an existing custom claim
    try {
      await auth.deleteUser(testUid);
    } catch {
      // Ignore if not present
    }
    await auth.createUser({
      uid: testUid,
      email: "setadmin1@test.com",
    });
    await auth.setCustomUserClaims(testUid, { role: "reviewer" });

    // Setup existing config/admins with notifyUids
    await db.doc("config/admins").set({
      uids: ["seed-admin-uid"],
      notifyUids: ["phone-alert-user-1"],
    });

    // 1. Add admin
    const result1 = await setAdmin({
      uid: testUid,
      project: "demo-hitchy",
      silent: true,
    });
    expect(result1.admin).toBe(true);
    expect(result1.uid).toBe(testUid);

    // Verify Auth claims
    const userSnap1 = await auth.getUser(testUid);
    expect(userSnap1.customClaims?.["admin"]).toBe(true);
    expect(userSnap1.customClaims?.["role"]).toBe("reviewer");

    // Verify Firestore
    const adminDoc1 = (await db.doc("config/admins").get()).data() as AdminsDoc;
    expect(adminDoc1.uids).toContain("seed-admin-uid");
    expect(adminDoc1.uids).toContain(testUid);
    expect(adminDoc1.notifyUids).toEqual(["phone-alert-user-1"]);

    // 2. Run again (idempotency: no duplicate in uids array)
    const result2 = await setAdmin({
      uid: testUid,
      project: "demo-hitchy",
      silent: true,
    });
    expect(result2.admin).toBe(true);

    const adminDoc2 = (await db.doc("config/admins").get()).data() as AdminsDoc;
    const occurrences = adminDoc2.uids.filter((id) => id === testUid).length;
    expect(occurrences).toBe(1);
    expect(adminDoc2.notifyUids).toEqual(["phone-alert-user-1"]);
  });

  it("removes admin claim, keeps other claims, revokes tokens, and preserves notifyUids", async () => {
    const app = getApps().find((a) => a.name === "test-setadmin");
    if (!app) {
      throw new Error("Admin app not found");
    }
    const auth = getAuth(app);
    const db = getFirestore(app);

    const testUid = "test-setadmin-user-1";

    // Remove admin
    const removeResult = await setAdmin({
      uid: testUid,
      project: "demo-hitchy",
      remove: true,
      silent: true,
    });
    expect(removeResult.admin).toBe(false);
    expect(removeResult.tokensRevoked).toBe(true);

    // Verify Auth claims (admin deleted, role preserved)
    const userSnap = await auth.getUser(testUid);
    expect(userSnap.customClaims?.["admin"]).toBeUndefined();
    expect(userSnap.customClaims?.["role"]).toBe("reviewer");
    expect(userSnap.tokensValidAfterTime).toBeDefined();

    // Verify Firestore (testUid removed from uids, notifyUids untouched)
    const adminDoc = (await db.doc("config/admins").get()).data() as AdminsDoc;
    expect(adminDoc.uids).not.toContain(testUid);
    expect(adminDoc.uids).toContain("seed-admin-uid");
    expect(adminDoc.notifyUids).toEqual(["phone-alert-user-1"]);

    // Removing again is safe and idempotent
    const removeAgainResult = await setAdmin({
      uid: testUid,
      project: "demo-hitchy",
      remove: true,
      silent: true,
    });
    expect(removeAgainResult.admin).toBe(false);
  });

  it("creates config/admins doc with empty notifyUids if doc is missing when adding admin", async () => {
    const app = getApps().find((a) => a.name === "test-setadmin");
    if (!app) {
      throw new Error("Admin app not found");
    }
    const db = getFirestore(app);

    // Delete config/admins doc
    await db.doc("config/admins").delete();

    const testUid = "test-setadmin-user-1";
    await setAdmin({
      uid: testUid,
      project: "demo-hitchy",
      silent: true,
    });

    const adminDoc = (await db.doc("config/admins").get()).data() as AdminsDoc;
    expect(adminDoc.uids).toEqual([testUid]);
    expect(adminDoc.notifyUids).toEqual([]);
  });
});
