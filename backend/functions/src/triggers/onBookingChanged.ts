// onBookingChanged (BUILD_PLAN 8.6): the only place booking notifications come from.
// Every callable leaves the writing to this trigger, so one change sends one message.
import { onDocumentWritten } from "firebase-functions/firestore";
import * as logger from "firebase-functions/logger";
import { REGION } from "../lib/app.js";
import type { NotificationType } from "../lib/notifyTemplates.js";
import { sendToUser } from "../lib/notify.js";
import { getProfile } from "../repos/profiles.js";
import { getUser } from "../repos/users.js";
import type { BookingDoc, BookingStatus } from "../schemas/docs.js";

export interface BookingNotice {
  to: "host" | "rider";
  type: NotificationType;
}

/**
 * Who hears about a booking change, from 8.6. A change that is not in this table sends nothing,
 * so boarding, completing and payment stay silent here (payment has its own message, sent by
 * confirmRidePayment).
 */
export function noticeFor(
  before: BookingDoc | null,
  after: BookingDoc | null,
): BookingNotice | null {
  if (!after) return null;

  const was: BookingStatus | null = before?.status ?? null;
  const now = after.status;
  if (was === now) return null;

  // A brand new request.
  if (was === null) {
    return now === "requested" ? { to: "host", type: "booking_requested" } : null;
  }

  switch (now) {
    case "booked":
      return was === "requested" ? { to: "rider", type: "booking_accepted" } : null;
    case "declined":
      return was === "requested" ? { to: "rider", type: "booking_declined" } : null;
    case "expired":
      return was === "requested" ? { to: "rider", type: "request_expired" } : null;
    case "cancelled_by_rider":
      return was === "requested" || was === "booked"
        ? { to: "host", type: "booking_cancelled" }
        : null;
    case "cancelled_by_host":
      return was === "requested" || was === "booked"
        ? { to: "rider", type: "ride_cancelled" }
        : null;
    default:
      return null;
  }
}

/** The first name the message needs: the other person in the booking. */
async function firstNameOf(uid: string): Promise<string> {
  const profile = await getProfile(uid);
  if (profile) return profile.firstName;
  const user = await getUser(uid);
  return user?.firstName ?? "";
}

export async function onBookingChangedCore(
  before: BookingDoc | null,
  after: BookingDoc | null,
): Promise<BookingNotice | null> {
  const notice = noticeFor(before, after);
  if (!notice || !after) return null;

  // The host hears about the rider, and the rider about the host.
  const to = notice.to === "host" ? after.hostId : after.riderUid;
  const about = notice.to === "host" ? after.riderUid : after.hostId;

  await sendToUser(to, notice.type, {
    name: await firstNameOf(about),
    seats: after.seats,
    rideId: after.rideId,
    riderUid: after.riderUid,
  });

  logger.info("onBookingChanged.sent", { type: notice.type, rideId: after.rideId });
  return notice;
}

export const onBookingChanged = onDocumentWritten(
  { region: REGION, document: "rides/{rideId}/bookings/{riderUid}" },
  async (event) => {
    const before = (event.data?.before.data() as BookingDoc | undefined) ?? null;
    const after = (event.data?.after.data() as BookingDoc | undefined) ?? null;
    await onBookingChangedCore(before, after);
  },
);
