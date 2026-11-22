import { describe, expect, it } from 'vitest';
import { FallbackProductSource } from '../src/adapters/FallbackProductSource';
import type { Product } from '../src/domain/types';
import type { ProductSource } from '../src/ports/ProductSource';
import { makeProduct, StaticProductSource } from './helpers';

class FlakySource implements ProductSource {
  calls = 0;
  fail = true;
  async listProducts(): Promise<Product[]> {
    this.calls++;
    if (this.fail) throw new Error('down');
    return [makeProduct(1, { title: 'live' })];
  }
}

describe('FallbackProductSource', () => {
  it('uses the primary when it works', async () => {
    const primary = new FlakySource();
    primary.fail = false;
    const source = new FallbackProductSource(primary, new StaticProductSource([makeProduct(2, { title: 'fallback' })]));
    expect((await source.listProducts())[0]!.title).toBe('live');
  });

  it('uses the fallback when the primary fails', async () => {
    const source = new FallbackProductSource(new FlakySource(), new StaticProductSource([makeProduct(2, { title: 'fallback' })]));
    expect((await source.listProducts())[0]!.title).toBe('fallback');
  });

  it('skips the primary for 30 seconds after a failure, then tries it again', async () => {
    const primary = new FlakySource();
    let time = 0;
    const source = new FallbackProductSource(primary, new StaticProductSource([makeProduct(2)]), () => time);

    await source.listProducts();
    expect(primary.calls).toBe(1);

    time += 29_000;
    await source.listProducts();
    expect(primary.calls).toBe(1);

    time += 2_000;
    primary.fail = false;
    expect((await source.listProducts())[0]!.title).toBe('live');
    expect(primary.calls).toBe(2);
  });
});
