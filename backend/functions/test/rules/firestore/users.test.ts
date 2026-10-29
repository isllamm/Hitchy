// Firestore rules (BUILD_PLAN 9.1): users, their subcollections, kyc and profiles.
import {
  assertFails,
  assertSucceeds,
  type RulesTestEnvironment,
} from "@firebase/rules-unit-testing";
import {
  collection,
  deleteDoc,
  doc,
  getDoc,
  getDocs,
  serverTimestamp,
  setDoc,
  updateDoc,
  type DocumentData,
} from "firebase/firestore";
import { afterAll, beforeAll, beforeEach, describe, it } from "vitest";
import {
  anon,
  asAdmin,
  asUser,
  firestoreRulesEnv,
  RIDER,
  seed,
  someTime,
  STRANGER,
  userDoc,
} from "../../helpers/rules.js";

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
    [`users/${RIDER}`]: userDoc(),
    [`users/${STRANGER}`]: userDoc(),
    "companies/c1": { name: { ar: "شركة", en: "Company" }, active: true },
  });
});

const me = () => doc(asUser(env, RIDER), `users/${RIDER}`);
const place = (label = "home", zoneId = "z1", pointId = "p1") => ({ label, zoneId, pointId });
const updateMe = (fields: DocumentData) =>
  updateDoc(me(), { ...fields, updatedAt: serverTimestamp() });

describe("users/{uid}", () => {
  it("the owner and an admin can get it", async () => {
    await assertSucceeds(getDoc(me()));
    await assertSucceeds(getDoc(doc(asAdmin(env), `users/${RIDER}`)));
  });

  it("another user or a signed-out client cannot get it", async () => {
    await assertFails(getDoc(doc(asUser(env, STRANGER), `users/${RIDER}`)));
    await assertFails(getDoc(doc(anon(env), `users/${RIDER}`)));
  });

  it("only an admin can list users", async () => {
    await assertSucceeds(getDocs(collection(asAdmin(env), "users")));
    await assertFails(getDocs(collection(asUser(env, RIDER), "users")));
  });

  it("the owner can update the basic profile fields with updatedAt = server time", async () => {
    await assertSucceeds(
      updateMe({
        firstName: "New",
        lastName: "Name",
        companyId: "c1",
        companyOther: null,
        workHours: { start: "08:00", end: "17:30" },
        language: "ar",
        photoPath: `avatars/${RIDER}.jpg`,
        emergencyContact: { name: "Contact", phone: "test-phone" },
      }),
    );
  });

  it("refuses an update without updatedAt = server time", async () => {
    await assertFails(updateDoc(me(), { firstName: "New" }));
    await assertFails(updateDoc(me(), { firstName: "New", updatedAt: someTime }));
  });

  it("the owner cannot write host, credits, points, activeRole or gender", async () => {
    await assertFails(updateMe({ "host.status": "approved" }));
    await assertFails(updateMe({ "host.credits.regular": 99 }));
    await assertFails(updateMe({ "points.balance": 1000 }));
    await assertFails(updateMe({ activeRole: "host" }));
    await assertFails(updateMe({ gender: "male" }));
    await assertFails(updateMe({ suspended: true, firstName: "X" }));
    await assertFails(updateMe({ "rider.unpaidFees": 5, firstName: "X" }));
  });

  it("another user and an admin client cannot update it", async () => {
    const fields = { firstName: "New", updatedAt: serverTimestamp() };
    await assertFails(updateDoc(doc(asUser(env, STRANGER), `users/${RIDER}`), fields));
    await assertFails(updateDoc(doc(asAdmin(env), `users/${RIDER}`), fields));
  });

  it("nobody can create or delete it from a client", async () => {
    await assertFails(setDoc(doc(asUser(env, "newUser"), "users/newUser"), userDoc()));
    await assertFails(deleteDoc(me()));
  });

  it("checks names, language and company", async () => {
    await assertFails(updateMe({ firstName: "" }));
    await assertFails(updateMe({ lastName: "x".repeat(41) }));
    await assertSucceeds(updateMe({ firstName: "x".repeat(40) }));
    await assertFails(updateMe({ language: "fr" }));
    await assertFails(updateMe({ companyId: "missingCompany" }));
    await assertFails(updateMe({ companyOther: "x".repeat(61) }));
    await assertSucceeds(updateMe({ companyOther: "x".repeat(60) }));
  });

  it("allows only the owner's own avatar path", async () => {
    await assertFails(updateMe({ photoPath: `avatars/${STRANGER}.jpg` }));
    await assertFails(updateMe({ photoPath: "kyc/anything.jpg" }));
  });

  it("checks workHours are HH:mm", async () => {
    await assertFails(updateMe({ workHours: { start: "7:30", end: "17:00" } }));
    await assertFails(updateMe({ workHours: { start: "08:00", end: "24:00" } }));
    await assertFails(updateMe({ workHours: { start: "08:00", end: 1700 } }));
    await assertSucceeds(updateMe({ workHours: { start: "00:00", end: "23:59" } }));
    await assertSucceeds(updateMe({ workHours: null }));
  });

  it("checks the emergency contact types", async () => {
    await assertFails(updateMe({ emergencyContact: { name: "Contact" } }));
    await assertFails(updateMe({ emergencyContact: { name: "Contact", phone: 123 } }));
  });

  describe("savedPlaces", () => {
    it("the owner can save up to 5 places", async () => {
      await assertSucceeds(
        updateMe({
          savedPlaces: [
            place("home"),
            place("work"),
            place("other"),
            place("other", "z2"),
            place("other", "z2", "p2"),
          ],
        }),
      );
    });

    it("refuses a 6th place", async () => {
      await assertFails(updateMe({ savedPlaces: Array.from({ length: 6 }, () => place("other")) }));
    });

    it("checks every place, including the 5th", async () => {
      const four = [place(), place(), place(), place()];
      await assertFails(updateMe({ savedPlaces: [...four, place("school")] }));
      await assertFails(updateMe({ savedPlaces: [...four, place("home", "")] }));
      await assertFails(updateMe({ savedPlaces: [...four, place("home", "z1", "")] }));
      await assertFails(updateMe({ savedPlaces: [...four, { ...place(), lat: 30 }] }));
      await assertFails(updateMe({ savedPlaces: [...four, { label: "home", zoneId: "z1" }] }));
      await assertFails(updateMe({ savedPlaces: [...four, "home"] }));
      await assertFails(updateMe({ savedPlaces: [place("home", "x".repeat(129))] }));
    });

    it("must be a list", async () => {
      await assertFails(updateMe({ savedPlaces: null }));
      await assertFails(updateMe({ savedPlaces: { 0: place() } }));
    });
  });
});

describe("users/{uid}/devices/{id}", () => {
  const device = () => ({
    fcmToken: "test-token",
    platform: "android",
    appVersion: "0.1.0",
    language: "en",
    updatedAt: serverTimestamp(),
  });
  const myDevice = () => doc(asUser(env, RIDER), `users/${RIDER}/devices/d1`);

  it("the owner can create, update, read and delete a device", async () => {
    await assertSucceeds(setDoc(myDevice(), device()));
    await assertSucceeds(setDoc(myDevice(), { ...device(), language: "ar" }));
    await assertSucceeds(getDoc(myDevice()));
    await assertSucceeds(getDocs(collection(asUser(env, RIDER), `users/${RIDER}/devices`)));
    await assertSucceeds(deleteDoc(myDevice()));
  });

  it("refuses invalid device fields", async () => {
    await assertFails(setDoc(myDevice(), { ...device(), extra: true }));
    await assertFails(setDoc(myDevice(), { ...device(), platform: "symbian" }));
    await assertFails(setDoc(myDevice(), { ...device(), language: "fr" }));
    await assertFails(setDoc(myDevice(), { ...device(), fcmToken: "x".repeat(4096) }));
    await assertFails(setDoc(myDevice(), { ...device(), updatedAt: someTime }));
    const missing = device() as Record<string, unknown>;
    delete missing.appVersion;
    await assertFails(setDoc(myDevice(), missing));
  });

  it("another user or an admin cannot read, write or delete it", async () => {
    await seed(env, { [`users/${RIDER}/devices/d1`]: { ...device(), updatedAt: someTime } });
    const theirs = doc(asUser(env, STRANGER), `users/${RIDER}/devices/d1`);
    await assertFails(getDoc(theirs));
    await assertFails(setDoc(theirs, device()));
    await assertFails(deleteDoc(theirs));
    await assertFails(getDoc(doc(asAdmin(env), `users/${RIDER}/devices/d1`)));
  });
});

describe("users/{uid}/notifications/{id} (inbox)", () => {
  const item = {
    type: "booking_accepted",
    title: { ar: "عنوان", en: "Title" },
    body: { ar: "نص", en: "Body" },
    deepLink: "hitchy://rides/r1",
    createdAt: someTime,
    readAt: null,
    expireAt: someTime,
  };
  const path = `users/${RIDER}/notifications/n1`;

  beforeEach(async () => {
    await seed(env, { [path]: item });
  });

  it("the owner can read and list their inbox", async () => {
    await assertSucceeds(getDoc(doc(asUser(env, RIDER), path)));
    await assertSucceeds(getDocs(collection(asUser(env, RIDER), `users/${RIDER}/notifications`)));
  });

  it("another user or an admin cannot read it", async () => {
    await assertFails(getDoc(doc(asUser(env, STRANGER), path)));
    await assertFails(getDoc(doc(asAdmin(env), path)));
  });

  it("the owner can set only readAt, to the server time", async () => {
    await assertSucceeds(updateDoc(doc(asUser(env, RIDER), path), { readAt: serverTimestamp() }));
    await assertFails(updateDoc(doc(asUser(env, RIDER), path), { readAt: someTime }));
    await assertFails(
      updateDoc(doc(asUser(env, RIDER), path), {
        readAt: serverTimestamp(),
        title: { ar: "x", en: "x" },
      }),
    );
  });

  it("nobody else can mark it, and nobody can create or delete it", async () => {
    await assertFails(updateDoc(doc(asUser(env, STRANGER), path), { readAt: serverTimestamp() }));
    await assertFails(setDoc(doc(asUser(env, RIDER), `users/${RIDER}/notifications/n2`), item));
    await assertFails(deleteDoc(doc(asUser(env, RIDER), path)));
  });
});

describe.each(["vehicles", "ledger", "points"])("users/{uid}/%s/{id}", (sub) => {
  const path = `users/${RIDER}/${sub}/x1`;

  beforeEach(async () => {
    await seed(env, { [path]: { createdAt: someTime } });
  });

  it("the owner and an admin can get and list it", async () => {
    await assertSucceeds(getDoc(doc(asUser(env, RIDER), path)));
    await assertSucceeds(getDocs(collection(asUser(env, RIDER), `users/${RIDER}/${sub}`)));
    await assertSucceeds(getDoc(doc(asAdmin(env), path)));
    await assertSucceeds(getDocs(collection(asAdmin(env), `users/${RIDER}/${sub}`)));
  });

  it("another user cannot read it and the owner cannot write it", async () => {
    await assertFails(getDoc(doc(asUser(env, STRANGER), path)));
    await assertFails(setDoc(doc(asUser(env, RIDER), path), { createdAt: someTime }));
    await assertFails(deleteDoc(doc(asUser(env, RIDER), path)));
  });
});

describe("other subcollections of users/{uid}", () => {
  it("are denied, even to the owner", async () => {
    await seed(env, { [`users/${RIDER}/secrets/x1`]: { a: 1 } });
    await assertFails(getDoc(doc(asUser(env, RIDER), `users/${RIDER}/secrets/x1`)));
  });
});

describe("kyc/{uid}", () => {
  beforeEach(async () => {
    await seed(env, { [`kyc/${RIDER}`]: { status: "submitted" } });
  });

  it("the owner and an admin can get it; only an admin can list", async () => {
    await assertSucceeds(getDoc(doc(asUser(env, RIDER), `kyc/${RIDER}`)));
    await assertSucceeds(getDoc(doc(asAdmin(env), `kyc/${RIDER}`)));
    await assertSucceeds(getDocs(collection(asAdmin(env), "kyc")));
    await assertFails(getDocs(collection(asUser(env, RIDER), "kyc")));
  });

  it("another user cannot read it and the owner cannot write it", async () => {
    await assertFails(getDoc(doc(asUser(env, STRANGER), `kyc/${RIDER}`)));
    await assertFails(updateDoc(doc(asUser(env, RIDER), `kyc/${RIDER}`), { status: "approved" }));
  });
});

describe("profiles/{uid}", () => {
  beforeEach(async () => {
    await seed(env, { [`profiles/${RIDER}`]: { firstName: "Test" } });
  });

  it("any signed-in user can get and list profiles", async () => {
    await assertSucceeds(getDoc(doc(asUser(env, STRANGER), `profiles/${RIDER}`)));
    await assertSucceeds(getDocs(collection(asUser(env, STRANGER), "profiles")));
  });

  it("a signed-out client cannot read and nobody can write", async () => {
    await assertFails(getDoc(doc(anon(env), `profiles/${RIDER}`)));
    await assertFails(
      setDoc(doc(asUser(env, RIDER), `profiles/${RIDER}`), { firstName: "Changed" }),
    );
  });
});
