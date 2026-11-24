import nodemailer from 'nodemailer';
import type { SendMailOptions } from 'nodemailer';
import type { EmailMessage, EmailSender } from '../ports/EmailSender';

export interface SmtpConfig {
  host: string;
  port: number;
  secure: boolean;
  user?: string;
  pass?: string;
}

interface MailTransport {
  sendMail(options: SendMailOptions): Promise<unknown>;
}

/**
 * Works with any SMTP server: Mailpit locally, and Amazon SES in AWS via its SMTP interface
 * (host email-smtp.<region>.amazonaws.com + SMTP credentials), with no code change.
 */
export class SmtpEmailSender implements EmailSender {
  private readonly transport: MailTransport;

  constructor(config: SmtpConfig, transport?: MailTransport) {
    this.transport =
      transport ??
      nodemailer.createTransport({
        host: config.host,
        port: config.port,
        secure: config.secure,
        auth: config.user && config.pass ? { user: config.user, pass: config.pass } : undefined,
        connectionTimeout: 5_000,
        socketTimeout: 10_000,
      });
  }

  async send(message: EmailMessage): Promise<void> {
    await this.transport.sendMail({
      from: message.from,
      to: message.to,
      replyTo: message.replyTo,
      subject: message.subject,
      text: message.text,
    });
  }
}
