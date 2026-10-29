// Firebase Admin SDK clients and project-wide constants (BUILD_PLAN 4.2).
import { getApps, initializeApp } from "firebase-admin/app";
import { getAuth } from "firebase-admin/auth";
import { getFirestore } from "firebase-admin/firestore";
import { getMessaging } from "firebase-admin/messaging";
import { getStorage } from "firebase-admin/storage";

const app = getApps()[0] ?? initializeApp();

export const db = getFirestore(app);
export const auth = getAuth(app);
export const storage = getStorage(app);
export const messaging = getMessaging(app);

// Every function and the apps use this region.
export const REGION = "europe-west1"; // TODO(DECISION-01)

// Admin dashboard per project, for {admin} links in admin alerts. Filled in after P5-W01
// creates the admin Hosting sites.
const ADMIN_URLS: Partial<Record<string, string>> = {
  "hitchy-dev": "TODO-HUMAN", // TODO(DECISION-16)
  "hitchy-prod": "TODO-HUMAN", // TODO(DECISION-16)
};
export const ADMIN_URL = ADMIN_URLS[process.env.GCLOUD_PROJECT ?? ""] ?? "TODO-HUMAN";
