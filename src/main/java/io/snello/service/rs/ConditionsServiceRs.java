package io.snello.service.rs;

import io.snello.api.service.AbstractServiceRs;
import io.snello.model.events.ConditionCreateUpdateEvent;
import io.snello.model.events.ConditionDeleteEvent;
import io.snello.service.ApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Map;

import static io.snello.management.AppConstants.*;
import static javax.ws.rs.core.Response.ok;
import static javax.ws.rs.core.Response.serverError;

@Path(CONDITIONS_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ConditionsServiceRs extends AbstractServiceRs {

    @Inject
    ApiService apiService;

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
