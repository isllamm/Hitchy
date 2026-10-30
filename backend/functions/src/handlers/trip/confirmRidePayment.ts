// confirmRidePayment (BUILD_PLAN 8.16, v1.2; replaces confirmSeatPayment). The owner taps
// Confirm Payment once and every rider who travelled pays from their wallet (R22).
// Hitchy takes no cut, and no money leaves the two wallets.
import { FieldValue, Timestamp, type Transaction } from "firebase-admin/firestore";
import { db } from "../../lib/app.js";
import { makeCallable } from "../../lib/callable.js";
import type { CallCtx } from "../../lib/ctx.js";
import { fail } from "../../lib/errors.js";
import { withIdempotency } from "../../lib/idempotency.js";
import { sendToUser } from "../../lib/notify.js";
import { bookingRef, listBookings } from "../../repos/bookings.js";
import { getFee } from "../../repos/fees.js";
import { getProfile } from "../../repos/profiles.js";
import { getRide } from "../../repos/rides.js";
import { getUser } from "../../repos/users.js";
import { addToWallet, getBalance } from "../../repos/wallets.js";
import { ConfirmRidePaymentInput } from "../../schemas/inputs.js";
import type { ConfirmRidePaymentOutput } from "../../schemas/io.js";

export async function confirmRidePaymentCore(
  input: ConfirmRidePaymentInput,
  ctx: CallCtx,
): Promise<ConfirmRidePaymentOutput> {
  const result = await withIdempotency(ctx.uid, input.requestId, async (tx: Transaction) => {
    const ride = await getRide(input.rideId, tx);
    if (!ride || ride.hostId !== ctx.uid) fail("NOT_FOUND");
    if (ride.status !== "finished") fail("RIDE_NOT_FINISHED");

    const unpaid = (await listBookings(input.rideId, tx)).filter(
      (b) => b.status === "completed" && b.paymentConfirmedAt === null,
    );
    if (unpaid.length === 0) fail("PAYMENT_ALREADY_CONFIRMED");

    // All reads first: Firestore transactions allow no read after a write.
    const fees = new Map<string, number>();
    for (const booking of unpaid) {
      if (booking.lateFeeId === null) continue;
      const fee = await getFee(booking.lateFeeId, tx);
      // Only a fee still unpaid is collected; a disputed one waits for resolveFee (8.7).
      if (fee?.status === "unpaid") fees.set(booking.lateFeeId, fee.amount);
    }

    const riderBalances = new Map<string, number>();
    for (const booking of unpaid) {
      riderBalances.set(booking.riderUid, await getBalance(booking.riderUid, tx));
    }
    let ownerBalance = await getBalance(ctx.uid, tx);

    const at = Timestamp.fromDate(ctx.now);
    let total = 0;

    for (const booking of unpaid) {
      const feeAmount = booking.lateFeeId !== null ? (fees.get(booking.lateFeeId) ?? 0) : 0;
      const amount = booking.total + feeAmount;
      total += amount;

      // The rider may go below 0, but only through a late fee [DECISION-30].
      const riderBalance = riderBalances.get(booking.riderUid) ?? 0;
      addToWallet(
        tx,
        booking.riderUid,
        -amount,
        { reason: "ride_payment", refId: input.rideId, by: ctx.uid },
        riderBalance,
        ctx.now,
      );

      ownerBalance = addToWallet(
        tx,
        ctx.uid,
        amount,
        { reason: "ride_earning", refId: input.rideId, by: ctx.uid },
        ownerBalance,
        ctx.now,
      );

      tx.update(bookingRef(input.rideId, booking.riderUid), {
        paymentConfirmedAt: at,
        updatedAt: at,
      });

      if (booking.lateFeeId !== null && feeAmount > 0) {
        tx.update(db.collection("fees").doc(booking.lateFeeId), {
          status: "paid",
          updatedAt: at,
        });
        tx.update(db.collection("users").doc(booking.riderUid), {
          "rider.unpaidFees": FieldValue.increment(-1),
          updatedAt: at,
        });
      }
    }

    return { paidBookings: unpaid.length, total, riderUids: unpaid.map((b) => b.riderUid) };
  });

  // After the commit, never inside it (8.1). The receipt opens from this message.
  const owner = (await getProfile(ctx.uid))?.firstName ?? (await getUser(ctx.uid))?.firstName ?? "";
  for (const riderUid of result.riderUids) {
    await sendToUser(riderUid, "payment_confirmed", { name: owner, rideId: input.rideId });
  }

  return { paidBookings: result.paidBookings, total: result.total };
}

export const confirmRidePayment = makeCallable(
  "confirmRidePayment",
  ConfirmRidePaymentInput,
  confirmRidePaymentCore,
);
