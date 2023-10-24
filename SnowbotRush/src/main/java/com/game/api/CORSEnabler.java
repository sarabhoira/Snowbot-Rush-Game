package com.game.api;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

@Provider
@Priority(Priorities.HEADER_DECORATOR)
public class CORSEnabler implements ContainerResponseFilter {

    /**
     * do not modify this file, this injects CORS headers into any outgoing HTTP message
     */

    public String makeRawHTTP(ContainerRequestContext request) {
        String ret = "";
        ret += request.getMethod() + "\n";
        for (String key: request.getHeaders().keySet()) {
            ret += key + ": " + request.getHeaders().getFirst(key) + "\n";
        }
        return ret;
    }
    public String makeRawHTTP(ContainerResponseContext request) {
        String ret = "";
        ret += request.getStatus() + "\n";
        for (String key: request.getHeaders().keySet()) {
            ret += key + ": " + request.getHeaders().getFirst(key) + "\n";
        }
        return ret;
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        final MultivaluedMap<String,Object> headers = responseContext.getHeaders();

//        System.out.println(makeRawHTTP(requestContext));
//        System.out.println(makeRawHTTP(responseContext));

        headers.add("Access-Control-Allow-Origin", "*");
        headers.add("Access-Control-Allow-Headers", "Authorization, Origin, X-Requested-With, Content-Type");
        headers.add("Access-Control-Expose-Headers", "Location, Content-Disposition");
        headers.add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
    }
}