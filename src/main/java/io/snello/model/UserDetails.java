package io.snello.model;

import io.quarkus.security.credential.Credential;
import io.quarkus.security.identity.SecurityIdentity;

import java.security.Permission;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionStage;

public class UserDetails implements SecurityIdentity {


    public UserDetails(String username, List<String> roles) {

    }

    @Override
    public Principal getPrincipal() {
        return null;
    }

    @Override
    public boolean isAnonymous() {
        return false;
    }

    @Override
    public Set<String> getRoles() {
        return null;
    }

    @Override
    public <T extends Credential> T getCredential(Class<T> credentialType) {
        return null;
    }

    @Override
    public Set<Credential> getCredentials() {
        return null;
    }

    @Override
    public <T> T getAttribute(String name) {
        return null;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public CompletionStage<Boolean> checkPermission(Permission permission) {
        return null;
    }
}
