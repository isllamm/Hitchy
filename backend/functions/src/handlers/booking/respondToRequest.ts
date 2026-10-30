// respondToRequest (BUILD_PLAN 8.6): the owner accepts or declines a seat request (R20).
// The notifications come from the onBookingChanged trigger (P3-B06), never from here.
import { Timestamp } from "firebase-admin/firestore";
import { makeCallable } from "../../lib/callable.js";
import type { CallCtx } from "../../lib/ctx.js";
import { fail } from "../../lib/errors.js";
import { db } from "../../lib/app.js";
import { bookingRef, getBooking, updateBooking } from "../../repos/bookings.js";
import { getRide, updateRide } from "../../repos/rides.js";
import { RespondToRequestInput } from "../../schemas/inputs.js";
import type { RespondToRequestOutput } from "../../schemas/io.js";

export async function respondToRequestCore(
  input: RespondToRequestInput,
  ctx: CallCtx,
): Promise<RespondToRequestOutput> {
  return db.runTransaction(async (tx) => {
    const ride = await getRide(input.rideId, tx);
    if (!ride || ride.hostId !== ctx.uid) fail("NOT_FOUND");
    if (ride.status !== "open" && ride.status !== "full") fail("RIDE_NOT_OPEN");

    const booking = await getBooking(input.rideId, input.riderUid, tx);
    if (!booking) fail("BOOKING_NOT_FOUND");
    if (booking.status !== "requested") fail("REQUEST_NOT_PENDING");
    if (ctx.now.getTime() >= booking.expiresAt.toDate().getTime()) fail("REQUEST_EXPIRED");

    const at = Timestamp.fromDate(ctx.now);
    const ref = bookingRef(input.rideId, input.riderUid);

    if (input.accept) {
      updateBooking(tx, ref, { status: "booked", respondedAt: at, updatedAt: at });
      updateRide(tx, input.rideId, {
        bookingsCount: ride.bookingsCount + 1,
        updatedAt: at,
      });
      return { status: "booked" };
    }

    // Declining gives the held seats back and reopens a full ride.
    updateBooking(tx, ref, { status: "declined", respondedAt: at, updatedAt: at });
    const seatsLeft = ride.seatsLeft + booking.seats;
    updateRide(tx, input.rideId, {
      seatsLeft,
      status: ride.status === "full" ? "open" : ride.status,
      updatedAt: at,
    });
    return { status: "declined" };
  });
}

export const respondToRequest = makeCallable(
  "respondToRequest",
  RespondToRequestInput,
  respondToRequestCore,
);
