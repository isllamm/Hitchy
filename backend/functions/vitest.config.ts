import { defineConfig } from "vitest/config";

export default defineConfig({
  test: {
    environment: "node",
    projects: [
      {
        extends: true,
        test: {
          name: "unit",
          include: ["test/unit/**/*.test.ts"],
        },
      },
      {
        // Integration tests share the emulator database, so their files run one at a time.
        extends: true,
        test: {
          name: "integration",
          include: ["test/integration/**/*.test.ts"],
          fileParallelism: false,
        },
      },
      {
        // Rules tests share one emulator and clear it between tests, so their files run one at a time.
        extends: true,
        test: {
          name: "rules",
          include: ["test/rules/**/*.test.ts"],
          fileParallelism: false,
        },
      },
    ],
  },
});
