package com.meltmedia.representation;

import com.meltmedia.data.User;
import com.meltmedia.util.BakedBeanUtils;
import com.meltmedia.serialize.DoNotSerialize;
import com.praxissoftware.rest.core.Link;
import com.praxissoftware.rest.core.Representation;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.hibernate.validator.constraints.Email;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * Representation of users for serialization
 * User: jheun
 * Date: 6/26/13
 */
public class UserRepresentation extends User implements Representation {

  private List<Link> links = new ArrayList<Link>();

  public UserRepresentation() { }

  public UserRepresentation( User user ) {

    try {

      BakedBeanUtils.safelyCopyProperties( user, this );

    } catch (BakedBeanUtils.HalfBakedBeanException ex) {

      throw new IllegalArgumentException( ex );

    }

  }

  @Override
  @NotNull(message="Email address must not be null.")
  @Email(message="A valid email address is required.")
  @Size(min=6, message="A valid email address is required.")
  public String getEmail() {

    return super.getEmail();

  }

  @Override
  @NotNull(message="A valid password is required.")
  @JsonSerialize(using=DoNotSerialize.class)
  @Size(min=4, message="Password must be at least 4 characters long.")
  public String getPassword() {

    return super.getPassword();

  }

  @Override
  @JsonIgnore
  public String getSalt() {

    // Don't show it!
    return null;

  }

  public List<Link> getLinks() {
    return links;
  }

  public void setLinks( List<Link> links ) {
    this.links = links;
  }


}
