# Project Rules

## Mode
- DO NOT modify any file unless I explicitly say "implement" or "fix"
- When analyzing, read and report only
- Never assume requirements — ask me to decide

## Workflow
- We work spec-driven: analyze first, spec second, implement third
- One ticket item per implementation cycle
- Show me the diff after each change, wait for my approval before committing
- Commit messages follow: type(scope): description (MQ-7412 #N)

## SDK Boundary
- Files in the sdk/ package are third-party. Do not modify them.
- If a fix requires an SDK change, flag it and propose an app-side alternative.

## Code Style
- Follow existing patterns in the codebase
- New UI follows Compose (reference: CancelBookingSheet)
- Legacy screens stay in their current pattern (Java + data binding)
- Don't introduce new libraries or dependencies without asking

## What I Submit
- Branch with incremental commits (one per ticket item)
- DECISIONS.md (I write this myself, don't generate it)
- Agent transcripts (this session)
