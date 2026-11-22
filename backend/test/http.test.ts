import request from 'supertest';
import { describe, expect, it } from 'vitest';
import { createApp } from '../src/http/app';
import { CatalogueService } from '../src/services/CatalogueService';
import { EnquiryService } from '../src/services/EnquiryService';
import type { Product } from '../src/domain/types';
import { makeProduct, RecordingEmailSender, sampleCatalogue, StaticProductSource } from './helpers';

function build(opts: { products?: Product[]; featuredIds?: number[]; rateLimit?: number } = {}) {
  const catalogue = new CatalogueService(new StaticProductSource(opts.products ?? sampleCatalogue), {
    featuredProductIds: opts.featuredIds ?? [],
  });
  const email = new RecordingEmailSender();
  const enquiries = new EnquiryService(catalogue, email, {
    salesInbox: 'sales@example.com',
    from: 'Catalogue <no-reply@example.com>',
  });
  const app = createApp({
    catalogue,
    enquiries,
    enquiryRateLimitMax: opts.rateLimit ?? 100,
    corsOrigins: [],
  });
  return { app, email };
}

const validEnquiry = {
  productId: 2,
  name: 'Asha Rao',
  email: 'asha@example.com',
  phone: '+61 412 345 678',
  message: 'Is this in stock?',
};

describe('catalogue endpoints', () => {
  it('GET /health', async () => {
    const res = await request(build().app).get('/health');
    expect(res.status).toBe(200);
    expect(res.headers['x-powered-by']).toBeUndefined();
  });

  it('GET /api/home returns 5 featured products in the contract shape', async () => {
    const res = await request(build().app).get('/api/home');
    expect(res.status).toBe(200);
    expect(res.body.featured).toHaveLength(5);
    expect(res.body.featured[0]).toEqual({
      id: 5,
      title: 'Product 5',
      description: 'Description 5',
      price: 10,
      category: 'jewelery',
      imageUrl: 'https://img.example/5.png',
      rating: { rate: 4.9, count: 1000 },
    });
  });

  it('GET /api/home honours curated featured ids', async () => {
    const res = await request(build({ featuredIds: [3, 1] }).app).get('/api/home');
    expect(res.body.featured.map((p: Product) => p.id)).toEqual([3, 1]);
  });

  it('GET /api/categories', async () => {
    const res = await request(build().app).get('/api/categories');
    expect(res.body.categories).toEqual(['electronics', 'jewelery', "men's clothing", "women's clothing"]);
  });

  it('GET /api/categories/:name handles names with spaces and apostrophes', async () => {
    const res = await request(build().app).get(`/api/categories/${encodeURIComponent("men's clothing")}`);
    expect(res.status).toBe(200);
    expect(res.body.topRated.map((p: Product) => p.id)).toEqual([8, 7]);
    expect(res.body.others).toEqual([]);
  });

  it('GET /api/categories/:name returns 404 for an unknown category', async () => {
    const res = await request(build().app).get('/api/categories/toys');
    expect(res.status).toBe(404);
  });

  it('GET /api/products?q= returns only matching products', async () => {
    const products = [
      makeProduct(1, { title: 'Gold Ring' }),
      makeProduct(2, { title: 'Monitor', description: 'Sharp', category: 'electronics' }),
    ];
    const res = await request(build({ products }).app).get('/api/products?q=ring');
    expect(res.status).toBe(200);
    expect(res.body.products.map((p: Product) => p.id)).toEqual([1]);
  });

  it('GET /api/products?q= with no match returns an empty list', async () => {
    const res = await request(build().app).get('/api/products?q=zzz');
    expect(res.status).toBe(200);
    expect(res.body.products).toEqual([]);
  });

  it.each(['/api/products', '/api/products?q=', '/api/products?q=%20%20'])('rejects a blank search: %s', async (url) => {
    const res = await request(build().app).get(url);
    expect(res.status).toBe(400);
    expect(res.body.errors.q).toBeTruthy();
  });

  it('GET /api/products/:id', async () => {
    const res = await request(build().app).get('/api/products/2');
    expect(res.status).toBe(200);
    expect(res.body.id).toBe(2);
  });

  it('GET /api/products/:id returns 404 when missing and 400 when malformed', async () => {
    const { app } = build();
    expect((await request(app).get('/api/products/999')).status).toBe(404);
    expect((await request(app).get('/api/products/abc')).status).toBe(400);
  });

  it('unknown routes return a JSON 404', async () => {
    const res = await request(build().app).get('/nope');
    expect(res.status).toBe(404);
    expect(res.body.error).toBeTruthy();
  });
});

describe('POST /api/enquiries', () => {
  it('sends an email to the sales inbox and returns 201', async () => {
    const { app, email } = build();

    const res = await request(app).post('/api/enquiries').send(validEnquiry);

    expect(res.status).toBe(201);
    expect(email.sent).toHaveLength(1);
    const message = email.sent[0]!;
    expect(message.to).toBe('sales@example.com');
    expect(message.from).toBe('Catalogue <no-reply@example.com>');
    expect(message.replyTo).toEqual({ name: 'Asha Rao', address: 'asha@example.com' });
    expect(message.subject).toContain('Product 2');
    expect(message.text).toContain('Is this in stock?');
    expect(message.text).toContain('+61 412 345 678');
  });

  it('treats a blank phone as not provided', async () => {
    const { app, email } = build();

    const res = await request(app).post('/api/enquiries').send({ ...validEnquiry, phone: '  ' });

    expect(res.status).toBe(201);
    expect(email.sent[0]!.text).toContain('not provided');
  });

  it('returns field errors and sends nothing when the body is invalid', async () => {
    const { app, email } = build();

    const res = await request(app).post('/api/enquiries').send({ productId: 2, name: ' ', email: '', message: '' });

    expect(res.status).toBe(400);
    expect(res.body.errors).toEqual({
      name: 'Please enter your name',
      email: 'Please enter your email',
      message: 'Please tell us what you need',
    });
    expect(email.sent).toHaveLength(0);
  });

  it('validates email and phone formats', async () => {
    const { app } = build();

    const res = await request(app)
      .post('/api/enquiries')
      .send({ ...validEnquiry, email: 'not-an-email', phone: 'call me maybe' });

    expect(res.status).toBe(400);
    expect(res.body.errors.email).toBe('Enter a valid email address');
    expect(res.body.errors.phone).toBe('Enter a valid phone number');
  });

  it('rejects an over-long message', async () => {
    const res = await request(build().app)
      .post('/api/enquiries')
      .send({ ...validEnquiry, message: 'x'.repeat(1001) });
    expect(res.status).toBe(400);
    expect(res.body.errors.message).toBe('Message is too long');
  });

  it('returns 404 for an unknown product and sends nothing', async () => {
    const { app, email } = build();
    const res = await request(app).post('/api/enquiries').send({ ...validEnquiry, productId: 999 });
    expect(res.status).toBe(404);
    expect(email.sent).toHaveLength(0);
  });

  it('returns 502 when the email cannot be delivered', async () => {
    const { app, email } = build();
    email.failWith = new Error('SMTP down');

    const res = await request(app).post('/api/enquiries').send(validEnquiry);

    expect(res.status).toBe(502);
    expect(res.body.error).toBeTruthy();
  });

  it('returns 400 for malformed JSON', async () => {
    const res = await request(build().app)
      .post('/api/enquiries')
      .set('Content-Type', 'application/json')
      .send('{not json');
    expect(res.status).toBe(400);
  });

  it('keeps line breaks from product data out of the email subject', async () => {
    const products = [makeProduct(2, { title: 'Ring\r\nBcc: evil@example.com' })];
    const { app, email } = build({ products });

    await request(app).post('/api/enquiries').send(validEnquiry);

    expect(email.sent[0]!.subject).not.toMatch(/[\r\n]/);
  });

  it('rate limits repeated enquiries', async () => {
    const { app } = build({ rateLimit: 2 });
    await request(app).post('/api/enquiries').send(validEnquiry);
    await request(app).post('/api/enquiries').send(validEnquiry);

    const res = await request(app).post('/api/enquiries').send(validEnquiry);

    expect(res.status).toBe(429);
  });
});
