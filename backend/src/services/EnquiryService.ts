import { EmailDeliveryError } from '../domain/errors';
import type { Enquiry } from '../domain/types';
import type { EmailSender } from '../ports/EmailSender';
import type { CatalogueService } from './CatalogueService';
import { buildEnquiryEmail, type EnquiryEmailConfig } from './enquiryEmail';

export class EnquiryService {
  constructor(
    private readonly catalogue: CatalogueService,
    private readonly email: EmailSender,
    private readonly config: EnquiryEmailConfig,
  ) {}

  /** Throws NotFoundError for an unknown product and EmailDeliveryError if the email cannot be sent. */
  async submit(enquiry: Enquiry): Promise<void> {
    const product = await this.catalogue.getProduct(enquiry.productId);
    try {
      await this.email.send(buildEnquiryEmail(product, enquiry, this.config));
    } catch (cause) {
      throw new EmailDeliveryError({ cause });
    }
  }
}
