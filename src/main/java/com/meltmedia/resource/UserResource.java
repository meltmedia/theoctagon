package com.meltmedia.resource;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.meltmedia.data.PaginationList;
import com.meltmedia.dao.UserDAO;
import com.meltmedia.data.User;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.meltmedia.representation.JsonMessageException;
import com.meltmedia.representation.UserRepresentation;
import com.meltmedia.service.ValidationService;
import com.meltmedia.util.BakedBeanUtils;
import com.meltmedia.util.UserUtil;
import com.praxissoftware.rest.core.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * UserResource: jheun
 * Date: 6/26/13
 */

@Path("/user")
@Singleton
public class UserResource {

  private final CreateUserRepresentationFunction createUserRepresentationFunction = new CreateUserRepresentationFunction();
  private final Logger log = LoggerFactory.getLogger( getClass() );

  @Context UriInfo uriInfo;
  @Context HttpServletResponse response;

  @Inject ValidationService validationService;
  @Inject UserDAO dao;

  @GET
  @Produces("application/json")
  public List<UserRepresentation> getUsers(@QueryParam("page") int page, @QueryParam("limit") int limit) {
    try {
      PaginationList<User> users = dao.list(page, Optional.of(limit));

      addPaginationHeaders(response, users);

      List<UserRepresentation> userReps = Lists.transform(users, createUserRepresentationFunction);
      return userReps;
    } catch (IllegalArgumentException e) { //TODO provide more meaningful message to client
      throw new JsonMessageException(Response.Status.BAD_REQUEST, e.getLocalizedMessage());
    }
  }

  private void addPaginationHeaders(HttpServletResponse response, PaginationList<User> users) {
    response.addHeader("Pagination-Limit", String.valueOf(users.getLimit()));
    response.addHeader("Pagination-Page", String.valueOf(users.getPage()));
    response.addHeader("Pagination-Last-Page", String.valueOf(users.getLastPage()));

    URI nextPageUri = createExperimentalNextPageUri(users);
    response.addHeader("Experimental-Pagination-Page-Next", nextPageUri.toASCIIString());
  }

  private URI createExperimentalNextPageUri(PaginationList<User> users) {
    int nextPageNumber = users.getNextPageNumber();
    return uriInfo.getBaseUriBuilder().path(UserResource.class).replaceQueryParam("limit", users.getLimit()).replaceQueryParam("page", nextPageNumber).build();
  }

  @GET
  @Path("/{userId}")
  @Produces("application/json")
  public UserRepresentation getUser(@PathParam("userId") long id) {
    User user = dao.get( id );

    if (user == null) {
      throw new WebApplicationException( 404 );
    }

    return createRepresentation( user );
  }

  @POST
  @Consumes("application/json")
  @Produces("application/json")
  public UserRepresentation addUser(UserRepresentation rep) {

    // Validate the new user
    validationService.runValidationForJaxWS( rep );

    User user = new User();

    try {

      // Copy the appropriate properties to the new User object
      BakedBeanUtils.safelyCopyProperties( rep, user );

    } catch ( BakedBeanUtils.HalfBakedBeanException ex ) {

      log.error( "There was an error processing the new user input.", ex );
      throw new JsonMessageException( Response.Status.INTERNAL_SERVER_ERROR, "There was an error processing the input." );

    }

    // Set the new password, salting and hashing and all that neat jazz
    UserUtil.setupNewPassword( user, rep.getPassword().toCharArray() );

    // Create the user in the system
    dao.create( user );

    // Return a representation of the user
    return createUserRepresentationFunction.apply( user );

  }

  private UserRepresentation createRepresentation(User user) {
    UserRepresentation rep = new UserRepresentation(user);
    addFullEntityLink(rep, user);
    return rep;
  }

  private void addFullEntityLink(UserRepresentation rep, User user) {
    Link fullEntityLink = new Link(uriInfo.getBaseUriBuilder().path(UserResource.class).path(user.getId().toString()).build(), "self", MediaType.APPLICATION_JSON);
    rep.getLinks().add(fullEntityLink);
  }

  private class CreateUserRepresentationFunction implements Function<User, UserRepresentation> {
    @Override
    public UserRepresentation apply(User user) {
      return createRepresentation(user);
    }
  }
}
