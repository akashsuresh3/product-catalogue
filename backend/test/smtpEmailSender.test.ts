import { describe, expect, it, vi } from 'vitest';
import { SmtpEmailSender } from '../src/adapters/SmtpEmailSender';

describe('SmtpEmailSender', () => {
  it('hands the message to the transport unchanged', async () => {
    const sendMail = vi.fn().mockResolvedValue({});
    const sender = new SmtpEmailSender({ host: 'h', port: 1, secure: false }, { sendMail });

    await sender.send({
      to: 'sales@example.com',
      from: 'Catalogue <no-reply@example.com>',
      replyTo: { name: 'Asha', address: 'asha@example.com' },
      subject: 'Hello',
      text: 'Body',
    });

    expect(sendMail).toHaveBeenCalledWith({
      to: 'sales@example.com',
      from: 'Catalogue <no-reply@example.com>',
      replyTo: { name: 'Asha', address: 'asha@example.com' },
      subject: 'Hello',
      text: 'Body',
    });
  });

  it('propagates transport failures', async () => {
    const sendMail = vi.fn().mockRejectedValue(new Error('connection refused'));
    const sender = new SmtpEmailSender({ host: 'h', port: 1, secure: false }, { sendMail });

    await expect(
      sender.send({ to: 'a@b.co', from: 'c@d.co', subject: 's', text: 't' }),
    ).rejects.toThrow('connection refused');
  });
});
