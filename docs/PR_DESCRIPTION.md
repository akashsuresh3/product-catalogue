# Product catalogue: Android client + Node backend

## What this does

A product catalogue PoC. Customers browse featured products, browse by category, search, view a product and submit an enquiry, which is emailed to the sales inbox. Product data comes from the Fake Store API, only ever through our backend.

Acceptance criteria:

| Criterion | Where |
|---|---|
| Home shows 5 featured products | `GET /api/home`, `HomeScreen`. Curated by `FEATURED_PRODUCT_IDS`, else top 5 by rating |
| Category page: top rated, then additional | `GET /api/categories/:name`, `CategoryScreen` (top 3, then the rest) |
| Search shows only matching products | `GET /api/products?q=`, `SearchScreen`; runs on button press only |
| Product tile opens details | `ProductTile` -> `DetailScreen` |
| Enquire -> email to sales inbox + "Thank you" | `EnquiryScreen` -> `POST /api/enquiries` -> SMTP (Mailpit locally) |

## Stack and why

Native Android (Kotlin, Compose, MVI, Hilt, Retrofit) with a small Node + TypeScript backend. Node was chosen as the lowest-friction backend so the effort stays on Android. Full reasoning in the README and [DECISIONS.md](../DECISIONS.md).

## Design in brief

- Client layers `ui` -> `domain` <- `data`; the UI only knows a repository interface.
- Backend ports and adapters: `ProductSource` and `EmailSender`. Caching and an offline fallback are decorators, so replacing the data source or email provider means one new adapter.
- All design tokens are in `ui/theme`.

## How to run

```
docker compose up --build          # backend + Mailpit (inbox at :8025)
# then open android/ in Android Studio (JDK 17) and run on an emulator
```

## Testing

- Android unit tests: ViewModels, validator, repository via MockWebServer
- Android Compose UI tests: tile/category clicks, retry, enquiry form
- Backend (vitest + supertest): logic, endpoints, failure paths, cache/fallback, adapters, config

Verified by me:

- [x] Android `testDebugUnitTest` passes
- [x] Ran the app end to end against the Docker backend; the enquiry email arrived in Mailpit
- [x] Ran backend `npm test` and `npm run typecheck` result
- [x] Ran `connectedDebugAndroidTest` result

## Assumptions and cuts

Summarised here, detailed in [DECISIONS.md](../DECISIONS.md): curated featured IDs, top 3 per category, USD prices, no offline mode, no enquiry persistence, phone portrait only, single Gradle module.

## AI usage

Built with Claude, directed in stages (plan -> Android contracts -> ViewModels and tests -> screens -> backend). I reviewed and ran the output; the AI's mistakes I caught, and how, are in [AI_USAGE.md](../AI_USAGE.md).

## Known limitations

See "Known limitations and risks" in DECISIONS.md. Main ones: enquiries are not persisted, in-memory cache and rate limit are per instance, the release build config is a placeholder.

## Time spent

Planning: 20 minutes. Choosing backend to ensure least setup and focus on native Android development. I chose MVI architecture and compose for the UI.
Building: Setup(30 minutes) + Development(1 hour) : I had to download node and Docker Desktop for the backend on my device. I used around 1 hour for developing the application with help of Claude using Sonnet 5 model.
Testing: 20 minutes. I ran all tests and ensure the app works end to end. Enquire button successfully sends the mail to mailpit with the message.
Reviewing: 20 minutes. I cross checked all code to ensure no gaps. I recorded a small video of the working app.
Total: 2 hours and 30 minutes
