import { getApps, initializeApp } from "firebase-admin/app";
import { getAuth } from "firebase-admin/auth";
import { getStorage } from "firebase-admin/storage";
import { getFirestore } from "firebase-admin/firestore";
import { beforeAll, describe, expect, it } from "vitest";
import type {
  UserDoc,
  VehicleDoc,
  WalletDoc,
  WalletEntryDoc,
  ZoneDoc,
} from "../../src/schemas/docs.js";
import {
  DEV_PROJECT_ID,
  hasPlaceholderPoints,
  releasePhoneNumber,
  runSeed,
  validateProjectTarget,
  type ZoneWithPoints,
} from "../../src/seed/seed.js";

const PROJECT_ID = "demo-hitchy";

beforeAll(() => {
  process.env["FIRESTORE_EMULATOR_HOST"] = "127.0.0.1:8080";
  process.env["FIREBASE_AUTH_EMULATOR_HOST"] = "127.0.0.1:9099";
  if (!getApps().some((a) => a.name === "test-admin")) {
    initializeApp({ projectId: PROJECT_ID }, "test-admin");
  }
});

describe("validateProjectTarget", () => {
  it("defaults to demo-hitchy emulator when project is omitted or demo-hitchy", () => {
    expect(validateProjectTarget(undefined)).toEqual({
      isDev: false,
      targetProject: "demo-hitchy",
    });
    expect(validateProjectTarget("demo-hitchy")).toEqual({
      isDev: false,
      targetProject: "demo-hitchy",
    });
  });

  it("refuses unknown and production projects", () => {
    expect(() => validateProjectTarget("prod")).toThrow(
      /Refusing to seed unknown or production project/,
    );
    expect(() => validateProjectTarget("hitchy-prod")).toThrow(
      /Refusing to seed unknown or production project/,
    );
    expect(() => validateProjectTarget("staging")).toThrow(
      /Refusing to seed unknown or production project/,
    );
    // The old name never existed as a Firebase project: only the real dev project ID is accepted.
    expect(() => validateProjectTarget("hitchy-dev")).toThrow(
      /Refusing to seed unknown or production project/,
    );
  });

  it("knows the real dev project and refuses it while real values are TODO-HUMAN", () => {
    expect(DEV_PROJECT_ID).toBe("hitchy-bb4f9");
    const savedFirestore = process.env["FIRESTORE_EMULATOR_HOST"];
    const savedAuth = process.env["FIREBASE_AUTH_EMULATOR_HOST"];
    delete process.env["FIRESTORE_EMULATOR_HOST"];
    delete process.env["FIREBASE_AUTH_EMULATOR_HOST"];
    try {
      expect(() => validateProjectTarget("hitchy-bb4f9")).toThrow(
        /Refusing to seed dev project while real values/,
      );
    } finally {
      if (savedFirestore !== undefined) process.env["FIRESTORE_EMULATOR_HOST"] = savedFirestore;
      if (savedAuth !== undefined) process.env["FIREBASE_AUTH_EMULATOR_HOST"] = savedAuth;
    }
  });

  it("flags only active pickup points that are still test placeholders", () => {
    const zone = (active: boolean, en: string): ZoneWithPoints => ({
      id: "z",
      name: { ar: "ز", en: "Z" },
      active: true,
      order: 1,
      points: [{ id: "p1", name: { ar: "ن", en }, lat: 30, lng: 31, active }],
    });
    expect(hasPlaceholderPoints([zone(true, "Test point 1")])).toBe(true);
    expect(hasPlaceholderPoints([zone(false, "Test point 1")])).toBe(false);
    expect(hasPlaceholderPoints([zone(true, "Point 90 Mall")])).toBe(false);
  });

  it("refuses dev project when emulator environment variables are set", () => {
    process.env["FIRESTORE_EMULATOR_HOST"] = "127.0.0.1:8080";
    expect(() => validateProjectTarget("dev")).toThrow(
      /Refusing to seed dev project with emulator environment variables set/,
    );
  });

  it("refuses dev project when real data has TODO-HUMAN values", () => {
    const savedFirestore = process.env["FIRESTORE_EMULATOR_HOST"];
    const savedAuth = process.env["FIREBASE_AUTH_EMULATOR_HOST"];
    delete process.env["FIRESTORE_EMULATOR_HOST"];
    delete process.env["FIREBASE_AUTH_EMULATOR_HOST"];

    try {
      expect(() => validateProjectTarget("dev")).toThrow(
        /Refusing to seed dev project while real values/,
      );
    } finally {
      process.env["FIRESTORE_EMULATOR_HOST"] = savedFirestore;
      process.env["FIREBASE_AUTH_EMULATOR_HOST"] = savedAuth;
    }
  });
});

describe("releasePhoneNumber", () => {
  const PHONE = "+201000000099"; // a made-up number that no seed user has

  async function clean() {
    const auth = getAuth(getApps().find((a) => a.name === "test-admin"));
    for (const uid of ["clash-random-uid", "clash-seed-uid"]) {
      await auth.deleteUser(uid).catch(() => undefined);
    }
    return auth;
  }

  it("removes a user with another uid that holds the number", async () => {
    const auth = await clean();
    await auth.createUser({ uid: "clash-random-uid", phoneNumber: PHONE });
    expect(await releasePhoneNumber(auth, "clash-seed-uid", PHONE)).toBe(true);
    await expect(auth.getUserByPhoneNumber(PHONE)).rejects.toMatchObject({
      code: "auth/user-not-found",
    });
    // The seed can now create the demo user with its fixed uid.
    await auth.createUser({ uid: "clash-seed-uid", phoneNumber: PHONE });
    await clean();
  });

  it("keeps the user that already has the seed uid, and does nothing when the number is free", async () => {
    const auth = await clean();
    expect(await releasePhoneNumber(auth, "clash-seed-uid", PHONE)).toBe(false);
    await auth.createUser({ uid: "clash-seed-uid", phoneNumber: PHONE });
    expect(await releasePhoneNumber(auth, "clash-seed-uid", PHONE)).toBe(false);
    expect((await auth.getUser("clash-seed-uid")).uid).toBe("clash-seed-uid");
    await clean();
  });
});

describe("runSeed on emulator", () => {
  it("seeds all documents, creates auth users with admin claim, and is idempotent", async () => {
    const app = getApps().find((a) => a.name === "test-admin");
    if (!app) {
      throw new Error("Admin app not initialized");
    }
    const db = getFirestore(app);
    const auth = getAuth(app);

    // First run
    const stats1 = await runSeed({ project: "demo-hitchy", silent: true });
    expect(stats1).toEqual({
      configs: 6,
      zones: 4,
      points: 12,
      routes: 2,
      companies: 2,
      users: 12,
      vehicles: 5,
      kyc: 5,
      wallets: 12,
      profiles: 12,
      avatars: 11,
    });

    // Check config documents
    const pricingSnap = await db.doc("config/pricing").get();
    expect(pricingSnap.exists).toBe(true);
    expect(pricingSnap.data()?.["fuelPriceEgpPerLiter"]).toBe(22.25);

    const adminsSnap = await db.doc("config/admins").get();
    expect(adminsSnap.exists).toBe(true);
    expect(adminsSnap.data()?.["uids"]).toContain("admin-test-uid");

    // Check reference data
    const zoneSnap = await db.doc("zones/fifth_settlement").get();
    expect(zoneSnap.exists).toBe(true);
    const zoneData = zoneSnap.data() as ZoneDoc;
    expect(zoneData.name.en).toBe("Fifth Settlement");

    const pointSnap = await db.doc("zones/fifth_settlement/points/p1").get();
    expect(pointSnap.exists).toBe(true);

    const routeSnap = await db.doc("routes/fifth_settlement__smart_village").get();
    expect(routeSnap.exists).toBe(true);
    expect(routeSnap.data()?.["distanceKm"]).toBe(55);
    expect(routeSnap.data()?.["usualMinutes"]).toBe(65);

    const companySnap = await db.doc("companies/test_company_a").get();
    expect(companySnap.exists).toBe(true);

    // Check users and auth claims
    const userSnap = await db.doc("users/host-test-uid").get();
    expect(userSnap.exists).toBe(true);
    const userData = userSnap.data() as UserDoc;
    expect(userData.host.status).toBe("approved");
    expect(userData.host.verified).toBe(true);

    // The demo owner has a name and an avatar file in Storage that photoPath points at.
    expect(userData.firstName).toBe("Ahmed");
    expect(userData.photoPath).toBe("avatars/host-test-uid.jpg");
    const [avatarExists] = await getStorage(app)
      .bucket("demo-hitchy.appspot.com")
      .file("avatars/host-test-uid.jpg")
      .exists();
    expect(avatarExists).toBe(true);
    const adminProfile = await db.doc("profiles/admin-test-uid").get();
    expect((adminProfile.data() as { photoPath: string | null }).photoPath).toBeNull();

    const vehicleSnap = await db.doc("users/host-test-uid/vehicles/vehicle-host-1").get();
    expect(vehicleSnap.exists).toBe(true);
    const vehicleData = vehicleSnap.data() as VehicleDoc;
    expect(vehicleData.status).toBe("approved");

    const kycSnap = await db.doc("kyc/host-test-uid").get();
    expect(kycSnap.exists).toBe(true);
    expect(kycSnap.data()?.["status"]).toBe("approved");

    const adminAuthUser = await auth.getUser("admin-test-uid");
    expect(adminAuthUser.customClaims?.["admin"]).toBe(true);

    // Check wallets (v1.2 [DECISION-32]): riders start with 250 EGP, owners with 0.
    const riderWalletSnap = await db.doc("wallets/rider-test-uid").get();
    expect(riderWalletSnap.exists).toBe(true);
    expect((riderWalletSnap.data() as WalletDoc).balance).toBe(250);

    const hostWalletSnap = await db.doc("wallets/host-test-uid").get();
    expect(hostWalletSnap.exists).toBe(true);
    expect((hostWalletSnap.data() as WalletDoc).balance).toBe(0);

    // Every balance equals the sum of its entries.
    for (const uid of ["rider-test-uid", "host-test-uid"]) {
      const walletSnap = await db.doc(`wallets/${uid}`).get();
      const entriesSnap = await db.collection(`wallets/${uid}/entries`).get();
      const sum = entriesSnap.docs.reduce((acc, d) => acc + (d.data() as WalletEntryDoc).delta, 0);
      expect(sum).toBe((walletSnap.data() as WalletDoc).balance);
    }

    const seedEntrySnap = await db.doc("wallets/rider-test-uid/entries/seed").get();
    expect(seedEntrySnap.exists).toBe(true);
    const seedEntry = seedEntrySnap.data() as WalletEntryDoc;
    expect(seedEntry.reason).toBe("seed");
    expect(seedEntry.delta).toBe(250);
    expect(seedEntry.balanceAfter).toBe(250);
    expect(seedEntry.refId).toBeNull();

    // Second run (idempotency verification: must succeed and produce identical stats)
    const stats2 = await runSeed({ project: "demo-hitchy", silent: true });
    expect(stats2).toEqual(stats1);

    // The fixed entry id means re-seeding does not add a second seed entry.
    const entriesAfterSnap = await db.collection("wallets/rider-test-uid/entries").get();
    expect(entriesAfterSnap.size).toBe(1);
  });
});
