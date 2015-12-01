package com.meltmedia

import groovyx.net.http.RESTClient
import org.junit.Test
import org.junit.BeforeClass


/**
 * Created with IntelliJ IDEA.
 * User: jheun
 * Date: 6/26/13
 */
class UserIntegration {

    private static final String JSON = "application/json"
    private static final String URL = "http://localhost:8080/api/user"


    @BeforeClass
    public static void testCreateUserBatch() {

        def http = new RESTClient( URL )

        for(int i = 0; i < 55;i++)
        {
           def email = i + "testUser@meltdev.com";
           def resp = http.post(body: [ email:email, password:"vespa" ], requestContentType: JSON)
        }
       
        def resp = http.get(path: "/api/user", requestContentType: JSON, query: ["limit": 25, "page": 0])

        assert resp.status == 200
        assert resp.data.asList.size == 25

        println "I made some users"
    }


    /**
     * Test that we can create a good user and that we can get that user after it has been created
     */
    @Test
    public void testCreateGoodUser() {

        def http = new RESTClient( URL )
        def email = "testUser@meltdev.com"

        // Create the user
        def resp = http.post(body: [ email:email, password:"vespa" ], requestContentType: JSON)

        assert resp.status == 200
        assert resp.data.id != null
        assert resp.data.email == email

        // Get the user
        resp = http.get( path:"/api/user/" + resp.data.id, requestContentType: JSON )

        assert resp.status == 200
        assert resp.data.id != null
        assert resp.data.email == email

    }

    @Test
    public void testCreateUserWithBadEmail() {

        def http = new RESTClient( URL )

        try {

            // Attempt to create the user
            http.post(body: [ email:"testmeltdev.com", password:"vespa" ], requestContentType: JSON)
            assert false, "Expected exception"

        } catch( ex ) {

            assert ex.response.status == 400

        }

    }

    @Test
    public void testCreateUserWithNullEmail() {

        def http = new RESTClient( URL )

        try {

            // Attempt to create the user
            http.post(body: [ password:"vespa" ], requestContentType: JSON)
            assert false, "Expected exception"

        } catch( ex ) {

            assert ex.response.status == 400

        }

    }


    @Test
    public void testCreateUserWithBadPassword() {

        def http = new RESTClient( URL )

        try {

            // Attempt to create the user
            http.post(body: [ email:"test@meltdev.com", password:" " ], requestContentType: JSON)
            assert false, "Expected exception"

        } catch( ex ) {

            assert ex.response.status == 400

        }

    }

    @Test
    public void testCreateUserWithNullPassword() {

        def http = new RESTClient( URL )

        try {

            // Attempt to create the user
            http.post(body: [ email:"testmeltdev.com" ], requestContentType: JSON)
            assert false, "Expected exception"

        } catch( ex ) {

            assert ex.response.status == 400

        }
    }


    @Test
    public void testGetNonExistantUser() {

        def http = new RESTClient( URL )

        try {

            // Attempt to create the user
            http.get(path: "/api/user/-1", requestContentType: JSON)
            assert false, "Expected exception"

        } catch( ex ) {

            assert ex.response.status == 404

        }

    }

     @Test
    public void testListUser() {

        def http = new RESTClient( URL )

        // Create a user so we have at least 1
        http.post(body: [ email:"testUser.list@meltdev.com", password:"vespa" ], requestContentType: JSON)

        def myQuery = [ page: "0", limit: "25" ]

        def resp = http.get(path: "/api/user", requestContentType: JSON, query : myQuery)

        assert resp.status == 200
        assert resp.data.asList.size > 0
    }

    @Test
    public void testListUserNegativePage(){
        println "Test Start"

        def http = new RESTClient( URL )

        // Create a user so we have at least 1
        http.post(body: [ email:"testUser2.list@meltdev.com", password:"vespa" ], requestContentType: JSON)

        def myQuery = [ page: "-5", limit: "25" ]

        def resp = http.get(path: "/api/user", requestContentType: JSON, query : myQuery)

        println "Negative list size: $resp.data.asList.size"

        assert resp.status == 200
        assert resp.data.asList.size > 0
    }

    @Test
    public void testListUserPageOutOfBounds(){
        println "Test Start"

        def http = new RESTClient( URL )

        // Create a user so we have at least 1
        http.post(body: [ email:"testUser3.list@meltdev.com", password:"vespa" ], requestContentType: JSON)

        def myQuery = [ page: "999", limit: "25" ]

        def resp = http.get(path: "/api/user", requestContentType: JSON, query : myQuery)

        println "Out of bounds final page size: $resp.data.asList.size"
        println "Out of bounds first id? page size: $resp.data.asList[0].email"

        assert resp.status == 200
        assert resp.data.asList.size > 0
    }
}
