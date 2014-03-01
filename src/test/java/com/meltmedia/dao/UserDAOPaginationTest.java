package com.meltmedia.dao;

import com.google.common.base.Optional;
import com.meltmedia.data.PaginationList;
import com.meltmedia.data.User;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.jpa.JpaPersistModule;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

import static com.meltmedia.dao.UserDAO.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class UserDAOPaginationTest {

  private static final Random RANDOM = new Random();

  private final Logger log = LoggerFactory.getLogger(getClass());

  private PersistService service;
  private UserDAO dao;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @BeforeClass
  @SuppressWarnings("ConstantConditions")
  public static void makeSureMaxAndDefaultMakeSense() {
    assert MAX_LIST_LIMIT >= DEFAULT_LIST_LIMIT : String.format("The default of %d should not be more than the max of %d", DEFAULT_LIST_LIMIT, MAX_LIST_LIMIT);
  }

  @Before
  public void setupContext() throws IllegalArgumentException, NoSuchFieldException {

    Injector injector = Guice.createInjector( new JpaPersistModule("unitTest") );

    service = injector.getInstance( PersistService.class );
    service.start();

    dao = injector.getInstance( UserDAO.class );

  }

  @After
  public void tearDown() {

    service.stop();

  }

  @Test
  public void listWithNoUsersShouldReturnEmptyList() {
    deleteAllUsers();

    PaginationList<User> list = dao.list(0, Optional.of(DEFAULT_LIST_LIMIT));

    assertTrue(list.isEmpty());
  }

  @Test
  public void listWithNegativePageShouldThrowException() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(String.format("Negative value (%d) passed to setFirstResult", -DEFAULT_LIST_LIMIT));

    dao.list(-1, Optional.of(DEFAULT_LIST_LIMIT));
  }

  @Test
  public void listWithNegativeLimitShouldThrowException() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(String.format("Negative value (%d) passed to setFirstResult", -DEFAULT_LIST_LIMIT));

    dao.list(1, Optional.of(-DEFAULT_LIST_LIMIT));
  }

  @Test
  public void testBasicListAcrossMultiplePages() {
    regenerateTestUsers(DEFAULT_LIST_LIMIT);
    int limit = DEFAULT_LIST_LIMIT / 7;

    int page = 0;
    PaginationList<User> list = dao.list(page, Optional.of(limit));

    int pagesRequired = calculateNumberOfPagesRequiredToListAllUsers(DEFAULT_LIST_LIMIT, limit);
    assertThat(list.getLastPage(), is(pagesRequired-1));

    PaginationList<User> expected = new PaginationList<User>(list.getEntities(), page, limit, (long) DEFAULT_LIST_LIMIT);
    assertEquals(expected, list);
  }

  @Test
  public void listWithInfiniteLimitShouldReturnNoMoreThanDefaultLimit() {
    int numUsers = MAX_LIST_LIMIT * 2;
    regenerateTestUsers(numUsers);

    int page = 0;
    PaginationList<User> list = dao.list(page, Optional.of(INFINITE_LIST_LIMIT));

    assertThat(list.size(), is(DEFAULT_LIST_LIMIT));

    PaginationList<User> expected = new PaginationList<User>(list.getEntities(), page, DEFAULT_LIST_LIMIT, (long)numUsers);
    assertEquals(expected, list);
  }

  @Test
  public void listWithUnspecifiedLimitShouldReturnNoMoreThanDefaultLimit() {
    int numUsers = MAX_LIST_LIMIT * 2;
    regenerateTestUsers(numUsers);

    int page = 0;
    PaginationList<User> list = dao.list(page, Optional.<Integer>absent());

    assertThat(list.size(), is(DEFAULT_LIST_LIMIT));

    PaginationList<User> expected = new PaginationList<User>(list.getEntities(), page, DEFAULT_LIST_LIMIT, (long)numUsers);
    assertEquals(expected, list);
  }

  @Test
  public void pagingThroughUsersShouldMatchExpectedNumberOfPages() {
    int numUsers = regenerateRandomNumberOfTestUsers();
    int usersPerPage = nextNonZeroRandomUpTo(numUsers);

    int pageCount = pageThroughUsersUntilExhausted(numUsers, usersPerPage);

    int expected = calculateNumberOfPagesRequiredToListAllUsers(numUsers, usersPerPage);
    assertThat(pageCount, is(expected));
  }

  private void regenerateTestUsers(int num) {
    deleteAllUsers();
    generateTestUsers(num);
    log.info(num + " users generated");
  }

  private void deleteAllUsers() {
    for (User user : dao.list()) {
      dao.delete(user);
    }
    assert dao.list().isEmpty() : "All users should have been removed.";
  }

  private void generateTestUsers(int n) {
    for (int i = 0; i < n; i++) {
      User user = new User();
      user.setEmail(String.format("testUser%d@meltdev.com", i));
      user.setPassword("pizzathehutt");

      dao.create(user);
    }
  }

  private int regenerateRandomNumberOfTestUsers() {
    int numUsers = nextNonZeroRandomUpTo(100);
    regenerateTestUsers(numUsers);
    return numUsers;
  }

  private int nextNonZeroRandomUpTo(int n) {
    int i = 0;
    while (i == 0) {
      i = RANDOM.nextInt(n);
    }
    return i;
  }

  private int pageThroughUsersUntilExhausted(int numUsers, int limit) {
    int pageCount = 0;
    int remaining = numUsers;
    while (remaining > 0) {
      PaginationList<User> list = dao.list(pageCount, Optional.of(limit));
      assert list.getPage() == pageCount : String.format("page number %d should match page count %d", list.getPage(), pageCount);
      remaining -= list.size();
      pageCount++;
    }
    return pageCount;
  }

  /**
   * deliberately different calculation than {@link PaginationList#getLastPage()} to catch a mistake in one or the other
   */
  private int calculateNumberOfPagesRequiredToListAllUsers(int numUsers, int limit) {
    int expectedNumberOfPagesToListAllUsers = numUsers / limit;
    boolean allUsersDidNotFitOnLastPage = numUsers % limit > 0;
    if (allUsersDidNotFitOnLastPage) {
      expectedNumberOfPagesToListAllUsers++;
    }
    System.err.printf("********* Given %d users, with a limit of %d it should take %d pages to list all users.%n", numUsers, limit, expectedNumberOfPagesToListAllUsers);
    return expectedNumberOfPagesToListAllUsers;
  }
}
