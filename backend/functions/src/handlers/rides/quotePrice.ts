// quotePrice (BUILD_PLAN 8.5): the seat price range a host may post.
import { quote, trafficLevelAt } from "../../domain/pricing.js";
import { makeCallable } from "../../lib/callable.js";
import { getConfig } from "../../lib/config.js";
import type { CallCtx } from "../../lib/ctx.js";
import { fail } from "../../lib/errors.js";
import { minuteOfDay } from "../../lib/time.js";
import { getRoute } from "../../repos/routes.js";
import { getVehicle } from "../../repos/vehicles.js";
import { QuotePriceInput } from "../../schemas/inputs.js";
import type { QuotePriceOutput } from "../../schemas/io.js";

export async function quotePriceCore(
  input: QuotePriceInput,
  ctx: CallCtx,
): Promise<QuotePriceOutput> {
  if (ctx.user?.host.status !== "approved") fail("HOST_NOT_APPROVED");

  const vehicle = await getVehicle(ctx.uid, input.vehicleId);
  if (!vehicle) fail("NOT_FOUND");

  const pricing = await getConfig("pricing");
  if (pricing.litersPer100Km[vehicle.type] == null) fail("SCOOTER_DISABLED");

  const route = await getRoute(input.fromZone, input.toZone);
  if (!route) fail("ROUTE_NOT_SUPPORTED");

  if (input.seats > vehicle.maxSeats) fail("SEATS_INVALID");

  const departAt = new Date(input.departAt);
  const trafficLevel = trafficLevelAt(minuteOfDay(departAt), pricing);
  const breakdown = quote(
    {
      distanceKm: route.distanceKm,
      vehicleType: vehicle.type,
      trafficLevel,
      seats: input.seats,
    },
    pricing,
  );

  return {
    ...breakdown,
    trafficLevel,
    distanceKm: route.distanceKm,
    configVersion: pricing.version,
  };
}

export const quotePrice = makeCallable("quotePrice", QuotePriceInput, quotePriceCore);
