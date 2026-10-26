# Decisions

## What I did

Bug 4 (crash): providerLine() was force-unwrapping provider with !! but two bookings (b-007, b-018) have no provider in the JSON. Replaced with null-safe handling that returns an empty string. The detail screen hides the provider view when it gets an empty string back.

Bug 1 (recycling): setDataBinding() in BookingItemViewModel was setting the status dot color and flag chip visibility but never resetting them. On scroll, recycled views kept state from previous rows. Fixed by resetting all mutable view state at the top of every bind. Status dot goes to neutral grey, flag chip goes to GONE. I kept the dot visible for terminal statuses rather than hiding it because GONE would cause a layout shift on a small fixed-size element.

Bug 3 (spinner hang): Booking b-002 has stallsOnStart = true, which makes the SDK return without ever calling the callback. The progress dialog is non-cancellable, so the app freezes permanently. Fixed with a 10-second timeout (Handler.postDelayed) and a shared cancelStartTrip() cleanup method that handles timeout, failure, and back press. The start button also disables on tap to prevent duplicate calls.

I initially tried OnBackPressedCallback for the back-button escape, but it didn't work. More on that in the "agent was wrong" section below. Ended up adding one protected method to BaseActivity that makes the dialog cancelable during the startTrip flow. The SDK was not modified.

Bug 2 (inset): The detail screen opts into fullscreen content but never applies nav bar insets to the bottom action buttons. On phones with gesture nav, the Start Trip button sits behind the system bar. Fixed with an OnApplyWindowInsetsListener on the actions container. I used the listener approach instead of BaseActivity.adjustBottomPadding() because that helper reads insets eagerly, which can return null before the first layout pass.

Point 5a (provider in list): Added the provider name as a 13sp muted text line below the time range in the booking card. Hidden entirely when provider is null. The flag chip was rechained to anchor below the provider line so the card stacks correctly.

Point 5b (filter): Two Material Chips above the RecyclerView: "Current" (active + upcoming) and "All." Defaults to Current on launch. The full booking list from the SDK stays in memory; filtering happens client-side without another SDK call. No persistence across app restarts.

## What I left alone, and why

Start Trip is enabled for every booking regardless of status. A cancelled or completed booking probably shouldn't have a working Start Trip button, but it wasn't reported in the ticket. First thing I'd guard next.

Tapping a list row fast enough could launch two detail activities. Minor, not reported.

Rotation during an in-flight SDK call drops the callback because CallbackWrapper holds context as a WeakReference. The new activity starts a fresh load, which is fine for the list but could lose a startTrip result. Fixing this properly means introducing a ViewModel or some retained state holder, which is a bigger move than a bug-fix ticket.

flagLabel() falls through to the raw JSON string for unknown flag types. Works today but would surface internal strings if new flags get added.

Whether the filter should persist across sessions is a product question. I defaulted to no persistence and would ask before assuming.

## Assumptions

"Relevant right now" means active and upcoming. The ticket doesn't define it. This felt like the right default for drivers who need to see what's next, not their full booking history.

Provider line is hidden when null, not replaced with "Unknown provider." Two bookings lack a provider. A placeholder label adds noise without helping the driver do anything.

10 seconds for the stall timeout. A driver sitting in a vehicle needs to know quickly that something failed. 10 seconds is short enough to be useful and long enough to not false-positive on a legitimately slow response.

Filter defaults to "Current" because the whole point of the feature request was that drivers with many bookings find the list hard to scan. Defaulting to "All" would defeat that purpose.

Adding one protected method to BaseActivity is justified. It's app code, not the SDK, and it avoids duplicating the entire dialog-building logic in the detail screen.

## Where I used AI, and where I did not

AI did the initial codebase read, root cause analysis, and implementation of each fix. I wrote a spec for each ticket item before handing it to the agent and reviewed every diff before committing.

I made the product decisions myself: priority order, timeout value, default filter state, provider display behavior, what to leave out. SPEC.md decision sections, commit messages, and this file are mine.

The one place I caught and corrected the agent is described below.

## A time the agent was wrong

For Bug 3 the agent proposed using OnBackPressedCallback to let the user back out of the frozen spinner. The reasoning was sound: register a callback, enable it when the trip starts, have it call cancelStartTrip() on back press.

I implemented it and tested. The back press did nothing. The global progress dialog is a non-cancellable Dialog that covers the entire window. It intercepts all input events, including back, at the window level before the activity's OnBackPressedDispatcher ever sees them.

I told the agent it didn't work and why. It proposed two options: either add a small protected method to BaseActivity to make the dialog cancelable with a listener, or build a separate local dialog in the detail screen. I picked the BaseActivity approach because it's one method with no side effects on other callers, and it avoids duplicating 15 lines of dialog code. Tested again, back button now correctly dismisses the spinner and re-enables the Start Trip button.

## What I would do next

1. Guard Start Trip with a status check so cancelled and completed bookings can't trigger it.
2. Unit tests for the null provider path and the filter logic.
3. ViewModel for the detail screen so an in-flight SDK call survives rotation.
4. Empty state for the Current filter when a driver has no active or upcoming bookings.
5. Ask product whether filter state should persist across sessions.