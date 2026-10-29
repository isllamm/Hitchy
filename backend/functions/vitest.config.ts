import { defineConfig } from "vitest/config";

export default defineConfig({
  test: {
    environment: "node",
    projects: [
      {
        extends: true,
        test: {
          name: "default",
          include: ["test/**/*.test.ts"],
          exclude: ["test/rules/**"],
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
