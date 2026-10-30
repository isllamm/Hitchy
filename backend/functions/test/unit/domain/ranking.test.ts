// domain/ranking.ts (BUILD_PLAN 8.6): each factor moves the score the way the plan says.
import { describe, expect, it } from "vitest";
import {
  ratingScore,
  rankScore,
  reliability,
  TIER_BONUS,
  timeFit,
  TIME_FIT_WINDOW_MINUTES,
  WEIGHTS,
  type RankInput,
} from "../../../src/domain/ranking.js";

const PICKED = new Date("2026-11-02T06:30:00Z").getTime();
const MINUTE = 60000;

function base(overrides: Partial<RankInput> = {}): RankInput {
  return {
    pickupAtMs: PICKED,
    pickedTimeMs: PICKED,
    sameCompany: false,
    ratingAvg: null,
    ratingCount: 0,
    verified: false,
    tier: "bronze",
    lateCancels: 0,
    hostLateCount: 0,
    tripsCompleted: 0,
    ...overrides,
  };
}

describe("timeFit", () => {
  it("is 1 at the picked time and 0 two hours away", () => {
    expect(timeFit(PICKED, PICKED)).toBe(1);
    expect(timeFit(PICKED + TIME_FIT_WINDOW_MINUTES * MINUTE, PICKED)).toBe(0);
    expect(timeFit(PICKED - TIME_FIT_WINDOW_MINUTES * MINUTE, PICKED)).toBe(0);
  });

  it("never goes below 0 and treats early and late the same", () => {
    expect(timeFit(PICKED + 300 * MINUTE, PICKED)).toBe(0);
    expect(timeFit(PICKED + 30 * MINUTE, PICKED)).toBeCloseTo(0.75);
    expect(timeFit(PICKED - 30 * MINUTE, PICKED)).toBeCloseTo(0.75);
  });
});

describe("ratingScore", () => {
  it("scores an owner with fewer than 3 ratings in the middle", () => {
    expect(ratingScore(5, 2)).toBe(0.5);
    expect(ratingScore(null, 0)).toBe(0.5);
  });

  it("maps 3 to 5 stars onto 0 to 1 and clamps outside it", () => {
    expect(ratingScore(3, 10)).toBe(0);
    expect(ratingScore(4, 10)).toBe(0.5);
    expect(ratingScore(5, 10)).toBe(1);
    expect(ratingScore(2, 10)).toBe(0);
  });
});

describe("reliability", () => {
  it("is 1 for a clean record and 0 once the let-downs reach the floor of 5", () => {
    expect(reliability({ lateCancels: 0, hostLateCount: 0, tripsCompleted: 0 })).toBe(1);
    expect(reliability({ lateCancels: 5, hostLateCount: 0, tripsCompleted: 0 })).toBe(0);
    expect(reliability({ lateCancels: 3, hostLateCount: 2, tripsCompleted: 2 })).toBe(0);
  });

  it("counts let-downs against the trips done, with a floor of 5 trips", () => {
    expect(reliability({ lateCancels: 1, hostLateCount: 0, tripsCompleted: 0 })).toBeCloseTo(0.8);
    expect(reliability({ lateCancels: 2, hostLateCount: 0, tripsCompleted: 20 })).toBeCloseTo(0.9);
  });
});

describe("rankScore", () => {
  it("adds every factor with the weights of 8.6", () => {
    const best = rankScore(
      base({
        sameCompany: true,
        ratingAvg: 5,
        ratingCount: 10,
        verified: true,
        tier: "gold",
      }),
    );
    expect(best).toBe(
      WEIGHTS.timeFit +
        WEIGHTS.sameCompany +
        WEIGHTS.rating +
        WEIGHTS.verified +
        TIER_BONUS.gold +
        WEIGHTS.reliability,
    );
  });

  it("ranks a closer pickup above a far one, all else equal", () => {
    const near = rankScore(base({ pickupAtMs: PICKED + 10 * MINUTE }));
    const far = rankScore(base({ pickupAtMs: PICKED + 100 * MINUTE }));
    expect(near).toBeGreaterThan(far);
  });

  it("ranks the same company, Verified, a higher tier and a better rating above the rest", () => {
    const plain = rankScore(base());
    expect(rankScore(base({ sameCompany: true }))).toBeGreaterThan(plain);
    expect(rankScore(base({ verified: true }))).toBeGreaterThan(plain);
    expect(rankScore(base({ tier: "silver" }))).toBeGreaterThan(plain);
    expect(rankScore(base({ tier: "gold" }))).toBeGreaterThan(rankScore(base({ tier: "silver" })));
    expect(rankScore(base({ ratingAvg: 5, ratingCount: 10 }))).toBeGreaterThan(plain);
  });

  it("ranks an owner who lets riders down below one who does not", () => {
    const reliable = rankScore(base({ tripsCompleted: 20 }));
    const unreliable = rankScore(base({ tripsCompleted: 20, lateCancels: 4, hostLateCount: 4 }));
    expect(reliable).toBeGreaterThan(unreliable);
  });

  it("weighs the time fit above every other single factor", () => {
    const onTimeOnly = rankScore(base());
    const everythingButTime = rankScore(
      base({
        pickupAtMs: PICKED + TIME_FIT_WINDOW_MINUTES * MINUTE,
        sameCompany: true,
        verified: true,
      }),
    );
    // 40 for a perfect time fit beats 20 + 10 for company and Verified.
    expect(onTimeOnly).toBeGreaterThan(everythingButTime - WEIGHTS.rating);
  });
});
