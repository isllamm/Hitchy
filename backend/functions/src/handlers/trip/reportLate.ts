// reportLate (BUILD_PLAN 8.7, with the free wait of 8.16): the owner records a 10 EGP late fee.
// Nothing is charged by itself; the fee exists only once the owner reports it (R9).
import { FieldValue, Timestamp } from "firebase-admin/firestore";
import { db } from "../../lib/app.js";
import { makeCallable } from "../../lib/callable.js";
import { getConfig } from "../../lib/config.js";
import type { CallCtx } from "../../lib/ctx.js";
import { fail } from "../../lib/errors.js";
import { bookingRef, getBooking } from "../../repos/bookings.js";
import { newFeeRef, writeFee } from "../../repos/fees.js";
import { getRide } from "../../repos/rides.js";
import { ReportLateInput } from "../../schemas/inputs.js";
import type { ReportLateOutput } from "../../schemas/io.js";

const MINUTE_MS = 60 * 1000;

export async function reportLateCore(
  input: ReportLateInput,
  ctx: CallCtx,
): Promise<ReportLateOutput> {
  const limits = await getConfig("limits");

  return db.runTransaction(async (tx) => {
    const ride = await getRide(input.rideId, tx);
    if (!ride || ride.hostId !== ctx.uid) fail("NOT_FOUND");

    const booking = await getBooking(input.rideId, input.riderUid, tx);
    if (!booking || (booking.status !== "booked" && booking.status !== "boarded")) {
      fail("BOOKING_NOT_FOUND");
    }
    if (booking.lateFeeId !== null) fail("FEE_EXISTS");

    // v1.2: the free wait runs from the moment the owner tapped "I'm here".
    const arrivedMs = booking.hostArrivedAt?.toDate().getTime() ?? null;
    const graceMs = limits.lateGraceMinutes * MINUTE_MS;

    // A rider who checked in at the pickup inside the free wait was on time.
    const checkedInMs = booking.checkedInAt?.toDate().getTime() ?? null;
    if (
      arrivedMs !== null &&
      checkedInMs !== null &&
      checkedInMs <= arrivedMs + graceMs &&
      booking.checkInDistanceM !== null &&
      booking.checkInDistanceM <= limits.checkInRadiusM
    ) {
      fail("RIDER_WAS_ON_TIME");
    }

    // Without "I'm here" there is no wait to be late for.
    if (arrivedMs === null) fail("GRACE_NOT_OVER");

    if (input.kind === "late") {
      if (ctx.now.getTime() < arrivedMs + graceMs) fail("GRACE_NOT_OVER");
    } else {
      if (booking.status !== "booked") fail("BOOKING_NOT_FOUND");
      if (ctx.now.getTime() < arrivedMs + limits.noShowMinutes * MINUTE_MS) {
        fail("GRACE_NOT_OVER");
      }
    }

    const at = Timestamp.fromDate(ctx.now);
    const feeRef = newFeeRef();
    writeFee(
      tx,
      feeRef,
      {
        rideId: input.rideId,
        riderUid: input.riderUid,
        hostId: ctx.uid,
        amount: limits.lateFeeEgp,
        reason: input.kind,
        evidence: {
          checkedInAt: booking.checkedInAt,
          checkInDistanceM: booking.checkInDistanceM,
          reportedAt: at,
        },
      },
      ctx.now,
    );

    // One fee per booking, whatever its seats [DECISION-24].
    tx.update(bookingRef(input.rideId, input.riderUid), {
      lateFeeId: feeRef.id,
      updatedAt: at,
      ...(input.kind === "no_show" ? { status: "no_show" as const } : {}),
    });

    // An unpaid fee blocks the rider's next request (R9).
    tx.update(db.collection("users").doc(input.riderUid), {
      "rider.unpaidFees": FieldValue.increment(1),
      updatedAt: at,
    });

    return { feeId: feeRef.id };
  });
}

export const reportLate = makeCallable("reportLate", ReportLateInput, reportLateCore);
