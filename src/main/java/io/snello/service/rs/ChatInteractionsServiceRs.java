package io.snello.service.rs;

import io.snello.api.service.AbstractServiceRs;
import io.snello.service.ApiService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import static io.snello.management.AppConstants.CHAT_INTERACTIONS;
import static io.snello.management.AppConstants.CHAT_INTERACTIONS_PATH;

@Path(CHAT_INTERACTIONS_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ChatInteractionsServiceRs extends AbstractServiceRs {

    @Inject
    public ChatInteractionsServiceRs(ApiService apiService) {
        super(apiService, CHAT_INTERACTIONS, "");
    }

    public ChatInteractionsServiceRs() {
        super();
    }
}