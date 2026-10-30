# Error Catalog (EN / AR)

This catalog defines every business failure reason in Hitchy, its corresponding Firebase Functions HTTPS status code, and its localized user-facing messages in English and Arabic.

The backend source of truth is `backend/functions/src/lib/errors.ts` and `docs/BUILD_PLAN.md` (Appendix D).
Both Android and iOS apps map these reasons to local resource strings (`error_<reason_lowercase>`).

---

## 1. Conventions

1. **Server Errors**: When a callable fails with a business error, it returns an `HttpsError` where:
   - `code`: The standard Firebase Functions status code from the table below.
   - `message`: The reason string (e.g. `"RIDE_FULL"`).
   - `details.reason`: The exact reason string (e.g. `"RIDE_FULL"`).
   - `details.extra`: Optional context (e.g. validation issue field paths for `INVALID_INPUT`).
2. **Client Handling**:
   - The client reads `error.details.reason` and looks up the localized string key `error_<reason_lowercase>`.
   - If `details.reason` is missing or unrecognized, the client displays a generic error message (`error_internal`).
   - Clients retry **only** network connection errors (`NETWORK`), never business errors.
3. **App-Only Errors**: Reasons marked `(app only)` originate exclusively within the mobile apps (e.g. client-side Phone Auth verification or offline state) and are never sent by the server.

---

## 2. Complete Error Matrix

| Reason                      | HTTPS Code            | English                                                   | Arabic                                         |
| --------------------------- | --------------------- | --------------------------------------------------------- | ---------------------------------------------- |
| `UNAUTHENTICATED`           | `unauthenticated`     | Please sign in again.                                     | سجّل دخولك تاني من فضلك.                       |
| `PERMISSION_DENIED`         | `permission-denied`   | You can't do this.                                        | مش مسموحلك تعمل كده.                           |
| `INVALID_INPUT`             | `invalid-argument`    | Some information is missing or wrong.                     | فيه بيانات ناقصة أو غلط.                       |
| `PROFILE_REQUIRED`          | `failed-precondition` | Complete your profile first.                              | كمّل بياناتك الأول.                            |
| `SUSPENDED`                 | `permission-denied`   | Your account is paused. Contact support.                  | حسابك موقوف مؤقتاً، كلّم الدعم.                |
| `NOT_FOUND`                 | `not-found`           | We couldn't find this.                                    | مش لاقيين الحاجة دي.                           |
| `FEATURE_DISABLED`          | `failed-precondition` | This isn't available yet.                                 | الميزة دي لسه مش متاحة.                        |
| `INTERNAL`                  | `internal`            | Something went wrong. Try again.                          | حصلت مشكلة، جرّب تاني.                         |
| `PHONE_REQUIRED`            | `failed-precondition` | Sign in with your phone number first.                     | سجّل برقم موبايلك الأول.                       |
| `GENDER_LOCKED`             | `failed-precondition` | Gender can't be changed. Contact support.                 | النوع مينفعش يتغير، كلّم الدعم.                |
| `HOST_NOT_APPROVED`         | `failed-precondition` | Your owner account isn't approved yet.                    | حسابك كصاحب عربية لسه متوافقش عليه.            |
| `VEHICLE_LIMIT`             | `failed-precondition` | You can add up to 2 vehicles.                             | تقدر تضيف عربيتين بس.                          |
| `SCOOTER_DISABLED`          | `failed-precondition` | Scooters aren't available yet.                            | السكوتر لسه مش متاح.                           |
| `KYC_ALREADY_SUBMITTED`     | `failed-precondition` | Your documents are under review.                          | ورقك بيتراجع دلوقتي.                           |
| `KYC_MISSING_FILES`         | `failed-precondition` | Some documents are missing.                               | فيه ورق ناقص.                                  |
| `KYC_NOT_SUBMITTED`         | `failed-precondition` | There are no documents to review.                         | مفيش ورق للمراجعة.                             |
| `VEHICLE_NOT_APPROVED`      | `failed-precondition` | This vehicle isn't approved yet.                          | العربية دي لسه متوافقش عليها.                  |
| `ROUTE_NOT_SUPPORTED`       | `failed-precondition` | This route isn't available yet.                           | الطريق ده لسه مش متاح.                         |
| `SEATS_INVALID`             | `invalid-argument`    | Choose a valid number of seats.                           | اختار عدد كراسي مناسب.                         |
| `DEPART_TOO_SOON`           | `failed-precondition` | Departure must be at least 30 minutes from now.           | ميعاد الخروج لازم يكون بعد نص ساعة على الأقل.  |
| `DEPART_TOO_FAR`            | `failed-precondition` | You can post up to 7 days ahead.                          | تقدر تنزّل مشوار لحد 7 أيام قدام.              |
| `PRICE_OUT_OF_RANGE`        | `invalid-argument`    | The price is outside the allowed range.                   | السعر برّه الحدود المسموحة.                    |
| `WOMEN_ONLY_HOST`           | `permission-denied`   | Only women owners can offer Women-only rides.             | مشاوير "ستات بس" لصاحبات العربيات بس.          |
| `DAILY_RIDE_LIMIT`          | `failed-precondition` | You can post one ride each way per day.                   | تقدر تنزّل مشوار رايح وواحد جاي في اليوم.      |
| `RIDE_TIME_CONFLICT`        | `failed-precondition` | You have another ride close to this time.                 | عندك مشوار تاني قريب من الميعاد ده.            |
| `NO_CREDITS`                | `failed-precondition` | Buy a trip package to post rides.                         | اشتري باكدج رحلات عشان تنزّل مشاوير.           |
| `RIDE_HAS_STARTED`          | `failed-precondition` | This ride has already started.                            | المشوار بدأ خلاص.                              |
| `RIDE_NOT_OPEN`             | `failed-precondition` | This ride isn't available anymore.                        | المشوار ده مبقاش متاح.                         |
| `RIDE_FULL`                 | `failed-precondition` | There aren't enough seats left.                           | مفيش كراسي كفاية فاضلة.                        |
| `OWN_RIDE`                  | `failed-precondition` | You can't book your own ride.                             | مينفعش تحجز في مشوارك.                         |
| `ALREADY_BOOKED`            | `already-exists`      | You already have a booking or request on this ride.       | عندك حجز أو طلب في المشوار ده بالفعل.          |
| `UNPAID_FEE`                | `failed-precondition` | Pay your late fee before booking.                         | ادفع غرامة التأخير الأول.                      |
| `WOMEN_ONLY`                | `permission-denied`   | This ride is for women only.                              | المشوار ده للستات بس.                          |
| `TIME_CONFLICT`             | `failed-precondition` | You have another booking close to this time.              | عندك حجز تاني قريب من الميعاد ده.              |
| `TOO_LATE_TO_BOOK`          | `failed-precondition` | Booking closes 15 minutes before departure.               | الحجز بيقفل قبل الميعاد بربع ساعة.             |
| `BOOKING_NOT_FOUND`         | `not-found`           | We couldn't find your booking.                            | مش لاقيين حجزك.                                |
| `HOST_NOT_LATE`             | `failed-precondition` | You can report after 10 minutes past departure.           | تقدر تبلّغ بعد ميعاد الخروج بـ 10 دقايق.       |
| `START_WINDOW_CLOSED`       | `failed-precondition` | Rides can only be started within 30 minutes of departure. | المشوار بيتبدأ في خلال نص ساعة من ميعاده بس.   |
| `RIDE_NOT_STARTED`          | `failed-precondition` | The ride hasn't started yet.                              | المشوار لسه مابدأش.                            |
| `CHECK_IN_CLOSED`           | `failed-precondition` | Check-in is available when the owner is near.             | تقدر تسجل وصول لما صاحب العربية يقرّب.         |
| `NOT_ON_RIDE`               | `permission-denied`   | You aren't booked on this ride.                           | إنت مش حاجز في المشوار ده.                     |
| `GRACE_NOT_OVER`            | `failed-precondition` | Please wait for the 5-minute grace period.                | استنى مهلة الـ 5 دقايق من فضلك.                |
| `RIDER_WAS_ON_TIME`         | `failed-precondition` | The rider checked in on time.                             | الراكب وصل في ميعاده.                          |
| `FEE_EXISTS`                | `already-exists`      | A fee was already applied.                                | الغرامة دي اتسجلت بالفعل.                      |
| `FEE_NOT_FOUND`             | `not-found`           | We couldn't find this fee.                                | مش لاقيين الغرامة دي.                          |
| `FEE_NOT_DISPUTABLE`        | `failed-precondition` | This fee can't be disputed now.                           | مينفعش تعترض على الغرامة دي دلوقتي.            |
| `RATING_NOT_ALLOWED`        | `failed-precondition` | You can rate after the trip ends.                         | تقدر تقيّم بعد ما المشوار يخلص.                |
| `RATING_EXISTS`             | `already-exists`      | You already rated this trip.                              | إنت قيّمت المشوار ده قبل كده.                  |
| `RATING_WINDOW_CLOSED`      | `failed-precondition` | Ratings close 7 days after the trip.                      | التقييم بيقفل بعد المشوار بـ 7 أيام.           |
| `PACKAGE_NOT_FOUND`         | `not-found`           | This package isn't available.                             | الباكدج ده مش متاح.                            |
| `TOO_MANY_PENDING`          | `resource-exhausted`  | You have orders waiting for confirmation.                 | عندك طلبات لسه مستنية تأكيد.                   |
| `ORDER_NOT_PENDING`         | `failed-precondition` | This order was already handled.                           | الطلب ده اتعامل معاه خلاص.                     |
| `NOT_ENOUGH_POINTS`         | `failed-precondition` | You need 300 points.                                      | محتاج 300 نقطة.                                |
| `AI_DAILY_LIMIT`            | `resource-exhausted`  | You've used today's assistant messages. Use search.       | خلصت رسايل المساعد النهارده، استخدم البحث.     |
| `AI_UNAVAILABLE`            | `unavailable`         | The assistant isn't available now. Use search.            | المساعد مش متاح دلوقتي، استخدم البحث.          |
| `INVITE_INVALID`            | `not-found`           | This invite code isn't valid.                             | كود الدعوة ده مش صحيح.                         |
| `FIXED_RIDE_FULL`           | `failed-precondition` | This Fixed Ride is full.                                  | المشوار الثابت ده مليان.                       |
| `FIXED_RIDE_EXISTS`         | `already-exists`      | You already have a Fixed Ride this way.                   | عندك مشوار ثابت في نفس الاتجاه.                |
| `SKIP_TOO_LATE`             | `failed-precondition` | Skips close at 8 PM the day before.                       | الإلغاء بيقفل الساعة 8 بالليل اليوم اللي قبله. |
| `CANNOT_REPORT_SELF`        | `invalid-argument`    | You can't report yourself.                                | مينفعش تبلّغ عن نفسك.                          |
| `ACTIVE_TRIP_SOON`          | `failed-precondition` | Cancel your trips in the next 24 hours first.             | ألغي مشاويرك اللي في الـ 24 ساعة الجاية الأول. |
| `REQUEST_NOT_PENDING`       | `failed-precondition` | This request was already answered.                        | الطلب ده اترد عليه خلاص.                       |
| `REQUEST_EXPIRED`           | `failed-precondition` | This request has expired.                                 | الطلب ده وقته خلص.                             |
| `CHAT_CLOSED`               | `failed-precondition` | Chat for this ride is closed.                             | الشات بتاع المشوار ده اتقفل.                   |
| `CONTACT_NOT_ALLOWED`       | `permission-denied`   | You can call only after a booking is accepted.            | تقدر تتصل بس بعد ما الحجز يتقبل.               |
| `PAYMENT_ALREADY_CONFIRMED` | `already-exists`      | This payment was already confirmed.                       | الدفع ده اتأكد قبل كده.                        |
| `INSUFFICIENT_BALANCE`      | `failed-precondition` | Your wallet balance isn't enough for this ride.            | رصيد محفظتك مش كفاية للمشوار ده.               |
| `STOPS_INVALID`             | `invalid-argument`    | Check the stops: each needs a pickup point and a later time. | راجع نقط الركوب: كل نقطة محتاجة مكان وميعاد بعد اللي قبلها. |
| `PICKUP_NOT_ON_RIDE`        | `failed-precondition` | This pickup point isn't on this ride.                      | نقطة الركوب دي مش في المشوار ده.               |
| `RIDE_NOT_FINISHED`         | `failed-precondition` | End the trip first.                                        | خلّص المشوار الأول.                            |

---

## 3. App-Only Reasons

These reasons are handled locally within client applications and never emitted by the server:

| Reason              | English                                   | Arabic                                    | Note                                                                 |
| ------------------- | ----------------------------------------- | ----------------------------------------- | -------------------------------------------------------------------- |
| `OTP_INVALID`       | The code is wrong.                        | الكود غلط.                                | Phone Auth OTP verification failed.                                  |
| `OTP_EXPIRED`       | The code expired. Send a new one.         | الكود انتهى، ابعت كود جديد.               | Phone Auth SMS timeout.                                              |
| `TOO_MANY_ATTEMPTS` | Too many tries. Try again later.          | محاولات كتير، جرّب بعد شوية.              | Firebase Auth rate limit.                                            |
| `SMS_QUOTA`         | We can't send codes right now. Try later. | مش قادرين نبعت كود دلوقتي، جرّب بعد شوية. | SMS project quota exceeded.                                          |
| `NETWORK`           | No internet connection.                   | مفيش اتصال بالإنترنت.                     | Offline network state. The only reason eligible for automatic retry. |
