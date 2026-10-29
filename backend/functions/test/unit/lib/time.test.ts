import { DateTime } from "luxon";
import { describe, expect, it } from "vitest";
import {
  CAIRO,
  cairoDateTime,
  dateKey,
  hhmmToMinutes,
  minuteOfDay,
  startOfCairoDay,
  startOfCairoWeek,
} from "../../../src/lib/time.js";

const MIN = 60_000;
const HOUR = 60 * MIN;
const DAY = 24 * HOUR;

interface OffsetChange {
  at: Date; // the first instant with the new offset
  from: number; // minutes east of UTC
  to: number;
}

// Finds when Cairo's UTC offset changes in a year, from luxon's time zone data,
// so the summer time dates are never hard-coded.
function offsetChanges(year: number): OffsetChange[] {
  const offsetAt = (ms: number) => DateTime.fromMillis(ms, { zone: CAIRO }).offset;
  const found: OffsetChange[] = [];
  const end = Date.UTC(year + 1, 0, 1);
  for (let t = Date.UTC(year, 0, 1); t < end; t += HOUR) {
    if (offsetAt(t) === offsetAt(t + HOUR)) continue;
    let at = t;
    while (offsetAt(at) === offsetAt(t)) at += MIN;
    found.push({ at: new Date(at), from: offsetAt(t), to: offsetAt(at) });
  }
  return found;
}

const changes = offsetChanges(2026);

function change(kind: "start" | "end"): number {
  const c = changes.find((x) => (kind === "start" ? x.to > x.from : x.to < x.from));
  if (!c) throw new Error(`No summer time ${kind} found in 2026`);
  return c.at.getTime();
}

const at = (ms: number) => new Date(ms);

describe("Cairo time", () => {
  it("finds Egypt's two summer time changes in 2026", () => {
    expect(changes).toHaveLength(2);
  });

  it("reads a plain winter instant", () => {
    const t = new Date("2026-11-02T05:30:00Z");
    expect(dateKey(t)).toBe("2026-11-02");
    expect(minuteOfDay(t)).toBe(7 * 60 + 30);
  });

  describe("when summer time starts (midnight jumps to 01:00)", () => {
    const t = change("start");

    it("dateKey moves to the new day at the jump", () => {
      expect(dateKey(at(t - 1))).not.toBe(dateKey(at(t)));
    });

    it("minuteOfDay goes from 23:59 to 01:00", () => {
      expect(minuteOfDay(at(t - MIN))).toBe(23 * 60 + 59);
      expect(minuteOfDay(at(t))).toBe(60);
    });

    it("the day starts at 01:00", () => {
      expect(startOfCairoDay(at(t + 11 * HOUR))).toEqual(at(t));
    });

    it("a skipped time moves forward by the skipped hour", () => {
      expect(cairoDateTime(dateKey(at(t)), "00:30")).toEqual(at(t + 30 * MIN));
      expect(cairoDateTime(dateKey(at(t)), "01:30")).toEqual(at(t + 30 * MIN));
    });

    it("that week is one hour short", () => {
      const week = startOfCairoWeek(at(t));
      const next = startOfCairoWeek(at(week.getTime() + 8 * DAY));
      expect(next.getTime() - week.getTime()).toBe(7 * DAY - HOUR);
    });
  });

  describe("when summer time ends (midnight goes back to 23:00)", () => {
    const t = change("end");

    it("dateKey stays on the same day through the repeated hour", () => {
      expect(dateKey(at(t - 1))).toBe(dateKey(at(t)));
    });

    it("minuteOfDay goes from 23:59 back to 23:00", () => {
      expect(minuteOfDay(at(t - MIN))).toBe(23 * 60 + 59);
      expect(minuteOfDay(at(t))).toBe(23 * 60);
    });

    it("the next day starts an hour later", () => {
      expect(dateKey(at(t + HOUR))).not.toBe(dateKey(at(t)));
      expect(minuteOfDay(at(t + HOUR))).toBe(0);
      expect(startOfCairoDay(at(t + 2 * HOUR))).toEqual(at(t + HOUR));
    });

    it("that week is one hour long", () => {
      const week = startOfCairoWeek(at(t));
      const next = startOfCairoWeek(at(week.getTime() + 8 * DAY));
      expect(next.getTime() - week.getTime()).toBe(7 * DAY + HOUR);
    });
  });

  it("starts every week of 2026 on Sunday at midnight", () => {
    for (let day = Date.UTC(2026, 0, 1, 12); day < Date.UTC(2027, 0, 1); day += DAY) {
      const start = startOfCairoWeek(at(day));
      expect(DateTime.fromJSDate(start, { zone: CAIRO }).weekday).toBe(7);
      expect(minuteOfDay(start)).toBe(0);
      expect(start.getTime()).toBeLessThanOrEqual(day);
      expect(day - start.getTime()).toBeLessThan(7 * DAY + HOUR);
    }
  });

  describe("hhmmToMinutes", () => {
    it("converts 00:00 to 0", () => {
      expect(hhmmToMinutes("00:00")).toBe(0);
    });

    it("converts 23:59 to 1439", () => {
      expect(hhmmToMinutes("23:59")).toBe(1439);
    });

    it("converts 07:30 to 450", () => {
      expect(hhmmToMinutes("07:30")).toBe(450);
    });

    it("throws on 24:00", () => {
      expect(() => hhmmToMinutes("24:00")).toThrow(RangeError);
    });

    it("throws on invalid format or values", () => {
      expect(() => hhmmToMinutes("12:60")).toThrow(RangeError);
      expect(() => hhmmToMinutes("-1:00")).toThrow(Error);
      expect(() => hhmmToMinutes("invalid")).toThrow(Error);
      expect(() => hhmmToMinutes("7:30")).toThrow(Error);
    });
  });
});
