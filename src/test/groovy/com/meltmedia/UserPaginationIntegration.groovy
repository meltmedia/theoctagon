package com.meltmedia

import groovyx.net.http.RESTClient
import org.junit.BeforeClass
import org.junit.Test

/**
 * Created with IntelliJ IDEA.
 * User: jheun
 * Date: 6/26/13
 */
class UserPaginationIntegration {

    private static final JSON = "application/json"
    private static final URL = "http://localhost:8080/api/user"
    private static final http = new RESTClient( URL )
    private static final EXPECTED_DEFAULT_LIMIT = 30
    private static final EXPECTED_DEFAULT_PAGE = 0
    private static final EXPECTED_USERS_AT_STARTUP = 1

    @BeforeClass
    public static void makeSureOnlyExpectedNumberOfUsersExistAtStart() {
        def resp = http.get(requestContentType: JSON)
        assert resp.data.asList.size == EXPECTED_USERS_AT_STARTUP
    }

    @Test
    public void testListUsersWithNoParameters() {
        def resp = http.get(requestContentType: JSON)

        assert resp.status == 200
        def list = resp.data.asList
        assert list.size > 0 && list.size <= EXPECTED_DEFAULT_LIMIT
        assert resp.headers.'Pagination-Limit' == EXPECTED_DEFAULT_LIMIT.toString()
        assert resp.headers.'Pagination-Page' == EXPECTED_DEFAULT_PAGE.toString()
    }

    @Test
    public void testListUsersWithNegativePage() {
        try {
            http.get(query: [page: -17], requestContentType: JSON)
            assert false, 'Expected exception'
        } catch (ex) {
            assert ex.response.status == 400
            assert ex.response.data.message.matches(/Negative value \(-\d+\) passed to setFirstResult/)
        }
    }

    @Test
    public void testListUsersWithNegativeLimit() {
        try {
            http.get(query: [limit: -13], requestContentType: JSON)
            assert false, 'Expected exception'
        } catch (ex) {
            assert ex.response.status == 400
            assert ex.response.data.message.matches(/Negative value \(-\d+\) passed to setMaxResults/)
        }
    }

    @Test
    public void testListUsersWithPageOnly() {
        def resp = http.get(query: [page: 0], requestContentType: JSON)

        assert resp.status == 200
        def list = resp.data.asList
        assert list.size > 0 && list.size <= EXPECTED_DEFAULT_LIMIT
        assert resp.headers.'Pagination-Limit' == EXPECTED_DEFAULT_LIMIT.toString()
        assert resp.headers.'Pagination-Page' == 0.toString()
    }

    @Test
    public void testListUsersWithPageAndLimit() {
        def resp = http.get(query: [page: 0, limit: 5], requestContentType: JSON)

        assert resp.status == 200
        def list = resp.data.asList
        assert list.size > 0 && list.size <= 5
        assert resp.headers.'Pagination-Limit' == 5.toString()
        assert resp.headers.'Pagination-Page' == 0.toString()
    }

    @Test
    public void testListUsersWithLimitOnly() {
        def resp = http.get(query: [limit: 5], requestContentType: JSON)

        assert resp.status == 200
        def list = resp.data.asList
        assert list.size > 0 && list.size <= 5
        assert resp.headers.'Pagination-Limit' == 5.toString()
        assert resp.headers.'Pagination-Page' == EXPECTED_DEFAULT_PAGE.toString()
    }

    @Test
    public void testListUsers() { //TODO refactor
        def numUsers = 53
        def limit = 7

        createTestUsers(http, numUsers-EXPECTED_USERS_AT_STARTUP)

        int expectedPages = Math.ceil(numUsers / limit)
        int expectedLastPage = expectedPages - 1
        int expectedFullPages = Math.floor(numUsers / limit)

        def entitiesSeen = 0;
        def currentPage = 0

        while (expectedFullPages > 0) {
            def expectedEntitiesOnThisPage = limit
            entitiesSeen = flipPage(currentPage, limit, expectedLastPage, expectedEntitiesOnThisPage, entitiesSeen)
            currentPage++

            if (currentPage == expectedFullPages) {
                break
            }
        }

        if (expectedFullPages < expectedPages) {
            def expectedEntitiesOnThisPage = numUsers - entitiesSeen
            entitiesSeen = flipPage(currentPage, limit, expectedLastPage, expectedEntitiesOnThisPage, entitiesSeen)
            currentPage++
        }

        assert entitiesSeen == numUsers
        assert currentPage == expectedPages
    }

    private int flipPage(final int currentPage, final int limit, final int expectedLastPage, final int expectedEntitiesOnThisPage, int entitiesSeen) {
        def resp = http.get(query: [page: currentPage, limit: limit], requestContentType: JSON)

        assert resp.status == 200

        assert resp.data.asList.size == expectedEntitiesOnThisPage
        assert resp.headers.'Pagination-Limit' == limit.toString()
        assert resp.headers.'Pagination-Page' == currentPage.toString()
        assert resp.headers.'Pagination-Last-Page' == expectedLastPage.toString()

        entitiesSeen += resp.data.asList.size
    }

    private void createTestUsers(RESTClient http, int numUsers = 1) {
        for (int i = 0; i < numUsers; i++) {
            def addr = String.format("testUser.list.%d@meltdev.com", i)
            def resp = http.post(body: [email: addr, password: "pizzathehut"], requestContentType: JSON)
            def id = resp.data.id

            resp = http.get( path:"/api/user/" + resp.data.id, requestContentType: JSON )

            assert resp.status == 200
            assert resp.data.id == id
            assert resp.data.email == addr
        }
    }

}
