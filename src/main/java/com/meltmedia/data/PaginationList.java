package com.meltmedia.data;

import com.google.common.base.Objects;

import java.util.AbstractList;
import java.util.List;

public class PaginationList<E extends IEntity> extends AbstractList<E> implements List<E> {
  private final List<E> entities;
  private final int page;
  private final int limit;
  private final long count;
  private final int lastPage;
  private final int nextPageNumber;

  public PaginationList(List<E> entities, int page, int limit, long count) {
    this.entities = entities;
    this.page = page;
    this.limit = limit;
    this.count = count;
    lastPage = calculateLastPage();
    nextPageNumber = calculateNextPageNumber();
  }

  private int calculateNextPageNumber() { //TODO decide if special case when page == lastPage
    return Math.min(getPage() + 1, getLastPage());
  }

  private int calculateLastPage() { //TODO handle zero limit
    return (int) Math.ceil(getCount() / getLimit());
  }

  @Override
  public E get(int index) {
    return entities.get(index);
  }

  @Override
  public int size() {
    return entities.size();
  }

  public List<E> getEntities() {
    return entities;
  }

  public int getPage() {
    return page;
  }

  public long getCount() {
    return count;
  }

  public int getLimit() {
    return limit;
  }

  public int getLastPage() {
    return lastPage;
  }

  public int getNextPageNumber() {
    return nextPageNumber;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    PaginationList that = (PaginationList) o;

    if (count != that.count) return false;
    if (limit != that.limit) return false;
    if (nextPageNumber != that.nextPageNumber) return false;
    if (page != that.page) return false;
    if (lastPage != that.lastPage) return false;
    if (entities != null ? !entities.equals(that.entities) : that.entities != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (entities != null ? entities.hashCode() : 0);
    result = 31 * result + page;
    result = 31 * result + limit;
    result = 31 * result + (int) (count ^ (count >>> 32));
    result = 31 * result + lastPage;
    result = 31 * result + nextPageNumber;
    return result;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
        .add("entities", entities)
        .add("page", page)
        .add("limit", limit)
        .add("count", count)
        .add("lastPage", lastPage)
        .add("nextPageNumber", nextPageNumber)
        .toString();
  }
}

