package io.snello.api.service;

import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public abstract class AbstractServiceRs<T, U> {

    @POST
    @Transactional
    public Response persist(T object) {
        return null;
    }


    @GET
    @Path("/{id}")
    @Transactional
    public Response fetch(@PathParam("id") U id) {
        return null;
    }


    @PUT
    @Path("/{id}")
    @Transactional
    public Response update(@PathParam("id") U id, T object) {
        return null;
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") U id) {
        return null;
    }

    @GET
    @Transactional
    public Response getList(
            @DefaultValue("0") @QueryParam("startRow") Integer startRow,
            @DefaultValue("10") @QueryParam("pageSize") Integer pageSize,
            @QueryParam("orderBy") String orderBy, @Context UriInfo ui) {
        return null;
    }
}
