package io.snello.service.producer;

import io.quarkus.mailer.Mailer;
import io.snello.api.service.MailService;
import io.snello.service.mail.SmtpMailService;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class MailProducer {

    Logger logger = Logger.getLogger(getClass());

    @Inject
    Mailer mailer;

    @ConfigProperty(name = "snello.mailtype", defaultValue = "")
    String mailtype;

    public MailProducer() {
        logger.info("MailProducer");
    }

    @Produces
    public MailService db() throws Exception {
        System.out.println("mailtype: " + mailtype);
        switch (mailtype) {
            case "smtp":
                return new SmtpMailService(mailer);
            default:
                throw new Exception("no mailtype");
        }
    }
}
