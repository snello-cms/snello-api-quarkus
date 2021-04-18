package io.snello.service.producer;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class RsExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        exception.printStackTrace();
        return Response.serverError().entity(new FailMsg(exception.getMessage())).build();
    }

    private class FailMsg {
        public String msg;

        public FailMsg(String msg) {
            this.msg = msg;
        }
    }
}

