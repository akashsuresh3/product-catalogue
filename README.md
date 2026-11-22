# Product Catalogue (Android + Node backend)

A product catalogue proof of concept: customers browse featured and categorised products, search, view a product, and submit an enquiry that is emailed to the sales inbox.

- **Client:** native Android, Kotlin, Jetpack Compose, MVI, Hilt, Retrofit
- **Backend:** Node.js 20 + TypeScript (Express), packaged with Docker
- **Product data:** Fake Store API, reached only through the backend
- **Email:** SMTP; Mailpit locally (free, no account), swappable for Amazon SES

See [DECISIONS.md](DECISIONS.md) for assumptions and trade-offs, and [AI_USAGE.md](AI_USAGE.md) for how AI was used.

## Why these choices

| Choice | Reason |
|---|---|
| Android client | The assignment is an Android role, so the effort goes into the client. |
| Node + TypeScript backend | Lowest setup cost for a reviewer (one Docker command, no PHP/Composer). Keeps the backend small so the Android work stays the focus. |
| Express | Smallest well-known option; the app is six routes. |
| Mailpit | Real SMTP with a web inbox and no accounts, so the reviewer can see the enquiry email arrive. |

## Prerequisites

- Docker (with Compose)
- Android Studio (current stable) with an emulator (API 26+)
- **JDK 17** for Gradle. Android Studio's bundled JBR works: Settings → Build, Execution, Deployment → Build Tools → Gradle → *Gradle JDK*.
  Do not put `org.gradle.java.home` in the project's `gradle.properties`; if you need it, put it in `~/.gradle/gradle.properties`.

## Run it (about 5 minutes)

**1. Start the backend and the mail inbox**

```
docker compose up --build
```

| What | Where |
|---|---|
| API | http://localhost:3000 (health check: `/health`) |
| Sales inbox (Mailpit) | http://localhost:8025 |

No internet? Set `PRODUCT_SOURCE: fixture` in `docker-compose.yml` to use bundled sample data. If the Fake Store API is unreachable, the backend falls back to the same sample data automatically.

**2. Run the app**

1. Open the `android/` folder in Android Studio and let Gradle sync.
2. Start an emulator and press Run.

The emulator reaches your machine at `10.0.2.2`, so the debug build already points at `http://10.0.2.2:3000/api/`.

*Physical device:* use your computer's LAN IP, e.g. `./gradlew installDebug -PapiBaseUrl=http://192.168.1.20:3000/api/`.

**3. Try the main flow**

Home (featured products, categories) → tap a category → search (top-right icon) → open a product → **Enquire** → submit. You should see "Thank you!", and the email appears in Mailpit at http://localhost:8025.

## Configuration

Backend settings are environment variables; all have defaults. See [`backend/.env.example`](backend/.env.example).

| Variable | Purpose | Default |
|---|---|---|
| `FEATURED_PRODUCT_IDS` | The curated home page products, e.g. `1,2,3,4,5`. If unset, the 5 highest rated are shown. | unset |
| `SALES_INBOX_EMAIL` | Where enquiries are sent | `sales@example.com` |
| `PRODUCT_SOURCE` | `fakestore` or `fixture` | `fakestore` |
| `EMAIL_PROVIDER` | `smtp` or `console` (prints the email) | `smtp` |
| `SMTP_HOST` / `SMTP_PORT` / `SMTP_USER` / `SMTP_PASS` / `SMTP_SECURE` | SMTP settings | Mailpit |

No secrets are committed. Real SMTP credentials belong in the deployment environment.

## Tests

```
# Backend (Node 20+)
cd backend && npm install && npm test && npm run typecheck

# Android unit tests (no emulator needed)
cd android && ./gradlew testDebugUnitTest

# Android Compose UI tests (needs a running emulator)
cd android && ./gradlew connectedDebugAndroidTest
```

What is covered:

- **Android unit:** all five ViewModels (state transitions, effects, form validation and submit flow), the validator, and the repository against MockWebServer (DTO mapping, URL encoding, 404/5xx/400 mapping, malformed payloads, no network).
- **Android UI:** product tile and category clicks, error retry, enquiry form input, submit, field errors and thank-you view.
- **Backend:** catalogue logic (featured, category split, search), every endpoint including enquiry validation, 404 and 502 paths, rate limiting, cache and fallback behaviour, the Fake Store mapping, the SMTP adapter, and config parsing.

## API

All under `/api`. The Android app talks only to these; it never calls Fake Store.

| Method and path | Result |
|---|---|
| `GET /home` | `{ featured: Product[] }` |
| `GET /categories` | `{ categories: string[] }` |
| `GET /categories/:name` | `{ topRated: Product[], others: Product[] }` (404 if unknown) |
| `GET /products?q=` | `{ products: Product[] }` (400 if `q` is blank) |
| `GET /products/:id` | `Product` (404 if unknown) |
| `POST /enquiries` | 201, or 400 `{ errors: { field: message } }`, 404, 429, 502 |

## Project layout

```
android/   Kotlin app: ui (MVI screens) -> domain <- data (Retrofit)
backend/   ports (ProductSource, EmailSender) / adapters / services / http
docker-compose.yml   backend + Mailpit
CLAUDE.md  conventions for AI agents working in this repo
```

## Time spent
Planning: 20 minutes. Choosing backend to ensure least setup and focus on native Android development. I chose MVI architecture and compose for the UI.
Building: Setup(30 minutes) + Development(1 hour) : I had to download node and Docker Desktop for the backend on my device. I used around 1 hour for developing the application with help of Claude using Sonnet 5 model.
Testing: 20 minutes. I ran all tests and ensure the app works end to end. Enquire button successfully sends the mail to mailpit with the message.
Reviewing: 20 minutes. I cross checked all code to ensure no gaps. I recorded a small video of the working app.
Total: 2 hours and 30 minutes

