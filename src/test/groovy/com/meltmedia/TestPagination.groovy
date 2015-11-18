//package com.meltmedia
//
//import groovyx.net.http.RESTClient
//import org.junit.Test
//
///**
// * Created by patarbas on 11/17/2015.
// */
//
//public class TestPagination {
//
//    private static final String JSON = "application/json"
//    private static final String URL = "http://localhost:8080/api/user";
//
//    @Test
//    public TestPagination(){
//        def http = new RESTClient (URL)
//        def resp = http.get(path: "api/user", requestContentType: JSON)
//        assert (!resp.data.asList.size >= 10) // Checking to make sure I have no more than 10 users returned with no special parameters specified.
//    }
//}