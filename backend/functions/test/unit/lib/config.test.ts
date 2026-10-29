import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { resetClock, setClock } from "../../../src/lib/clock.js";
import { createConfigCache } from "../../../src/lib/config.js";
import type { ConfigName } from "../../../src/schemas/config.js";

describe("config cache", () => {
  let time = 0;
  let loads: ConfigName[] = [];
  const load = (name: ConfigName) => {
    loads.push(name);
    return Promise.resolve({ version: loads.length });
  };

  beforeEach(() => {
    time = 0;
    loads = [];
    setClock(() => new Date(time));
  });
  afterEach(() => {
    resetClock();
  });

  it("reads a config once within 60 seconds", async () => {
    const cache = createConfigCache(load);
    await cache.get("limits");
    time = 59_999;
    const limits = await cache.get("limits");
    expect(loads).toEqual(["limits"]);
    expect(limits.version).toBe(1);
  });

  it("reads it again once 60 seconds have passed", async () => {
    const cache = createConfigCache(load);
    await cache.get("limits");
    time = 60_000;
    const limits = await cache.get("limits");
    expect(loads).toEqual(["limits", "limits"]);
    expect(limits.version).toBe(2);
  });

  it("keeps each config separately", async () => {
    const cache = createConfigCache(load);
    await cache.get("limits");
    await cache.get("pricing");
    await cache.get("limits");
    expect(loads).toEqual(["limits", "pricing"]);
  });

  it("clear() forgets everything", async () => {
    const cache = createConfigCache(load);
    await cache.get("limits");
    cache.clear();
    await cache.get("limits");
    expect(loads).toEqual(["limits", "limits"]);
  });
});
