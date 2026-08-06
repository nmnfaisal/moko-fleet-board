# MOQO tech challenge: Fleet Board

Thanks for making it to this stage. This is not a greenfield exercise. It is a small slice of the kind
of codebase you would actually work in: a booking list and detail screen, ten years of habits in
miniature, a Java activity next to a Compose bottom sheet, and a callback-based SDK layer.

Your job is to work a support ticket in it. See [TICKET.md](TICKET.md).

**The problems in here are deliberate.** We planted them for this exercise, and
[TICKET.md](TICKET.md) is how you find them: it reaches you as support reports, so it describes what
drivers noticed rather than what is wrong. Please do not read the defects as a sample of how we write
code, none of this shipped to anyone. What *is* representative is the shape of the project: Java next
to Compose, data binding next to a design system, and an SDK layer you do not own. Working inside that
mix is most of the job.

## Time

**Plan for about three focused hours.** There is no deadline and no timer, so pick your own moment.

The ticket contains more than three hours of work on purpose. **We do not expect you to finish it.**
Deciding what to do, what to leave, and being able to say why, is a large part of what we are looking
at. A submission that fixes two things well and explains the rest beats one that touches everything.

## On AI

**Use AI coding tools, and tell us how.** That is how we work every day, so a submission produced
without them tells us less, not more.

Two things follow from that:

- **Send us the agent transcripts.** Claude Code writes them to `~/.claude/projects/...`, or use
  `/export`. Cursor, Copilot, Codex, whatever you use: export the session. If your tool cannot export,
  paste the conversation into a file. A submission without transcripts is incomplete.
- **Remove anything private first.** Do not send us transcripts containing a current or former
  employer's code, and strip any tokens or personal data. If in doubt, redact.

We are not counting how much of the diff the agent wrote. We are reading how you directed it, what you
checked, and where you disagreed with it.

## What to send us

1. **A branch or patch** with your work. The repo has one base commit, so branch from it. Commit messages
   in whatever style you normally use.
2. **The agent transcripts.**
3. **[DECISIONS.md](DECISIONS.md)**, filled in. It is short on purpose. This is the part reviewers read
   first.

No screen recording, no slide deck, no write-up beyond DECISIONS.md.

## Running it

```bash
./gradlew :app:installDebug     # device or emulator, minSdk 29
./gradlew :app:testDebugUnitTest
```

If you have more than one device attached, `installDebug` targets all of them. Use
`adb -s <serial> install -r app/build/outputs/apk/debug/app-debug.apk` instead.

A phone is a better target than a tablet here.

No network and no credentials needed. The SDK layer reads `app/src/main/assets/bookings.json` with an
artificial delay so it behaves like a real request, including one booking that behaves badly.

## What is in here

| Path | What it is |
| --- | --- |
| `sdk/` | Stand-in for our mobile SDK. Callback based, results marshalled to the main thread, context held weakly. Treat it as a third-party dependency: you can read it, but changing it is a bigger decision than changing app code. |
| `core/BaseActivity.java` | Shared activity plumbing: edge-to-edge setup, the global progress overlay, an inset helper. |
| `bookings/` | The legacy surface. Java, data binding, RecyclerView, item view models. |
| `feature/cancellation/` | The one screen already migrated to Compose. New UI is meant to look like this. |
| `ui/theme/`, `ui/components/` | The Compose design system, colours resolved from XML so white label flavours work. |

Our production app is Kotlin and Java, AGP 9.2, compileSdk 36, minSdk 29, Compose alongside XML with
data binding, and a package-by-feature migration in progress. The versions here mirror it.

## Questions

If something in the ticket is unclear, you have two valid options: ask us, or decide and write down what
you assumed. Both are fine. Guessing silently is the one that costs you.
