import { z } from 'zod';

const csvPositiveInts = z
  .string()
  .default('')
  .transform((value, ctx) => {
    const numbers = value
      .split(',')
      .map((part) => part.trim())
      .filter(Boolean)
      .map(Number);
    if (numbers.some((n) => !Number.isInteger(n) || n <= 0)) {
      ctx.addIssue({ code: z.ZodIssueCode.custom, message: 'must be a comma-separated list of positive integers' });
      return z.NEVER;
    }
    return numbers;
  });

const csvStrings = z
  .string()
  .default('')
  .transform((value) =>
    value
      .split(',')
      .map((part) => part.trim())
      .filter(Boolean),
  );

const envSchema = z.object({
  PORT: z.coerce.number().int().min(1).max(65535).default(3000),

  PRODUCT_SOURCE: z.enum(['fakestore', 'fixture']).default('fakestore'),
  PRODUCT_SOURCE_URL: z.string().url().default('https://fakestoreapi.com/products'),
  PRODUCT_CACHE_TTL_SECONDS: z.coerce.number().int().min(0).default(300),
  PRODUCT_FETCH_TIMEOUT_MS: z.coerce.number().int().min(100).default(3000),
  FEATURED_PRODUCT_IDS: csvPositiveInts,

  SALES_INBOX_EMAIL: z.string().email().default('sales@example.com'),
  EMAIL_FROM: z.string().min(3).default('Catalogue <no-reply@example.com>'),
  EMAIL_PROVIDER: z.enum(['smtp', 'console']).default('smtp'),
  SMTP_HOST: z.string().default('localhost'),
  SMTP_PORT: z.coerce.number().int().min(1).max(65535).default(1025),
  SMTP_SECURE: z
    .enum(['true', 'false'])
    .default('false')
    .transform((v) => v === 'true'),
  SMTP_USER: z.string().optional(),
  SMTP_PASS: z.string().optional(),

  CORS_ORIGINS: csvStrings,
  ENQUIRY_RATE_LIMIT_MAX: z.coerce.number().int().min(1).default(10),
});

export interface Config {
  port: number;
  product: {
    source: 'fakestore' | 'fixture';
    url: string;
    cacheTtlSeconds: number;
    fetchTimeoutMs: number;
    featuredIds: number[];
  };
  email: {
    provider: 'smtp' | 'console';
    salesInbox: string;
    from: string;
    smtp: { host: string; port: number; secure: boolean; user?: string; pass?: string };
  };
  http: { corsOrigins: string[]; enquiryRateLimitMax: number };
}

/** Parses and validates configuration once at startup, so bad config fails fast with a clear message. */
export function loadConfig(env: NodeJS.ProcessEnv = process.env): Config {
  const result = envSchema.safeParse(env);
  if (!result.success) {
    const details = result.error.issues.map((i) => `- ${i.path.join('.')}: ${i.message}`).join('\n');
    throw new Error(`Invalid configuration:\n${details}`);
  }
  const e = result.data;
  return {
    port: e.PORT,
    product: {
      source: e.PRODUCT_SOURCE,
      url: e.PRODUCT_SOURCE_URL,
      cacheTtlSeconds: e.PRODUCT_CACHE_TTL_SECONDS,
      fetchTimeoutMs: e.PRODUCT_FETCH_TIMEOUT_MS,
      featuredIds: e.FEATURED_PRODUCT_IDS,
    },
    email: {
      provider: e.EMAIL_PROVIDER,
      salesInbox: e.SALES_INBOX_EMAIL,
      from: e.EMAIL_FROM,
      smtp: {
        host: e.SMTP_HOST,
        port: e.SMTP_PORT,
        secure: e.SMTP_SECURE,
        user: e.SMTP_USER || undefined,
        pass: e.SMTP_PASS || undefined,
      },
    },
    http: { corsOrigins: e.CORS_ORIGINS, enquiryRateLimitMax: e.ENQUIRY_RATE_LIMIT_MAX },
  };
}
