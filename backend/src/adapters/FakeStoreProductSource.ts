import { z } from 'zod';
import { UpstreamError } from '../domain/errors';
import type { Product } from '../domain/types';
import type { ProductSource } from '../ports/ProductSource';

// Validate what the upstream really returns. If it changes shape we fail loudly (and fall back)
// instead of passing garbage to the app.
const upstreamSchema = z.array(
  z.object({
    id: z.number().int(),
    title: z.string(),
    price: z.number(),
    description: z.string(),
    category: z.string(),
    image: z.string(),
    rating: z.object({ rate: z.number(), count: z.number() }),
  }),
);

export class FakeStoreProductSource implements ProductSource {
  constructor(
    private readonly url: string,
    private readonly timeoutMs: number,
    private readonly fetchFn: typeof fetch = fetch,
  ) {}

  async listProducts(): Promise<Product[]> {
    let response: Response;
    try {
      response = await this.fetchFn(this.url, {
        signal: AbortSignal.timeout(this.timeoutMs),
        headers: { accept: 'application/json', 'user-agent': 'product-catalogue-backend' },
      });
    } catch (cause) {
      throw new UpstreamError('Product source unreachable', { cause });
    }
    if (!response.ok) {
      throw new UpstreamError(`Product source returned HTTP ${response.status}`);
    }

    const body: unknown = await response.json().catch(() => undefined);
    const parsed = upstreamSchema.safeParse(body);
    if (!parsed.success) {
      throw new UpstreamError('Product source returned an unexpected payload');
    }

    // Upstream naming (image) is mapped to our contract (imageUrl) here and nowhere else.
    return parsed.data.map((p) => ({
      id: p.id,
      title: p.title,
      description: p.description,
      price: p.price,
      category: p.category,
      imageUrl: p.image,
      rating: { rate: p.rating.rate, count: p.rating.count },
    }));
  }
}
