package com.meltmedia.util;

/**
 * This mimics Callable without throwing an exception
 * @author @jacobheun
 */
public interface NoExceptionCallable<T> {

  T call();

  /**
   * Does nothing.
   */
  NoExceptionCallable<?> NoOp = new NoExceptionCallable<Void>() {
    public Void call() {
      return null;
    };
  };

}
