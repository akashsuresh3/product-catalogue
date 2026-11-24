import type { EmailMessage, EmailSender } from '../ports/EmailSender';

/** Development helper (EMAIL_PROVIDER=console): prints the email instead of sending it. */
export class ConsoleEmailSender implements EmailSender {
  async send(message: EmailMessage): Promise<void> {
    console.log(`--- email to ${message.to} ---\nSubject: ${message.subject}\n\n${message.text}\n---`);
  }
}
