package com.meltmedia.representation;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created with IntelliJ IDEA.
 * User: jheun
 * Date: 6/27/13
 */
public class JsonMessageException extends WebApplicationException {

  private static final long serialVersionUID = -1214177432896854700L;

  public JsonMessageException(Response.Status status, String message) {

    super( Response.status( status ).header( "Content-type", MediaType.APPLICATION_JSON ).entity( new ErrorMessageRepresentation( message )).build() );

  }

}
