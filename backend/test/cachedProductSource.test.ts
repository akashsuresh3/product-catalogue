import { describe, expect, it } from 'vitest';
import { CachedProductSource } from '../src/adapters/CachedProductSource';
import type { Product } from '../src/domain/types';
import type { ProductSource } from '../src/ports/ProductSource';
import { makeProduct } from './helpers';

class ScriptedSource implements ProductSource {
  calls = 0;
  fail = false;
  constructor(private data: Product[]) {}
  async listProducts(): Promise<Product[]> {
    this.calls++;
    if (this.fail) throw new Error('upstream down');
    return this.data;
  }
}

const setup = (ttlMs = 60_000) => {
  const inner = new ScriptedSource([makeProduct(1)]);
  let time = 0;
  const cached = new CachedProductSource(inner, ttlMs, () => time);
  return { inner, cached, advance: (ms: number) => (time += ms) };
};

describe('CachedProductSource', () => {
  it('serves from cache within the TTL', async () => {
    const { inner, cached, advance } = setup();
    await cached.listProducts();
    advance(59_000);
    await cached.listProducts();
    expect(inner.calls).toBe(1);
  });

  it('refreshes after the TTL', async () => {
    const { inner, cached, advance } = setup();
    await cached.listProducts();
    advance(60_001);
    await cached.listProducts();
    expect(inner.calls).toBe(2);
  });

  it('shares one upstream call between concurrent requests', async () => {
    const { inner, cached } = setup();
    await Promise.all([cached.listProducts(), cached.listProducts(), cached.listProducts()]);
    expect(inner.calls).toBe(1);
  });

  it('serves stale data when a refresh fails, and backs off before retrying', async () => {
    const { inner, cached, advance } = setup();
    await cached.listProducts();
    advance(60_001);
    inner.fail = true;

    expect(await cached.listProducts()).toHaveLength(1); // stale, upstream tried once
    expect(inner.calls).toBe(2);

    advance(10_000);
    await cached.listProducts(); // within the 30s back-off: no new upstream call
    expect(inner.calls).toBe(2);

    advance(21_000);
    await cached.listProducts(); // back-off over: tries again
    expect(inner.calls).toBe(3);
  });

  it('rethrows when there is nothing cached to fall back on', async () => {
    const { inner, cached } = setup();
    inner.fail = true;
    await expect(cached.listProducts()).rejects.toThrow('upstream down');
  });
});
