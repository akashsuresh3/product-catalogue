# Project conventions (read before changing code)

Monorepo: `android/` (Kotlin, Compose, MVI, Hilt, Retrofit) and `backend/` (Node + TypeScript, Express).

## Android
- Layers: `ui` -> `domain` <- `data`. `ui` never imports `data`. `domain` has no Android or Retrofit imports.
- MVI per screen in `ui/<feature>/`: `XContract.kt` (State, Intent, Effect), `XViewModel.kt`, `XScreen.kt`.
  - State is an immutable data class. Only the ViewModel changes it, via `setState { copy(...) }`.
  - `onIntent()` is the only entry point from the UI. One-off events use Effects, never state flags.
  - Screens are split: a stateful `XScreen` (wires the ViewModel, turns effects into navigation callbacks) and a stateless `XView(state, onIntent)`. UI tests target the `XView`s.
  - Navigation lives only in `ui/navigation`; screens receive plain lambdas.
- Repositories return `Result<T>`; failures are `CatalogueException` subtypes. Rethrow `CancellationException`.
- Design tokens (colours, type, shapes) only in `ui/theme`. No hard-coded colours or text sizes in screens.
- Base URL comes from `BuildConfig.API_BASE_URL`. No secrets in the repo.
- Tests: ViewModels with MockK + Turbine + coroutines-test; data layer with MockWebServer.

## Backend
- Ports and adapters: product source and email sender are interfaces; adapters are swappable by config.
- Validate all input with zod. Config is parsed and validated at startup. No secrets committed; use `.env.example`.
