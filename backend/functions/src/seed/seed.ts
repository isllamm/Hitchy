import { readFileSync } from "node:fs";
import { getApps, initializeApp, type App } from "firebase-admin/app";
import { getAuth, type Auth } from "firebase-admin/auth";
import { getFirestore, Timestamp, type Firestore } from "firebase-admin/firestore";
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
  RouteDoc,
  UserDoc,
  VehicleDoc,
  ZoneDoc,
} from "../schemas/docs.js";

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
}

interface ZoneWithPoints extends ZoneDoc {
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

  if (normalized === "dev" || normalized === "hitchy-dev") {
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
    const hasFakePoints = zones.some((z) =>
      z.points.some((p) => p.name.en.toLowerCase().includes("test")),
    );
    if (hasFakePoints) {
      throw new Error(
        "Refusing to seed dev project while pickup points are test placeholders (TODO-HUMAN coordinates).",
      );
    }

    return { isDev: true, targetProject: "hitchy-dev" };
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
  return initializeApp({ projectId }, appName);
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

  // 5. Test Users (Emulator Only!)
  if (!isDev) {
    const users = readJsonFile("users.json") as TestUserSeed[];
    const deterministicTime = Timestamp.fromDate(new Date("2026-10-29T00:00:00Z"));
    const freeUntil = Timestamp.fromDate(new Date("2026-11-28T00:00:00Z"));

    for (const u of users) {
      try {
        await auth.getUser(u.uid);
        await auth.updateUser(u.uid, {
          phoneNumber: u.phone,
          displayName: `${u.firstName} ${u.lastName}`,
          email: u.email,
        });
      } catch {
        await auth.createUser({
          uid: u.uid,
          phoneNumber: u.phone,
          displayName: `${u.firstName} ${u.lastName}`,
          email: u.email,
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
        photoPath: null,
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
      `✔ Users seeded (${String(stats.users)}), Vehicles (${String(stats.vehicles)}), KYC (${String(stats.kyc)})`,
    );
  } else {
    log("ℹ Dev target: Users, Vehicles and KYC are skipped as intended.");
  }

  log("🎉 Database seeding completed successfully!");
  return stats;
}
