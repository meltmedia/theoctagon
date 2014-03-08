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

import javax.servlet.http.HttpServletResponse;
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

  protected UserRepresentation createRepresentation(User user) {

    UserRepresentation rep = new UserRepresentation(user);
    // Link to the full entity
    rep.getLinks().add( new Link( uriInfo.getBaseUriBuilder().path( UserResource.class ).path( user.getId().toString()).build(), "self", MediaType.APPLICATION_JSON ) );
    return rep;

  }

  @Inject ValidationService validationService;
  @Inject UserDAO dao;

    /**
     * This method lists a range of users as specified by pageSize and pageNumber. If neither are specified
     * it defaults to pageSize = 10 and pageNumber = 1. This method uses a 1-based index for pages.
     */
    @GET
    @Produces("application/json")
    public List<UserRepresentation> getUsers(@DefaultValue("10") @QueryParam("pageSize") int pageSize,
                                           @DefaultValue("1") @QueryParam("pageNumber") int pageNumber,
                                           @Context HttpServletResponse response) {
        // Get full list of users
        List<User> users = dao.list();
        List<UserRepresentation> userReps = new ArrayList<UserRepresentation>();

        // If the request contains an illegal parameter, throw HTTP 400 exception.
        // Some might argue that we should allow 0 for pageSize/pageNumber, but
        // the resulting page for either parameter would always display 0 users
        // so that seems unproductive.
        if (pageSize <= 0 || pageNumber <= 0)
            throw new WebApplicationException( 400 );

        // Calculate starting and ending index
        int startingIndex = pageSize * (pageNumber - 1);
        int endingIndex = startingIndex + pageSize;

        // Make sure we don't go beyond the final user in the data source
        int maxIndexToDisplay = Math.min(endingIndex, users.size()) - 1;

        // Add all users to list within our calculated range
        for (int i = startingIndex; i <= maxIndexToDisplay; i++) {
            userReps.add( createRepresentation( users.get(i) ) );
        }

        // Add an HTTP Header to describe what page number we're serving (particularly
        // useful when no parameters are provided by user)
        response.setHeader("Content-Range", "Page Number: " + String.valueOf(pageNumber));

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
