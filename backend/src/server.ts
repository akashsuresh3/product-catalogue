import 'dotenv/config';
import { CachedProductSource } from './adapters/CachedProductSource';
import { ConsoleEmailSender } from './adapters/ConsoleEmailSender';
import { FakeStoreProductSource } from './adapters/FakeStoreProductSource';
import { FallbackProductSource } from './adapters/FallbackProductSource';
import { FixtureProductSource } from './adapters/FixtureProductSource';
import { SmtpEmailSender } from './adapters/SmtpEmailSender';
import { loadConfig } from './config/env';
import { createApp } from './http/app';
import type { EmailSender } from './ports/EmailSender';
import type { ProductSource } from './ports/ProductSource';
import { CatalogueService } from './services/CatalogueService';
import { EnquiryService } from './services/EnquiryService';

// Composition root: the only place that knows which concrete adapters are in use.
const config = loadConfig();

const productSource: ProductSource =
  config.product.source === 'fixture'
    ? new FixtureProductSource()
    : new FallbackProductSource(
        new CachedProductSource(
          new FakeStoreProductSource(config.product.url, config.product.fetchTimeoutMs),
          config.product.cacheTtlSeconds * 1000,
        ),
        new FixtureProductSource(),
      );

const emailSender: EmailSender =
  config.email.provider === 'console' ? new ConsoleEmailSender() : new SmtpEmailSender(config.email.smtp);

const catalogue = new CatalogueService(productSource, { featuredProductIds: config.product.featuredIds });
const enquiries = new EnquiryService(catalogue, emailSender, {
  salesInbox: config.email.salesInbox,
  from: config.email.from,
});

const app = createApp({
  catalogue,
  enquiries,
  enquiryRateLimitMax: config.http.enquiryRateLimitMax,
  corsOrigins: config.http.corsOrigins,
});

const server = app.listen(config.port, () => {
  console.log(
    `Catalogue backend listening on :${config.port} (products: ${config.product.source}, email: ${config.email.provider})`,
  );
});

const shutdown = () => server.close(() => process.exit(0));
process.on('SIGTERM', shutdown);
process.on('SIGINT', shutdown);
