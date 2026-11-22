import { z, type ZodError } from 'zod';

// Messages mirror the Android client's validator so the user sees the same wording either way.
const digitCount = (value: string) => value.replace(/\D/g, '').length;

export const enquirySchema = z.object({
  productId: z.number({ required_error: 'Product is required', invalid_type_error: 'Invalid product' }).int().positive(),
  name: z
    .string({ required_error: 'Please enter your name', invalid_type_error: 'Please enter your name' })
    .trim()
    .min(1, 'Please enter your name')
    .max(100, 'Name is too long'),
  email: z
    .string({ required_error: 'Please enter your email', invalid_type_error: 'Please enter your email' })
    .trim()
    .min(1, 'Please enter your email')
    .max(254, 'Enter a valid email address')
    .email('Enter a valid email address'),
  phone: z
    .string()
    .trim()
    .nullish()
    .refine((v) => !v || (/^[+\d\s()-]+$/.test(v) && digitCount(v) >= 8 && digitCount(v) <= 15), {
      message: 'Enter a valid phone number',
    })
    .transform((v) => (v ? v : undefined)),
  message: z
    .string({ required_error: 'Please tell us what you need', invalid_type_error: 'Please tell us what you need' })
    .trim()
    .min(1, 'Please tell us what you need')
    .max(1000, 'Message is too long'),
});

export const searchQuerySchema = z
  .string({ required_error: 'Enter a search term', invalid_type_error: 'Enter a search term' })
  .trim()
  .min(1, 'Enter a search term')
  .max(100, 'Search term is too long');

export const productIdSchema = z.coerce.number().int().positive();

/** First message per field, in the shape the client expects: { "email": "Enter a valid email address" }. */
export function toFieldErrors(error: ZodError): Record<string, string> {
  const errors: Record<string, string> = {};
  for (const issue of error.issues) {
    const key = String(issue.path[0] ?? 'body');
    if (!(key in errors)) errors[key] = issue.message;
  }
  return errors;
}
