// P4-B01: completeProfile against the emulators (BUILD_PLAN 8.3).
import { Timestamp } from "firebase-admin/firestore";
import { afterAll, afterEach, beforeAll, describe, expect, it } from "vitest";
import { completeProfileCore } from "../../src/handlers/profile/completeProfile.js";
import { db } from "../../src/lib/app.js";
import { resetClock, setClock } from "../../src/lib/clock.js";
import type { CallCtx } from "../../src/lib/ctx.js";
import type { CodeDoc, CompanyDoc, KycDoc, UserDoc } from "../../src/schemas/docs.js";
import type { CompleteProfileInput } from "../../src/schemas/inputs.js";
import { FIXED_NOW } from "../helpers/docs.js";

const TEST_PHONE = "+201000000099";
const INVITER_PHONE = "+201000000098";
const UID_NEW = "p4b01-user-new";
const UID_INVITER = "p4b01-user-inviter";
const UID_SEEDED = "p4b01-user-seeded";
const COMPANY_ACTIVE = "p4b01-company-active";
const COMPANY_INACTIVE = "p4b01-company-inactive";
const INVITER_CODE = "INVIT1";
const SEEDED_CODE = "SEEDED";

function ctxFor(uid: string, phone: string | null = TEST_PHONE): CallCtx {
  return { uid, phone, isAdmin: false, user: null, now: FIXED_NOW };
}

function baseInput(overrides: Partial<CompleteProfileInput> = {}): CompleteProfileInput {
  return {
    firstName: "Amr",
    lastName: "Diab",
    gender: "male",
    companyId: null,
    companyOther: null,
    workHours: { start: "08:30", end: "17:00" },
    language: "ar",
    referralCode: null,
    ...overrides,
  };
}

describe("completeProfile", () => {
  beforeAll(async () => {
    setClock(() => FIXED_NOW);

    // Seed companies
    const activeComp: CompanyDoc = {
      name: { ar: "شركة فودافون", en: "Vodafone" },
      zoneId: "smart-village",
      active: true,
    };
    await db.collection("companies").doc(COMPANY_ACTIVE).set(activeComp);

    const inactiveComp: CompanyDoc = {
      name: { ar: "شركة مغلقة", en: "Closed Co" },
      zoneId: "smart-village",
      active: false,
    };
    await db.collection("companies").doc(COMPANY_INACTIVE).set(inactiveComp);

    // Seed inviter code
    const codeDoc: CodeDoc = {
      type: "referral",
      ownerId: UID_INVITER,
      refId: UID_INVITER,
      createdAt: Timestamp.fromDate(FIXED_NOW),
    };
    await db.collection("codes").doc(INVITER_CODE).set(codeDoc);

    // Seed a user with referralCode directly (like seed.ts)
    const seededUserDoc: Partial<UserDoc> = {
      schemaVersion: 1,
      firstName: "Hany",
      lastName: "Shaker",
      phone: INVITER_PHONE,
      gender: "male",
      referralCode: SEEDED_CODE,
      activeRole: "rider",
      suspended: false,
    };
    await db.collection("users").doc(UID_SEEDED).set(seededUserDoc);
  });

  afterAll(async () => {
    resetClock();
    await db.collection("companies").doc(COMPANY_ACTIVE).delete();
    await db.collection("companies").doc(COMPANY_INACTIVE).delete();
    await db.collection("codes").doc(INVITER_CODE).delete();
    await db.collection("users").doc(UID_SEEDED).delete();
  });

  afterEach(async () => {
    // Clean up created user docs
    const snaps = await db.collection("users").get();
    for (const doc of snaps.docs) {
      if (doc.id.startsWith("p4b01-user-") && doc.id !== UID_SEEDED) {
        await doc.ref.delete();
      }
    }
    const kycSnaps = await db.collection("kyc").get();
    for (const doc of kycSnaps.docs) {
      if (doc.id.startsWith("p4b01-user-")) {
        await doc.ref.delete();
      }
    }
  });

  it("fails with PHONE_REQUIRED when phone is missing from context", async () => {
    const ctx = ctxFor("p4b01-user-nophone", null);
    await expect(completeProfileCore(baseInput(), ctx)).rejects.toMatchObject({
      reason: "PHONE_REQUIRED",
    });
  });

  it("creates a new user with every default and initializes kyc doc", async () => {
    const ctx = ctxFor(UID_NEW);
    const out = await completeProfileCore(
      baseInput({
        companyId: COMPANY_ACTIVE,
        companyOther: null,
      }),
      ctx,
    );

    expect(out).toEqual({
      activeRole: "rider",
      hostStatus: "none",
      referralApplied: false,
    });

    // Verify user doc
    const userSnap = await db.collection("users").doc(UID_NEW).get();
    expect(userSnap.exists).toBe(true);
    const u = userSnap.data() as UserDoc;

    expect(u.schemaVersion).toBe(1);
    expect(u.phone).toBe(TEST_PHONE);
    expect(u.firstName).toBe("Amr");
    expect(u.lastName).toBe("Diab");
    expect(u.gender).toBe("male");
    expect(u.companyId).toBe(COMPANY_ACTIVE);
    expect(u.companyOther).toBeNull();
    expect(u.workHours).toEqual({ start: "08:30", end: "17:00" });
    expect(u.language).toBe("ar");
    expect(u.photoPath).toBeNull();
    expect(u.emergencyContact).toBeNull();
    expect(u.savedPlaces).toEqual([]);
    expect(u.activeRole).toBe("rider");
    expect(u.referredBy).toBeNull();
    expect(u.referralRewarded).toBe(false);
    expect(u.blockedUids).toEqual([]);
    expect(u.suspended).toBe(false);
    expect(u.points).toEqual({ balance: 0 });
    expect(u.rider).toEqual({
      tier: "bronze",
      ratingSum: 0,
      ratingCount: 0,
      tripsCompleted: 0,
      unpaidFees: 0,
      lateCancels: 0,
    });
    expect(u.host).toEqual({
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
    });

    // Unique referral code in codes collection
    expect(typeof u.referralCode).toBe("string");
    expect(u.referralCode.length).toBe(6);
    const codeSnap = await db.collection("codes").doc(u.referralCode).get();
    expect(codeSnap.exists).toBe(true);
    const codeDoc = codeSnap.data() as CodeDoc;
    expect(codeDoc.type).toBe("referral");
    expect(codeDoc.ownerId).toBe(UID_NEW);

    // Verify kyc doc
    const kycSnap = await db.collection("kyc").doc(UID_NEW).get();
    expect(kycSnap.exists).toBe(true);
    const kyc = kycSnap.data() as KycDoc;
    expect(kyc.schemaVersion).toBe(1);
    expect(kyc.status).toBe("none");
    expect(kyc.vehicleId).toBeNull();
    expect(kyc.submittedAt).toBeNull();
    expect(kyc.reviewedAt).toBeNull();
    expect(kyc.reviewerUid).toBeNull();
    expect(kyc.note).toBeNull();
    expect(kyc.docs).toEqual({
      idFront: { path: `kyc/${UID_NEW}/id_front.jpg`, status: "missing", rejectReason: null },
      idBack: { path: `kyc/${UID_NEW}/id_back.jpg`, status: "missing", rejectReason: null },
      drivingLicense: {
        path: `kyc/${UID_NEW}/driving_license.jpg`,
        status: "missing",
        rejectReason: null,
      },
      vehicleLicense: {
        path: `kyc/${UID_NEW}/vehicle_license.jpg`,
        status: "missing",
        rejectReason: null,
      },
      criminalRecord: {
        path: `kyc/${UID_NEW}/criminal_record.jpg`,
        status: "missing",
        rejectReason: null,
      },
    });
  });

  it("updates allowed fields only on a second call", async () => {
    const ctx = ctxFor(UID_NEW);
    await completeProfileCore(baseInput(), ctx);

    // Call again with updated names and work hours
    const updatedNow = new Date("2026-11-03T10:00:00.000Z");
    setClock(() => updatedNow);
    const updatedCtx = { ...ctx, now: updatedNow };

    const out = await completeProfileCore(
      baseInput({
        firstName: "Amr Mohamed",
        lastName: "Diab Abdelbasset",
        workHours: { start: "09:00", end: "18:00" },
        language: "en",
        companyOther: "Freelancer",
      }),
      updatedCtx,
    );

    expect(out).toEqual({
      activeRole: "rider",
      hostStatus: "none",
      referralApplied: false,
    });

    const userSnap = await db.collection("users").doc(UID_NEW).get();
    const u = userSnap.data() as UserDoc;
    expect(u.firstName).toBe("Amr Mohamed");
    expect(u.lastName).toBe("Diab Abdelbasset");
    expect(u.workHours).toEqual({ start: "09:00", end: "18:00" });
    expect(u.language).toBe("en");
    expect(u.companyOther).toBe("Freelancer");
    expect(u.gender).toBe("male");
    expect(u.activeRole).toBe("rider");
  });

  it("throws GENDER_LOCKED if changing gender on an existing user", async () => {
    const ctx = ctxFor(UID_NEW);
    await completeProfileCore(baseInput({ gender: "male" }), ctx);

    await expect(completeProfileCore(baseInput({ gender: "female" }), ctx)).rejects.toMatchObject({
      reason: "GENDER_LOCKED",
    });
  });

  it("throws INVALID_INPUT when companyId does not exist or is inactive", async () => {
    const ctx = ctxFor("p4b01-user-comp-missing");
    await expect(
      completeProfileCore(baseInput({ companyId: "nonexistent-co" }), ctx),
    ).rejects.toMatchObject({
      reason: "INVALID_INPUT",
    });

    await expect(
      completeProfileCore(baseInput({ companyId: COMPANY_INACTIVE }), ctx),
    ).rejects.toMatchObject({
      reason: "INVALID_INPUT",
    });
  });

  it("applies a valid referral code from codes collection", async () => {
    const ctx = ctxFor("p4b01-user-referred");
    const out = await completeProfileCore(baseInput({ referralCode: INVITER_CODE }), ctx);

    expect(out.referralApplied).toBe(true);

    const userSnap = await db.collection("users").doc("p4b01-user-referred").get();
    const u = userSnap.data() as UserDoc;
    expect(u.referredBy).toBe(UID_INVITER);
    expect(u.referralRewarded).toBe(false);
  });

  it("applies a valid referral code from seeded users collection", async () => {
    const ctx = ctxFor("p4b01-user-referred-seed");
    const out = await completeProfileCore(baseInput({ referralCode: SEEDED_CODE }), ctx);

    expect(out.referralApplied).toBe(true);

    const userSnap = await db.collection("users").doc("p4b01-user-referred-seed").get();
    const u = userSnap.data() as UserDoc;
    expect(u.referredBy).toBe(UID_SEEDED);
  });

  it("ignores unknown or own referral code without failing (referralApplied: false)", async () => {
    // Unknown code
    const ctx1 = ctxFor("p4b01-user-unknown-ref");
    const out1 = await completeProfileCore(baseInput({ referralCode: "UNKNOWN_XYZ" }), ctx1);
    expect(out1.referralApplied).toBe(false);
    const snap1 = await db.collection("users").doc("p4b01-user-unknown-ref").get();
    expect(snap1.data()?.referredBy).toBeNull();

    // Inviter's own code
    const ctxInviter = ctxFor(UID_INVITER);
    const out2 = await completeProfileCore(baseInput({ referralCode: INVITER_CODE }), ctxInviter);
    expect(out2.referralApplied).toBe(false);
    const snap2 = await db.collection("users").doc(UID_INVITER).get();
    expect(snap2.data()?.referredBy).toBeNull();
  });
});
