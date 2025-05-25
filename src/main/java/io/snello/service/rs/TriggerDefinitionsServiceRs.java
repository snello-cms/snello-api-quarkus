package io.snello.service.rs;

import io.snello.api.service.AbstractServiceRs;
import io.snello.service.ApiService;
import io.snello.util.MetadataUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.Map;

import static io.snello.management.AppConstants.*;

@Path(TRIGGER_DEFINITION_QUERY)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class TriggerDefinitionsServiceRs extends AbstractServiceRs {

    @Inject
    public TriggerDefinitionsServiceRs(ApiService apiService) {
        super(apiService, TRIGGER_DEFINITION_QUERY, "");
    }

    public TriggerDefinitionsServiceRs() {
        super();
    }



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
    protected void preUpdate(Map<String, Object> object) throws Exception {
        if (object.get(QUERY_NAME) == null) {
            throw new Exception(MSG_QUERY_NAME_IS_EMPTY);
        }
        if (MetadataUtils.isReserved(object.get(QUERY_NAME))) {
            throw new Exception(MSG_QUERY_NAME_IS_RESERVED);
        }
    }


}
