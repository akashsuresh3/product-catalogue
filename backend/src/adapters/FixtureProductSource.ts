import type { Product } from '../domain/types';
import type { ProductSource } from '../ports/ProductSource';
import { fixtureProducts } from './products.fixture';

export class FixtureProductSource implements ProductSource {
  async listProducts(): Promise<Product[]> {
    return fixtureProducts;
  }
}
