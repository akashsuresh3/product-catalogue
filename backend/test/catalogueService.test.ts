import { describe, expect, it } from 'vitest';
import { NotFoundError } from '../src/domain/errors';
import { CatalogueService } from '../src/services/CatalogueService';
import { makeProduct, sampleCatalogue, StaticProductSource } from './helpers';

const service = (featuredProductIds: number[] = []) =>
  new CatalogueService(new StaticProductSource(sampleCatalogue), { featuredProductIds });

const ids = (products: { id: number }[]) => products.map((p) => p.id);

describe('featured', () => {
  it('returns the 5 highest rated when nothing is curated (ties broken by review count)', async () => {
    expect(ids(await service().featured())).toEqual([5, 2, 4, 8, 7]);
  });

  it('returns the curated products in the configured order', async () => {
    expect(ids(await service([3, 1]).featured())).toEqual([3, 1]);
  });

  it('skips curated ids that do not exist', async () => {
    expect(ids(await service([3, 999, 1]).featured())).toEqual([3, 1]);
  });
});

describe('categories', () => {
  it('lists each category once, sorted', async () => {
    expect(await service().categories()).toEqual(['electronics', 'jewelery', "men's clothing", "women's clothing"]);
  });
});

describe('category', () => {
  it('splits into top 3 by rating and the rest', async () => {
    const result = await service().category('electronics');
    expect(ids(result.topRated)).toEqual([2, 4, 1]);
    expect(ids(result.others)).toEqual([3]);
  });

  it('matches the name case-insensitively and ignores surrounding spaces', async () => {
    const result = await service().category("  MEN'S Clothing ");
    expect(ids(result.topRated)).toEqual([8, 7]);
    expect(result.others).toEqual([]);
  });

  it('never repeats a product between topRated and others', async () => {
    const result = await service().category('electronics');
    const all = [...ids(result.topRated), ...ids(result.others)];
    expect(new Set(all).size).toBe(all.length);
  });

  it('throws NotFoundError for an unknown category', async () => {
    await expect(service().category('toys')).rejects.toBeInstanceOf(NotFoundError);
  });
});

describe('search', () => {
  const products = [
    makeProduct(1, { title: 'Gold Ring', description: 'Shiny band', category: 'jewelery', rate: 4.0 }),
    makeProduct(2, { title: 'Silver Necklace', description: 'Pairs with any ring', category: 'jewelery', rate: 4.5 }),
    makeProduct(3, { title: '4K Monitor', description: 'Sharp display', category: 'electronics', rate: 4.2 }),
  ];
  const search = (q: string) =>
    new CatalogueService(new StaticProductSource(products), { featuredProductIds: [] }).search(q);

  it('matches title and description, case-insensitively', async () => {
    expect(ids(await search('RING'))).toEqual([2, 1]); // ordered by rating
  });

  it('matches the category', async () => {
    expect(ids(await search('electronics'))).toEqual([3]);
  });

  it('requires every word to match', async () => {
    expect(ids(await search('gold ring'))).toEqual([1]);
  });

  it('returns only products that match', async () => {
    expect(await search('zzz')).toEqual([]);
  });

  it('returns nothing for a blank query', async () => {
    expect(await search('   ')).toEqual([]);
  });
});

describe('getProduct', () => {
  it('returns the product', async () => {
    expect((await service().getProduct(4)).id).toBe(4);
  });

  it('throws NotFoundError when missing', async () => {
    await expect(service().getProduct(999)).rejects.toBeInstanceOf(NotFoundError);
  });
});
