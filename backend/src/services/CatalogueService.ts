import { NotFoundError } from '../domain/errors';
import type { Product } from '../domain/types';
import type { ProductSource } from '../ports/ProductSource';

export interface CatalogueOptions {
  /** Curated product IDs for the home page. Empty means "use the top rated". */
  featuredProductIds: number[];
  featuredCount?: number;
  topRatedPerCategory?: number;
}

export interface CategoryProducts {
  topRated: Product[];
  others: Product[];
}

/** Highest rating first; more reviews, then lower id, break ties so ordering is stable. */
const byRating = (a: Product, b: Product) =>
  b.rating.rate - a.rating.rate || b.rating.count - a.rating.count || a.id - b.id;

export class CatalogueService {
  private readonly featuredCount: number;
  private readonly topRatedPerCategory: number;

  constructor(
    private readonly source: ProductSource,
    private readonly options: CatalogueOptions,
  ) {
    this.featuredCount = options.featuredCount ?? 5;
    this.topRatedPerCategory = options.topRatedPerCategory ?? 3;
  }

  /** The curated list if configured (unknown IDs are skipped), otherwise the top rated overall. */
  async featured(): Promise<Product[]> {
    const all = await this.source.listProducts();
    if (this.options.featuredProductIds.length > 0) {
      const byId = new Map(all.map((p) => [p.id, p] as const));
      return this.options.featuredProductIds
        .map((id) => byId.get(id))
        .filter((p): p is Product => p !== undefined);
    }
    return [...all].sort(byRating).slice(0, this.featuredCount);
  }

  async categories(): Promise<string[]> {
    const all = await this.source.listProducts();
    return [...new Set(all.map((p) => p.category))].sort();
  }

  async category(name: string): Promise<CategoryProducts> {
    const wanted = name.trim().toLowerCase();
    const all = await this.source.listProducts();
    const inCategory = all.filter((p) => p.category.toLowerCase() === wanted).sort(byRating);
    if (inCategory.length === 0) throw new NotFoundError('Category not found');
    return {
      topRated: inCategory.slice(0, this.topRatedPerCategory),
      others: inCategory.slice(this.topRatedPerCategory),
    };
  }

  /**
   * Case-insensitive. Every word of the query must appear somewhere in the title, description
   * or category ("gold ring" matches "Gold Ring" and "ring, gold-plated", not "Silver Ring").
   */
  async search(query: string): Promise<Product[]> {
    const terms = query.toLowerCase().split(/\s+/).filter(Boolean);
    if (terms.length === 0) return [];
    const all = await this.source.listProducts();
    return all
      .filter((p) => {
        const haystack = `${p.title} ${p.description} ${p.category}`.toLowerCase();
        return terms.every((term) => haystack.includes(term));
      })
      .sort(byRating);
  }

  async getProduct(id: number): Promise<Product> {
    const all = await this.source.listProducts();
    const product = all.find((p) => p.id === id);
    if (!product) throw new NotFoundError('Product not found');
    return product;
  }
}
