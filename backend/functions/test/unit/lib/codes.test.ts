import { describe, expect, it } from "vitest";
import { CODE_ALPHABET, CODE_LENGTH, formatCode, randomCode } from "../../../src/lib/codes.js";

describe("short codes", () => {
  it("uses 32 characters with no look-alikes", () => {
    expect(new Set(CODE_ALPHABET).size).toBe(32);
    for (const c of ["0", "O", "1", "I"]) expect(CODE_ALPHABET).not.toContain(c);
  });

  it("makes 6-character codes from the alphabet only", () => {
    for (let i = 0; i < 2000; i++) {
      const code = randomCode();
      expect(code).toHaveLength(CODE_LENGTH);
      for (const c of code) expect(CODE_ALPHABET).toContain(c);
    }
  });

  it("prefixes package references with HT- and nothing else", () => {
    expect(formatCode("reference", "7K3QX9")).toBe("HT-7K3QX9");
    expect(formatCode("invite", "7K3QX9")).toBe("7K3QX9");
    expect(formatCode("referral", "7K3QX9")).toBe("7K3QX9");
  });
});
