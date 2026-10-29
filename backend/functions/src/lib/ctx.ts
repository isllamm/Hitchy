// The call context and the common checks every callable runs first (BUILD_PLAN 8.1).
import type { CallableRequest } from "firebase-functions/https";
import type { UserDoc } from "../schemas/docs.js";
import { db } from "./app.js";
import { now } from "./clock.js";
import { getConfig } from "./config.js";
import { fail } from "./errors.js";

export interface CallCtx {
  uid: string;
  phone: string | null;
  isAdmin: boolean;
  user: UserDoc | null;
  now: Date;
}

export interface CtxOptions {
  admin: boolean;
  profile: boolean;
  allowSuspended: boolean;
}

// Checks 1–5 of 8.1, in order: signed in, App Check (when enforced), admin claim, profile,
// not suspended. Admin functions skip the profile and suspended checks: dashboard accounts
// have no users/{uid} (ADR 0003).
export async function buildCtx(req: CallableRequest, opts: CtxOptions): Promise<CallCtx> {
  const authData = req.auth;
  if (!authData) fail("UNAUTHENTICATED");

  const limits = await getConfig("limits");
  if (limits.appCheckEnforced && !req.app) fail("UNAUTHENTICATED");

  const isAdmin = authData.token.admin === true;
  if (opts.admin && !isAdmin) fail("PERMISSION_DENIED");

  let user: UserDoc | null = null;
  if (!opts.admin) {
    const snap = await db.collection("users").doc(authData.uid).get();
    user = snap.exists ? (snap.data() as UserDoc) : null;
    if (opts.profile && !user) fail("PROFILE_REQUIRED");
    if (!opts.allowSuspended && user?.suspended === true) fail("SUSPENDED");
  }

  return {
    uid: authData.uid,
    phone: authData.token.phone_number ?? null,
    isAdmin,
    user,
    now: now(),
  };
}
