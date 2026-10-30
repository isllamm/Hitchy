// startRide (BUILD_PLAN 8.7): the owner starts the trip. The riders' push comes from P7-B04.
import { Timestamp } from "firebase-admin/firestore";
import { db } from "../../lib/app.js";
import { makeCallable } from "../../lib/callable.js";
import { getConfig } from "../../lib/config.js";
import type { CallCtx } from "../../lib/ctx.js";
import { fail } from "../../lib/errors.js";
import { getRide, updateRide } from "../../repos/rides.js";
import { StartRideInput } from "../../schemas/inputs.js";
import type { EmptyOutput } from "../../schemas/io.js";

const MINUTE_MS = 60 * 1000;

export async function startRideCore(input: StartRideInput, ctx: CallCtx): Promise<EmptyOutput> {
  const limits = await getConfig("limits");

  return db.runTransaction(async (tx) => {
    const ride = await getRide(input.rideId, tx);
    if (!ride || ride.hostId !== ctx.uid) fail("NOT_FOUND");
    if (ride.status !== "open" && ride.status !== "full") fail("RIDE_NOT_OPEN");

    const apart = Math.abs(ctx.now.getTime() - ride.departAt.toDate().getTime());
    if (apart > limits.startWindowMinutes * MINUTE_MS) fail("START_WINDOW_CLOSED");

    const at = Timestamp.fromDate(ctx.now);
    updateRide(tx, input.rideId, { status: "started", startedAt: at, updatedAt: at });
    return {};
  });
}

export const startRide = makeCallable("startRide", StartRideInput, startRideCore);
