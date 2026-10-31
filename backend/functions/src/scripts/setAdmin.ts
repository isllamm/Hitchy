import { getApps, initializeApp, type App } from "firebase-admin/app";
import { getAuth, type Auth } from "firebase-admin/auth";
import { FieldValue, getFirestore, type Firestore } from "firebase-admin/firestore";
import type { AdminsDoc } from "../schemas/docs.js";

export interface SetAdminOptions {
  uid: string;
  project?: string;
  remove?: boolean;
  confirm?: boolean;
  silent?: boolean;
}

export interface SetAdminResult {
  uid: string;
  admin: boolean;
  targetProject: string;
  tokensRevoked?: boolean;
}

export interface ProjectValidationResult {
  isDev: boolean;
  isProd: boolean;
  isEmulator: boolean;
  targetProject: string;
}

export function validateSetAdminTarget(
  project?: string,
  confirm?: boolean,
): ProjectValidationResult {
  const normalized = project?.trim().toLowerCase();

  if (
    !normalized ||
    normalized === "demo-hitchy" ||
    normalized === "local" ||
    normalized === "emulator"
  ) {
    return {
      isDev: false,
      isProd: false,
      isEmulator: true,
      targetProject: "demo-hitchy",
    };
  }

  if (normalized === "dev" || normalized === "hitchy-bb4f9") {
    if (process.env["FIRESTORE_EMULATOR_HOST"] || process.env["FIREBASE_AUTH_EMULATOR_HOST"]) {
      throw new Error(
        "Refusing to run set-admin on dev project with emulator environment variables set.",
      );
    }
    return {
      isDev: true,
      isProd: false,
      isEmulator: false,
      targetProject: "hitchy-bb4f9",
    };
  }

  if (normalized === "prod" || normalized === "hitchy-prod") {
    if (!confirm) {
      throw new Error("Modifying admin status on prod requires --confirm flag.");
    }
    if (process.env["FIRESTORE_EMULATOR_HOST"] || process.env["FIREBASE_AUTH_EMULATOR_HOST"]) {
      throw new Error(
        "Refusing to run set-admin on prod project with emulator environment variables set.",
      );
    }
    return {
      isDev: false,
      isProd: true,
      isEmulator: false,
      targetProject: "hitchy-prod",
    };
  }

  throw new Error(
    `Invalid project: "${String(project)}". Allowed values are demo-hitchy (default), dev, or prod.`,
  );
}

function getOrInitAdminApp(projectId: string): App {
  const appName = `setadmin-${projectId}`;
  const existing = getApps().find((a) => a.name === appName);
  if (existing) {
    return existing;
  }
  return initializeApp({ projectId }, appName);
}

export async function setAdmin(opts: SetAdminOptions): Promise<SetAdminResult> {
  const trimmedUid = opts.uid.trim();
  if (!trimmedUid) {
    throw new Error("Missing required argument: --uid <uid>");
  }

  const { targetProject, isEmulator } = validateSetAdminTarget(opts.project, opts.confirm);

  if (isEmulator) {
    if (!process.env["FIRESTORE_EMULATOR_HOST"]) {
      process.env["FIRESTORE_EMULATOR_HOST"] = "127.0.0.1:8080";
    }
    if (!process.env["FIREBASE_AUTH_EMULATOR_HOST"]) {
      process.env["FIREBASE_AUTH_EMULATOR_HOST"] = "127.0.0.1:9099";
    }
  }

  const app = getOrInitAdminApp(targetProject);
  const auth: Auth = getAuth(app);
  const db: Firestore = getFirestore(app);

  const log = (msg: string) => {
    if (!opts.silent) {
      console.log(msg);
    }
  };

  // 1. Verify user exists in Firebase Auth
  let user;
  try {
    user = await auth.getUser(trimmedUid);
  } catch (err: unknown) {
    const code = (err as { code?: string }).code;
    if (code === "auth/user-not-found") {
      throw new Error(`User with UID "${trimmedUid}" not found in Firebase Auth.`);
    }
    throw err;
  }

  const adminRef = db.doc("config/admins");

  if (opts.remove) {
    // 2a. Remove admin claim, preserving other custom claims
    const existingClaims: Record<string, unknown> = {
      ...(user.customClaims ?? {}),
    };
    delete existingClaims["admin"];
    const claimsToSet = Object.keys(existingClaims).length > 0 ? existingClaims : null;
    await auth.setCustomUserClaims(trimmedUid, claimsToSet);

    // 2b. Revoke refresh tokens
    await auth.revokeRefreshTokens(trimmedUid);

    // 2c. Update config/admins in Firestore transaction
    await db.runTransaction(async (tx) => {
      const snap = await tx.get(adminRef);
      if (!snap.exists) {
        return;
      }
      const data = snap.data() as Partial<AdminsDoc> | undefined;
      const currentUids = Array.isArray(data?.uids) ? data.uids : [];
      if (currentUids.includes(trimmedUid)) {
        tx.update(adminRef, {
          uids: FieldValue.arrayRemove(trimmedUid),
        });
      }
    });

    log(`✔ Admin claim removed from user "${trimmedUid}".`);
    log(`✔ config/admins.uids updated in Firestore.`);
    log(`✔ Refresh tokens revoked for user "${trimmedUid}".`);
    log(`Note: The user must sign out and sign in again for ID token claims to update.`);

    return {
      uid: trimmedUid,
      admin: false,
      targetProject,
      tokensRevoked: true,
    };
  }

  // 3a. Add admin claim, preserving other custom claims
  const existingClaims: Record<string, unknown> = {
    ...(user.customClaims ?? {}),
  };
  existingClaims["admin"] = true;
  await auth.setCustomUserClaims(trimmedUid, existingClaims);

  // 3b. Update config/admins in Firestore transaction
  await db.runTransaction(async (tx) => {
    const snap = await tx.get(adminRef);
    if (!snap.exists) {
      const newDoc: AdminsDoc = {
        uids: [trimmedUid],
        notifyUids: [],
      };
      tx.set(adminRef, newDoc);
    } else {
      const data = snap.data() as Partial<AdminsDoc> | undefined;
      const currentUids = Array.isArray(data?.uids) ? data.uids : [];
      if (!currentUids.includes(trimmedUid)) {
        tx.update(adminRef, {
          uids: FieldValue.arrayUnion(trimmedUid),
        });
      }
    }
  });

  log(`✔ Admin claim granted to user "${trimmedUid}".`);
  log(`✔ config/admins.uids updated in Firestore.`);
  log(`Note: The user must sign out and sign in again for ID token claims to update.`);

  return {
    uid: trimmedUid,
    admin: true,
    targetProject,
  };
}
