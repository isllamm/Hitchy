// Types of every Firestore document (BUILD_PLAN section 6, v1.1). They are the source of truth;
// contract/firestore.md mirrors them. Written in P1-B02 because lib/ needs them (ADR 0003).

// ---- 6.1 Shared types ----

// No Firebase imports in schemas/: the admin web imports these files too.
// The Admin SDK and web SDK Timestamp classes both match this shape.
export interface Timestamp {
  seconds: number;
  nanoseconds: number;
  toDate(): Date;
}

export type Gender = "female" | "male";
export type Role = "rider" | "host";
export type Language = "ar" | "en";
export type Tier = "bronze" | "silver" | "gold";
export type VehicleType = "car" | "scooter";
export type Direction = "to_work" | "to_home";
export type TrafficLevel = "offPeak" | "normal" | "peak";
export type HostStatus = "none" | "pending" | "approved" | "rejected";
export interface LocalizedName {
  ar: string;
  en: string;
}
export interface GeoPointLite {
  lat: number;
  lng: number;
}
export type HHmm = string; // "08:30", Cairo time
export type DateKey = string; // "2026-11-02", Cairo date

export interface Base {
  schemaVersion: 1;
  createdAt: Timestamp;
  updatedAt: Timestamp;
}

// ---- 6.2 Users and profiles ----

// users/{uid} — private. Read: owner, admin.
export interface UserDoc extends Base {
  phone: string; // "+2010…", taken from the auth token only
  firstName: string;
  lastName: string;
  gender: Gender; // locked after it is set (admin can fix)
  companyId: string | null;
  companyOther: string | null; // free text when the company is not in the list
  workHours: { start: HHmm; end: HHmm } | null;
  language: Language;
  photoPath: string | null; // "avatars/{uid}.jpg"
  emergencyContact: { name: string; phone: string } | null;
  savedPlaces: { label: "home" | "work" | "other"; zoneId: string; pointId: string }[]; // max maxSavedPlaces; fixed points only (v1.1)
  activeRole: Role; // written only by switchRole
  referralCode: string;
  referredBy: string | null; // uid of the inviter
  referralRewarded: boolean;
  blockedUids: string[]; // max 200
  suspended: boolean;
  rider: {
    tier: Tier;
    ratingSum: number;
    ratingCount: number;
    tripsCompleted: number;
    unpaidFees: number; // unpaid + disputed fees
    lateCancels: number;
  };
  host: {
    status: HostStatus;
    verified: boolean;
    approvedAt: Timestamp | null;
    freeUntil: Timestamp | null; // approvedAt + freeMonthDays
    freeEndingNotified: boolean;
    credits: { regular: number; fixed: number }; // may go below 0 (see finishRide)
    tier: Tier;
    ratingSum: number;
    ratingCount: number;
    tripsCompleted: number;
    lateCancels: number;
    hostLateCount: number;
  };
  points: { balance: number };
}

// Fields the owner may write directly (rules check this list):
export const OWNER_WRITABLE_USER_FIELDS = [
  "firstName",
  "lastName",
  "companyId",
  "companyOther",
  "workHours",
  "language",
  "photoPath",
  "emergencyContact",
  "savedPlaces",
  "updatedAt",
] as const;

// profiles/{uid} — public card. Read: any signed-in user. Written by syncPublicProfile.
export interface ProfileDoc {
  schemaVersion: 1;
  firstName: string;
  photoPath: string | null;
  hostRating: { avg: number | null; count: number };
  hostTrips: number; // host.tripsCompleted, for "★ 4.9 · 128 rides"
  riderRating: { avg: number | null; count: number };
  verified: boolean;
  hostTier: Tier;
  riderTier: Tier;
  updatedAt: Timestamp;
}

// users/{uid}/devices/{installId} — written by the owner.
export interface DeviceDoc {
  fcmToken: string;
  platform: "android" | "ios" | "web";
  appVersion: string;
  language: Language;
  updatedAt: Timestamp;
}

// users/{uid}/notifications/{id} — the inbox (v1.1). Written by lib/notify.ts with every push.
// Read: owner. The owner may set only readAt. Deleted by a TTL policy on expireAt [DECISION-27].
export interface NotificationDoc {
  type: string; // a type from section 8.15
  title: LocalizedName; // both languages, so a language switch still reads well
  body: LocalizedName;
  deepLink: string;
  createdAt: Timestamp;
  readAt: Timestamp | null;
  expireAt: Timestamp; // createdAt + notificationRetentionDays
}

// users/{uid}/vehicles/{vehicleId} — written by functions.
export interface VehicleDoc extends Base {
  type: VehicleType;
  make: string;
  model: string;
  year: number;
  color: string;
  plate: string;
  maxSeats: number; // from config: car 3, scooter 1
  status: "pending" | "approved" | "rejected";
}

// users/{uid}/ledger/{entryId} — every credit change. Written by functions.
export interface LedgerEntry {
  delta: number; // +10, -1, +3 …
  bucket: "regular" | "fixed";
  reason: "package" | "trip" | "reward" | "admin";
  refId: string; // orderId, rideId, points entry id
  balanceAfter: number;
  createdAt: Timestamp;
}

// users/{uid}/points/{entryId} — every points change. Written by functions.
export interface PointsEntry {
  amount: number; // + earn, - spend or expire
  remaining: number; // earn entries only; 0 for spend/expire
  open: boolean; // remaining > 0
  reason:
    | "trip"
    | "on_time_week"
    | "five_star"
    | "fixed_week"
    | "referral"
    | "redeem"
    | "expire"
    | "admin";
  refId: string;
  expiresAt: Timestamp | null; // earn entries only
  createdAt: Timestamp;
}

// ---- 6.3 Host verification ----

export type KycDocKey =
  "idFront" | "idBack" | "drivingLicense" | "vehicleLicense" | "criminalRecord";

// kyc/{uid}. Read: owner, admin. Written by functions.
export interface KycDoc extends Base {
  status: "none" | "submitted" | "approved" | "rejected";
  vehicleId: string | null;
  docs: Record<
    KycDocKey,
    {
      path: string; // "kyc/{uid}/id_front.jpg"
      status: "missing" | "uploaded" | "accepted" | "rejected";
      rejectReason: "blurry" | "expired" | "mismatch" | "missing" | "other" | null;
    }
  >;
  submittedAt: Timestamp | null;
  reviewedAt: Timestamp | null;
  reviewerUid: string | null;
  note: string | null;
}

// ---- 6.4 Places ----

// companies/{companyId}. Read: signed-in. Written by admin scripts.
export interface CompanyDoc {
  name: LocalizedName;
  zoneId: string;
  active: boolean;
}

// zones/{zoneId}. Read: signed-in.
export interface ZoneDoc {
  name: LocalizedName;
  active: boolean;
  order: number;
}

// zones/{zoneId}/points/{pointId}. Read: signed-in.
export interface PointDoc {
  name: LocalizedName;
  lat: number;
  lng: number;
  active: boolean;
}

// routes/{fromZone}__{toZone}. Read: signed-in.
export interface RouteDoc {
  fromZone: string;
  toZone: string;
  distanceKm: number;
  usualMinutes: number;
}

// ---- 6.5 Rides and bookings ----

export type RideStatus = "open" | "full" | "started" | "finished" | "cancelled" | "expired";

// rides/{rideId}. Read: host, riders with an accepted booking, admin. Written by functions.
export interface RideDoc extends Base {
  hostId: string;
  hostCompanyId: string | null;
  vehicleId: string;
  vehicle: { type: VehicleType; make: string; model: string; color: string; plate: string };
  fromZone: string;
  fromPointId: string;
  fromPointName: LocalizedName;
  fromPoint: GeoPointLite;
  toZone: string;
  toPointId: string;
  toPointName: LocalizedName;
  direction: Direction;
  departAt: Timestamp;
  dateKey: DateKey;
  seatsTotal: number;
  seatsLeft: number; // seats not held by a requested or accepted booking
  pricePerSeat: number;
  priceRange: {
    min: number;
    recommended: number;
    max: number;
    trafficLevel: TrafficLevel;
    distanceKm: number;
    configVersion: number;
  };
  womenOnly: boolean;
  status: RideStatus;
  fixedRideId: string | null;
  bookingsCount: number; // accepted bookings, for "first booking" logic
  creditConsumed: boolean;
  postProcessed: boolean; // set by onRideFinished
  autoFinished: boolean;
  startedAt: Timestamp | null;
  finishedAt: Timestamp | null;
  cancelledAt: Timestamp | null;
  cancelReason:
    "sick" | "car_problem" | "plans_changed" | "other" | "not_started" | "suspended" | null;
}

// "requested" waits for the host; "booked" means the host accepted (R20).
export type BookingStatus =
  | "requested"
  | "booked"
  | "boarded"
  | "completed"
  | "declined"
  | "expired"
  | "cancelled_by_rider"
  | "cancelled_by_host"
  | "no_show";

// rides/{rideId}/bookings/{riderUid}. Read: that rider, the host, admin. Written by functions.
export interface BookingDoc extends Base {
  rideId: string;
  riderUid: string;
  hostId: string;
  departAt: Timestamp; // copy, for the rider's trips query
  status: BookingStatus;
  seats: number; // 1 … seatsLeft at request time (R21)
  price: number; // per seat, copied at request time
  total: number; // price × seats, paid to the host directly
  expiresAt: Timestamp; // request deadline [DECISION-21]
  respondedAt: Timestamp | null; // when the host accepted or declined
  checkedInAt: Timestamp | null;
  checkInDistanceM: number | null;
  boardedAt: Timestamp | null;
  lateFeeId: string | null;
  paymentConfirmedAt: Timestamp | null; // the host confirmed the rider paid (R22)
  verifiedTogether: boolean;
  rewarded: boolean;
  lateCancel: boolean;
  cancelledAt: Timestamp | null;
  cancelReason:
    | "host_late"
    | "plans_changed"
    | "found_another_ride"
    | "other"
    | "ride_cancelled"
    | "not_started"
    | null;
}

// rides/{rideId}/presence/{uid}. Written by the owner while the ride is started.
export interface PresenceDoc {
  last: { lat: number; lng: number; accuracyM: number; at: Timestamp };
  samples: { lat: number; lng: number; at: Timestamp }[]; // max 200
  updatedAt: Timestamp;
}

// rides/{rideId}/chats/{riderUid}/messages/{messageId} (v1.1, R23).
// Read: the host and that rider (not admins). Written by sendChatMessage.
export interface ChatMessageDoc {
  fromUid: string;
  text: string; // 1–500 characters; never logged
  createdAt: Timestamp;
  expireAt: Timestamp; // TTL: ride departAt + chatRetentionDays [DECISION-23]
}

// ---- 6.6 Fees, ratings, packages ----

// fees/{feeId}. Read: that rider, that host, admin. Written by functions.
export interface FeeDoc extends Base {
  rideId: string;
  riderUid: string;
  hostId: string;
  amount: number; // config.limits.lateFeeEgp
  reason: "late" | "no_show";
  status: "unpaid" | "paid" | "disputed" | "waived";
  evidence: {
    checkedInAt: Timestamp | null;
    checkInDistanceM: number | null;
    reportedAt: Timestamp;
  };
  dispute: { text: string; at: Timestamp } | null;
  resolvedBy: string | null;
}

// ratings/{rideId}_{fromUid}_{toUid}. Read: author, admin. Written by functions.
export interface RatingDoc {
  rideId: string;
  fromUid: string;
  toUid: string;
  roleRated: Role;
  stars: 1 | 2 | 3 | 4 | 5;
  tags: string[]; // from RATING_TAGS
  createdAt: Timestamp;
}
export const RATING_TAGS = {
  host: [
    "on_time",
    "safe_driving",
    "clean_car",
    "friendly",
    "followed_route",
    "late",
    "unsafe_driving",
    "rude",
  ],
  rider: ["on_time", "polite", "easy_pickup", "late", "rude"],
} as const;

// packageOrders/{orderId}. Read: owner host, admin. Written by functions.
export interface PackageOrderDoc extends Base {
  hostId: string;
  packageId: string;
  trips: number;
  bucket: "regular" | "fixed";
  amountEgp: number;
  reference: string; // "HT-7K3QX9"
  status: "pending" | "confirmed" | "rejected";
  note: string | null;
  confirmedAt: Timestamp | null;
  confirmedBy: string | null;
}

// ---- 6.7 Fixed Ride ----

// fixedRides/{fixedRideId}. Read: host, its riders, admin. Written by functions.
export interface FixedRideDoc extends Base {
  hostId: string;
  vehicleId: string;
  fromZone: string;
  fromPointId: string;
  toZone: string;
  toPointId: string;
  direction: Direction;
  weekdays: number[]; // 0 = Sunday … 6 = Saturday (Cairo)
  departTime: HHmm;
  seats: number;
  pricePerSeat: number;
  womenOnly: boolean;
  riderUids: string[];
  inviteCode: string;
  startDate: DateKey;
  status: "active" | "paused" | "ended";
}

// fixedRides/{fixedRideId}/skips/{dateKey}. Written by functions.
export interface FixedSkipDoc {
  riderUids: string[];
  hostSkipped: boolean;
  updatedAt: Timestamp;
}

// ---- 6.8 Safety, admin, system ----

// reports/{id}. Read: admin.
export interface ReportDoc extends Base {
  reporterUid: string | null; // null for system reports (low rating)
  targetUid: string;
  rideId: string | null;
  category: "unsafe" | "harassment" | "no_show" | "payment" | "low_rating" | "other";
  text: string | null;
  status: "open" | "closed";
}

// safetyEvents/{id}. Read: admin.
export interface SafetyEventDoc {
  uid: string;
  rideId: string;
  type: "sos";
  lat: number | null;
  lng: number | null;
  createdAt: Timestamp;
  handledBy: string | null;
}

// adminActions/{id}. Read: admin.
export interface AdminActionDoc {
  adminUid: string;
  action: string;
  targetUid: string | null;
  refId: string | null;
  note: string | null;
  createdAt: Timestamp;
}

// metrics/impact. Read: signed-in. metrics/{isoWeek}: admin.
export interface ImpactDoc {
  tripsCompleted: number;
  litersSaved: number;
  co2Kg: number;
  egpSaved: number;
  updatedAt: Timestamp;
}
export interface WeekMetricsDoc {
  week: string;
  cohortSize: number;
  retained: number;
  rate: number;
  createdAt: Timestamp;
}

// Functions only (no client access):

// codes/{code}
export interface CodeDoc {
  type: "reference" | "invite" | "referral";
  ownerId: string;
  refId: string;
  createdAt: Timestamp;
}

// idempotency/{uid}_{requestId}
export interface IdempotencyDoc {
  result: unknown;
  expireAt: Timestamp;
}

// aiUsage/{uid}_{yyyymmdd}
export interface AiUsageDoc {
  count: number;
  updatedAt: Timestamp;
}

// aiBudget/{yyyymm}
export interface AiBudgetDoc {
  estimatedEgp: number;
  updatedAt: Timestamp;
}

// config/admins
export interface AdminsDoc {
  uids: string[]; // dashboard accounts
  notifyUids: string[]; // app accounts that get admin alerts on the phone
}
