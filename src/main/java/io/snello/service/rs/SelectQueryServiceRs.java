package io.snello.service.rs;

import io.snello.api.service.AbstractServiceRs;
import io.snello.service.ApiService;
import io.snello.model.events.SelectQueryCreateUpdateEvent;
import io.snello.model.events.SelectQueryDeleteEvent;
import io.snello.util.MetadataUtils;
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

@Path(SELECT_QUERY_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class SelectQueryServiceRs extends AbstractServiceRs {

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
