import type { Product } from '../domain/types';
import type { ProductSource } from '../ports/ProductSource';

const COOLDOWN_MS = 30_000;

/**
 * Decorator: if the primary source fails and there is nothing cached, serve the fallback
 * so the app still works (useful for demos and flaky third-party APIs). After a failure the
 * primary is skipped for 30 seconds so we do not pay its timeout on every request.
 */
export class FallbackProductSource implements ProductSource {
  private skipPrimaryUntil = 0;

  constructor(
    private readonly primary: ProductSource,
    private readonly fallback: ProductSource,
    private readonly now: () => number = Date.now,
  ) {}

  async listProducts(): Promise<Product[]> {
    if (this.now() >= this.skipPrimaryUntil) {
      try {
        return await this.primary.listProducts();
      } catch (error) {
        console.warn('Primary product source failed; using fallback data', error);
        this.skipPrimaryUntil = this.now() + COOLDOWN_MS;
      }
    }
    return this.fallback.listProducts();
  }
}
