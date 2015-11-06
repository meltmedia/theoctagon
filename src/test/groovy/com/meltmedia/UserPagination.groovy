package com.meltmedia

import groovyx.net.http.RESTClient
import org.junit.Test

/**
 * Created by fern on 11/5/2015.
 */
class UserPagination {
    // URL
    private static final String JSON = "application/json"
    private static final String URL = "http://localhost:8080";

    @Test
    public void testDefaultPaginate() {
        def http = new RESTClient ( URL )

        def resp = http.get(path: "/api/user", requestContentType: JSON)

        assert !(resp.data.asList.size > 35)
    }

}
