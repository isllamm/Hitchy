// P4-B02: switchRole (BUILD_PLAN 8.3).
import { Timestamp } from "firebase-admin/firestore";
import { db } from "../../lib/app.js";
import { makeCallable } from "../../lib/callable.js";
import { fail } from "../../lib/errors.js";
import type { CallCtx } from "../../lib/ctx.js";
import type { UserDoc } from "../../schemas/docs.js";
import { SwitchRoleInput } from "../../schemas/inputs.js";
import type { SwitchRoleOutput } from "../../schemas/io.js";

export async function switchRoleCore(
  input: SwitchRoleInput,
  ctx: CallCtx,
): Promise<SwitchRoleOutput> {
  return await db.runTransaction(async (tx) => {
    const userRef = db.collection("users").doc(ctx.uid);
    const snap = await tx.get(userRef);
    if (!snap.exists) {
      fail("PROFILE_REQUIRED");
    }

    const user = snap.data() as UserDoc;
    if (input.role === "host" && user.host.status !== "approved") {
      fail("HOST_NOT_APPROVED");
    }

    tx.update(userRef, {
      activeRole: input.role,
      updatedAt: Timestamp.fromDate(ctx.now),
    });

    return {
      activeRole: input.role,
    };
  });
}

export const switchRole = makeCallable("switchRole", SwitchRoleInput, switchRoleCore);
