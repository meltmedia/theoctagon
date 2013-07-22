package com.meltmedia.representation;

/**
 *
 * @author @jacobheun
 *
 */
public class ErrorMessageRepresentation {

  private String message;

  public ErrorMessageRepresentation( String message ) {
    super();
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage( String message ) {
    this.message = message;
  }

}
