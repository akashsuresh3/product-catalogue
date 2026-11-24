import type { Enquiry, Product } from '../domain/types';
import type { EmailMessage } from '../ports/EmailSender';

export interface EnquiryEmailConfig {
  salesInbox: string;
  from: string;
}

/** Header values must never contain line breaks (header injection), whatever the source of the text. */
const singleLine = (value: string) => value.replace(/[\r\n]+/g, ' ').trim();

export function buildEnquiryEmail(product: Product, enquiry: Enquiry, config: EnquiryEmailConfig): EmailMessage {
  const customerName = singleLine(enquiry.name);
  const text = [
    'New product enquiry',
    '',
    `Product:  ${singleLine(product.title)} (#${product.id})`,
    `Category: ${singleLine(product.category)}`,
    `Price:    $${product.price.toFixed(2)}`,
    '',
    `Customer: ${customerName}`,
    `Email:    ${enquiry.email}`,
    `Phone:    ${enquiry.phone ?? 'not provided'}`,
    '',
    'Message:',
    enquiry.message,
  ].join('\n');

  return {
    to: config.salesInbox,
    // Sent from our own address (not the customer's) so mail providers do not reject it as spoofed;
    // Reply-To lets sales answer the customer directly.
    from: config.from,
    replyTo: { name: customerName, address: enquiry.email },
    subject: `New enquiry: ${singleLine(product.title)} (#${product.id})`,
    text,
  };
}
