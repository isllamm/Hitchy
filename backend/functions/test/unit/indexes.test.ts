import { readFileSync } from "node:fs";
import { describe, expect, it } from "vitest";

interface IndexField {
  fieldPath: string;
  order?: "ASCENDING" | "DESCENDING";
  arrayConfig?: "CONTAINS";
}

interface CompositeIndex {
  collectionGroup: string;
  queryScope: "COLLECTION" | "COLLECTION_GROUP";
  fields: IndexField[];
}

interface FieldOverrideIndex {
  order?: "ASCENDING" | "DESCENDING";
  arrayConfig?: "CONTAINS";
  queryScope: "COLLECTION" | "COLLECTION_GROUP";
}

interface FieldOverride {
  collectionGroup: string;
  fieldPath: string;
  indexes: FieldOverrideIndex[];
}

interface FirestoreIndexesDoc {
  indexes: CompositeIndex[];
  fieldOverrides?: FieldOverride[];
}

function loadIndexes(): FirestoreIndexesDoc {
  const filePath = new URL("../../../firestore.indexes.json", import.meta.url);
  const raw = readFileSync(filePath, "utf8");
  return JSON.parse(raw) as FirestoreIndexesDoc;
}

describe("backend/firestore.indexes.json", () => {
  it("loads valid JSON with indexes and fieldOverrides arrays", () => {
    const doc = loadIndexes();
    expect(Array.isArray(doc.indexes)).toBe(true);
    expect(Array.isArray(doc.fieldOverrides)).toBe(true);
  });

  it("every composite index has valid collectionGroup, queryScope, and at least 2 fields", () => {
    const { indexes } = loadIndexes();
    expect(indexes.length).toBeGreaterThan(0);

    for (const index of indexes) {
      expect(typeof index.collectionGroup).toBe("string");
      expect(index.collectionGroup.trim().length).toBeGreaterThan(0);
      expect(["COLLECTION", "COLLECTION_GROUP"]).toContain(index.queryScope);
      expect(Array.isArray(index.fields)).toBe(true);
      expect(index.fields.length).toBeGreaterThanOrEqual(2);
    }
  });

  it("every index field has fieldPath and either order (ASCENDING/DESCENDING) or arrayConfig (CONTAINS)", () => {
    const { indexes } = loadIndexes();

    for (const index of indexes) {
      for (const field of index.fields) {
        expect(typeof field.fieldPath).toBe("string");
        expect(field.fieldPath.trim().length).toBeGreaterThan(0);

        const hasOrder = field.order === "ASCENDING" || field.order === "DESCENDING";
        const hasArrayConfig = field.arrayConfig === "CONTAINS";
        expect(hasOrder || hasArrayConfig).toBe(true);
      }
    }
  });

  it("has no duplicate composite indexes", () => {
    const { indexes } = loadIndexes();
    const seen = new Set<string>();

    for (const idx of indexes) {
      const fieldsKey = idx.fields
        .map((f) => `${f.fieldPath}:${f.order ?? f.arrayConfig ?? ""}`)
        .join(",");
      const key = `${idx.collectionGroup}|${idx.queryScope}|${fieldsKey}`;

      expect(seen.has(key), `Duplicate index detected: ${key}`).toBe(false);
      seen.add(key);
    }
  });

  it("every fieldOverride has collectionGroup, fieldPath, and valid index overrides", () => {
    const { fieldOverrides = [] } = loadIndexes();
    expect(fieldOverrides.length).toBeGreaterThan(0);

    for (const override of fieldOverrides) {
      expect(typeof override.collectionGroup).toBe("string");
      expect(override.collectionGroup.trim().length).toBeGreaterThan(0);
      expect(typeof override.fieldPath).toBe("string");
      expect(override.fieldPath.trim().length).toBeGreaterThan(0);
      expect(Array.isArray(override.indexes)).toBe(true);
      expect(override.indexes.length).toBeGreaterThan(0);

      for (const entry of override.indexes) {
        expect(["COLLECTION", "COLLECTION_GROUP"]).toContain(entry.queryScope);
        const hasOrder = entry.order === "ASCENDING" || entry.order === "DESCENDING";
        const hasArrayConfig = entry.arrayConfig === "CONTAINS";
        expect(hasOrder || hasArrayConfig).toBe(true);
      }
    }
  });

  it("matches all 19 indexes and 2 field overrides defined in BUILD_PLAN section 10 and 8.16", () => {
    const doc = loadIndexes();
    expect(doc.indexes).toHaveLength(19);
    expect(doc.fieldOverrides).toHaveLength(2);

    // Verify presence of rides indexes (4 from section 10, 1 for the v1.2 pickup search)
    const ridesIndexes = doc.indexes.filter((i) => i.collectionGroup === "rides");
    expect(ridesIndexes).toHaveLength(5);

    // Verify presence of bookings collection group indexes
    const bookingsIndexes = doc.indexes.filter((i) => i.collectionGroup === "bookings");
    expect(bookingsIndexes).toHaveLength(4);
    for (const b of bookingsIndexes) {
      expect(b.queryScope).toBe("COLLECTION_GROUP");
    }

    // Verify points indexes (both COLLECTION and COLLECTION_GROUP for users/{uid}/points)
    const pointsIndexes = doc.indexes.filter((i) => i.collectionGroup === "points");
    expect(pointsIndexes).toHaveLength(2);
    expect(pointsIndexes.map((p) => p.queryScope).sort()).toEqual([
      "COLLECTION",
      "COLLECTION_GROUP",
    ]);

    // Verify overrides
    const overrideGroups = doc.fieldOverrides?.map((o) => o.collectionGroup).sort();
    expect(overrideGroups).toEqual(["bookings", "vehicles"]);
  });

  it("has the v1.2 search index on pickupZones (BUILD_PLAN 8.16)", () => {
    const { indexes } = loadIndexes();

    const search = indexes.find(
      (i) =>
        i.collectionGroup === "rides" &&
        i.queryScope === "COLLECTION" &&
        i.fields[0]?.fieldPath === "pickupZones",
    );

    expect(search, "Missing the rides index on pickupZones").toBeDefined();
    expect(search?.fields[0]?.arrayConfig).toBe("CONTAINS");
    expect(search?.fields.map((f) => f.fieldPath)).toEqual([
      "pickupZones",
      "toZone",
      "status",
      "departAt",
    ]);
  });
});
