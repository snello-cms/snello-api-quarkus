package io.snello.service.multitenant;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import io.quarkus.oidc.TenantResolver;
import io.snello.util.JwtUtils;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class SnelloTenantResolver implements TenantResolver {

    @Inject
    ObjectMapper objectMapper;

    @Override
    public String resolve(RoutingContext context) {
        String authorizationHeader = context.request().getHeader("Authorization");
        String issuer = JwtUtils.getIssuer(objectMapper, authorizationHeader);
        Log.info("issuer: " + issuer);
        if (issuer.contains("accounts") || issuer.contains("snello")) {
            String tenant = "snello";
            Log.info("tenant resolved as: " + tenant);
            return tenant;
        }
        String tenant = "default";
        Log.info("tenant resolved as: " + tenant);
        return tenant;
    }


}
