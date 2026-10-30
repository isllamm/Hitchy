// topUpWallet (BUILD_PLAN 8.16, v1.2): an admin adds balance to a wallet [DECISION-32].
// Paying in through a payment provider and owner withdrawals are [LATER] (R19).
import { db } from "../../lib/app.js";
import { logAdminAction } from "../../lib/audit.js";
import { makeCallable } from "../../lib/callable.js";
import type { CallCtx } from "../../lib/ctx.js";
import { fail } from "../../lib/errors.js";
import { withIdempotency } from "../../lib/idempotency.js";
import { addToWallet, getBalance } from "../../repos/wallets.js";
import { TopUpWalletInput } from "../../schemas/inputs.js";
import type { TopUpWalletOutput } from "../../schemas/io.js";

export async function topUpWalletCore(
  input: TopUpWalletInput,
  ctx: CallCtx,
): Promise<TopUpWalletOutput> {
  return withIdempotency(ctx.uid, input.requestId, async (tx) => {
    const user = await tx.get(db.collection("users").doc(input.uid));
    if (!user.exists) fail("NOT_FOUND");

    const before = await getBalance(input.uid, tx);
    const balance = addToWallet(
      tx,
      input.uid,
      input.amount,
      { reason: "top_up", refId: input.requestId, by: ctx.uid },
      before,
      ctx.now,
    );

    await logAdminAction(
      {
        adminUid: ctx.uid,
        action: "top_up_wallet",
        targetUid: input.uid,
        refId: input.requestId,
        note: `+${String(input.amount)} EGP`,
      },
      tx,
    );

    return { balance };
  });
}

export const topUpWallet = makeCallable("topUpWallet", TopUpWalletInput, topUpWalletCore, {
  admin: true,
});
