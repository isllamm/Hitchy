import { existsSync, readFileSync } from "node:fs";
import { getApps, initializeApp, type App } from "firebase-admin/app";
import { getAuth, type Auth } from "firebase-admin/auth";
import { getFirestore, Timestamp, type Firestore } from "firebase-admin/firestore";
import { getStorage } from "firebase-admin/storage";
import type {
  ImpactConfig,
  LimitsConfig,
  PackagesConfig,
  PricingConfig,
  RewardsConfig,
} from "../schemas/config.js";
import type {
  AdminsDoc,
  CompanyDoc,
  KycDoc,
  PointDoc,
  ProfileDoc,
  RouteDoc,
  UserDoc,
  VehicleDoc,
  WalletDoc,
  WalletEntryDoc,
  ZoneDoc,
} from "../schemas/docs.js";

/** The real Firebase dev project (alias `dev` in backend/.firebaserc). */
export const DEV_PROJECT_ID = "hitchy-bb4f9";

/** A point still named "test" is a placeholder: the real name and coordinates are not known yet. */
export function hasPlaceholderPoints(zones: ZoneWithPoints[]): boolean {
  return zones.some((z) =>
    z.points.some((p) => p.active && p.name.en.toLowerCase().includes("test")),
  );
}

export interface SeedOptions {
  project?: string;
  silent?: boolean;
}

export interface SeedStats {
  configs: number;
  zones: number;
  points: number;
  routes: number;
  companies: number;
  users: number;
  vehicles: number;
  kyc: number;
  wallets: number;
  profiles: number;
  avatars: number;
}

export interface ZoneWithPoints extends ZoneDoc {
  id: string;
  points: (PointDoc & { id: string })[];
}

interface TestUserSeed {
  uid: string;
  email: string;
  phone: string;
  firstName: string;
  lastName: string;
  gender: "male" | "female";
  activeRole: "rider" | "host";
  language: "ar" | "en";
  walletEgp: number; // v1.2 [DECISION-32]: riders start with 250 EGP, owners with 0
  isAdmin?: boolean;
  hostApproved?: boolean;
  vehicle?: {
    id: string;
    type: "car" | "scooter";
    make: string;
    model: string;
    year: number;
    color: string;
    plate: string;
    maxSeats: number;
    status: "pending" | "approved" | "rejected";
  };
}

/** The generated avatar picture of a seed user, or null when the user has none (the admin). */
function readAvatar(uid: string): Buffer | null {
  const fileUrl = new URL(`../../../seed/data/avatars/${uid}.jpg`, import.meta.url);
  return existsSync(fileUrl) ? readFileSync(fileUrl) : null;
}

function readJsonFile(relativePath: string): unknown {
  const fileUrl = new URL(`../../../seed/data/${relativePath}`, import.meta.url);
  return JSON.parse(readFileSync(fileUrl, "utf8")) as unknown;
}

export function validateProjectTarget(project: string | undefined): {
  isDev: boolean;
  targetProject: string;
} {
  const normalized = (project ?? "").trim();

  if (!normalized || normalized === "demo-hitchy") {
    return { isDev: false, targetProject: "demo-hitchy" };
  }

  if (normalized === "dev" || normalized === DEV_PROJECT_ID) {
    const hasFirestoreEmulator = Boolean(process.env["FIRESTORE_EMULATOR_HOST"]);
    const hasAuthEmulator = Boolean(process.env["FIREBASE_AUTH_EMULATOR_HOST"]);

    if (hasFirestoreEmulator || hasAuthEmulator) {
      throw new Error(
        "Refusing to seed dev project with emulator environment variables set (FIRESTORE_EMULATOR_HOST / FIREBASE_AUTH_EMULATOR_HOST).",
      );
    }

    const packages = readJsonFile("config/packages.json") as PackagesConfig;
    if (packages.instapayAddress === "TODO-HUMAN") {
      throw new Error(
        "Refusing to seed dev project while real values (e.g. config/packages.instapayAddress) are TODO-HUMAN.",
      );
    }

    const zones = readJsonFile("zones.json") as ZoneWithPoints[];
    const hasFakePoints = hasPlaceholderPoints(zones);
    if (hasFakePoints) {
      throw new Error(
        "Refusing to seed dev project while active pickup points are test placeholders (TODO-HUMAN coordinates).",
      );
    }

    return { isDev: true, targetProject: DEV_PROJECT_ID };
  }

  throw new Error(
    `Refusing to seed unknown or production project: "${normalized}". Seed script only supports emulator (default) or "dev".`,
  );
}

function getOrInitAdminApp(projectId: string): App {
  const appName = `seed-${projectId}`;
  const existing = getApps().find((a) => a.name === appName);
  if (existing) {
    return existing;
  }
  // Projects made after 2024 use the firebasestorage.app bucket name; the emulator keeps appspot.com.
  const storageBucket =
    projectId === DEV_PROJECT_ID ? `${projectId}.firebasestorage.app` : `${projectId}.appspot.com`;
  return initializeApp({ projectId, storageBucket }, appName);
}

export async function runSeed(opts: SeedOptions = {}): Promise<SeedStats> {
  const { isDev, targetProject } = validateProjectTarget(opts.project);

  if (!isDev) {
    if (!process.env["FIRESTORE_EMULATOR_HOST"]) {
      process.env["FIRESTORE_EMULATOR_HOST"] = "127.0.0.1:8080";
    }
    if (!process.env["FIREBASE_AUTH_EMULATOR_HOST"]) {
      process.env["FIREBASE_AUTH_EMULATOR_HOST"] = "127.0.0.1:9099";
    }
    if (!process.env["FIREBASE_STORAGE_EMULATOR_HOST"]) {
      process.env["FIREBASE_STORAGE_EMULATOR_HOST"] = "127.0.0.1:9199";
    }
  }

  const app = getOrInitAdminApp(targetProject);
  const db: Firestore = getFirestore(app);
  const auth: Auth = getAuth(app);

  const stats: SeedStats = {
    configs: 0,
    zones: 0,
    points: 0,
    routes: 0,
    companies: 0,
    users: 0,
    vehicles: 0,
    kyc: 0,
    wallets: 0,
    profiles: 0,
    avatars: 0,
  };

  const log = (msg: string) => {
    if (!opts.silent) {
      console.log(msg);
    }
  };

  log(`Seeding target project: ${targetProject} (isDev: ${String(isDev)})...`);

  // 1. Config documents
  const pricing = readJsonFile("config/pricing.json") as PricingConfig;
  const packages = readJsonFile("config/packages.json") as PackagesConfig;
  const limits = readJsonFile("config/limits.json") as LimitsConfig;
  const rewards = readJsonFile("config/rewards.json") as RewardsConfig;
  const impact = readJsonFile("config/impact.json") as ImpactConfig;
  const admins = readJsonFile("config/admins.json") as AdminsDoc;

  await db.doc("config/pricing").set(pricing);
  await db.doc("config/packages").set(packages);
  await db.doc("config/limits").set(limits);
  await db.doc("config/rewards").set(rewards);
  await db.doc("config/impact").set(impact);
  await db.doc("config/admins").set(admins);
  stats.configs = 6;
  log("✔ Config documents seeded (6)");

  // 2. Zones & Points
  const zones = readJsonFile("zones.json") as ZoneWithPoints[];
  for (const z of zones) {
    const zoneDoc: ZoneDoc = {
      name: z.name,
      active: z.active,
      order: z.order,
    };
    await db.doc(`zones/${z.id}`).set(zoneDoc);
    stats.zones += 1;

    for (const p of z.points) {
      const pointDoc: PointDoc = {
        name: p.name,
        lat: p.lat,
        lng: p.lng,
        active: p.active,
      };
      await db.doc(`zones/${z.id}/points/${p.id}`).set(pointDoc);
      stats.points += 1;
    }
  }
  log(`✔ Zones seeded (${String(stats.zones)}) and Points (${String(stats.points)})`);

  // 3. Routes
  const routes = readJsonFile("routes.json") as RouteDoc[];
  for (const r of routes) {
    const routeId = `${r.fromZone}__${r.toZone}`;
    const routeDoc: RouteDoc = {
      fromZone: r.fromZone,
      toZone: r.toZone,
      distanceKm: r.distanceKm,
      usualMinutes: r.usualMinutes,
    };
    await db.doc(`routes/${routeId}`).set(routeDoc);
    stats.routes += 1;
  }
  log(`✔ Routes seeded (${String(stats.routes)})`);

  // 4. Companies
  const companies = readJsonFile("companies.json") as (CompanyDoc & { id: string })[];
  for (const c of companies) {
    const companyDoc: CompanyDoc = {
      name: c.name,
      zoneId: c.zoneId,
      active: c.active,
    };
    await db.doc(`companies/${c.id}`).set(companyDoc);
    stats.companies += 1;
  }
  log(`✔ Companies seeded (${String(stats.companies)})`);

  // 5. Demo users. The emulator and the dev project both get them; the admin only on the emulator.
  const users = readJsonFile("users.json") as TestUserSeed[];
  const deterministicTime = Timestamp.fromDate(new Date("2026-10-29T00:00:00Z"));
  const freeUntil = Timestamp.fromDate(new Date("2026-11-28T00:00:00Z"));

  const bucket = getStorage(app).bucket();

  for (const u of users) {
    // An admin claim is never given to a made-up phone number in the real project (use set-admin).
    if (isDev && u.isAdmin) {
      log(`ℹ Dev target: admin user ${u.uid} skipped.`);
      continue;
    }

    // The avatar goes to Storage first, so the user's photoPath only points at a file that exists.
    let photoPath: string | null = null;
    const avatar = readAvatar(u.uid);
    if (avatar) {
      try {
        await bucket.file(`avatars/${u.uid}.jpg`).save(avatar, {
          contentType: "image/jpeg",
          resumable: false,
        });
        photoPath = `avatars/${u.uid}.jpg`;
        stats.avatars += 1;
      } catch (err) {
        log(`⚠ Avatar of ${u.uid} not uploaded (is the Storage emulator running?): ${String(err)}`);
      }
    }

    try {
      await auth.getUser(u.uid);
      await auth.updateUser(u.uid, {
        phoneNumber: u.phone,
        displayName: `${u.firstName} ${u.lastName}`,
        // Made-up addresses such as rider@test are not valid for the real Auth service: phone only there.
        email: isDev ? undefined : u.email,
      });
    } catch {
      await auth.createUser({
        uid: u.uid,
        phoneNumber: u.phone,
        displayName: `${u.firstName} ${u.lastName}`,
        // Made-up addresses such as rider@test are not valid for the real Auth service: phone only there.
        email: isDev ? undefined : u.email,
      });
    }

    if (u.isAdmin) {
      await auth.setCustomUserClaims(u.uid, { admin: true });
    }

    const userDoc: UserDoc = {
      schemaVersion: 1,
      phone: u.phone,
      firstName: u.firstName,
      lastName: u.lastName,
      gender: u.gender,
      companyId: null,
      companyOther: null,
      workHours: null,
      language: u.language,
      photoPath,
      emergencyContact: null,
      savedPlaces: [],
      activeRole: u.activeRole,
      referralCode: `HITCHY_${u.firstName.toUpperCase()}`,
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
        status: u.hostApproved ? "approved" : "none",
        verified: Boolean(u.hostApproved),
        approvedAt: u.hostApproved ? deterministicTime : null,
        freeUntil: u.hostApproved ? freeUntil : null,
        freeEndingNotified: false,
        credits: { regular: u.hostApproved ? 10 : 0, fixed: 0 },
        tier: "bronze",
        ratingSum: 0,
        ratingCount: 0,
        tripsCompleted: 0,
        lateCancels: 0,
        hostLateCount: 0,
      },
      points: { balance: 0 },
      createdAt: deterministicTime,
      updatedAt: deterministicTime,
    };

    await db.doc(`users/${u.uid}`).set(userDoc);
    stats.users += 1;

    // The public card. syncPublicProfile (P4-B03) writes this in production; the seed writes
    // it here so search and ride cards have an owner before that trigger exists (ADR row 87).
    const profileDoc: ProfileDoc = {
      schemaVersion: 1,
      firstName: u.firstName,
      photoPath,
      hostRating: { avg: null, count: 0 },
      hostTrips: 0,
      riderRating: { avg: null, count: 0 },
      verified: Boolean(u.hostApproved),
      hostTier: "bronze",
      riderTier: "bronze",
      updatedAt: deterministicTime,
    };
    await db.doc(`profiles/${u.uid}`).set(profileDoc);
    stats.profiles += 1;

    // Wallet (v1.2, R19) with its one `seed` entry, so every balance equals the sum of its
    // entries. The entry id is fixed, so re-seeding does not add a second one.
    const walletDoc: WalletDoc = {
      balance: u.walletEgp,
      updatedAt: deterministicTime,
    };
    await db.doc(`wallets/${u.uid}`).set(walletDoc);

    const seedEntry: WalletEntryDoc = {
      delta: u.walletEgp,
      reason: "seed",
      refId: null,
      balanceAfter: u.walletEgp,
      by: "seed",
      createdAt: deterministicTime,
    };
    await db.doc(`wallets/${u.uid}/entries/seed`).set(seedEntry);
    stats.wallets += 1;

    if (u.vehicle) {
      const vehicleDoc: VehicleDoc = {
        schemaVersion: 1,
        type: u.vehicle.type,
        make: u.vehicle.make,
        model: u.vehicle.model,
        year: u.vehicle.year,
        color: u.vehicle.color,
        plate: u.vehicle.plate,
        maxSeats: u.vehicle.maxSeats,
        status: u.vehicle.status,
        createdAt: deterministicTime,
        updatedAt: deterministicTime,
      };
      await db.doc(`users/${u.uid}/vehicles/${u.vehicle.id}`).set(vehicleDoc);
      stats.vehicles += 1;

      const kycDoc: KycDoc = {
        schemaVersion: 1,
        status: "approved",
        vehicleId: u.vehicle.id,
        docs: {
          idFront: {
            path: `kyc/${u.uid}/id_front.jpg`,
            status: "accepted",
            rejectReason: null,
          },
          idBack: {
            path: `kyc/${u.uid}/id_back.jpg`,
            status: "accepted",
            rejectReason: null,
          },
          drivingLicense: {
            path: `kyc/${u.uid}/driving_license.jpg`,
            status: "accepted",
            rejectReason: null,
          },
          vehicleLicense: {
            path: `kyc/${u.uid}/vehicle_license.jpg`,
            status: "accepted",
            rejectReason: null,
          },
          criminalRecord: {
            path: `kyc/${u.uid}/criminal_record.jpg`,
            status: "accepted",
            rejectReason: null,
          },
        },
        submittedAt: deterministicTime,
        reviewedAt: deterministicTime,
        reviewerUid: "admin-test-uid",
        note: null,
        createdAt: deterministicTime,
        updatedAt: deterministicTime,
      };
      await db.doc(`kyc/${u.uid}`).set(kycDoc);
      stats.kyc += 1;
    }
  }
  log(
    `✔ Users seeded (${String(stats.users)}), Avatars (${String(stats.avatars)}), Profiles (${String(stats.profiles)}), Vehicles (${String(stats.vehicles)}), KYC (${String(stats.kyc)}), Wallets (${String(stats.wallets)})`,
  );

  log("🎉 Database seeding completed successfully!");
  return stats;
}
