package io.snello.service.rs;

import io.snello.api.service.AbstractServiceRs;
import io.snello.model.events.ConditionCreateUpdateEvent;
import io.snello.model.events.ConditionDeleteEvent;
import io.snello.service.ApiService;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Map;

import static io.snello.management.AppConstants.CONDITIONS;
import static io.snello.management.AppConstants.CONDITIONS_PATH;

@Path(CONDITIONS_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ConditionsServiceRs extends AbstractServiceRs {


    @Inject
    Event eventPublisher;

    @Inject
    ConditionsServiceRs(ApiService apiService) {
        super(apiService, CONDITIONS, "");
    }

    public ConditionsServiceRs() {
    }

    @Override
    protected void postPersist(Map<String, Object> object) {
        eventPublisher.fireAsync(new ConditionCreateUpdateEvent(object));
    }

    @Override
    protected void postUpdate(Map<String, Object> object) {
        eventPublisher.fireAsync(new ConditionCreateUpdateEvent(object));
    }

    @Override
    protected void postDelete(String id) {
        eventPublisher.fireAsync(new ConditionDeleteEvent(id));
    }
}
