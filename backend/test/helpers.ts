import type { Product } from '../src/domain/types';
import type { EmailMessage, EmailSender } from '../src/ports/EmailSender';
import type { ProductSource } from '../src/ports/ProductSource';

export function makeProduct(
  id: number,
  overrides: Partial<Omit<Product, 'rating'>> & { rate?: number; count?: number } = {},
): Product {
  const { rate = 4, count = 10, ...rest } = overrides;
  return {
    id,
    title: `Product ${id}`,
    description: `Description ${id}`,
    price: 10,
    category: 'electronics',
    imageUrl: `https://img.example/${id}.png`,
    rating: { rate, count },
    ...rest,
  };
}

export class StaticProductSource implements ProductSource {
  calls = 0;
  constructor(public products: Product[]) {}
  async listProducts(): Promise<Product[]> {
    this.calls++;
    return this.products;
  }
}

export class RecordingEmailSender implements EmailSender {
  sent: EmailMessage[] = [];
  failWith?: Error;
  async send(message: EmailMessage): Promise<void> {
    if (this.failWith) throw this.failWith;
    this.sent.push(message);
  }
}

/** A mixed catalogue used by several tests. Ratings are chosen so ordering and ties are meaningful. */
export const sampleCatalogue: Product[] = [
  makeProduct(1, { rate: 4.0, count: 10 }),
  makeProduct(2, { rate: 4.8, count: 100 }),
  makeProduct(3, { rate: 3.0, count: 5 }),
  makeProduct(4, { rate: 4.5, count: 50 }),
  makeProduct(5, { category: 'jewelery', rate: 4.9, count: 1000 }),
  makeProduct(6, { category: 'jewelery', rate: 2.0, count: 3 }),
  makeProduct(7, { category: "men's clothing", rate: 4.1, count: 20 }),
  makeProduct(8, { category: "men's clothing", rate: 4.1, count: 200 }),
  makeProduct(9, { category: "women's clothing", rate: 3.5, count: 7 }),
];
