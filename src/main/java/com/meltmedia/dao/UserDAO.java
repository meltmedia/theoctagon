package com.meltmedia.dao;

import com.google.common.base.Optional;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import com.meltmedia.data.PaginationList;
import com.meltmedia.data.User;

import javax.persistence.Query;
import java.util.List;

@Singleton
public class UserDAO extends AbstractDAO<User> {
  
  public UserDAO() {
    super(User.class);
  }

  @Override
  @Transactional
  public User get(Long id) {
    return super.get(id);
  }

  @Override
  @Transactional
  public User create(User entity) {
    return super.create(entity);
  }

  @Override
  @Transactional
  public User update(User entity) {
    return super.update(entity);
  }

  @Override
  @Transactional
  public Boolean delete(User entity) {
    return super.delete(entity);
  }

  @Override
  @Transactional
  public Boolean deleteById(long id) {
    return super.deleteById(id);
  }

  @Override
  @Transactional
  public List<User> list() {
    return super.list();
  }

  @Override
  @Transactional
  public PaginationList<User> list(int page, Optional<Integer> optionalLimit) {
    return super.list(page, optionalLimit);
  }

  @SuppressWarnings("unchecked")
  public User getByEmail(String email) {
    Query query = provider.get().createQuery("select usr from User usr where lower(usr.email) = :email");
    query.setParameter("email", email.toLowerCase());
    List<User> users = query.getResultList();
    if (users.size() > 0) {
      return users.get(0);
    } else {
      return null;
    }
  }  
  
}
