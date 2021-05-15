package io.snello.service.trigger.mail;

import io.snello.model.events.TriggerEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.ObservesAsync;

@ApplicationScoped
public class MailTriggerService {

    public void observe(@ObservesAsync TriggerEvent triggerEvent) {

    }
}
