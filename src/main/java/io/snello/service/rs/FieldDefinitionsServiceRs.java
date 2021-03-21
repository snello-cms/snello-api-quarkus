package io.snello.service.rs;


import io.snello.api.service.AbstractServiceRs;
import io.snello.management.AppConstants;
import io.snello.model.events.FieldDefinitionCreateUpdateEvent;
import io.snello.model.events.FieldDefinitionDeleteEvent;
import io.snello.service.ApiService;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Map;

import static io.snello.management.AppConstants.FIELD_DEFINITIONS;
import static io.snello.management.AppConstants.FIELD_DEFINITIONS_PATH;

@Path(FIELD_DEFINITIONS_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class FieldDefinitionsServiceRs extends AbstractServiceRs {

    @Inject
    Event eventPublisher;

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
        eventPublisher.fireAsync(new FieldDefinitionCreateUpdateEvent(object));
    }

    @Override
    protected void postUpdate(Map<String, Object> object) {
        eventPublisher.fireAsync(new FieldDefinitionCreateUpdateEvent(object));
    }

    @Override
    protected void postDelete(String id) {
        eventPublisher.fireAsync(new FieldDefinitionDeleteEvent(id));
    }

}
