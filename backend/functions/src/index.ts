// Cloud Functions entry point: exports only (BUILD_PLAN 4.2).
// Each callable, trigger, job and task is re-exported here when its task adds it.
export { completeProfile } from "./handlers/profile/completeProfile.js";
export { switchRole } from "./handlers/profile/switchRole.js";
export { quotePrice } from "./handlers/rides/quotePrice.js";
export { createRide } from "./handlers/rides/createRide.js";
export { searchRides } from "./handlers/booking/searchRides.js";
export { requestSeats } from "./handlers/booking/requestSeats.js";
export { respondToRequest } from "./handlers/booking/respondToRequest.js";
export { cancelBooking } from "./handlers/booking/cancelBooking.js";
export { startRide } from "./handlers/trip/startRide.js";
export { arriveAtStop } from "./handlers/trip/arriveAtStop.js";
export { checkIn } from "./handlers/trip/checkIn.js";
export { markBoarded } from "./handlers/trip/markBoarded.js";
export { reportLate } from "./handlers/trip/reportLate.js";
export { finishRide } from "./handlers/trip/finishRide.js";
