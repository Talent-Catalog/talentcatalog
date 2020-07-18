package org.tbbtalent.server.service.email;

import java.nio.charset.StandardCharsets;

import org.tbbtalent.server.model.db.User;
import org.tbbtalent.server.service.email.EmailSender.EmailType;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

public class EmailTester {

    public static void main(String[] args) {
//        testStubSend();
        testSmtpSend();
    }
    
    private static void testStubSend() {
        EmailSender emailSender = stubEmailSender();
        EmailHelper helper = new EmailHelper(emailSender, textTemplateEngine(), htmlTemplateEngine());
        helper.sendResetPasswordEmail(user());
    }
    
    private static void testSmtpSend() {
        EmailSender emailSender = smtpEmailSender();
        EmailHelper helper = new EmailHelper(emailSender, textTemplateEngine(), htmlTemplateEngine());
        helper.sendResetPasswordEmail(user());
    }
    
    private static User user() {
        User user = new User();
        user.setEmail("test@dp.test.com");
        user.setFirstName("sadfs");
        user.setLastName("dsrfgdg");
        user.setResetToken("sdjfsljf sjflkjsdlkfjlsdkjflksdjf");
        return user;
    }

    private static EmailSender stubEmailSender() {
        EmailSender sender = new EmailSender();
        sender.setType(EmailType.STUB);
        sender.setHost("");
        sender.setPort(0);
        sender.setUser("");
        sender.setPassword("");
        sender.setAuthenticate(false);
        sender.setDefaultEmail("test-from@dp.test.com");
        sender.setTestOverrideEmail("");
        sender.init();
        return sender;
    }
    
    private static EmailSender smtpEmailSender() {
        EmailSender sender = new EmailSender();
        sender.setType(EmailType.SMTP);
        sender.setHost("smtp.gmail.com");
        sender.setPort(587);
//        sender.setUser("devtestingdp@gmail.com");
//        sender.setPassword("H@6J9bpcIDHa");
        sender.setUser("tbbtalent@talentbeyondboundaries.org");
        sender.setPassword("TBB4Talent!now");
        sender.setAuthenticate(true);
        sender.setDefaultEmail("martina@digitalpurpose.com.au");
        sender.setTestOverrideEmail("martina+tbb@digitalpurpose.com.au");
        sender.init();
        return sender;
    }

    public static TemplateEngine textTemplateEngine() {
        final SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.addTemplateResolver(textTemplateResolver());
        return templateEngine;
    }
    
    public static TemplateEngine htmlTemplateEngine() {
        final SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.addTemplateResolver(htmlTemplateResolver());
        return templateEngine;
    }
    
    private static ITemplateResolver textTemplateResolver() {
        final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setOrder(Integer.valueOf(2));
        templateResolver.setPrefix("mail/");
        templateResolver.setSuffix(".txt");
        templateResolver.setTemplateMode(TemplateMode.TEXT);
        templateResolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        templateResolver.setCacheable(false);
        return templateResolver;
    }

    private static ITemplateResolver htmlTemplateResolver() {
        final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setOrder(Integer.valueOf(2));
        templateResolver.setPrefix("mail/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        templateResolver.setCacheable(false);
        return templateResolver;
    }

}
