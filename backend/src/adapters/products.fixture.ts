import type { Product } from '../domain/types';

// Synthetic data (not copied from the Fake Store API) used when the live API is unreachable,
// and for PRODUCT_SOURCE=fixture. Four categories x four products so "top rated" and
// "additional products" both have content.
const rows: Array<[title: string, price: number, category: string, rate: number, count: number]> = [
  ['27-inch 4K Monitor', 329.0, 'electronics', 4.2, 210],
  ['Portable SSD 1TB', 109.5, 'electronics', 4.7, 540],
  ['Wireless Earbuds', 79.99, 'electronics', 3.9, 320],
  ['USB-C Hub 7-in-1', 45.0, 'electronics', 4.4, 150],
  ['Silver Pendant Necklace', 64.0, 'jewelery', 4.4, 130],
  ['Gold-plated Ring', 120.0, 'jewelery', 3.9, 60],
  ['Pearl Drop Earrings', 89.0, 'jewelery', 4.8, 95],
  ['Stainless Steel Bracelet', 54.5, 'jewelery', 4.1, 210],
  ['Slim Fit Jacket', 89.99, "men's clothing", 4.1, 300],
  ['Cotton Crew T-Shirt', 18.0, "men's clothing", 4.6, 880],
  ['Chino Trousers', 49.0, "men's clothing", 4.3, 410],
  ['Wool Blend Scarf', 27.5, "men's clothing", 3.7, 75],
  ['Rain Shell Jacket', 99.0, "women's clothing", 4.3, 260],
  ['Knit Cardigan', 58.0, "women's clothing", 3.8, 90],
  ['Linen Summer Dress', 72.0, "women's clothing", 4.5, 340],
  ['High-waist Jeans', 65.0, "women's clothing", 4.0, 520],
];

export const fixtureProducts: Product[] = rows.map(([title, price, category, rate, count], index) => ({
  id: index + 1,
  title,
  description: `${title}. Sample item shown when the live product API is unavailable.`,
  price,
  category,
  imageUrl: `https://placehold.co/400x400/png?text=${encodeURIComponent(title)}`,
  rating: { rate, count },
}));
