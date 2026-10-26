# MQ-7412 Work Spec

## Architecture

**Stack:** Java activities + XML data binding (legacy screens) alongside Kotlin/Compose (new screens).
No ViewModel, LiveData, or coroutines. Callback-based SDK marshals results to the main thread
via `Handler(context.mainLooper)`.

**Key layers:**

| Layer | Files | Notes |
|---|---|---|
| SDK (read-only) | `sdk/FleetApi.kt`, `sdk/Models.kt`, `sdk/Callback.kt` | Singleton, single IO thread, 700 ms fake delay. Results via `CallbackWrapper` which holds context as `WeakReference`. |
| Core plumbing | `core/BaseActivity.java` | EdgeToEdge, global progress dialog (non-cancellable), `adjustBottomPadding()` helper. |
| Legacy surface | `bookings/` | Java, data binding, RecyclerView. `BookingItemViewModel` writes a `Booking` into a row binding. `BookingFormatter` formats strings. |
| New surface | `feature/cancellation/CancelBookingSheet.kt` | Reference template for new Compose screens. |
| Design system | `ui/theme/AppTheme.kt`, `ui/components/SheetActionButtons.kt` | Colors resolved from XML for white-label support. |

**Data flow:**
```
bookings.json (asset)
  → FleetApi (IO thread, 700 ms delay)
    → CallbackWrapper (WeakRef guard → Handler.post to main)
      → Activity callback (main thread)
        → adapter.setBookings() / binding.setText() / etc.
```

---

## Priority Order

1. Bug 4 — crash on open (NPE, affects live users immediately)
2. Bug 1 — wrong labels on scroll (RecyclerView recycling, affects all list users)
3. Bug 3 — permanent spinner (stall on b-002, affects start-trip users)
4. Bug 2 — button hidden behind nav bar (insets, device-dependent)
5. Point 5a — provider name in list row
6. Point 5b — active/upcoming filter chips

---

## Bug 4 — "The app closes when I open a booking"

**Root cause:** `BookingFormatter.providerLine()` (`BookingFormatter.kt:32`) uses `!!` on `booking.provider`,
which is `Provider?`. Two bookings in `bookings.json` have no `provider` field (b-007, b-018).
Opening either throws `KotlinNullPointerException`, crashing the activity.

```kotlin
// Current — crashes when provider is null
fun providerLine(context: Context, booking: Booking): String =
    context.getString(R.string.detail_provided_by, booking.provider!!.name)
```

Also blocks Point 5a: the same null must be handled in the list row once provider is displayed there.

**Fix approach:**
- In `BookingFormatter.providerLine()`: replace `!!` with a null check. Return an empty string when
  `provider` is null; the caller sets the view visibility accordingly.
- In `BookingDetailActivity.render()`: hide `binding.provider` when the returned string is blank.
- No SDK changes needed.

---

## Bug 1 — "Wrong labels on my bookings"

**Root cause:** `BookingItemViewModel.setDataBinding()` (`BookingItemViewModel.java:34`) has two
conditional branches that set view state but no matching reset branches for the else-cases.
RecyclerView reuses ViewHolders, so state from a previous binding bleeds into the next.

```java
// statusDot: only set for ACTIVE/UPCOMING — CANCELLED/COMPLETED rows keep the previous tint
if (status == BookingStatus.ACTIVE || status == BookingStatus.UPCOMING) {
    binding.statusDot.setImageTintList(...);
}
// no else → recycled green dot stays green on a cancelled row

// flagChip: only shown when flag != null — rows without a flag keep the chip visible
if (flag != null) {
    binding.flagChip.setVisibility(View.VISIBLE);
}
// no else → recycled "Needs cleaning" chip stays on rows that have no flag
```

**Fix approach:**
- `statusDot`: add an `else` branch that sets the tint to `R.color.status_neutral` (and optionally
  reduces alpha or hides the dot for terminal statuses — a product call to note in DECISIONS.md).
- `flagChip`: add an `else` branch that sets visibility back to `View.GONE`.
- No SDK or layout changes needed.

---

## Bug 3 — "The app hangs on a loading spinner"

**Root cause:** `FleetApi.startTrip()` (`FleetApi.kt:44`) deliberately never calls `wrapper.success()`
or `wrapper.failure()` for bookings where `stallsOnStart == true` (b-002 in the asset).
`BookingDetailActivity.startTrip()` (`BookingDetailActivity.java:79`) calls `showGlobalProgress()`
before the SDK call. Since no callback ever fires, `dismissGlobalProgress()` is never reached.
The progress dialog is `setCancelable(false)`, swallows all touches, and blocks the app permanently.

**Fix approach (app-side only — SDK is read-only):**

Two complementary changes in `BookingDetailActivity`:

1. **Timeout:** post a `Handler.postDelayed` runnable before the SDK call (e.g. 15 s). If it fires
   before the callback, dismiss the dialog and show an error message. Cancel the runnable in both
   `onSuccess` and `onFailure`.

2. **Back-button safety valve:** override `onBackPressed` (or use `OnBackPressedCallback`) in
   `BookingDetailActivity` to call `dismissGlobalProgress()` and re-enable the Start Trip button
   when the dialog is up. This gives the user an escape even if the timeout fails.

The `startTrip` button should also be disabled as soon as tapped (prevents duplicate in-flight calls
regardless of the stall scenario).

---

## Bug 2 — "I cannot reach the Start trip button"

**Root cause:** `BookingDetailActivity.isFullscreenContent()` returns `true`, which opts out of
`BaseActivity.applySystemBarPaddingToDecor()`. The screen is responsible for handling its own insets
but never does. The `actions` LinearLayout in `activity_booking_detail.xml` (line 104) is pinned to
the bottom of the ConstraintLayout with no bottom padding, so on phones with gesture navigation or
a software nav bar the Start Trip button sits behind the system bar.

`BaseActivity.adjustBottomPadding()` exists for exactly this pattern but is never called.

**Fix approach:**
- In `BookingDetailActivity.onCreate()` (or after `setContentView`), attach a
  `ViewCompat.setOnApplyWindowInsetsListener` to `binding.actions` that applies the navigation-bar
  inset as bottom padding, preserving the existing `paddingTop`.
- Alternatively call `BaseActivity.adjustBottomPadding(binding.actions, 0)` — but the eager
  read in that helper can return null before first layout; the listener approach is safer.
- No SDK or model changes needed.

---

## Point 5a — Provider name in list row

**Product decisions:**
- Provider name appears as a muted text line **below the time range** (`subtitle`) in `item_booking.xml`.
- When `provider` is null the line is hidden entirely — no fallback text.

**Fix approach:**
- Add a `TextView` (`providerName`) to `item_booking.xml`, positioned below `subtitle`, styled with
  `@color/text_muted` at 13–14 sp to match `subtitle`.
- In `BookingItemViewModel.setDataBinding()`: if `booking.provider != null`, set the text and show
  the view; else hide it (`View.GONE`).
- Reuse `booking.provider?.name` directly — no `BookingFormatter` method needed for this.
- Bug 4's null-provider fix is a prerequisite (the same null must be handled consistently).

---

## Point 5b — Active/upcoming filter

**Product decisions:**
- "Relevant right now" = `status == ACTIVE || status == UPCOMING`.
- Default state: filtered (show active + upcoming only).
- Toggle: two filter chips above the RecyclerView — **"Current"** (default, active+upcoming) and
  **"All"** — not tabs.
- No persistence; resets to "Current" on app restart.

**Fix approach:**
- Add a `ChipGroup` with two `Chip` views above the RecyclerView in `activity_booking_list.xml`.
  Use `app:singleSelection="true"` on the group.
- `BookingsAdapter` or `BookingListActivity` holds the full list and the current filter; refilters
  on chip selection and calls `adapter.setBookings(filtered)`.
- The master list from the SDK load is kept in `BookingListActivity` so re-filtering does not
  require a second SDK call.
- Chip style should follow the existing `bg_chip` drawable so it matches the flag chips.

---

## Scratch Notes

*(Fill in during implementation — one bullet per decision or surprise)*

- Bug 4: providerLine null-safe with empty-string sentinel; detail screen hides provider view when empty; no SDK change needed.
- Bug 1: reset statusDot tint to neutral and flagChip to GONE on every bind; kept dot visible for terminal statuses (neutral grey, no layout shift).
- Bug 3: 10 s timeout via Handler.postDelayed, button disabled on tap, cancelStartTrip() shared cleanup for timeout/failure/back. Initially tried OnBackPressedCallback for back-button escape but it didn't work — dialog intercepts back press before the callback fires. Switched to adding setGlobalProgressCancelable to BaseActivity (one protected method); dialog routes its own cancel to cancelStartTrip(). SDK untouched. Caught the back-button issue during manual testing.
- Bug 2: listener approach on actions container, not eager read. Preserves existing padding on left/top/right, adds nav bar height to bottom. Returns insets unconsumed.
- Point 5a: provider name below time range, 13sp muted text, hidden when null. Flag chip rechained to sit below provider line.

- 
- 
- 
