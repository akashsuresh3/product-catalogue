export interface EmailMessage {
  to: string;
  from: string;
  replyTo?: { name: string; address: string };
  subject: string;
  text: string;
}

/** Email provider boundary. SMTP today; SES/SendGrid/etc. later is a new adapter, nothing else changes. */
export interface EmailSender {
  send(message: EmailMessage): Promise<void>;
}
