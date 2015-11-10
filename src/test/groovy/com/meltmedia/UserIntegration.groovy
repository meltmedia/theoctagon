package com.meltmedia
import groovyx.net.http.RESTClient
import org.junit.Before
import org.junit.Test
/**
 * Created with IntelliJ IDEA.
 * User: jheun
 * Date: 6/26/13
 */
class UserIntegration {

    private static final String JSON = "application/json"
    private static final String URL = "http://localhost:8080/api/user"

    @Before
    public void setup() {
        def http = new RESTClient( URL )

        // Creates a bunch of users
        for (int i = 1; i <= 100; i++) {
            http.post(body: [ email:"testUser" + i +".list@meltdev.com", password:"vespa" ], requestContentType: JSON)
        }
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
    public void testGetUserList() {
        def http = new RESTClient( URL );
        def resp = http.get(path: "/api/user", requestContentType: JSON);

        assert resp.status == 200;
        assert resp.data.asList.size <= 25;
    }

    @Test
    public void testGetBiggerUserList() {
        final int NUMBER_OF_USERS = 200;

        def http = new RESTClient( URL + "?numberOfUsers=" + NUMBER_OF_USERS );
        def resp = http.get(path: "/api/user", requestContentType: JSON);

        assert resp.status == 200;
        assert resp.data.asList.size <= NUMBER_OF_USERS;
    }

    @Test
    public void testGetNegativeUserList() {
        final int NUMBER_OF_USERS = -1;

        def http = new RESTClient( URL + "?numberOfUsers=" + NUMBER_OF_USERS );
        def resp = http.get(path: "/api/user", requestContentType: JSON);

        assert resp.status == 200;
        assert resp.data.asList.size == 0;
    }

    @Test
    public void testRequestValidPage() {
        final int PAGE_NUMBER = 2;
        final int NUMBER_OF_USERS = 50;

        def http = new RESTClient( URL + "?numberOfUsers=" + NUMBER_OF_USERS + "&pageNumber=" + PAGE_NUMBER );
        def resp = http.get(path: "/api/user", requestContentType: JSON);

        assert resp.status == 200;
        assert resp.data.asList.size <= 50;
    }

    @Test
    public void testRequestNegativeOutOfBoundsPage() {
        final int PAGE_NUMBER = -1;

        def http = new RESTClient( URL + "?pageNumber=" + PAGE_NUMBER );
        def resp = http.get(path: "/api/user", requestContentType: JSON);

        assert resp.status == 200;
        assert resp.data.asList.size == 0;
    }

    @Test
    public void testRequestPositiveOutOfBoundsPage() {
        final int PAGE_NUMBER = 1000;

        def http = new RESTClient( URL + "?pageNumber=" + PAGE_NUMBER );
        def resp = http.get(path: "/api/user", requestContentType: JSON);

        assert resp.status == 200;
        assert resp.data.asList.size == 0;
    }
}
