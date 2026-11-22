export class NotFoundError extends Error {
  constructor(message = 'Not found') {
    super(message);
    this.name = 'NotFoundError';
  }
}

/** [fieldErrors] maps a request field name to a message the client can show next to that field. */
export class ValidationError extends Error {
  constructor(public readonly fieldErrors: Record<string, string>) {
    super('Validation failed');
    this.name = 'ValidationError';
  }
}

export class UpstreamError extends Error {
  constructor(message: string, options?: { cause?: unknown }) {
    super(message, options);
    this.name = 'UpstreamError';
  }
}

export class EmailDeliveryError extends Error {
  constructor(options?: { cause?: unknown }) {
    super('Could not deliver enquiry email', options);
    this.name = 'EmailDeliveryError';
  }
}
