package io.snello.filter;

import io.quarkus.runtime.annotations.RegisterForReflection;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class RsExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        exception.printStackTrace();
        return Response.serverError().entity(new FailMsg(exception.getMessage())).build();
    }

    @RegisterForReflection
    private class FailMsg {
        public String msg;

        public FailMsg(String msg) {
            this.msg = msg;
        }
    }
}


