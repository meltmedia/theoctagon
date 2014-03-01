package com.meltmedia.data;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table( name = "USER" )
public class User extends BaseEntity {
  private String email;
  private String password;
  private String salt;

  public User() {

  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getSalt() {
    return salt;
  }

  public void setSalt(String salt) {
    this.salt = salt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    User user = (User) o;

    if (email != null ? !email.equals(user.email) : user.email != null) return false;
    if (password != null ? !password.equals(user.password) : user.password != null) return false;
    if (salt != null ? !salt.equals(user.salt) : user.salt != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = email != null ? email.hashCode() : 0;
    result = 31 * result + (password != null ? password.hashCode() : 0);
    result = 31 * result + (salt != null ? salt.hashCode() : 0);
    return result;
  }
}