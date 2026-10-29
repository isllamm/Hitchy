// Typed access to config/{name} with a 60-second cache (BUILD_PLAN 7). Business numbers live
// here, never in code; updateConfig changes them and bumps "version".
import type { ConfigDocs, ConfigName } from "../schemas/config.js";
import { db } from "./app.js";
import { now } from "./clock.js";

const TTL_MS = 60_000;

export interface ConfigCache {
  get<N extends ConfigName>(name: N): Promise<ConfigDocs[N]>;
  clear(): void;
}

// A cache over any loader, so tests can give it a fake one.
export function createConfigCache(
  load: (name: ConfigName) => Promise<unknown>,
  ttlMs = TTL_MS,
): ConfigCache {
  const entries = new Map<ConfigName, { value: unknown; loadedAt: number }>();
  return {
    async get<N extends ConfigName>(name: N): Promise<ConfigDocs[N]> {
      const t = now().getTime();
      const hit = entries.get(name);
      if (hit && t - hit.loadedAt < ttlMs) return hit.value as ConfigDocs[N];
      const value = await load(name);
      entries.set(name, { value, loadedAt: t });
      return value as ConfigDocs[N];
    },
    clear() {
      entries.clear();
    },
  };
}

async function loadFromFirestore(name: ConfigName): Promise<unknown> {
  const snap = await db.collection("config").doc(name).get();
  if (!snap.exists) throw new Error(`config/${name} is missing`);
  return snap.data();
}

const cache = createConfigCache(loadFromFirestore);

export function getConfig<N extends ConfigName>(name: N): Promise<ConfigDocs[N]> {
  return cache.get(name);
}

export function clearConfigCache(): void {
  cache.clear();
}
