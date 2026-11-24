import cors from 'cors';
import express, { type NextFunction, type Request, type RequestHandler, type Response } from 'express';
import { rateLimit } from 'express-rate-limit';
import helmet from 'helmet';
import { ValidationError } from '../domain/errors';
import type { Product } from '../domain/types';
import type { CatalogueService } from '../services/CatalogueService';
import type { EnquiryService } from '../services/EnquiryService';
import { errorHandler, notFoundHandler } from './errorHandler';
import { enquirySchema, productIdSchema, searchQuerySchema, toFieldErrors } from './schemas';

export interface AppDeps {
  catalogue: CatalogueService;
  enquiries: EnquiryService;
  enquiryRateLimitMax: number;
  corsOrigins: string[];
}

/** The wire contract with the Android client. Explicit, so internal fields never leak by accident. */
const toDto = (p: Product) => ({
  id: p.id,
  title: p.title,
  description: p.description,
  price: p.price,
  category: p.category,
  imageUrl: p.imageUrl,
  rating: { rate: p.rating.rate, count: p.rating.count },
});

/** Express 4 does not catch rejected promises from async handlers; this forwards them to the error middleware. */
const asyncHandler =
  (fn: (req: Request, res: Response) => Promise<void>): RequestHandler =>
  (req: Request, res: Response, next: NextFunction) => {
    fn(req, res).catch(next);
  };

export function createApp(deps: AppDeps) {
  const app = express();
  app.disable('x-powered-by');
  app.use(helmet());
  if (deps.corsOrigins.length > 0) app.use(cors({ origin: deps.corsOrigins }));
  app.use(express.json({ limit: '10kb' }));

  app.get('/health', (_req, res) => {
    res.json({ status: 'ok' });
  });

  const enquiryLimiter = rateLimit({
    windowMs: 15 * 60 * 1000,
    limit: deps.enquiryRateLimitMax,
    standardHeaders: 'draft-7',
    legacyHeaders: false,
    message: { error: 'Too many enquiries. Please try again later.' },
  });

  const api = express.Router();

  api.get(
    '/home',
    asyncHandler(async (_req, res) => {
      res.json({ featured: (await deps.catalogue.featured()).map(toDto) });
    }),
  );

  api.get(
    '/categories',
    asyncHandler(async (_req, res) => {
      res.json({ categories: await deps.catalogue.categories() });
    }),
  );

  api.get(
    '/categories/:name',
    asyncHandler(async (req, res) => {
      const { topRated, others } = await deps.catalogue.category(req.params.name ?? '');
      res.json({ topRated: topRated.map(toDto), others: others.map(toDto) });
    }),
  );

  api.get(
    '/products',
    asyncHandler(async (req, res) => {
      const query = searchQuerySchema.safeParse(req.query.q);
      if (!query.success) throw new ValidationError({ q: query.error.issues[0]?.message ?? 'Invalid search term' });
      res.json({ products: (await deps.catalogue.search(query.data)).map(toDto) });
    }),
  );

  api.get(
    '/products/:id',
    asyncHandler(async (req, res) => {
      const id = productIdSchema.safeParse(req.params.id);
      if (!id.success) throw new ValidationError({ id: 'Invalid product id' });
      res.json(toDto(await deps.catalogue.getProduct(id.data)));
    }),
  );

  api.post(
    '/enquiries',
    enquiryLimiter,
    asyncHandler(async (req, res) => {
      const parsed = enquirySchema.safeParse(req.body);
      if (!parsed.success) throw new ValidationError(toFieldErrors(parsed.error));
      await deps.enquiries.submit(parsed.data);
      res.status(201).json({ status: 'received' });
    }),
  );

  app.use('/api', api);
  app.use(notFoundHandler);
  app.use(errorHandler);
  return app;
}
