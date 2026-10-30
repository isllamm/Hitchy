// Output types of the callables (BUILD_PLAN 8.2 and the outputs in 8.3–8.7, Phases 4–8).
// Later phases add theirs. Instants are ISO-8601 UTC strings. No Firebase imports: the admin web
// imports this file too.
import type {
  GeoPointLite,
  HostStatus,
  LocalizedName,
  RideStatus,
  Role,
  Tier,
  TrafficLevel,
  VehicleType,
} from "./docs.js";

// ---- 8.2 Shared output types ----

export interface RideCard {
  rideId: string;
  departAt: string; // ISO UTC
  arriveAt: string; // departAt + route usualMinutes (v1.1)
  fromZone: string;
  toZone: string;
  fromPointId: string;
  toPointId: string;
  fromPointName: LocalizedName;
  toPointName: LocalizedName;
  pricePerSeat: number;
  fairPrice: boolean; // pricePerSeat <= priceRange.recommended
  seatsLeft: number;
  walkM: number | null; // rider's chosen point → this pickup point (v1.1)
  // v1.2, R24: the pickup this rider would use, the start point or the stop in their zone
  // nearest to their point. The time window and the walk use this pickup (8.16).
  pickup: { id: string; name: LocalizedName; at: string; walkM: number };
  bestMatch: boolean; // first card under "recommended" sort (v1.1)
  womenOnly: boolean;
  vehicleType: VehicleType;
  fixed: boolean; // part of a Fixed Ride
  host: {
    uid: string;
    firstName: string;
    photoPath: string | null;
    rating: number | null;
    ratingCount: number;
    trips: number; // profiles.hostTrips (v1.1)
    verified: boolean;
    tier: Tier;
    sameCompany: boolean; // never the company name
  };
}

// Only for the host and riders with an accepted booking.
export interface RideView extends RideCard {
  status: RideStatus;
  fromPoint: GeoPointLite;
  vehicle: { make: string; model: string; color: string; plate: string };
}

export interface QuoteResult {
  min: number;
  recommended: number;
  max: number;
  trafficLevel: TrafficLevel;
  distanceKm: number;
  fuelCostEgp: number;
  tripCostEgp: number;
  configVersion: number;
}

// Callables whose output is {}.
export type EmptyOutput = Record<string, never>;

// ---- 8.3 Profile and roles (Phase 4) ----

export interface CompleteProfileOutput {
  activeRole: Role;
  hostStatus: HostStatus;
  referralApplied: boolean;
}

export interface SwitchRoleOutput {
  activeRole: Role;
}

// ---- 8.4 Host verification and admin (Phase 5) ----

export interface AddVehicleOutput {
  vehicleId: string;
}

export interface SubmitHostApplicationOutput {
  status: "submitted";
}

export interface ApproveHostOutput {
  freeUntil: string; // ISO UTC
}

export interface UpdateConfigOutput {
  version: number;
}

// rejectHost, approveVehicle, setVerified, setGender and requestVerification return EmptyOutput.

// ---- 8.5 Rides and pricing (Phase 6) ----

export type QuotePriceOutput = QuoteResult;

export interface CreateRideOutput {
  rideId: string;
}

export interface CancelRideOutput {
  cancelledBookings: number;
}

// ---- 8.6 Search and booking (Phase 7) ----

export interface SearchRidesOutput {
  rides: RideCard[];
}

export interface RequestSeatsOutput {
  booking: {
    rideId: string;
    status: "requested";
    seats: number;
    price: number;
    total: number;
    expiresAt: string; // ISO UTC
  };
  ride: RideCard;
}

export interface RespondToRequestOutput {
  status: "booked" | "declined";
}

export interface CancelBookingOutput {
  lateCancel: boolean;
}

// ---- 8.7 Trip day (Phase 8) ----

export interface CheckInOutput {
  distanceM: number;
}

export interface ReportLateOutput {
  feeId: string;
}

export interface SendChatMessageOutput {
  messageId: string;
}

export interface GetContactPhoneOutput {
  phone: string; // E.164
}

export interface FinishRideOutput {
  completedBookings: number;
  creditUsed: boolean;
}

// startRide, markBoarded, disputeFee, resolveFee, markFeePaid, confirmSeatPayment and
// submitRating return EmptyOutput.

// ---- 8.16 Demo changes (v1.2) ----

export interface ArriveAtStopOutput {
  bookings: number; // bookings at this pickup whose free wait just started
}

export interface ConfirmRidePaymentOutput {
  paidBookings: number;
  total: number; // integer EGP moved to the owner
}

export interface TopUpWalletOutput {
  balance: number; // the user's balance after the top-up
}
