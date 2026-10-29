import { describe, expect, it } from "vitest";
import {
  fill,
  type NotificationType,
  render,
  TEMPLATES,
} from "../../../src/lib/notifyTemplates.js";

const SAMPLE = { name: "Omar", seats: 2, rideId: "r1", riderUid: "u2", id: "f1" };
const ADMIN = "https://admin.test";
const types = Object.keys(TEMPLATES) as NotificationType[];

describe("notification templates", () => {
  it.each(types)("%s renders in both languages with no placeholder left", (type) => {
    const r = render(type, SAMPLE, ADMIN);
    for (const text of [r.title.en, r.title.ar, r.deepLink]) expect(text).not.toMatch(/[{}]/);
  });

  it.each(types)("%s links into the app, or the dashboard for admin alerts", (type) => {
    const r = render(type, SAMPLE, ADMIN);
    const prefix = r.channel === "admin" ? `${ADMIN}/` : "hitchy://";
    expect(r.deepLink.startsWith(prefix)).toBe(true);
  });

  it("never says driver", () => {
    for (const type of types) expect(TEMPLATES[type].text.en).not.toMatch(/driver/i);
  });

  it("fails loudly on a missing placeholder", () => {
    expect(() => fill("{name} cancelled their booking", {})).toThrow("{name}");
  });
});
