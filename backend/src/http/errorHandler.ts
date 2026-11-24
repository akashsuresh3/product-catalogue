import type { ErrorRequestHandler, RequestHandler } from 'express';
import { EmailDeliveryError, NotFoundError, UpstreamError, ValidationError } from '../domain/errors';

export const notFoundHandler: RequestHandler = (_req, res) => {
  res.status(404).json({ error: 'Not found' });
};

// Express recognises error middleware by its 4 arguments, so the unused `_next` must stay.
export const errorHandler: ErrorRequestHandler = (err, _req, res, _next) => {
  if (err instanceof ValidationError) {
    res.status(400).json({ errors: err.fieldErrors });
    return;
  }
  if (err instanceof NotFoundError) {
    res.status(404).json({ error: err.message });
    return;
  }
  if (err instanceof EmailDeliveryError) {
    console.error('Enquiry email could not be delivered', err.cause);
    res.status(502).json({ error: 'We could not send your enquiry right now. Please try again.' });
    return;
  }
  if (err instanceof UpstreamError) {
    console.error('Product source failed', err);
    res.status(502).json({ error: 'Product data is temporarily unavailable.' });
    return;
  }
  // Client errors raised by Express middleware, e.g. malformed JSON (400) or an oversized body (413).
  const status = (err as { status?: unknown } | null)?.status;
  if (typeof status === 'number' && status >= 400 && status < 500) {
    res.status(status).json({ error: 'Bad request' });
    return;
  }
  console.error('Unhandled error', err);
  res.status(500).json({ error: 'Internal server error' });
};
