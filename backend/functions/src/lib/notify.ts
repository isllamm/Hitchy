// Sends notifications (BUILD_PLAN 8.15, P7-B04): data-only FCM messages with high priority, so
// the app builds the notification and a tap follows the deep link. Every message to a user also
// goes to their inbox (users/{uid}/notifications) [DECISION-27]; admin alerts do not.
// Call these after a transaction commits, never inside it.
import * as logger from "firebase-functions/logger";
import { type QueryDocumentSnapshot, Timestamp } from "firebase-admin/firestore";
import type { AdminsDoc, DeviceDoc, Language, NotificationDoc } from "../schemas/docs.js";
import { ADMIN_URL, db, messaging } from "./app.js";
import { now } from "./clock.js";
import { getConfig } from "./config.js";
import {
  type NotificationType,
  render,
  type RenderedText,
  type TemplateParams,
  bodyFor,
  textFor,
} from "./notifyTemplates.js";

const DEAD_TOKEN_ERRORS = new Set([
  "messaging/registration-token-not-registered",
  "messaging/invalid-registration-token",
]);
const DAY_MS = 24 * 60 * 60 * 1000;
const FCM_BATCH = 500;

type DeviceSnap = QueryDocumentSnapshot<DeviceDoc>;

export interface PushMessage {
  token: string;
  data: Record<string, string>;
  android: { priority: "high" };
}

export interface PushResult {
  responses: { error?: { code: string } }[];
  failureCount: number;
}

// The only place that talks to FCM. Tests replace it with setPushSender(), the way lib/clock.ts
// replaces the system clock.
const fcmSender = (messages: PushMessage[]): Promise<PushResult> => messaging.sendEach(messages);
let sendPush = fcmSender;

export function setPushSender(fn: (messages: PushMessage[]) => Promise<PushResult>): void {
  sendPush = fn;
}

export function resetPushSender(): void {
  sendPush = fcmSender;
}

function devicesOf(uid: string) {
  return db
    .collection("users")
    .doc(uid)
    .collection("devices")
    .withConverter<DeviceDoc>({
      toFirestore: (d) => d,
      fromFirestore: (s) => s.data() as DeviceDoc,
    });
}

// Sends to each device in its own language (or `language` when given) and deletes the
// tokens FCM reports as dead.
async function sendToDevices(
  type: NotificationType,
  rendered: RenderedText,
  devices: DeviceSnap[],
  language?: Language,
): Promise<void> {
  for (let i = 0; i < devices.length; i += FCM_BATCH) {
    const batch = devices.slice(i, i + FCM_BATCH);
    const result = await sendPush(
      batch.map((d) => ({
        token: d.data().fcmToken,
        data: {
          type,
          title: textFor(rendered, language ?? d.data().language),
          body: bodyFor(rendered, language ?? d.data().language),
          deepLink: rendered.deepLink,
          channel: rendered.channel,
        },
        android: { priority: "high" as const },
      })),
    );
    const dead = result.responses.flatMap((r, k) => {
      const device = batch[k];
      return device && r.error && DEAD_TOKEN_ERRORS.has(r.error.code) ? [device] : [];
    });
    await Promise.all(dead.map((d) => d.ref.delete()));
    logger.info("notify.sent", {
      type,
      devices: batch.length,
      failed: result.failureCount,
      removed: dead.length,
    });
  }
}

export async function sendToUser(
  uid: string,
  type: NotificationType,
  params: TemplateParams = {},
): Promise<void> {
  const rendered = render(type, params, ADMIN_URL);

  // The inbox is the record that must survive (P3-B06): FCM is not emulated, so a push can
  // always fail locally, and that must never cost the user their notification.
  try {
    const devices = await devicesOf(uid).get();
    await sendToDevices(type, rendered, devices.docs);
  } catch (e) {
    logger.warn("notify.pushFailed", { type, uid, error: String(e) });
  }

  if (rendered.channel === "admin") return;

  const limits = await getConfig("limits");
  const at = now().getTime();
  const item: NotificationDoc = {
    type,
    title: rendered.title,
    body: rendered.body,
    deepLink: rendered.deepLink,
    createdAt: Timestamp.fromMillis(at),
    readAt: null,
    expireAt: Timestamp.fromMillis(at + limits.notificationRetentionDays * DAY_MS),
  };
  await db.collection("users").doc(uid).collection("notifications").add(item);
}

export async function sendToUsers(
  uids: string[],
  type: NotificationType,
  params: TemplateParams = {},
): Promise<void> {
  await Promise.all(uids.map((uid) => sendToUser(uid, type, params)));
}

// Admin alerts go to the dashboards' web push tokens (config/admins.uids) and to the phones of
// config/admins.notifyUids, always in English, and are not stored in an inbox.
export async function sendToAdmins(
  type: NotificationType,
  params: TemplateParams = {},
): Promise<void> {
  const snap = await db.collection("config").doc("admins").get();
  const admins = snap.data() as AdminsDoc | undefined;
  if (!admins) return;
  const rendered = render(type, params, ADMIN_URL);

  const web = await Promise.all(
    admins.uids.map((uid) => devicesOf(uid).where("platform", "==", "web").get()),
  );
  const phones = await Promise.all(admins.notifyUids.map((uid) => devicesOf(uid).get()));
  const devices = [...web, ...phones].flatMap((q) => q.docs);
  await sendToDevices(type, rendered, devices, "en");
}
