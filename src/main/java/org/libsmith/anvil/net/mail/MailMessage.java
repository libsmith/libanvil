package org.libsmith.anvil.net.mail;

import org.libsmith.anvil.net.mail.MailDelivery.DeliveryResult;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.annotation.Nonnull;
import javax.mail.*;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;


/**
 * @author Dmitriy Balakin <dmitriy.balakin@0x0000.ru>
 * @created 31.08.2015 17:17
 */
public class MailMessage extends MimeMessage {

    protected MailMessage() {
        super((Session) null);
    }

    public static Builder builder() {
        return new Builder();
    }

    public DeliveryResult sendVia(MailDelivery mailDelivery) {
        return mailDelivery.send(this);
    }

    public DeliveryResult sendVia(Supplier<? extends MailDelivery> mailDelivery) {
        return sendVia(mailDelivery.get());
    }

    public CompletableFuture<DeliveryResult> sendAsyncVia(MailDelivery mailDelivery) {
        return mailDelivery.sendAsync(this);
    }

    public CompletableFuture<DeliveryResult> sendAsyncVia(Supplier<? extends MailDelivery> mailDelivery) {
        return sendAsyncVia(mailDelivery.get());
    }

    public static class Builder {

        private String from;
        private List<String> toRecipients;
        private List<String> ccRecipients;
        private List<String> bccRecipients;
        private List<String> replyTo;
        private Map<String, DataSource> attachments;
        private Map<String, DataSource> inlines;
        private String subject;
        private String message;
        private boolean html;

        protected Builder()
        { }

        public @Nonnull Builder from(@Nonnull String sender) {
            this.from = sender;
            return this;
        }

        public @Nonnull Builder to(@Nonnull String ... recipients) {
            return to(Arrays.asList(recipients));
        }

        public @Nonnull Builder to(@Nonnull Collection<String> recipients) {
            for (String recipient : recipients) {
                to(recipient);
            }
            return this;
        }

        public @Nonnull Builder to(@Nonnull String recipient) {
            if (toRecipients == null) {
                toRecipients = new ArrayList<>();
            }
            toRecipients.add(recipient);
            return this;
        }

        public @Nonnull Builder cc(@Nonnull String ... recipients) {
            return cc(Arrays.asList(recipients));
        }

        public @Nonnull Builder cc(@Nonnull Collection<String> recipients) {
            for (String recipient : recipients) {
                cc(recipient);
            }
            return this;
        }

        public @Nonnull Builder cc(@Nonnull String recipient) {
            if (ccRecipients == null) {
                ccRecipients = new ArrayList<>();
            }
            ccRecipients.add(recipient);
            return this;
        }

        public @Nonnull Builder bcc(@Nonnull String ... recipients) {
            return bcc(Arrays.asList(recipients));
        }

        public @Nonnull Builder bcc(@Nonnull Collection<String> recipients) {
            for (String recipient : recipients) {
                bcc(recipient);
            }
            return this;
        }

        public @Nonnull Builder bcc(@Nonnull String recipient) {
            if (bccRecipients == null) {
                bccRecipients = new ArrayList<>();
            }
            bccRecipients.add(recipient);
            return this;
        }

        public @Nonnull Builder replyTo(@Nonnull String recipient) {
            if (replyTo == null) {
                replyTo = new ArrayList<>();
            }
            replyTo.add(recipient);
            return this;
        }

        public @Nonnull Builder subject(@Nonnull String subject) {
            this.subject = subject;
            return this;
        }

        public @Nonnull Builder message(@Nonnull String message) {
            this.message = message;
            this.html = false;
            return this;
        }

        public @Nonnull Builder htmlMessage(@Nonnull String message) {
            this.message = message;
            this.html = true;
            return this;
        }

        public @Nonnull Builder attachment(@Nonnull DataSource dataSource) {
            return attachment(dataSource.getName(), dataSource);
        }

        public @Nonnull Builder attachment(@Nonnull String name, @Nonnull DataSource dataSource) {
            if (this.attachments == null) {
                this.attachments = new LinkedHashMap<>();
            }
            this.attachments.put(name, dataSource);
            return this;
        }

        public @Nonnull Builder inline(@Nonnull DataSource dataSource) {
            return inline(dataSource.getName(), dataSource);
        }

        public @Nonnull Builder inline(@Nonnull String name, @Nonnull DataSource dataSource) {
            if (this.inlines == null) {
                this.inlines = new LinkedHashMap<>();
            }
            this.inlines.put(name, dataSource);
            return this;
        }

        public @Nonnull MailMessage build() {
            try {
                MailMessage mailMessage = new MailMessage();
                if (toRecipients != null) {
                    for (String toRecipient : toRecipients) {
                        mailMessage.addRecipient(Message.RecipientType.TO, MailUtils.parseAddress(toRecipient));
                    }
                }
                if (ccRecipients != null) {
                    for (String ccRecipient : ccRecipients) {
                        mailMessage.addRecipient(Message.RecipientType.CC, MailUtils.parseAddress(ccRecipient));
                    }
                }
                if (bccRecipients != null) {
                    for (String bccRecipient : bccRecipients) {
                        mailMessage.addRecipient(Message.RecipientType.BCC, MailUtils.parseAddress(bccRecipient));
                    }
                }
                if (replyTo != null) {
                    Address[] addresses = new Address[replyTo.size()];
                    for (int i = 0; i < addresses.length; i++) {
                        addresses[i] = MailUtils.parseAddress(replyTo.get(i));
                    }
                    mailMessage.setReplyTo(addresses);
                }
                if (from != null) {
                    mailMessage.setFrom(MailUtils.parseAddress(from));
                }
                if (subject != null) {
                    mailMessage.setSubject(subject, "UTF-8");
                }

                Multipart multipart = new MimeMultipart();
                MimeBodyPart bodyPart = new MimeBodyPart();
                if (message != null) {
                    if (html) {
                        bodyPart.setDataHandler(new DataHandler(new DataSource() {
                            @Override
                            public InputStream getInputStream() throws IOException {
                                return new ByteArrayInputStream(message.getBytes("UTF-8"));
                            }

                            @Override
                            public OutputStream getOutputStream() throws IOException {
                                throw new UnsupportedOperationException();
                            }

                            @Override
                            public String getContentType() {
                                return "text/html; charset=utf-8";
                            }

                            @Override
                            public String getName() {
                                return "HTML Message";
                            }
                        }));
                    }
                    else {
                        bodyPart.setText(message, "UTF-8");
                    }
                }
                else {
                    bodyPart.setText("");
                }
                multipart.addBodyPart(bodyPart);

                if (inlines != null) {
                    for (Map.Entry<String, DataSource> entry : inlines.entrySet()) {
                        MimeBodyPart part = new MimeBodyPart();
                        part.setDisposition(MimeBodyPart.INLINE);
                        part.setDataHandler(new DataHandler(entry.getValue()));
                        part.setContentID("<" + entry.getKey() + ">");
                        part.setFileName(MimeUtility.encodeText(entry.getValue().getName(), null, null));
                        multipart.addBodyPart(part);
                    }
                }
                if (attachments != null) {
                    for (Map.Entry<String, DataSource> entry : attachments.entrySet()) {
                        MimeBodyPart part = new MimeBodyPart();
                        part.setDisposition(MimeBodyPart.ATTACHMENT);
                        part.setDataHandler(new DataHandler(entry.getValue()));
                        part.setFileName(MimeUtility.encodeText(entry.getKey(), null, null));
                        multipart.addBodyPart(part);
                    }
                }
                mailMessage.setContent(multipart);
                return mailMessage;
            }
            catch (MessagingException | UnsupportedEncodingException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}

