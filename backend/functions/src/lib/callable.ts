// makeCallable(): one way to build every callable (BUILD_PLAN 8.1).
// Usage: export const requestSeats = makeCallable("requestSeats", RequestSeatsInput, requestSeatsCore);
import { onCall } from "firebase-functions/https";
import * as logger from "firebase-functions/logger";
import type { z } from "zod";
import { REGION } from "./app.js";
import { buildCtx, type CallCtx } from "./ctx.js";
import { AppFailure, toHttps } from "./errors.js";

export interface CallableOptions {
  admin?: boolean;
  profile?: boolean;
  allowSuspended?: boolean;
}

export function makeCallable<S extends z.ZodType, O>(
  name: string,
  schema: S,
  core: (input: z.infer<S>, ctx: CallCtx) => Promise<O>,
  opts: CallableOptions = {},
) {
  const admin = opts.admin ?? false;
  const ctxOptions = {
    admin,
    profile: admin ? false : (opts.profile ?? true),
    allowSuspended: admin || (opts.allowSuspended ?? false),
  };

  // App Check is enforced inside buildCtx from config/limits, so it can change without a deploy.
  return onCall(
    { region: REGION, memory: "256MiB", timeoutSeconds: 30, maxInstances: 10, enforceAppCheck: false },
    async (req) => {
      const uid = req.auth?.uid ?? null;
      try {
        const ctx = await buildCtx(req, ctxOptions);
        const parsed = schema.safeParse(req.data);
        if (!parsed.success) {
          const paths = parsed.error.issues.map((issue) => issue.path.map(String).join("."));
          throw new AppFailure("INVALID_INPUT", paths);
        }
        const out = await core(parsed.data, ctx);
        logger.info(`${name}.ok`, { uid });
        return out;
      } catch (e) {
        if (e instanceof AppFailure) {
          logger.warn(`${name}.fail`, { uid, reason: e.reason });
          throw toHttps(e);
        }
        logger.error(`${name}.error`, { uid, error: String(e) });
        throw toHttps(new AppFailure("INTERNAL"));
      }
    },
  );
}
