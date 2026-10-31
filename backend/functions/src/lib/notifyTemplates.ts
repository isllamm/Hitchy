// Notification types, channels, deep links and text (BUILD_PLAN 8.15, v1.1).
// Placeholders: {name} (the other person's first name), {seats}, {seatWord} ("1 seat", "2 seats"),
// {rideId}, {riderUid}, {id}, {admin} (ADMIN_URL). `body` is the short line under the title in the
// app's Notifications list (the demo frame 676:10912). Text riders read says "owner" [DECISION-20]. Review the Arabic before release.
import type { Language, LocalizedName } from "../schemas/docs.js";

export type Channel = "bookings" | "trips" | "account" | "admin";

export interface Template {
  channel: Channel;
  deepLink: string;
  text: LocalizedName;
  body?: LocalizedName;
}

export const TEMPLATES = {
  new_host_application: {
    channel: "admin",
    deepLink: "{admin}/hosts",
    text: { en: "New host application to review", ar: "فيه طلب صاحب عربية جديد للمراجعة" },
  },
  host_approved: {
    channel: "account",
    deepLink: "hitchy://host/status",
    text: { en: "You're approved", ar: "اتوافق عليك" },
    body: { en: "Your first month is free. Post your first ride.", ar: "أول شهر ببلاش. انشر أول مشوار ليك." },
  },
  host_rejected: {
    channel: "account",
    deepLink: "hitchy://host/status",
    text: { en: "Some documents need fixing", ar: "فيه ورق محتاج يتعدل" },
    body: { en: "Open your application to upload them again.", ar: "افتح طلبك وارفعهم تاني." },
  },
  vehicle_reviewed: {
    channel: "account",
    deepLink: "hitchy://host/status",
    text: { en: "Your vehicle was reviewed", ar: "عربيتك اتراجعت" },
  },
  verified_granted: {
    channel: "account",
    deepLink: "hitchy://profile",
    text: { en: "You're now Verified", ar: "بقيت Verified" },
  },
  account_ready: {
    channel: "account",
    deepLink: "hitchy://home",
    text: { en: "Your Hitchy account is ready", ar: "حسابك على Hitchy جاهز" },
    body: { en: "You can now book rides.", ar: "تقدر تحجز مشاوير دلوقتي." },
  },
  verify_request: {
    channel: "admin",
    deepLink: "{admin}/hosts",
    text: { en: "A host asked for Verified", ar: "صاحب عربية طالب علامة Verified" },
  },
  booking_requested: {
    channel: "bookings",
    deepLink: "hitchy://ride/{rideId}",
    text: { en: "{name} asked for {seatWord}", ar: "{name} طلب يحجز {seatWord}" },
    body: { en: "Open the ride to accept or decline.", ar: "افتح المشوار عشان توافق أو ترفض." },
  },
  booking_accepted: {
    channel: "bookings",
    deepLink: "hitchy://trip/{rideId}",
    text: { en: "{name} accepted your request", ar: "{name} وافق على طلبك" },
    body: { en: "Your seat is confirmed. See you at the pickup point.", ar: "كرسيك اتأكد. نشوفك في مكان الركوب." },
  },
  booking_declined: {
    channel: "bookings",
    deepLink: "hitchy://search",
    text: { en: "{name} couldn't take your request", ar: "{name} مقدرش ياخد طلبك" },
    body: { en: "Search again to find another ride.", ar: "دوّر تاني على مشوار تاني." },
  },
  request_expired: {
    channel: "bookings",
    deepLink: "hitchy://search",
    text: { en: "Your request wasn't answered in time", ar: "طلبك متردش عليه في الوقت" },
    body: { en: "Search again to find another ride.", ar: "دوّر تاني على مشوار تاني." },
  },
  booking_cancelled: {
    channel: "bookings",
    deepLink: "hitchy://ride/{rideId}",
    text: { en: "{name} cancelled their booking", ar: "{name} لغى الحجز" },
    body: { en: "The seat is open again for other riders.", ar: "الكرسي بقى متاح لركاب تانيين." },
  },
  ride_cancelled: {
    channel: "bookings",
    deepLink: "hitchy://trips",
    text: { en: "Your ride was cancelled", ar: "مشوارك اتلغى" },
    body: { en: "Search again to find another ride.", ar: "دوّر تاني على مشوار تاني." },
  },
  ride_reminder: {
    channel: "trips",
    deepLink: "hitchy://trip/{rideId}",
    text: { en: "Your ride leaves in 30 minutes", ar: "مشوارك هيتحرك كمان نص ساعة" },
    body: { en: "Be at your pickup point a few minutes early.", ar: "خليك في مكان الركوب قبلها بكام دقيقة." },
  },
  ride_started: {
    channel: "trips",
    deepLink: "hitchy://trip/{rideId}",
    text: { en: "Your owner is on the way", ar: "صاحب العربية في الطريق" },
    body: { en: "Open the trip to follow the ride.", ar: "افتح الرحلة عشان تتابع المشوار." },
  },
  rider_at_pickup: {
    channel: "trips",
    deepLink: "hitchy://trip/{rideId}",
    text: { en: "{name} is at the pickup point", ar: "{name} وصل مكان الركوب" },
    body: { en: "Open the trip to mark them on board.", ar: "افتح الرحلة وسجّل إنه ركب." },
  },
  fee_created: {
    channel: "account",
    deepLink: "hitchy://fees",
    text: { en: "A 10 EGP late fee was recorded", ar: "اتسجلت عليك غرامة تأخير 10 جنيه" },
  },
  fee_paid: {
    channel: "account",
    deepLink: "hitchy://fees",
    text: { en: "Your late fee is marked as paid", ar: "الغرامة اتسجلت إنها اتدفعت" },
  },
  fee_resolved: {
    channel: "account",
    deepLink: "hitchy://fees",
    text: { en: "The fee dispute was resolved", ar: "الاعتراض على الغرامة اتحسم" },
  },
  fee_disputed: {
    channel: "admin",
    deepLink: "{admin}/disputes",
    text: { en: "A fee was disputed", ar: "فيه اعتراض على غرامة" },
  },
  chat_message: {
    channel: "trips",
    deepLink: "hitchy://chat/{rideId}/{riderUid}",
    text: { en: "New message from {name}", ar: "رسالة جديدة من {name}" },
  },
  rate_trip: {
    channel: "trips",
    deepLink: "hitchy://rate/{rideId}",
    text: { en: "How was your ride?", ar: "المشوار كان عامل إزاي؟" },
    body: { en: "Rate your trip to help other riders.", ar: "قيّم المشوار عشان تساعد الركاب التانيين." },
  },
  rate_riders: {
    channel: "trips",
    deepLink: "hitchy://rate/{rideId}",
    text: { en: "Rate your riders", ar: "قيّم الركاب" },
  },
  payment_confirmed: {
    channel: "account",
    deepLink: "hitchy://receipt/{rideId}",
    text: { en: "{name} confirmed your payment", ar: "{name} أكد إنك دفعت" },
    body: { en: "Your receipt for this ride is ready.", ar: "إيصال المشوار ده جاهز." },
  },
  low_balance: {
    channel: "account",
    deepLink: "hitchy://packages",
    text: { en: "You have 2 trips left", ar: "فاضلك رحلتين" },
  },
  free_month_ending: {
    channel: "account",
    deepLink: "hitchy://packages",
    text: { en: "Your free month ends soon", ar: "الشهر المجاني قرب يخلص" },
  },
  package_confirmed: {
    channel: "account",
    deepLink: "hitchy://packages",
    text: { en: "Your package is active", ar: "الباكدج اتفعّل" },
  },
  package_rejected: {
    channel: "account",
    deepLink: "hitchy://packages",
    text: { en: "We couldn't confirm your payment", ar: "مقدرناش نأكد الدفع" },
  },
  fixed_rider_joined: {
    channel: "bookings",
    deepLink: "hitchy://fixed/{id}",
    text: { en: "{name} joined your Fixed Ride", ar: "{name} انضم لمشوارك الثابت" },
  },
  fixed_price_adjusted: {
    channel: "account",
    deepLink: "hitchy://fixed/{id}",
    text: { en: "Tomorrow's seat price was adjusted", ar: "سعر الكرسي بكرة اتعدل" },
  },
  fixed_day_conflict: {
    channel: "account",
    deepLink: "hitchy://fixed/{id}",
    text: {
      en: "Tomorrow's Fixed Ride was skipped (time conflict)",
      ar: "مشوار بكرة الثابت اتلغى بسبب تعارض",
    },
  },
  fixed_no_credits: {
    channel: "account",
    deepLink: "hitchy://packages",
    text: { en: "Tomorrow's Fixed Ride needs credits", ar: "مشوار بكرة الثابت محتاج رصيد" },
  },
  fixed_skipped_unpaid_fee: {
    channel: "account",
    deepLink: "hitchy://fees",
    text: {
      en: "Pay your fee to keep your Fixed Ride seat",
      ar: "ادفع الغرامة عشان تحتفظ بكرسيك",
    },
  },
  backup_options: {
    channel: "bookings",
    deepLink: "hitchy://backup/{rideId}",
    text: { en: "We found other rides for you", ar: "لقينالك مشاوير تانية" },
  },
  sos: {
    channel: "admin",
    deepLink: "{admin}/sos",
    text: { en: "SOS from a ride", ar: "طلب استغاثة من مشوار" },
  },
  new_report: {
    channel: "admin",
    deepLink: "{admin}/reports",
    text: { en: "New user report", ar: "فيه بلاغ جديد" },
  },
} as const satisfies Record<string, Template>;

export type NotificationType = keyof typeof TEMPLATES;

export type TemplateParams = Partial<
  Record<"name" | "seats" | "seatWord" | "rideId" | "riderUid" | "id" | "admin", string | number>
>;

// "1 seat" / "2 seats"; Arabic: كرسي، كرسيين، 3 كراسي، 11 كرسي.
export function seatWord(seats: number, language: Language): string {
  if (language === "en") return seats === 1 ? "1 seat" : `${String(seats)} seats`;
  if (seats === 1) return "كرسي";
  if (seats === 2) return "كرسيين";
  return seats <= 10 ? `${String(seats)} كراسي` : `${String(seats)} كرسي`;
}

// Replaces {placeholders}; a missing value is a programming error.
export function fill(text: string, params: TemplateParams): string {
  return text.replace(/\{(\w+)\}/g, (_match, key: string) => {
    const value = params[key as keyof TemplateParams];
    if (value === undefined) throw new Error(`Missing {${key}} for a notification`);
    return String(value);
  });
}

export interface RenderedText {
  title: LocalizedName;
  body: LocalizedName;
  deepLink: string;
  channel: Channel;
}

export function render(
  type: NotificationType,
  params: TemplateParams,
  adminUrl: string,
): RenderedText {
  const template: Template = TEMPLATES[type];
  const seats = Number(params.seats);
  const forLanguage = (language: Language): TemplateParams =>
    Number.isFinite(seats) ? { ...params, seatWord: seatWord(seats, language) } : params;
  const en = forLanguage("en");
  const ar = forLanguage("ar");
  return {
    title: { en: fill(template.text.en, en), ar: fill(template.text.ar, ar) },
    body: template.body
      ? { en: fill(template.body.en, en), ar: fill(template.body.ar, ar) }
      : { en: "", ar: "" },
    deepLink: fill(template.deepLink, { ...params, admin: adminUrl }),
    channel: template.channel,
  };
}

export function textFor(rendered: RenderedText, language: Language): string {
  return rendered.title[language];
}

export function bodyFor(rendered: RenderedText, language: Language): string {
  return rendered.body[language];
}
