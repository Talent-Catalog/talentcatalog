package org.tbbtalent.server.service.email;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.exception.EmailSendFailedException;
import org.tbbtalent.server.model.User;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailHelper {
    private static final Logger log = LoggerFactory.getLogger(EmailHelper.class);

    
    private final EmailSender emailSender;
    private final TemplateEngine emailTemplateEngine;

    @Value("${web.portal}")
    private String portalUrl;
    
    @Autowired
    public EmailHelper(EmailSender emailSender,
                       TemplateEngine emailTemplateEngine) {
        this.emailSender = emailSender;
        this.emailTemplateEngine = emailTemplateEngine;
    }

    // ---------- Account ----------------

    public void sendRegistrationEmail(User user) throws EmailSendFailedException {

        String email = user.getEmail();
        String displayName = user.getDisplayName();
        // TODO: add confirmation token process
        String token = "TODO"; //user.getConfirmationToken();

        String subject = null;
        String body = null;
        try {
            final Context ctx = new Context();
            ctx.setVariable("name", displayName);
            ctx.setVariable("confirmUrl", portalUrl + "/confirm-email/" + token);
            ctx.setVariable("year", currentYear());

            subject = "Stillbirth CRE - Confirm your Registration Email";
            body = emailTemplateEngine.process("registration", ctx);

            emailSender.sendAsync(email, displayName, subject, body);
        } catch (Exception e) {
            log.error("error sending confirm registration email", e);
            throw new EmailSendFailedException(e);
        }
    }
    
    public void sendResetPasswordEmail(User user) throws EmailSendFailedException {
        
        String email = user.getEmail();
        String displayName = user.getDisplayName();
        String token = user.getResetToken();
        
        String subject = null;
        String body = null;
        try {
            final Context ctx = new Context();
            ctx.setVariable("resetUrl", portalUrl + "/reset-password/" + token);
            ctx.setVariable("year", currentYear());
            
            subject = "Stillbirth CRE - Reset Your Password";
            body = emailTemplateEngine.process("forgot_password", ctx);
            
            emailSender.sendAsync(email, displayName, subject, body);
        } catch (Exception e) {
            log.error("error sending reset password email", e);
            throw new EmailSendFailedException(e);
        }
    }
    
    private String currentYear() {
        return LocalDate.now().getYear() + "";
    }
}
