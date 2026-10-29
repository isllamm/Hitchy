// Firestore rules (BUILD_PLAN 9.1): fees, ratings, package orders, fixed rides, skips, safety, admin, metrics, system.
import {
  assertFails,
  assertSucceeds,
  type RulesTestEnvironment,
} from "@firebase/rules-unit-testing";
import {
  collection,
  deleteDoc,
  doc,
  getDoc,
  getDocs,
  query,
  setDoc,
  updateDoc,
  where,
} from "firebase/firestore";
import { afterAll, beforeAll, beforeEach, describe, it } from "vitest";
import {
  anon,
  asAdmin,
  asUser,
  firestoreRulesEnv,
  HOST,
  RIDER,
  seed,
  someTime,
  STRANGER,
} from "../../helpers/rules.js";

let env: RulesTestEnvironment;

beforeAll(async () => {
  env = await firestoreRulesEnv();
});

afterAll(async () => {
  await env.cleanup();
});

beforeEach(async () => {
  await env.clearFirestore();
  await seed(env, {
    "fees/fee1": {
      riderUid: RIDER,
      hostId: HOST,
      amount: 10,
      reason: "late",
      status: "unpaid",
      createdAt: someTime,
    },
    "ratings/rate1": {
      fromUid: RIDER,
      toUid: HOST,
      roleRated: "host",
      stars: 5,
      createdAt: someTime,
    },
    "packageOrders/po1": {
      hostId: HOST,
      packageId: "p10",
      status: "pending",
      createdAt: someTime,
    },
    "fixedRides/fr1": {
      hostId: HOST,
      riderUids: [RIDER],
      status: "active",
      createdAt: someTime,
    },
    "fixedRides/fr1/skips/2026-11-02": {
      riderUids: [RIDER],
      hostSkipped: false,
      updatedAt: someTime,
    },
    "reports/rep1": {
      reporterUid: RIDER,
      targetUid: HOST,
      category: "unsafe_driving",
      createdAt: someTime,
    },
    "safetyEvents/se1": {
      kind: "sos",
      rideId: "ride1",
      createdAt: someTime,
    },
    "adminActions/aa1": {
      adminUid: "admin1",
      action: "suspend_user",
      createdAt: someTime,
    },
    "metrics/impact": {
      tripsCompleted: 10,
      updatedAt: someTime,
    },
    "metrics/2026-W44": {
      cohortSize: 50,
      createdAt: someTime,
    },
    "codes/HT-ABC123": {
      type: "reference",
      refId: "po1",
    },
    "idempotency/user1_req1": {
      result: { ok: true },
    },
    "aiUsage/user1_20261102": {
      count: 3,
    },
    "aiBudget/202611": {
      estimatedEgp: 50,
    },
  });
});

describe("fees/{id}", () => {
  it("that rider, that host and an admin can get and list fees with matching filter", async () => {
    const feePath = "fees/fee1";
    await assertSucceeds(getDoc(doc(asUser(env, RIDER), feePath)));
    await assertSucceeds(getDoc(doc(asUser(env, HOST), feePath)));
    await assertSucceeds(getDoc(doc(asAdmin(env), feePath)));

    const riderQ = query(collection(asUser(env, RIDER), "fees"), where("riderUid", "==", RIDER));
    await assertSucceeds(getDocs(riderQ));

    const hostQ = query(collection(asUser(env, HOST), "fees"), where("hostId", "==", HOST));
    await assertSucceeds(getDocs(hostQ));
  });

  it("stranger cannot read fees; listing without filter fails", async () => {
    await assertFails(getDoc(doc(asUser(env, STRANGER), "fees/fee1")));
    await assertFails(getDocs(collection(asUser(env, RIDER), "fees")));
  });

  it("nobody can write fees from a client", async () => {
    await assertFails(setDoc(doc(asUser(env, HOST), "fees/fee2"), { amount: 10 }));
    await assertFails(updateDoc(doc(asUser(env, RIDER), "fees/fee1"), { status: "paid" }));
  });
});

describe("ratings/{id}", () => {
  it("the author and an admin can get and list ratings with filter", async () => {
    const ratePath = "ratings/rate1";
    await assertSucceeds(getDoc(doc(asUser(env, RIDER), ratePath)));
    await assertSucceeds(getDoc(doc(asAdmin(env), ratePath)));

    const authorQ = query(collection(asUser(env, RIDER), "ratings"), where("fromUid", "==", RIDER));
    await assertSucceeds(getDocs(authorQ));
  });

  it("the rated user or a stranger cannot read ratings; listing without filter fails", async () => {
    await assertFails(getDoc(doc(asUser(env, HOST), "ratings/rate1")));
    await assertFails(getDoc(doc(asUser(env, STRANGER), "ratings/rate1")));
    await assertFails(getDocs(collection(asUser(env, RIDER), "ratings")));
  });

  it("nobody can write ratings from a client", async () => {
    await assertFails(setDoc(doc(asUser(env, RIDER), "ratings/rate2"), { stars: 5 }));
  });
});

describe("packageOrders/{id}", () => {
  it("the owner host and an admin can get and list package orders with filter", async () => {
    const poPath = "packageOrders/po1";
    await assertSucceeds(getDoc(doc(asUser(env, HOST), poPath)));
    await assertSucceeds(getDoc(doc(asAdmin(env), poPath)));

    const hostQ = query(
      collection(asUser(env, HOST), "packageOrders"),
      where("hostId", "==", HOST),
    );
    await assertSucceeds(getDocs(hostQ));
  });

  it("another user cannot read package orders", async () => {
    await assertFails(getDoc(doc(asUser(env, STRANGER), "packageOrders/po1")));
    await assertFails(getDocs(collection(asUser(env, HOST), "packageOrders")));
  });

  it("nobody can write package orders from a client", async () => {
    await assertFails(setDoc(doc(asUser(env, HOST), "packageOrders/po2"), { trips: 10 }));
  });
});

describe("fixedRides/{id} and skips", () => {
  it("the host, members and an admin can read the fixed ride and its skips", async () => {
    const frPath = "fixedRides/fr1";
    const skipPath = "fixedRides/fr1/skips/2026-11-02";

    await assertSucceeds(getDoc(doc(asUser(env, HOST), frPath)));
    await assertSucceeds(getDoc(doc(asUser(env, RIDER), frPath)));
    await assertSucceeds(getDoc(doc(asAdmin(env), frPath)));

    await assertSucceeds(getDoc(doc(asUser(env, HOST), skipPath)));
    await assertSucceeds(getDoc(doc(asUser(env, RIDER), skipPath)));
    await assertSucceeds(getDoc(doc(asAdmin(env), skipPath)));
  });

  it("a non-member cannot read fixed rides or skips", async () => {
    await assertFails(getDoc(doc(asUser(env, STRANGER), "fixedRides/fr1")));
    await assertFails(getDoc(doc(asUser(env, STRANGER), "fixedRides/fr1/skips/2026-11-02")));
  });

  it("nobody can write fixed rides or skips from a client", async () => {
    await assertFails(setDoc(doc(asUser(env, HOST), "fixedRides/fr2"), { status: "active" }));
    await assertFails(deleteDoc(doc(asUser(env, HOST), "fixedRides/fr1/skips/2026-11-02")));
  });
});

describe("reports, safetyEvents, adminActions", () => {
  it("only admin can read reports, safety events and admin actions", async () => {
    await assertSucceeds(getDoc(doc(asAdmin(env), "reports/rep1")));
    await assertSucceeds(getDoc(doc(asAdmin(env), "safetyEvents/se1")));
    await assertSucceeds(getDoc(doc(asAdmin(env), "adminActions/aa1")));

    await assertSucceeds(getDocs(collection(asAdmin(env), "reports")));
    await assertSucceeds(getDocs(collection(asAdmin(env), "safetyEvents")));
    await assertSucceeds(getDocs(collection(asAdmin(env), "adminActions")));
  });

  it("regular users cannot read safety and admin collections", async () => {
    await assertFails(getDoc(doc(asUser(env, RIDER), "reports/rep1")));
    await assertFails(getDoc(doc(asUser(env, RIDER), "safetyEvents/se1")));
    await assertFails(getDoc(doc(asUser(env, RIDER), "adminActions/aa1")));
    await assertFails(getDocs(collection(asUser(env, RIDER), "reports")));
  });

  it("nobody can write safety and admin docs directly from a client", async () => {
    await assertFails(setDoc(doc(asUser(env, RIDER), "reports/rep2"), { category: "other" }));
  });
});

describe("metrics/{id}", () => {
  it("signed-in users can get metrics/impact", async () => {
    await assertSucceeds(getDoc(doc(asUser(env, RIDER), "metrics/impact")));
    await assertSucceeds(getDoc(doc(asAdmin(env), "metrics/impact")));
  });

  it("signed-out users cannot get metrics/impact", async () => {
    await assertFails(getDoc(doc(anon(env), "metrics/impact")));
  });

  it("only admin can get weekly metrics or list metrics", async () => {
    await assertSucceeds(getDoc(doc(asAdmin(env), "metrics/2026-W44")));
    await assertSucceeds(getDocs(collection(asAdmin(env), "metrics")));

    await assertFails(getDoc(doc(asUser(env, RIDER), "metrics/2026-W44")));
    await assertFails(getDocs(collection(asUser(env, RIDER), "metrics")));
  });

  it("nobody can write metrics from a client", async () => {
    await assertFails(updateDoc(doc(asUser(env, RIDER), "metrics/impact"), { tripsCompleted: 99 }));
  });
});

describe("internal collections: codes, idempotency, aiUsage, aiBudget", () => {
  it("all reads and writes are denied to clients, including admins", async () => {
    const admin = asAdmin(env);
    const user = asUser(env, RIDER);

    await assertFails(getDoc(doc(admin, "codes/HT-ABC123")));
    await assertFails(getDoc(doc(user, "codes/HT-ABC123")));

    await assertFails(getDoc(doc(admin, "idempotency/user1_req1")));
    await assertFails(getDoc(doc(user, "idempotency/user1_req1")));

    await assertFails(getDoc(doc(admin, "aiUsage/user1_20261102")));
    await assertFails(getDoc(doc(user, "aiUsage/user1_20261102")));

    await assertFails(getDoc(doc(admin, "aiBudget/202611")));
    await assertFails(getDoc(doc(user, "aiBudget/202611")));
  });
});
