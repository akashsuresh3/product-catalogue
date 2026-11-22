import { describe, expect, it } from 'vitest';
import { loadConfig } from '../src/config/env';

describe('loadConfig', () => {
  it('works with no environment at all (sensible local defaults)', () => {
    const config = loadConfig({});
    expect(config.port).toBe(3000);
    expect(config.product.source).toBe('fakestore');
    expect(config.product.featuredIds).toEqual([]);
    expect(config.email.provider).toBe('smtp');
    expect(config.email.smtp).toMatchObject({ host: 'localhost', port: 1025, secure: false });
    expect(config.http.corsOrigins).toEqual([]);
  });

  it('parses featured ids and booleans', () => {
    const config = loadConfig({ FEATURED_PRODUCT_IDS: '1, 2,3', SMTP_SECURE: 'true', PORT: '8080' });
    expect(config.product.featuredIds).toEqual([1, 2, 3]);
    expect(config.email.smtp.secure).toBe(true);
    expect(config.port).toBe(8080);
  });

  it('fails fast with a readable message on bad values', () => {
    expect(() => loadConfig({ PORT: 'abc' })).toThrow(/PORT/);
    expect(() => loadConfig({ FEATURED_PRODUCT_IDS: '1,x' })).toThrow(/FEATURED_PRODUCT_IDS/);
    expect(() => loadConfig({ SALES_INBOX_EMAIL: 'nope' })).toThrow(/SALES_INBOX_EMAIL/);
  });
});
