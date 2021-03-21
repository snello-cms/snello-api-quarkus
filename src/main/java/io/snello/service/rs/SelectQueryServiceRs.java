package io.snello.service.rs;

import io.snello.api.service.AbstractServiceRs;
import io.snello.model.events.SelectQueryCreateUpdateEvent;
import io.snello.model.events.SelectQueryDeleteEvent;
import io.snello.service.ApiService;
import io.snello.util.MetadataUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Map;

import static io.snello.management.AppConstants.*;

@Path(SELECT_QUERY_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class SelectQueryServiceRs extends AbstractServiceRs {

    @Inject
    public SelectQueryServiceRs(ApiService apiService) {
        super(apiService, SELECT_QUERY, "");
    }

    public SelectQueryServiceRs() {
        super();
    }

    @Inject
    Event eventPublisher;


    @Override
    protected void prePersist(Map<String, Object> object) throws Exception {
        if (object.get(QUERY_NAME) == null) {
            throw new Exception(MSG_QUERY_NAME_IS_EMPTY);
        }
        if (MetadataUtils.isReserved(object.get(QUERY_NAME))) {
            throw new Exception(MSG_QUERY_NAME_IS_RESERVED);
        }
    }

    @Override
    protected void postPersist(Map<String, Object> object) {
        eventPublisher.fireAsync(new SelectQueryCreateUpdateEvent(object));
    }

    @Override
    protected void preUpdate(Map<String, Object> object) throws Exception {
        if (object.get(QUERY_NAME) == null) {
            throw new Exception(MSG_QUERY_NAME_IS_EMPTY);
        }
        if (MetadataUtils.isReserved(object.get(QUERY_NAME))) {
            throw new Exception(MSG_QUERY_NAME_IS_RESERVED);
        }
    }

    @Override
    protected void postUpdate(Map<String, Object> object) {
        eventPublisher.fireAsync(new SelectQueryCreateUpdateEvent(object));
    }

    @Override
    protected void postDelete(String id) {
        eventPublisher.fireAsync(new SelectQueryDeleteEvent(id));
    }
}
