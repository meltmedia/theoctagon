package com.meltmedia.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.google.common.base.Optional;
import com.meltmedia.data.IEntity;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.meltmedia.data.PaginationList;

/**
 * Bare implementation of a DAO for simple entities with OpenJPA.
 * 
 * @author Devon Tackett
 *
 */
public abstract class AbstractDAO<E extends IEntity> implements IDAO<E> {

  static final int MAX_LIST_LIMIT = 100;
  static final int DEFAULT_LIST_LIMIT = 30;
  static final int INFINITE_LIST_LIMIT = 0;

  @Inject Provider<EntityManager> provider;

  protected Class<E> entityClass;
  
  public AbstractDAO( Class<E> entityClass ) {
    this.entityClass = entityClass;
  }
    
  public E get(Long id) {
    return provider.get().find(entityClass, id);
  }
  
  public E create(E entity) {
    provider.get().persist(entity);
    provider.get().flush();
    return entity;
  }
  
  public E update(E entity) {
    provider.get().persist(entity);
    provider.get().flush();
    return entity;
  }
  
  public E refresh(E entity) {
    provider.get().refresh(entity);
    return entity;
  }
  
  public Boolean delete(E entity) {
    provider.get().remove(entity);
    provider.get().flush();
    
    return true;
  }
  
  public Boolean lock(E entity) {
    provider.get().lock(entity, LockModeType.WRITE);
    return true;
  }
  
  public Boolean deleteById( long id ) {
    E entity = get(id);
    if( entity == null ) return true;
    provider.get().remove(entity);
    
    return true;
  }
  
  public List<E> list() {
    CriteriaBuilder builder = provider.get().getCriteriaBuilder();

    CriteriaQuery<E> criteria = builder.createQuery( entityClass );

    Root<E> listRoot = criteria.from( entityClass );

    criteria.select( listRoot );
    
    List<E> entities = provider.get().createQuery(criteria).getResultList();
    
    return entities;
  }

  public PaginationList<E> list(int page, Optional<Integer> optionalLimit) {
    CriteriaBuilder builder = provider.get().getCriteriaBuilder();

    CriteriaQuery<E> criteria = builder.createQuery( entityClass );

    Root<E> listRoot = criteria.from( entityClass );

    criteria.select( listRoot );
    criteria.orderBy(builder.asc(listRoot.get("createdDate")));

    int limit = enforceDefaultOrMaxListLimit(optionalLimit);

    List<E> entities = provider.get().createQuery(criteria).setFirstResult(limit*page).setMaxResults(limit).getResultList();
    PaginationList<E> paginatedEntities = new PaginationList<E>(entities, page, limit, count());
    
    return paginatedEntities;
  }

  private int enforceDefaultOrMaxListLimit(Optional<Integer> optionalLimit) {
    int limit = optionalLimit.or(DEFAULT_LIST_LIMIT);

    if (limit == INFINITE_LIST_LIMIT) {
      limit = DEFAULT_LIST_LIMIT;
    }

    return Math.min(limit, MAX_LIST_LIMIT);
  }

  @Override
  public Long count() {
    CriteriaBuilder builder = provider.get().getCriteriaBuilder();

    CriteriaQuery<Long> criteria = builder.createQuery(Long.class);

    Root<E> listRoot = criteria.from(entityClass);

    criteria.select(builder.count(listRoot));

    return provider.get().createQuery(criteria).getSingleResult();
  }
}