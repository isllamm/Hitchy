// Notification types, channels, deep links and text (BUILD_PLAN 8.15, v1.1).
// Placeholders: {name} (the other person's first name), {seats}, {rideId}, {riderUid}, {id},
// {admin} (ADMIN_URL). Text riders read says "owner" [DECISION-20]. Review the Arabic before release.
import type { Language, LocalizedName } from "../schemas/docs.js";

export type Channel = "bookings" | "trips" | "account" | "admin";

export interface Template {
  channel: Channel;
  deepLink: string;
  text: LocalizedName;
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
    text: { en: "You're approved. Your first month is free!", ar: "اتوافق عليك! أول شهر ببلاش" },
  },
  host_rejected: {
    channel: "account",
    deepLink: "hitchy://host/status",
    text: { en: "Some documents need fixing", ar: "فيه ورق محتاج يتعدل" },
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
  },
  verify_request: {
    channel: "admin",
    deepLink: "{admin}/hosts",
    text: { en: "A host asked for Verified", ar: "صاحب عربية طالب علامة Verified" },
  },
  booking_requested: {
    channel: "bookings",
    deepLink: "hitchy://ride/{rideId}",
    text: { en: "{name} asked for {seats} seat(s)", ar: "{name} طلب يحجز {seats} كرسي" },
  },
  booking_accepted: {
    channel: "bookings",
    deepLink: "hitchy://trip/{rideId}",
    text: {
      en: "{name} accepted your request. Your seat is confirmed",
      ar: "{name} وافق على طلبك، كرسيك اتأكد",
    },
  },
  booking_declined: {
    channel: "bookings",
    deepLink: "hitchy://search",
    text: { en: "{name} couldn't take your request", ar: "{name} مقدرش ياخد طلبك" },
  },
  request_expired: {
    channel: "bookings",
    deepLink: "hitchy://search",
    text: { en: "Your request wasn't answered in time", ar: "طلبك متردش عليه في الوقت" },
  },
  booking_cancelled: {
    channel: "bookings",
    deepLink: "hitchy://ride/{rideId}",
    text: { en: "{name} cancelled their booking", ar: "{name} لغى الحجز" },
  },
  ride_cancelled: {
    channel: "bookings",
    deepLink: "hitchy://trips",
    text: { en: "Your ride was cancelled", ar: "مشوارك اتلغى" },
  },
  ride_reminder: {
    channel: "trips",
    deepLink: "hitchy://trip/{rideId}",
    text: { en: "Your ride leaves in 30 minutes", ar: "مشوارك هيتحرك كمان نص ساعة" },
  },
  ride_started: {
    channel: "trips",
    deepLink: "hitchy://trip/{rideId}",
    text: { en: "Your owner is on the way", ar: "صاحب العربية في الطريق" },
  },
  rider_at_pickup: {
    channel: "trips",
    deepLink: "hitchy://trip/{rideId}",
    text: { en: "{name} is at the pickup point", ar: "{name} وصل مكان الركوب" },
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
  Record<"name" | "seats" | "rideId" | "riderUid" | "id" | "admin", string | number>
>;

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
  deepLink: string;
  channel: Channel;
}

export function render(
  type: NotificationType,
  params: TemplateParams,
  adminUrl: string,
): RenderedText {
  const template: Template = TEMPLATES[type];
  return {
    title: { en: fill(template.text.en, params), ar: fill(template.text.ar, params) },
    deepLink: fill(template.deepLink, { ...params, admin: adminUrl }),
    channel: template.channel,
  };
}

export function textFor(rendered: RenderedText, language: Language): string {
  return rendered.title[language];
}
