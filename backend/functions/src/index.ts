// Cloud Functions entry point: exports only (BUILD_PLAN 4.2).
// Each callable, trigger, job and task is re-exported here when its task adds it.
export { completeProfile } from "./handlers/profile/completeProfile.js";
export { switchRole } from "./handlers/profile/switchRole.js";
export { quotePrice } from "./handlers/rides/quotePrice.js";
export { createRide } from "./handlers/rides/createRide.js";
