// P4-B01: completeProfile (BUILD_PLAN 8.3).
import { Timestamp } from "firebase-admin/firestore";
import { db } from "../../lib/app.js";
import { makeCallable } from "../../lib/callable.js";
import { createUniqueCode } from "../../lib/codes.js";
import { fail } from "../../lib/errors.js";
import type { CallCtx } from "../../lib/ctx.js";
import type { CodeDoc, CompanyDoc, KycDoc, UserDoc } from "../../schemas/docs.js";
import { CompleteProfileInput } from "../../schemas/inputs.js";
import type { CompleteProfileOutput } from "../../schemas/io.js";

export async function completeProfileCore(
  input: CompleteProfileInput,
  ctx: CallCtx,
): Promise<CompleteProfileOutput> {
  const phone = ctx.phone;
  if (!phone) {
    fail("PHONE_REQUIRED");
  }

  return await db.runTransaction(async (tx) => {
    // 1. companyId given → companies/{id} exists and is active → else INVALID_INPUT
    if (input.companyId !== null) {
      const compSnap = await tx.get(db.collection("companies").doc(input.companyId));
      if (!compSnap.exists) {
        fail("INVALID_INPUT");
      }
      const compData = compSnap.data() as CompanyDoc;
      if (!compData.active) {
        fail("INVALID_INPUT");
      }
    }

    const userRef = db.collection("users").doc(ctx.uid);
    const userSnap = await tx.get(userRef);

    // 2. users/{uid} exists → if gender differs: GENDER_LOCKED; otherwise update owner-writable fields
    if (userSnap.exists) {
      const existingUser = userSnap.data() as UserDoc;
      if (existingUser.gender !== input.gender) {
        fail("GENDER_LOCKED");
      }

      tx.update(userRef, {
        firstName: input.firstName,
        lastName: input.lastName,
        companyId: input.companyId,
        companyOther: input.companyOther,
        workHours: input.workHours,
        language: input.language,
        updatedAt: Timestamp.fromDate(ctx.now),
      });

      return {
        activeRole: existingUser.activeRole,
        hostStatus: existingUser.host.status,
        referralApplied: false,
      };
    }

    // 3. New user → referral lookup
    let referredBy: string | null = null;
    let referralApplied = false;

    if (input.referralCode !== null) {
      // Lookup referral code in codes collection
      const codeRef = db.collection("codes").doc(input.referralCode);
      const codeSnap = await tx.get(codeRef);
      if (codeSnap.exists) {
        const codeDoc = codeSnap.data() as CodeDoc;
        if (codeDoc.type === "referral" && codeDoc.ownerId !== ctx.uid) {
          referredBy = codeDoc.ownerId;
          referralApplied = true;
        }
      } else {
        // Fallback: check users collection for matching referralCode (e.g. seed accounts)
        const userQuery = db
          .collection("users")
          .where("referralCode", "==", input.referralCode)
          .limit(1);
        const querySnap = await tx.get(userQuery);
        if (!querySnap.empty && querySnap.docs[0]) {
          const inviter = querySnap.docs[0];
          if (inviter.id !== ctx.uid) {
            referredBy = inviter.id;
            referralApplied = true;
          }
        }
      }
    }

    // Reserves a unique code by creating codes/{code} inside the transaction (reads then writes)
    const newReferralCode = await createUniqueCode(tx, "referral", ctx.uid, ctx.uid);

    const nowTimestamp = Timestamp.fromDate(ctx.now);

    const newUserDoc: UserDoc = {
      schemaVersion: 1,
      createdAt: nowTimestamp,
      updatedAt: nowTimestamp,
      phone,
      firstName: input.firstName,
      lastName: input.lastName,
      gender: input.gender,
      companyId: input.companyId,
      companyOther: input.companyOther,
      workHours: input.workHours,
      language: input.language,
      photoPath: null,
      emergencyContact: null,
      savedPlaces: [],
      activeRole: "rider",
      referralCode: newReferralCode,
      referredBy,
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
    };

    tx.create(userRef, newUserDoc);

    const kycRef = db.collection("kyc").doc(ctx.uid);
    const kycDoc: KycDoc = {
      schemaVersion: 1,
      createdAt: nowTimestamp,
      updatedAt: nowTimestamp,
      status: "none",
      vehicleId: null,
      docs: {
        idFront: {
          path: `kyc/${ctx.uid}/id_front.jpg`,
          status: "missing",
          rejectReason: null,
        },
        idBack: {
          path: `kyc/${ctx.uid}/id_back.jpg`,
          status: "missing",
          rejectReason: null,
        },
        drivingLicense: {
          path: `kyc/${ctx.uid}/driving_license.jpg`,
          status: "missing",
          rejectReason: null,
        },
        vehicleLicense: {
          path: `kyc/${ctx.uid}/vehicle_license.jpg`,
          status: "missing",
          rejectReason: null,
        },
        criminalRecord: {
          path: `kyc/${ctx.uid}/criminal_record.jpg`,
          status: "missing",
          rejectReason: null,
        },
      },
      submittedAt: null,
      reviewedAt: null,
      reviewerUid: null,
      note: null,
    };

    tx.create(kycRef, kycDoc);

    return {
      activeRole: "rider",
      hostStatus: "none",
      referralApplied,
    };
  });
}

export const completeProfile = makeCallable(
  "completeProfile",
  CompleteProfileInput,
  completeProfileCore,
  { profile: false },
);
