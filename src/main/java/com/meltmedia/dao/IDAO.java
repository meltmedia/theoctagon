package com.meltmedia.dao;

import java.util.List;

import com.google.common.base.Optional;
import com.meltmedia.data.IEntity;
import com.meltmedia.data.PaginationList;

/**
 * Base interface for DAO services.
 * 
 * @author Devon Tackett
 *
 */
public interface IDAO<E extends IEntity> {  
  public E get(Long id);
  public E update(E entity);
  public E create(E entity);
  public E refresh(E entity);
  public Boolean lock(E entity);
  public List<E> list();
  public PaginationList<E> list(int page, Optional<Integer> limit);         // List entities (with pagination support)
  public Boolean delete(E entity);
  public Boolean deleteById(long id);
  public Long count();
}
