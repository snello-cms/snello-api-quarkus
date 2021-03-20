package io.snello.service.mail.smtp;


import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.snello.service.mail.Email;
import io.snello.service.mail.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
//@Requires(property = EMAIL_TYPE, value = "smtp")
public class SmtpEmailService implements EmailService {

    Logger logger = LoggerFactory.getLogger(SmtpEmailService.class);

    @Inject
    Mailer mailer;


    @Override
    public void send(Email email) throws Exception {
        logger.info("1nd ===> get Mail Session..");
        Mail mail = Mail
                .withHtml(email.recipient,
                        email.subject,
                        email.body);
        mailer.send(mail);
        logger.info("1rd ===> Send mail");
    }

}
