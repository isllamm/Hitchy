// Storage rules tests (BUILD_PLAN 9.3).
import {
  assertFails,
  assertSucceeds,
  type RulesTestEnvironment,
} from "@firebase/rules-unit-testing";
import { deleteObject, getBytes, ref, uploadBytes, type FirebaseStorage } from "firebase/storage";
import { afterAll, beforeAll, beforeEach, describe, it } from "vitest";
import {
  HOST,
  RIDER,
  seed,
  someTime,
  storageAnon,
  storageAsAdmin,
  storageAsUser,
  storageRulesEnv,
  STRANGER,
} from "../helpers/rules.js";

let env: RulesTestEnvironment;

beforeAll(async () => {
  env = await storageRulesEnv();
});

afterAll(async () => {
  await env.cleanup();
});

const jpeg1kb = new Uint8Array(1024);
const jpegMeta = { contentType: "image/jpeg" };
const pngMeta = { contentType: "image/png" };

// Helper to write storage files with rules disabled
async function seedStorage(files: Record<string, Uint8Array>): Promise<void> {
  await env.withSecurityRulesDisabled(async (ctx) => {
    const st = ctx.storage() as unknown as FirebaseStorage;
    await Promise.all(
      Object.entries(files).map(([path, data]) => uploadBytes(ref(st, path), data, jpegMeta)),
    );
  });
}

beforeEach(async () => {
  await env.clearFirestore();
  await env.clearStorage();
});

describe("kyc/{uid}/{file}", () => {
  const kycFile = `kyc/${HOST}/id_front.jpg`;
  const criminalRecord = `kyc/${HOST}/criminal_record.jpg`;

  describe("reads", () => {
    beforeEach(async () => {
      await seedStorage({ [kycFile]: jpeg1kb });
    });

    it("an admin can read kyc documents", async () => {
      await assertSucceeds(getBytes(ref(storageAsAdmin(env), kycFile)));
    });

    it("the owner cannot read kyc documents directly from storage", async () => {
      await assertFails(getBytes(ref(storageAsUser(env, HOST), kycFile)));
    });

    it("other users and signed-out clients cannot read kyc documents", async () => {
      await assertFails(getBytes(ref(storageAsUser(env, STRANGER), kycFile)));
      await assertFails(getBytes(ref(storageAnon(env), kycFile)));
    });
  });

  describe("creates and updates across kyc states", () => {
    it("state 1 (no doc in kyc/{uid}): owner can upload all 5 kyc files", async () => {
      const st = storageAsUser(env, HOST);
      await assertSucceeds(uploadBytes(ref(st, `kyc/${HOST}/id_front.jpg`), jpeg1kb, jpegMeta));
      await assertSucceeds(uploadBytes(ref(st, `kyc/${HOST}/id_back.jpg`), jpeg1kb, jpegMeta));
      await assertSucceeds(
        uploadBytes(ref(st, `kyc/${HOST}/driving_license.jpg`), jpeg1kb, jpegMeta),
      );
      await assertSucceeds(
        uploadBytes(ref(st, `kyc/${HOST}/vehicle_license.jpg`), jpeg1kb, jpegMeta),
      );
      await assertSucceeds(
        uploadBytes(ref(st, `kyc/${HOST}/criminal_record.jpg`), jpeg1kb, jpegMeta),
      );
    });

    it("state 2 (kyc status = 'none'): owner can upload all 5 kyc files", async () => {
      await seed(env, { [`kyc/${HOST}`]: { status: "none", updatedAt: someTime } });
      const st = storageAsUser(env, HOST);
      await assertSucceeds(uploadBytes(ref(st, `kyc/${HOST}/id_front.jpg`), jpeg1kb, jpegMeta));
      await assertSucceeds(uploadBytes(ref(st, criminalRecord), jpeg1kb, jpegMeta));
    });

    it("state 3 (kyc status = 'rejected'): owner can upload all 5 kyc files for resubmission", async () => {
      await seed(env, { [`kyc/${HOST}`]: { status: "rejected", updatedAt: someTime } });
      const st = storageAsUser(env, HOST);
      await assertSucceeds(uploadBytes(ref(st, `kyc/${HOST}/id_front.jpg`), jpeg1kb, jpegMeta));
      await assertSucceeds(uploadBytes(ref(st, criminalRecord), jpeg1kb, jpegMeta));
    });

    it("state 4 (kyc status = 'submitted'): owner cannot upload general files, but can upload criminal_record.jpg", async () => {
      await seed(env, { [`kyc/${HOST}`]: { status: "submitted", updatedAt: someTime } });
      const st = storageAsUser(env, HOST);
      // General KYC files are blocked while under review
      await assertFails(uploadBytes(ref(st, `kyc/${HOST}/id_front.jpg`), jpeg1kb, jpegMeta));
      await assertFails(uploadBytes(ref(st, `kyc/${HOST}/driving_license.jpg`), jpeg1kb, jpegMeta));
      // criminal_record.jpg can be uploaded anytime
      await assertSucceeds(uploadBytes(ref(st, criminalRecord), jpeg1kb, jpegMeta));
    });

    it("state 5 (kyc status = 'approved'): owner cannot re-upload general files, but can upload criminal_record.jpg", async () => {
      await seed(env, { [`kyc/${HOST}`]: { status: "approved", updatedAt: someTime } });
      const st = storageAsUser(env, HOST);
      await assertFails(uploadBytes(ref(st, `kyc/${HOST}/id_front.jpg`), jpeg1kb, jpegMeta));
      await assertSucceeds(uploadBytes(ref(st, criminalRecord), jpeg1kb, jpegMeta));
    });
  });

  describe("upload validations", () => {
    it("refuses another user from uploading to host kyc folder", async () => {
      const st = storageAsUser(env, STRANGER);
      await assertFails(uploadBytes(ref(st, kycFile), jpeg1kb, jpegMeta));
    });

    it("refuses file names not in the allowed list", async () => {
      const st = storageAsUser(env, HOST);
      await assertFails(uploadBytes(ref(st, `kyc/${HOST}/other.jpg`), jpeg1kb, jpegMeta));
      await assertFails(uploadBytes(ref(st, `kyc/${HOST}/passport.jpg`), jpeg1kb, jpegMeta));
    });

    it("refuses non-jpeg content types", async () => {
      const st = storageAsUser(env, HOST);
      await assertFails(uploadBytes(ref(st, kycFile), jpeg1kb, pngMeta));
      await assertFails(uploadBytes(ref(st, kycFile), jpeg1kb, { contentType: "application/pdf" }));
    });

    it("refuses empty (0-byte) files", async () => {
      const st = storageAsUser(env, HOST);
      await assertFails(uploadBytes(ref(st, kycFile), new Uint8Array(0), jpegMeta));
    });

    it("refuses files 5 MB or larger", async () => {
      const st = storageAsUser(env, HOST);
      const largeFile = new Uint8Array(5 * 1024 * 1024);
      await assertFails(uploadBytes(ref(st, kycFile), largeFile, jpegMeta));
    });
  });

  describe("deletes", () => {
    beforeEach(async () => {
      await seedStorage({ [kycFile]: jpeg1kb });
    });

    it("nobody can delete kyc documents from a client, not even admin or owner", async () => {
      await assertFails(deleteObject(ref(storageAsUser(env, HOST), kycFile)));
      await assertFails(deleteObject(ref(storageAsAdmin(env), kycFile)));
    });
  });
});

describe("avatars/{fileName}", () => {
  const avatarPath = `avatars/${HOST}.jpg`;

  describe("reads", () => {
    beforeEach(async () => {
      await seedStorage({ [avatarPath]: jpeg1kb });
    });

    it("any signed-in user can read avatars", async () => {
      await assertSucceeds(getBytes(ref(storageAsUser(env, HOST), avatarPath)));
      await assertSucceeds(getBytes(ref(storageAsUser(env, RIDER), avatarPath)));
      await assertSucceeds(getBytes(ref(storageAsUser(env, STRANGER), avatarPath)));
      await assertSucceeds(getBytes(ref(storageAsAdmin(env), avatarPath)));
    });

    it("signed-out clients cannot read avatars", async () => {
      await assertFails(getBytes(ref(storageAnon(env), avatarPath)));
    });
  });

  describe("creates and updates", () => {
    it("the owner can upload their avatar", async () => {
      const st = storageAsUser(env, HOST);
      await assertSucceeds(uploadBytes(ref(st, avatarPath), jpeg1kb, jpegMeta));
    });

    it("a user cannot upload an avatar with another user's uid in the filename", async () => {
      const st = storageAsUser(env, STRANGER);
      await assertFails(uploadBytes(ref(st, avatarPath), jpeg1kb, jpegMeta));
    });

    it("refuses non-jpeg content types", async () => {
      const st = storageAsUser(env, HOST);
      await assertFails(uploadBytes(ref(st, avatarPath), jpeg1kb, pngMeta));
    });

    it("refuses empty (0-byte) files", async () => {
      const st = storageAsUser(env, HOST);
      await assertFails(uploadBytes(ref(st, avatarPath), new Uint8Array(0), jpegMeta));
    });

    it("refuses files 2 MB or larger", async () => {
      const st = storageAsUser(env, HOST);
      const largeAvatar = new Uint8Array(2 * 1024 * 1024);
      await assertFails(uploadBytes(ref(st, avatarPath), largeAvatar, jpegMeta));
    });

    it("signed-out clients cannot upload avatars", async () => {
      await assertFails(uploadBytes(ref(storageAnon(env), avatarPath), jpeg1kb, jpegMeta));
    });
  });

  describe("deletes", () => {
    beforeEach(async () => {
      await seedStorage({ [avatarPath]: jpeg1kb });
    });

    it("nobody can delete avatars from a client", async () => {
      await assertFails(deleteObject(ref(storageAsUser(env, HOST), avatarPath)));
      await assertFails(deleteObject(ref(storageAsAdmin(env), avatarPath)));
    });
  });
});

describe("unmatched storage paths", () => {
  it("all reads and writes are denied", async () => {
    const st = storageAsAdmin(env);
    await assertFails(uploadBytes(ref(st, "other/file.jpg"), jpeg1kb, jpegMeta));
    await assertFails(getBytes(ref(st, "other/file.jpg")));
  });
});
