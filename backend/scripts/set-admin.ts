#!/usr/bin/env node
import {
  setAdmin,
  type SetAdminOptions,
} from "../functions/src/scripts/setAdmin.js";

function printUsage(): void {
  // eslint-disable-next-line no-console
  console.log(`Usage: npm run set-admin -- --uid <uid> [--remove] [--project <dev|prod>] [--confirm]

Options:
  --uid <uid>          User ID to grant or revoke admin access (required)
  --remove             Revoke admin claim and remove from config/admins.uids
  --project <project>  Target project: demo-hitchy (default), dev, or prod
  --confirm            Required when modifying admin status on prod
  --help, -h           Show this help message`);
}

function parseArgs(): SetAdminOptions {
  const args = process.argv.slice(2);
  let uid = "";
  let project: string | undefined;
  let remove = false;
  let confirm = false;

  for (let i = 0; i < args.length; i++) {
    const arg = args[i];
    if (arg === "--help" || arg === "-h") {
      printUsage();
      process.exit(0);
    } else if (arg === "--uid" && i + 1 < args.length) {
      uid = args[i + 1] ?? "";
      i++;
    } else if (arg?.startsWith("--uid=")) {
      uid = arg.slice("--uid=".length);
    } else if (arg === "--project" && i + 1 < args.length) {
      project = args[i + 1];
      i++;
    } else if (arg?.startsWith("--project=")) {
      project = arg.slice("--project=".length);
    } else if (arg === "--remove") {
      remove = true;
    } else if (arg === "--confirm") {
      confirm = true;
    }
  }

  if (!uid.trim()) {
    // eslint-disable-next-line no-console
    console.error("Error: Missing required argument: --uid <uid>\n");
    printUsage();
    process.exit(1);
  }

  return { uid, project, remove, confirm };
}

async function main(): Promise<void> {
  const opts = parseArgs();
  try {
    await setAdmin(opts);
    process.exit(0);
  } catch (err) {
    // eslint-disable-next-line no-console
    console.error(
      "set-admin failed:",
      err instanceof Error ? err.message : err,
    );
    process.exit(1);
  }
}

void main();
