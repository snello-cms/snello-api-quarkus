package io.snello.service.trigger.mail;

import io.snello.model.events.TriggerEvent;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.ObservesAsync;

@ApplicationScoped
public class MailTriggerService {

    public void observe(@ObservesAsync TriggerEvent triggerEvent) {

    }
}
