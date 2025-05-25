package io.snello.service.rs;


import io.snello.api.service.AbstractServiceRs;
import io.snello.management.AppConstants;
import io.snello.model.events.FieldDefinitionCreateUpdateEvent;
import io.snello.model.events.FieldDefinitionDeleteEvent;
import io.snello.service.ApiService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.Map;

import static io.snello.management.AppConstants.FIELD_DEFINITIONS;
import static io.snello.management.AppConstants.FIELD_DEFINITIONS_PATH;

@Path(FIELD_DEFINITIONS_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class FieldDefinitionsServiceRs extends AbstractServiceRs {

    @Inject
    Event<FieldDefinitionCreateUpdateEvent> eventUpdatePublisher;
    @Inject
    Event<FieldDefinitionDeleteEvent> eventDeletePublisher;

    @Inject
    FieldDefinitionsServiceRs(ApiService apiService) {
        super(apiService, FIELD_DEFINITIONS, "name asc");
    }

    public FieldDefinitionsServiceRs() {
    }

    @Override
    protected void prePersist(Map<String, Object> object) throws Exception {
        object.put(AppConstants.UUID, java.util.UUID.randomUUID().toString());
    }

    @Override
    protected void postPersist(Map<String, Object> object) {
        eventUpdatePublisher.fireAsync(new FieldDefinitionCreateUpdateEvent(object));
    }

    @Override
    protected void postUpdate(Map<String, Object> object) {
        eventUpdatePublisher.fireAsync(new FieldDefinitionCreateUpdateEvent(object));
    }

    @Override
    protected void postDelete(String id) {
        eventDeletePublisher.fireAsync(new FieldDefinitionDeleteEvent(id));
    }

}
