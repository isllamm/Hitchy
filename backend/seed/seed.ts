#!/usr/bin/env node
import { runSeed } from "../functions/src/seed/seed.js";

function parseArgs(): { project?: string } {
  const args = process.argv.slice(2);
  let project: string | undefined;

  for (let i = 0; i < args.length; i++) {
    if (args[i] === "--project" && i + 1 < args.length) {
      project = args[i + 1];
      i++;
    } else if (args[i]?.startsWith("--project=")) {
      project = args[i].slice("--project=".length);
    }
  }

  return { project };
}

async function main(): Promise<void> {
  const { project } = parseArgs();
  try {
    await runSeed({ project });
    process.exit(0);
  } catch (err) {
    // eslint-disable-next-line no-console
    console.error("Seed failed:", err instanceof Error ? err.message : err);
    process.exit(1);
  }
}

void main();
