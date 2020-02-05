package org.tbbtalent.server.service.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.exception.EmailSendFailedException;
import org.tbbtalent.server.model.User;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDate;

@Service
public class EmailHelper {
    private static final Logger log = LoggerFactory.getLogger(EmailHelper.class);

    
    private final EmailSender emailSender;
    private final TemplateEngine textTemplateEngine;
    private final TemplateEngine htmlTemplateEngine;

    @Value("${web.portal}")
    private String portalUrl;
    
    @Autowired
    public EmailHelper(EmailSender emailSender,
                       TemplateEngine textTemplateEngine,
                       TemplateEngine htmlTemplateEngine) {
        this.emailSender = emailSender;
        this.textTemplateEngine = textTemplateEngine;
        this.htmlTemplateEngine = htmlTemplateEngine;
    }

    public void sendRegistrationEmail(User user, String preferredLanguage) throws EmailSendFailedException {

        String email = user.getEmail();
        String displayName = user.getDisplayName();

        String subject = null;
        String bodyText = null;
        String bodyHtml = null;
        try {
            final Context ctx = new Context();
            ctx.setVariable("displayName", displayName);
            ctx.setVariable("username", user.getUsername());
            ctx.setVariable("forgotPwdUrl", portalUrl + "/reset-password/");
            ctx.setVariable("year", currentYear());

            subject = "Talent Beyond Boundaries - Thank you for your application";
            bodyText = textTemplateEngine.process(getPath(preferredLanguage)+"registration", ctx);
            bodyHtml = htmlTemplateEngine.process(getPath(preferredLanguage)+"registration", ctx);

            emailSender.sendAsync(email, subject, bodyText, bodyHtml);
        } catch (Exception e) {
            log.error("error sending confirm registration email", e);
            throw new EmailSendFailedException(e);
        }
    }
    
    public void sendResetPasswordEmail(User user, String preferredLanguage) throws EmailSendFailedException {
        
        String email = user.getEmail();
        String displayName = user.getDisplayName();
        String token = user.getResetToken();
        
        String subject = null;
        String bodyText = null;
        String bodyHtml = null;
        try {
            final Context ctx = new Context();
            ctx.setVariable("displayName", displayName);
            ctx.setVariable("resetUrl", portalUrl + "/reset-password/" + token);
            ctx.setVariable("year", currentYear());
            
            subject = "Talent Beyond Boundaries - Reset Your Password";
            bodyText = textTemplateEngine.process(getPath(preferredLanguage)+"reset-password", ctx);
            bodyHtml = htmlTemplateEngine.process(getPath(preferredLanguage)+"reset-password", ctx);
            
            emailSender.sendAsync(email, subject, bodyText, bodyHtml);
        } catch (Exception e) {
            log.error("error sending reset password email", e);
            throw new EmailSendFailedException(e);
        }
    }

    public void sendIncompleteApplication(User user, String message, String preferredLanguage) throws EmailSendFailedException {

        String email = user.getEmail();
        String displayName = user.getDisplayName();

        String subject = null;
        String bodyText = null;
        String bodyHtml = null;
        try {
            final Context ctx = new Context();
            ctx.setVariable("displayName", displayName);
            ctx.setVariable("candidateMessage", message);
            ctx.setVariable("username", user.getUsername());
            ctx.setVariable("forgotPwdUrl", portalUrl + "/reset-password/");
            ctx.setVariable("year", currentYear());

            subject = "Talent Beyond Boundaries - Please provide more details application";
            bodyText = textTemplateEngine.process(getPath(preferredLanguage)+"incomplete-application", ctx);
            bodyHtml = htmlTemplateEngine.process(getPath(preferredLanguage)+"incomplete-application", ctx);

            emailSender.sendAsync(email, subject, bodyText, bodyHtml);
        } catch (Exception e) {
            log.error("error sending confirm registration email", e);
            throw new EmailSendFailedException(e);
        }
    }

    private String getPath(String preferredLanguage){
        if (preferredLanguage == null){
            preferredLanguage = "en";
        }
        return preferredLanguage + "/";
    }
    private String currentYear() {
        return LocalDate.now().getYear() + "";
    }
}
