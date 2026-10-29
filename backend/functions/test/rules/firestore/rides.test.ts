// Firestore rules (BUILD_PLAN 9.1): rides, bookings, presence, chats, collection groups.
import {
  assertFails,
  assertSucceeds,
  type RulesTestEnvironment,
} from "@firebase/rules-unit-testing";
import {
  collection,
  collectionGroup,
  deleteDoc,
  doc,
  getDoc,
  getDocs,
  query,
  serverTimestamp,
  setDoc,
  Timestamp,
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
  RIDER2,
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

const rideId = "ride1";
const ridePath = `rides/${rideId}`;

beforeEach(async () => {
  await env.clearFirestore();
  await seed(env, {
    [ridePath]: {
      hostId: HOST,
      status: "open",
      seatsLeft: 2,
      departAt: someTime,
      createdAt: someTime,
    },
    [`${ridePath}/bookings/${RIDER}`]: {
      rideId,
      riderUid: RIDER,
      hostId: HOST,
      status: "booked",
      seats: 1,
      createdAt: someTime,
    },
    [`${ridePath}/bookings/${RIDER2}`]: {
      rideId,
      riderUid: RIDER2,
      hostId: HOST,
      status: "requested",
      seats: 1,
      createdAt: someTime,
    },
    [`${ridePath}/presence/${HOST}`]: {
      last: { lat: 30.0, lng: 31.0, accuracyM: 10, at: someTime },
      samples: [],
      updatedAt: someTime,
    },
    [`${ridePath}/presence/${RIDER}`]: {
      last: { lat: 30.01, lng: 31.01, accuracyM: 15, at: someTime },
      samples: [],
      updatedAt: someTime,
    },
    [`${ridePath}/chats/${RIDER}/messages/m1`]: {
      fromUid: HOST,
      text: "On my way",
      createdAt: someTime,
    },
  });
});

describe("rides/{rideId}", () => {
  it("the host, an accepted rider and an admin can get the ride", async () => {
    await assertSucceeds(getDoc(doc(asUser(env, HOST), ridePath)));
    await assertSucceeds(getDoc(doc(asUser(env, RIDER), ridePath)));
    await assertSucceeds(getDoc(doc(asAdmin(env), ridePath)));
  });

  it("a rider with only a requested booking cannot get the ride", async () => {
    await assertFails(getDoc(doc(asUser(env, RIDER2), ridePath)));
  });

  it("a non-booked user or signed-out client cannot get the ride", async () => {
    await assertFails(getDoc(doc(asUser(env, STRANGER), ridePath)));
    await assertFails(getDoc(doc(anon(env), ridePath)));
  });

  it("the host can list their own rides with a filter; admin can list all", async () => {
    const hostQ = query(collection(asUser(env, HOST), "rides"), where("hostId", "==", HOST));
    await assertSucceeds(getDocs(hostQ));

    await assertSucceeds(getDocs(collection(asAdmin(env), "rides")));
  });

  it("a host without filter or filtering another host is denied", async () => {
    await assertFails(getDocs(collection(asUser(env, HOST), "rides")));
    const otherQ = query(collection(asUser(env, HOST), "rides"), where("hostId", "==", STRANGER));
    await assertFails(getDocs(otherQ));
  });

  it("nobody can create, update or delete a ride from a client", async () => {
    await assertFails(setDoc(doc(asUser(env, HOST), "rides/newRide"), { hostId: HOST }));
    await assertFails(updateDoc(doc(asUser(env, HOST), ridePath), { status: "cancelled" }));
    await assertFails(deleteDoc(doc(asAdmin(env), ridePath)));
  });
});

describe("rides/{rideId}/bookings/{riderUid}", () => {
  const myBooking = `${ridePath}/bookings/${RIDER}`;
  const otherBooking = `${ridePath}/bookings/${RIDER2}`;

  it("that rider, the host and an admin can get the booking", async () => {
    await assertSucceeds(getDoc(doc(asUser(env, RIDER), myBooking)));
    await assertSucceeds(getDoc(doc(asUser(env, HOST), myBooking)));
    await assertSucceeds(getDoc(doc(asAdmin(env), myBooking)));
  });

  it("a rider cannot read another rider's booking", async () => {
    await assertFails(getDoc(doc(asUser(env, RIDER), otherBooking)));
    await assertFails(getDoc(doc(asUser(env, STRANGER), myBooking)));
  });

  it("the host and an admin can list bookings on the ride; a rider cannot", async () => {
    await assertSucceeds(getDocs(collection(asUser(env, HOST), `${ridePath}/bookings`)));
    await assertSucceeds(getDocs(collection(asAdmin(env), `${ridePath}/bookings`)));
    await assertFails(getDocs(collection(asUser(env, STRANGER), `${ridePath}/bookings`)));
  });

  it("nobody can write bookings directly from a client", async () => {
    await assertFails(
      setDoc(doc(asUser(env, RIDER), `${ridePath}/bookings/${RIDER}`), { status: "booked" }),
    );
    await assertFails(updateDoc(doc(asUser(env, HOST), myBooking), { status: "completed" }));
  });
});

describe("collection group queries: bookings and vehicles", () => {
  it("a rider can list their own bookings across rides with riderUid filter", async () => {
    const q = query(
      collectionGroup(asUser(env, RIDER), "bookings"),
      where("riderUid", "==", RIDER),
    );
    await assertSucceeds(getDocs(q));
  });

  it("a rider cannot list bookings without filter or for another rider", async () => {
    await assertFails(getDocs(collectionGroup(asUser(env, RIDER), "bookings")));
    const otherQ = query(
      collectionGroup(asUser(env, RIDER), "bookings"),
      where("riderUid", "==", RIDER2),
    );
    await assertFails(getDocs(otherQ));
  });

  it("an admin can list collection group vehicles; a regular user cannot", async () => {
    await assertSucceeds(getDocs(collectionGroup(asAdmin(env), "vehicles")));
    await assertFails(getDocs(collectionGroup(asUser(env, RIDER), "vehicles")));
  });
});

describe("rides/{rideId}/presence/{uid}", () => {
  const hostPresence = `${ridePath}/presence/${HOST}`;
  const riderPresence = `${ridePath}/presence/${RIDER}`;

  const validPresenceDoc = () => ({
    last: { lat: 30.05, lng: 31.05, accuracyM: 10, at: Timestamp.now() },
    samples: [{ lat: 30.05, lng: 31.05, at: Timestamp.now() }],
    updatedAt: serverTimestamp(),
  });

  it("host and admin can read any presence doc on the ride", async () => {
    await assertSucceeds(getDoc(doc(asUser(env, HOST), riderPresence)));
    await assertSucceeds(getDoc(doc(asAdmin(env), riderPresence)));
  });

  it("a rider with accepted booking can read the host presence and their own", async () => {
    await assertSucceeds(getDoc(doc(asUser(env, RIDER), hostPresence)));
    await assertSucceeds(getDoc(doc(asUser(env, RIDER), riderPresence)));
  });

  it("a rider cannot read another rider's presence", async () => {
    await assertFails(getDoc(doc(asUser(env, RIDER2), riderPresence)));
  });

  it("a stranger or non-accepted rider cannot read host presence", async () => {
    await assertFails(getDoc(doc(asUser(env, STRANGER), hostPresence)));
    await assertFails(getDoc(doc(asUser(env, RIDER2), hostPresence)));
  });

  it("allows presence write when ride is started by user on the ride", async () => {
    await seed(env, { [ridePath]: { hostId: HOST, status: "started" } });

    await assertSucceeds(setDoc(doc(asUser(env, HOST), hostPresence), validPresenceDoc()));
    await assertSucceeds(setDoc(doc(asUser(env, RIDER), riderPresence), validPresenceDoc()));
  });

  it("presence write is refused when the ride is not started", async () => {
    // ride is currently "open"
    await assertFails(setDoc(doc(asUser(env, HOST), hostPresence), validPresenceDoc()));
  });

  it("presence write is refused for a stranger or invalid fields", async () => {
    await seed(env, { [ridePath]: { hostId: HOST, status: "started" } });

    // stranger writing
    await assertFails(
      setDoc(doc(asUser(env, STRANGER), `${ridePath}/presence/${STRANGER}`), validPresenceDoc()),
    );

    // invalid fields (extra field or missing updatedAt = server time)
    await assertFails(
      setDoc(doc(asUser(env, HOST), hostPresence), {
        ...validPresenceDoc(),
        extra: "invalid",
      }),
    );
    await assertFails(
      setDoc(doc(asUser(env, HOST), hostPresence), {
        ...validPresenceDoc(),
        updatedAt: someTime,
      }),
    );
  });
});

describe("rides/{rideId}/chats/{riderUid}/messages/{id}", () => {
  const msgPath = `${ridePath}/chats/${RIDER}/messages/m1`;

  it("the host and that rider can read messages", async () => {
    await assertSucceeds(getDoc(doc(asUser(env, HOST), msgPath)));
    await assertSucceeds(getDoc(doc(asUser(env, RIDER), msgPath)));
  });

  it("another rider and admin cannot read chat messages", async () => {
    await assertFails(getDoc(doc(asUser(env, RIDER2), msgPath)));
    await assertFails(getDoc(doc(asUser(env, STRANGER), msgPath)));
    await assertFails(getDoc(doc(asAdmin(env), msgPath)));
  });

  it("nobody can write messages directly from a client", async () => {
    await assertFails(
      setDoc(doc(asUser(env, RIDER), `${ridePath}/chats/${RIDER}/messages/m2`), {
        fromUid: RIDER,
        text: "Hello",
      }),
    );
  });
});
