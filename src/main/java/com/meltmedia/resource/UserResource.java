package com.meltmedia.resource;

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

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * UserResource: jheun
 * Date: 6/26/13
 */

@Path("/user")
@Singleton
public class UserResource {

  @Context UriInfo uriInfo;

  private Logger log = LoggerFactory.getLogger( getClass() );

  private static final String DEFAULT_PAGE = "0";
  private static final String DEFAULT_LIMIT = "25";

  protected UserRepresentation createRepresentation(User user) {

    UserRepresentation rep = new UserRepresentation(user);
    // Link to the full entity
    rep.getLinks().add( new Link( uriInfo.getBaseUriBuilder().path( UserResource.class ).path( user.getId().toString()).build(), "self", MediaType.APPLICATION_JSON ) );
    return rep;

  }

  @Inject ValidationService validationService;
  @Inject UserDAO dao;

  @GET
  @Produces("application/json")
  public List<UserRepresentation> getUsers(
    @DefaultValue(DEFAULT_PAGE) @QueryParam("page") int page,
    @DefaultValue(DEFAULT_LIMIT) @QueryParam("limit") int limit) {    
    
    if (page < 0) page = Integer.parseInt(DEFAULT_PAGE);
    if (limit < 0) limit = Integer.parseInt(DEFAULT_LIMIT);

    List<User> users = dao.list(page,limit);


    List<UserRepresentation> userReps = new ArrayList<UserRepresentation>();

    for (User user : users) {
      userReps.add( createRepresentation( user ) );
    }

    return userReps;
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
    return createRepresentation( user );

  }

}
