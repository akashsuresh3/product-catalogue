import { describe, expect, it, vi } from 'vitest';
import { FakeStoreProductSource } from '../src/adapters/FakeStoreProductSource';
import { UpstreamError } from '../src/domain/errors';

const upstreamItem = {
  id: 1,
  title: 'Backpack',
  price: 109.95,
  description: 'Fits a laptop',
  category: "men's clothing",
  image: 'https://fakestoreapi.com/img/1.jpg',
  rating: { rate: 3.9, count: 120 },
};

const fetchReturning = (body: unknown, status = 200) =>
  vi.fn(async () => new Response(JSON.stringify(body), { status })) as unknown as typeof fetch;

describe('FakeStoreProductSource', () => {
  it('maps the upstream shape to our product contract', async () => {
    const source = new FakeStoreProductSource('https://x.test/products', 1000, fetchReturning([upstreamItem]));

    expect(await source.listProducts()).toEqual([
      {
        id: 1,
        title: 'Backpack',
        description: 'Fits a laptop',
        price: 109.95,
        category: "men's clothing",
        imageUrl: 'https://fakestoreapi.com/img/1.jpg',
        rating: { rate: 3.9, count: 120 },
      },
    ]);
  });

  it('throws UpstreamError on a non-2xx response', async () => {
    const source = new FakeStoreProductSource('https://x.test', 1000, fetchReturning({}, 503));
    await expect(source.listProducts()).rejects.toBeInstanceOf(UpstreamError);
  });

  it('throws UpstreamError when the payload shape is unexpected', async () => {
    const source = new FakeStoreProductSource('https://x.test', 1000, fetchReturning([{ id: 'nope' }]));
    await expect(source.listProducts()).rejects.toBeInstanceOf(UpstreamError);
  });

  it('throws UpstreamError when the network call fails', async () => {
    const failing = vi.fn(async () => {
      throw new TypeError('fetch failed');
    }) as unknown as typeof fetch;
    const source = new FakeStoreProductSource('https://x.test', 1000, failing);
    await expect(source.listProducts()).rejects.toBeInstanceOf(UpstreamError);
  });
});
