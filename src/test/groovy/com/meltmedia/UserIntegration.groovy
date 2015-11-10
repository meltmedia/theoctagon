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
    private static boolean runOnce = false;

    @Before
    public void setup() {
        def http = new RESTClient( URL )

        // Only runs once instead of before each test
        if(!runOnce) {
            // Creates a bunch of users
            for (int i = 1; i <= 100; i++) {
                http.post(body: [email: "testUser" + i + ".list@meltdev.com", password: "vespa"], requestContentType: JSON)
            }
            runOnce = true;
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
    public void testGetBiggerPageSize() {
        final int PAGE_SIZE = 200;

        def http = new RESTClient( URL + "?pageSize=" + PAGE_SIZE );
        def resp = http.get(path: "/api/user", requestContentType: JSON);

        assert resp.status == 200;
        assert resp.data.asList.size <= PAGE_SIZE;
    }

    @Test
    public void testGetNegativePageSize() {
        final int PAGE_SIZE = -1;

        def http = new RESTClient( URL + "?pageSize=" + PAGE_SIZE );
        def resp = http.get(path: "/api/user", requestContentType: JSON);

        assert resp.status == 200;
        assert resp.data.asList.size == 0;
    }

    @Test
    public void testRequestValidPage() {
        final int PAGE = 2;
        final int PAGE_SIZE = 50;

        def http = new RESTClient( URL + "?pageSize=" + PAGE_SIZE + "&page=" + PAGE );
        def resp = http.get(path: "/api/user", requestContentType: JSON);

        assert resp.status == 200;
        assert resp.data.asList.size <= PAGE_SIZE;
    }

    @Test
    public void testRequestNegativeOutOfBoundsPage() {
        final int PAGE = -1;

        def http = new RESTClient( URL + "?page=" + PAGE );
        def resp = http.get(path: "/api/user", requestContentType: JSON);

        assert resp.status == 200;
        assert resp.data.asList.size == 0;
    }

    @Test
    public void testRequestPositiveOutOfBoundsPage() {
        final int PAGE = 1000;

        def http = new RESTClient( URL + "?page=" + PAGE );
        def resp = http.get(path: "/api/user", requestContentType: JSON);

        assert resp.status == 200;
        assert resp.data.asList.size == 0;
    }
}
