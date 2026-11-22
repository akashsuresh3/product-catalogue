# AI usage notes

## Tools and models
- Claude (chat interface) for planning, code generation and documentation. Model used: Sonnet 5.5.
- Android Studio, Gradle, Docker and an emulator to build and run everything; I ran the code, the AI could not.
- No other AI tools.

## Agent instruction file
`CLAUDE.md` at the repo root records the conventions the AI was held to: layer rules, MVI structure, error handling, where design tokens live, test tools, and backend ports and adapters.

## Plan
The plan was written before any code and is summarised here:
- Android client first, against a defined API contract; backend second.
- Backend: Node + TypeScript, chosen for the lowest setup cost; Mailpit for local email; SES-compatible SMTP.
- Android: single module, package-by-layer, MVI contract (State/Intent/Effect) per screen, repository interface, ViewModel tests before screens.
- Order: contracts and data layer -> ViewModels and tests -> Compose screens -> backend -> docs.
- Assumptions to record: featured list config, top 3 per category, search semantics, enquiry fields, USD, no offline.

## Key prompts and moments
1. Giving the AI the full brief and asking for a plan **before** any code, and agreeing the stack and defaults.
2. Fixing the order of work: Android contracts and data layer first, so the backend could be written to a contract the client already used.
3. Asking for ViewModel tests before the UI, so state logic was verified before any screen existed.
4. Pasting the Gradle JDK error, which led to the JDK 17 diagnosis and the decision to keep `org.gradle.java.home` out of the repo.
5. Reporting the `stringResource` compile error back with the fix, which was then applied and logged.

## Where the AI got something wrong, and how I caught it
- Dependency versions were pinned from the model's memory (no network access when generated).
  Check them against Android Studio's suggestions before the first build.
- First scaffold script failed: it used shell brace expansion under `sh`, which created a literal
  `{data` directory. Caught from the tool error; fixed and re-ran under bash.
- First draft of the repository used `inline` + `crossinline` on a private suspend helper and a
  separate SerializationException catch (redundant with the generic catch). Simplified.
- Enquiry ViewModel: first draft mapped a server 400 to field errors only, so a 400 with an unreadable
  body left the user with no feedback at all (button re-enabled, nothing shown). Caught while writing
  the failure-path tests; now falls back to a generic submit error, with a test for it.
- Search ViewModel: first draft let an older in-flight search overwrite a newer one. Added job
  cancellation on each new search.
- Naming clash caught before it compiled: my own CLAUDE.md said stateful composables are `XRoute`, but
  the type-safe navigation route classes are also `HomeRoute`, `ProductRoute`, etc. Renamed the
  composables to `XScreen` (stateful) and `XView` (stateless) and fixed CLAUDE.md.
- Android 15 (targetSdk 35) forces edge-to-edge, so content would sit under the status bar on a
  custom top bar and under the keyboard in the form. Added `enableEdgeToEdge()`, `statusBarsPadding()`,
  `navigationBarsPadding()` and `imePadding()` where needed.
- Compose: the AI called `stringResource(...)` inside the `LazyVerticalGrid { }` content block. That
  block is a LazyGridScope (not composable), so it failed to compile. Caught by the build; fixed by
  resolving the strings at the top of the composable, before the grid.
- Phone validation: the Android validator only counted digits while the backend (written later)
  also restricts characters. Noticed when writing the backend schema; aligned the client to match.
- Tooling slip, twice: scripts that create directories with `{a,b,c}` brace expansion ran under `sh`
  and created a literal `{a,b,c}` folder. Caught from the "Directory nonexistent" error; now using
  explicit paths.
- Backend design points I reviewed and kept: product source is a one-method port with Cache and
  Fallback as decorators (so the "data source will change" requirement is one new adapter);
  enquiry emails are sent From our own address with Reply-To set to the customer; line breaks are
  stripped from anything that goes into an email header.


## What I changed or decided myself
I kept Express over Fastify, kept the single module, fixed the Compose compile error, kept the JDK path out of the repo, tested the full flow against Mailpit.

