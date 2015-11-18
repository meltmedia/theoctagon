package com.meltmedia

import groovyx.net.http.RESTClient
import org.junit.Test

/**
 * Created by patarbas on 11/17/2015.
 */

public class UserPagination {

    private static final String JSON = "application/json"
    private static final String URL = "http://localhost:8080";

    @Test
    public void TestPagination(){
        def http = new RESTClient (URL)
        def resp = http.get(path: "api/user", requestContentType: JSON)
        assert resp.data.asList.size > 0
    }
}
