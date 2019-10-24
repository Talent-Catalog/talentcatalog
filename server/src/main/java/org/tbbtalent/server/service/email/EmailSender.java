package org.tbbtalent.server.service.email;

import java.io.IOException;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PostConstruct;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Provider;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailSender {

    private static final Logger log = LoggerFactory.getLogger(EmailSender.class);

    public enum EmailType {
        STUB, SMTP, MANDRILL
    }

    @Value("${email.type}")
    private EmailType type;
    @Value("${email.host}")
    private String host;
    @Value("${email.port}")
    private String port;
    @Value("${email.user}")
    private String user;
    @Value("${email.password}")
    private String password;
    @Value("${email.authenticated}")
    private Boolean authenticate;
    @Value("${email.default-email}")
    private String defaultEmail;
    @Value("${email.test-override-email}")
    private String testOverrideEmail;

    private Properties properties = new Properties();
    private Session session;
    private Transport transport;

    @PostConstruct
    public void init() {
        if (type == EmailType.SMTP) {
            log.info("email configured to use SMTP with host " + host + " and port " + port);
        } else if (type == EmailType.STUB) {
            log.info("email configured to use STUB, emails will be printed to the log file");
        } else if (type == EmailType.MANDRILL) {
            log.info("email configured to use MANDRILL, emails will be sent via Mandrill");
        }

        this.session = emailSession();
        this.transport = emailTransport();
    }
        
    public boolean send(String emailFrom,
                        String emailTo,
                        String bccTo,
                        String subject,
                        String content) {
        return doSend(emailFrom, emailTo, subject, content);
    }

    @Async
    public boolean sendAsync(String emailFrom,
                             String emailTo,
                             String subject,
                             String content) {
        return doSend(emailFrom, emailTo, subject, content);
    }
    
    private boolean doSend(String emailFrom,
                         String emailTo,
                         String subject,
                         String content) {
        
        log.info("sending email to " + emailTo + " with subject '" + subject + "'");
        
        MimeMessage email = new MimeMessage(session);
        try {
            email.addFrom(InternetAddress.parse(emailFrom));
            email.addRecipients(Message.RecipientType.TO, emailTo);
            email.setSubject(subject);
            email.setContent(createEmailBody(content));
            
            // check, if replace with test email
            applyTestOverrideIfRequired(email);
            
            send(email);
            
            return true;
        } catch (MessagingException e) {
            log.error("Error sending email to {}: {}", emailTo, e.getMessage());
        }
        
        return false;
    }

    public void send(MimeMessage mimeMessage) throws MessagingException {
        if (transport == null) {
            Transport.send(mimeMessage);
            return;
        }
        transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
    }
    
    @Async
    public void sendAsync(MimeMessage mimeMessage) throws MessagingException {
        if (transport == null) {
            Transport.send(mimeMessage);
            return;
        }
        transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
    }

    private static Multipart createEmailBody(String content) throws MessagingException {
        MimeMultipart body = new MimeMultipart();
        BodyPart bodyPart = new MimeBodyPart();
        bodyPart.setContent(content, "text/html; charset=utf-8");
        body.addBodyPart(bodyPart);
        return body;
    }
    
    private void applyTestOverrideIfRequired(MimeMessage email) {
        try {
            Address[] recipients = email.getAllRecipients();
            if (StringUtils.isNotBlank(testOverrideEmail) && recipients != null) {
                for (Address address : recipients) {
                    if (address instanceof InternetAddress) {
                        InternetAddress recipient = (InternetAddress)address;
                        log.info("replacing intended recipient " + recipient.getAddress() + " with " + testOverrideEmail);
                        recipient.setAddress(testOverrideEmail);
                    }
                }
            }
        } catch (Exception e) {
            log.error("unable to check recipients for test override", e);
        }
    }

    /*
     * @Bean
     * 
     * @ConditionalOnProperty(name = "email.type", havingValue = "MANDRILL")
     * public MandrillApi mandrillApi() { return new MandrillApi(password); }
     */

    public Session emailSession() {
        if (type == EmailType.SMTP) {
            properties.put("mail.transport.protocol", "smtp");
            properties.put("mail.from", defaultEmail);
            properties.put("mail.smtp.port", port);
            properties.put("mail.smtp.host", host);
            properties.put("mail.smtp.auth", authenticate != null ? authenticate.toString() : "false");
            properties.put("mail.smtp.username", user);
            properties.put("mail.smtp.starttls.enable", authenticate != null ? authenticate : false);
            if (authenticate != null && authenticate) {
                Authenticator authenticator = new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(user, password);
                    }
                };
                return Session.getDefaultInstance(properties, authenticator);
            } else {
                return Session.getDefaultInstance(properties);
            }
        } else if (type == EmailType.STUB) {
            return stubMailSession();
        }
        throw new RuntimeException("Email has not been correctly configured");
    }

    public Session stubMailSession() {
        Properties testMailProperties = new Properties();
        testMailProperties.setProperty("mail.testProtocol.class", "com.covata.util.mail.MailStubber$TestTransport");
        testMailProperties.setProperty("mail.transport.protocol.rfc822", "testProtocol");
        Session session = Session.getInstance(testMailProperties);
        Provider testMailProvider = new Provider(Provider.Type.TRANSPORT, "testProtocol",
                                                 "com.covata.util.mail.MailStubber$TestTransport", "", "");
        session.addProvider(testMailProvider);
        return session;
    }

    private Transport emailTransport() {
        if (type == EmailType.STUB) {
            return new StubTransport(stubMailSession(), new URLName("test"));
        }
        return null;
    }

    public static class StubTransport extends Transport {

        private static final Queue<String> MAIL = new LinkedBlockingQueue<>(20);

        public StubTransport(Session session,
                             URLName urlname) {
            super(session, urlname);
        }

        static String getMailMessage() {
            return MAIL.poll();
        }

        static void clearMail() {
            MAIL.clear();
        }

        @Override
        public void connect() throws MessagingException {
        }

        @Override
        public void sendMessage(Message msg,
                                Address[] addresses)
                throws MessagingException {
            try {
                BodyPart part = ((Multipart) msg.getContent()).getBodyPart(0);
                if (MAIL.size() < 20) {
                    MAIL.add(part.getContent().toString());
                }
                log.info("Email received (sent to: " + addresses[0] + "): \n\n" + part.getContent());
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
