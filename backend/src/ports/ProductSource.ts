import type { Product } from '../domain/types';

/**
 * Where product data comes from. The rest of the backend only depends on this interface, so the
 * Fake Store API can be replaced (database, CMS, ERP) by writing one new adapter.
 *
 * Kept deliberately small: with ~20 products the service filters in memory. A real source with
 * thousands of products would grow this port (paging, query, category filter).
 */
export interface ProductSource {
  listProducts(): Promise<Product[]>;
}
