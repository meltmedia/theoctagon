package com.meltmedia

import groovyx.net.http.RESTClient
import org.junit.Test

/**
 * Created with IntelliJ IDEA.
 * User: satkins
 * Date: 3/6/2013
 *
 * These tests check to make sure pagination of users is working properly.
 *
 * The following tests should only be run after /dataset-loader/users.sh has already been run
 */
class PagingIntegration {

    private static final String JSON = "application/json"
    private static final String URL = "http://localhost:8080/api/user"

    /**
     * Check user data source size, since other tests are dependent on this size
     */
    @Test
    public void testDataSourceSize() {
        def http = new RESTClient( URL )

        // Get one page with all users on it
        def resp = http.get( query: [ pageSize:"999999999", pageNumber:"1" ], requestContentType: JSON )

        // Check response status and size
        assert resp.status == 200
        assert resp.data.size() == 201, "Data source contains " + resp.data.size() + " user(s) when 201 users were expected. Have you run \"/dataset-loader/users.sh\"?"
    }

    /**
     * Test default paging
     */
    @Test
    public void testDefaultPaging() {
        def http = new RESTClient( URL )

        // Get the default page (page 1, page size 10)
        def resp = http.get( requestContentType: JSON )

        // Check response status and size
        assert resp.status == 200
        assert resp.data.size() == 10

        // Check that it displays IDs 1 through 10 (per default)
        for (int i = 0; i < 10; i++) {
            assert resp.data[i].id == i + 1
        }
    }

    /**
     * Test to make sure the final page truncates correctly if the range goes past the final user
     */
    @Test
    public void testMaxPageTruncation() {
        def http = new RESTClient( URL )

        // Get page 21, page size 10
        def resp = http.get( query: [ pageSize:"10", pageNumber:"21" ], requestContentType: JSON )
        assert resp.status == 200
        // Since we're looking at the 21st page that is 10 users long, we're attempting to view users 201-210.
        // So we should only get 1 user and it should be user 201.
        assert resp.data.size() == 1
        assert resp.data[0].id == 201
    }

    /**
     * Test to make sure that requesting a page that is empty because it is beyond the final user index does not crash
     */
    @Test
    public void testPastMaxPageEmptySet() {
        def http = new RESTClient( URL )

        // Get page 200, page size 10
        def resp = http.get( query: [ pageSize:"10", pageNumber:"200" ], requestContentType: JSON )

        // Checking that we get a successful status and no records are returned
        assert resp.status == 200
        assert resp.data.size() == 0
    }

    /**
     * Test illegal pageSize/pageNum
     */
    @Test
    public void testIllegalParameters() {
        def http = new RESTClient( URL )

        try {
            // Attempt to get the list
            def resp = http.get( query: [ pageSize:"-1", pageNum:"-1" ], requestContentType: JSON )
            assert false, "Expected exception"
        } catch( ex ) {
            assert ex.response.status == 400
        }
    }
}
