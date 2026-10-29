// Cairo time helpers (BUILD_PLAN 5.1). Egypt uses summer time: never hard-code +2 or +3.
import { DateTime } from "luxon";
import type { DateKey, HHmm } from "../schemas/docs.js";

export const CAIRO = "Africa/Cairo";

// Minutes since midnight from "HH:mm", 0 … 1439. Throws if invalid or out of range.
export function hhmmToMinutes(time: HHmm): number {
  const match = /^(\d{2}):(\d{2})$/.exec(time);
  if (!match) throw new Error(`Invalid time format, expected "HH:mm": ${time}`);
  const hours = Number(match[1]);
  const minutes = Number(match[2]);
  if (hours < 0 || hours >= 24 || minutes < 0 || minutes >= 60) {
    throw new RangeError(`Time out of range: ${time}`);
  }
  return hours * 60 + minutes;
}

export function toCairo(date: Date): DateTime {
  return DateTime.fromJSDate(date, { zone: CAIRO });
}

// The Cairo calendar date, "YYYY-MM-DD".
export function dateKey(date: Date): DateKey {
  return toCairo(date).toFormat("yyyy-MM-dd");
}

// Minutes since Cairo midnight, 0 … 1439.
export function minuteOfDay(date: Date): number {
  const t = toCairo(date);
  return t.hour * 60 + t.minute;
}

// The instant of "HH:mm" on a Cairo date. A time skipped by the summer time change
// moves forward by the skipped hour (luxon's rule).
export function cairoDateTime(key: DateKey, time: HHmm): Date {
  const t = DateTime.fromFormat(`${key} ${time}`, "yyyy-MM-dd HH:mm", { zone: CAIRO });
  if (!t.isValid) throw new Error(`Not a Cairo date and time: ${key} ${time}`);
  return t.toJSDate();
}

// The first instant of the Cairo day (01:00 on a day whose midnight is skipped).
export function startOfCairoDay(date: Date): Date {
  return toCairo(date).startOf("day").toJSDate();
}

// The first instant of the Cairo week. Weeks start on Sunday.
export function startOfCairoWeek(date: Date): Date {
  const t = toCairo(date);
  return t
    .minus({ days: t.weekday % 7 })
    .startOf("day")
    .toJSDate();
}
