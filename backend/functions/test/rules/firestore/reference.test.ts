// Firestore rules (BUILD_PLAN 9.1): companies, zones, points, routes, config.
import {
  assertFails,
  assertSucceeds,
  type RulesTestEnvironment,
} from "@firebase/rules-unit-testing";
import { collection, deleteDoc, doc, getDoc, getDocs, setDoc, updateDoc } from "firebase/firestore";
import { afterAll, beforeAll, beforeEach, describe, it } from "vitest";
import { anon, asAdmin, asUser, firestoreRulesEnv, RIDER, seed } from "../../helpers/rules.js";

let env: RulesTestEnvironment;

beforeAll(async () => {
  env = await firestoreRulesEnv();
});

afterAll(async () => {
  await env.cleanup();
});

beforeEach(async () => {
  await env.clearFirestore();
  await seed(env, {
    "companies/c1": { name: { ar: "شركة", en: "Company" }, active: true },
    "zones/z1": { name: { ar: "منطقة", en: "Zone" } },
    "zones/z1/points/p1": { name: { ar: "نقطة", en: "Point" }, lat: 30.0, lng: 31.0 },
    "routes/z1__z2": { distanceKm: 25, active: true },
    "config/pricing": { version: 1, fuelPriceEgpPerLiter: 22.25 },
    "config/packages": { version: 1 },
    "config/limits": { version: 1 },
    "config/rewards": { version: 1 },
    "config/impact": { version: 1 },
    "config/admins": { uids: ["admin1"], notifyUids: ["admin1"] },
  });
});

describe("companies, zones, points, routes", () => {
  it("signed-in users can read companies", async () => {
    const db = asUser(env, RIDER);
    await assertSucceeds(getDoc(doc(db, "companies/c1")));
    await assertSucceeds(getDocs(collection(db, "companies")));
  });

  it("signed-in users can read zones and their points", async () => {
    const db = asUser(env, RIDER);
    await assertSucceeds(getDoc(doc(db, "zones/z1")));
    await assertSucceeds(getDocs(collection(db, "zones")));
    await assertSucceeds(getDoc(doc(db, "zones/z1/points/p1")));
    await assertSucceeds(getDocs(collection(db, "zones/z1/points")));
  });

  it("signed-in users can read routes", async () => {
    const db = asUser(env, RIDER);
    await assertSucceeds(getDoc(doc(db, "routes/z1__z2")));
    await assertSucceeds(getDocs(collection(db, "routes")));
  });

  it("signed-out clients cannot read reference data", async () => {
    const db = anon(env);
    await assertFails(getDoc(doc(db, "companies/c1")));
    await assertFails(getDoc(doc(db, "zones/z1")));
    await assertFails(getDoc(doc(db, "zones/z1/points/p1")));
    await assertFails(getDoc(doc(db, "routes/z1__z2")));
  });

  it("nobody can write reference data from a client", async () => {
    const db = asAdmin(env);
    await assertFails(setDoc(doc(db, "companies/c2"), { name: "test" }));
    await assertFails(updateDoc(doc(db, "zones/z1"), { name: "test" }));
    await assertFails(deleteDoc(doc(db, "routes/z1__z2")));
  });
});

describe("config/{name}", () => {
  it("signed-in users can get business config docs", async () => {
    const db = asUser(env, RIDER);
    await assertSucceeds(getDoc(doc(db, "config/pricing")));
    await assertSucceeds(getDoc(doc(db, "config/packages")));
    await assertSucceeds(getDoc(doc(db, "config/limits")));
    await assertSucceeds(getDoc(doc(db, "config/rewards")));
    await assertSucceeds(getDoc(doc(db, "config/impact")));
  });

  it("nobody can get config/admins, not even an admin client", async () => {
    await assertFails(getDoc(doc(asUser(env, RIDER), "config/admins")));
    await assertFails(getDoc(doc(asAdmin(env), "config/admins")));
    await assertFails(getDoc(doc(anon(env), "config/admins")));
  });

  it("signed-out clients cannot get config", async () => {
    await assertFails(getDoc(doc(anon(env), "config/pricing")));
  });

  it("nobody can list config", async () => {
    await assertFails(getDocs(collection(asUser(env, RIDER), "config")));
    await assertFails(getDocs(collection(asAdmin(env), "config")));
  });

  it("nobody can write config from a client", async () => {
    await assertFails(setDoc(doc(asAdmin(env), "config/pricing"), { version: 2 }));
    await assertFails(updateDoc(doc(asUser(env, RIDER), "config/limits"), { version: 2 }));
  });
});
