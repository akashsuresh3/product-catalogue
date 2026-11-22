# Decisions and assumptions

## Assumptions (where the acceptance criteria were unclear)

| Topic | Assumption |
|---|---|
| "5 chosen top rated" products | A curated list of 5 IDs in backend config (`FEATURED_PRODUCT_IDS`). If it is not set, the 5 highest rated are used (ties broken by review count, then id). The client does not know which rule applied. |
| Category "top rated" | The top 3 products by rating in that category. "Additional products" are the rest, in a separate section. A category with 3 or fewer products has no additional section. |
| Search | Case-insensitive; every word of the query must appear in the title, description or category. Done on the backend. Runs only when the search button (or keyboard search key) is pressed, as the criteria say; typing alone does not search. |
| Blank search | Ignored by the app (nothing happens); rejected with 400 by the API. |
| Enquiry fields | Name, email, phone (optional), message (max 1000 characters), tied to a product ID. |
| Validation | Done on both sides with matching rules and messages. The backend is authoritative; its field errors are shown next to the form fields. |
| Sales inbox | One address from `SALES_INBOX_EMAIL`. |
| Email "From" | Our own address, with Reply-To set to the customer, so mail providers do not reject it as spoofed and sales can still reply directly. |
| Prices | USD, shown as returned. No currency or locale handling. |
| Product ids | Numeric, as in Fake Store. |
| Users | No accounts, cart or auth. |

## Requirements dropped or simplified, and why

| Cut | Why |
|---|---|
| Offline mode / local database | Not in the criteria. The app shows an error with a retry button instead. A Room cache is a straightforward later addition behind the repository. |
| Enquiry persistence | The criteria only require an email. The cost is that a failed send loses the enquiry (the user is told and can retry). See "next" below. |
| Tablet and landscape layouts | Out of the time-box; phone portrait only. |
| Pagination | About 20 products. The API wrappers (`{ products: [...] }`) leave room to add paging without breaking the client. |
| Multi-module Gradle | Overhead for a 2-3 hour build. The package structure (`ui`, `domain`, `data`) is the module boundary to cut along later. |
| Authentication on the API | No user data worth protecting yet; the enquiry route is rate limited instead. |
| Release build config | Release points at a placeholder HTTPS URL and is not minified. No deployment target exists yet. |
| Accessibility and localisation polish | Strings are in resources and icons have descriptions, but there has been no TalkBack or translation pass. |

## How the design supports growth beyond the PoC

The brief says the data source, look and feel, and email provider will change.

- **Data source:** the backend depends on a one-method `ProductSource` port. Fake Store is one adapter; caching and the offline fallback are decorators around it. A database, CMS or ERP is a new adapter plus one line in `server.ts`. The backend also maps upstream naming (`image`) to our contract (`imageUrl`), so neither the client nor the API contract changes when the source does.
- **Look and feel:** all colours live in `ui/theme`. Screens use Material theme tokens, not hard-coded values. Screens are split into a stateful `XScreen` and a stateless `XView(state, onIntent)`, so a redesign touches layout code only, not state or logic.
- **Email provider:** `EmailSender` is a port. The SMTP adapter already works with Amazon SES through its SMTP interface (configuration only). A different provider's API is one new adapter.
- **Client layering:** `ui` -> `domain` <- `data`. The UI depends on a `CatalogueRepository` interface and never sees Retrofit, DTOs or HTTP codes; failures arrive as typed `CatalogueException`s. Swapping networking or adding a cache does not touch screens.
- **MVI:** every screen has one State, one Intent type and one Effect type, with a single `onIntent` entry point. New behaviour is a new intent and a test, not new wiring.
- **AWS compatibility:** the backend is a stateless container (Dockerfile, health check, non-root user, config from environment), so it can run on ECS/Fargate or App Runner. The in-memory cache and rate limiter are per instance, which is fine for one instance (see below).

## What I would do next with another day

1. **Persist enquiries** (e.g. DynamoDB or Postgres) and send the email from a queue, so a mail outage never loses a customer's request.
2. **Run the Android flow tests against the real backend** in CI (Docker Compose + emulator), plus a contract test so the DTOs and API cannot drift apart. Today the contract is kept in sync by hand.
3. **Shared rate limiting and cache** (Redis) before running more than one backend instance.
4. **Offline support** with Room, and pagination once the catalogue is larger than a screenful.
5. **Real design system:** apply the entrepreneur's creative work in `ui/theme`, and add screenshot tests to protect it.
6. **Accessibility pass** (TalkBack, font scaling, touch targets) and localisation.
7. **Release pipeline:** minification, signing, HTTPS endpoint, crash reporting.
8. Pin the Mailpit image tag and add Dependabot or Renovate.

## Known limitations and risks

- Fake Store data is not stable: ids and ratings could change upstream. The 16 bundled fallback products are synthetic and use placeholder images.
- `FEATURED_PRODUCT_IDS` entries that do not exist are skipped silently, which can leave fewer than 5 featured products. It is logged nowhere; a startup warning would be a good addition.
- Navigation arguments are read through `SavedStateHandle` keys (`NavArgs`) that must match the route property names. This keeps ViewModels unit-testable on the JVM, but renaming a route property would break it silently. A test that checks them against the route classes is the fix.
- The rate limiter keys on client IP. Behind a load balancer the backend would need `trust proxy` configured or every user would share one limit.
- Search is a substring match, not relevance-ranked or typo tolerant.
