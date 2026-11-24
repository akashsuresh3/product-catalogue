import type { Product } from '../domain/types';
import type { ProductSource } from '../ports/ProductSource';

const RETRY_AFTER_FAILURE_MS = 30_000;

/**
 * Decorator: in-memory TTL cache with request de-duplication and stale-if-error.
 * If a refresh fails and we hold older data, keep serving it and retry in at most 30 seconds,
 * so an upstream outage does not make every request wait for a timeout.
 */
export class CachedProductSource implements ProductSource {
  private cache?: { at: number; data: Product[] };
  private inFlight?: Promise<Product[]>;

  constructor(
    private readonly inner: ProductSource,
    private readonly ttlMs: number,
    private readonly now: () => number = Date.now,
  ) {}

  listProducts(): Promise<Product[]> {
    if (this.cache && this.now() - this.cache.at < this.ttlMs) {
      return Promise.resolve(this.cache.data);
    }
    this.inFlight ??= this.refresh().finally(() => {
      this.inFlight = undefined;
    });
    return this.inFlight;
  }

  private async refresh(): Promise<Product[]> {
    try {
      const data = await this.inner.listProducts();
      this.cache = { at: this.now(), data };
      return data;
    } catch (error) {
      if (!this.cache) throw error;
      console.warn('Product refresh failed; serving stale data', error);
      const retryAfter = Math.min(this.ttlMs, RETRY_AFTER_FAILURE_MS);
      this.cache = { at: this.now() - this.ttlMs + retryAfter, data: this.cache.data };
      return this.cache.data;
    }
  }
}
