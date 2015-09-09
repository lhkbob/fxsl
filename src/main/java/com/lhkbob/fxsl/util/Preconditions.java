package com.lhkbob.fxsl.util;

import java.util.Collection;

/**
 *
 */
public class Preconditions {
  public static void noNullElements(String param, Iterable<?> o) throws NullPointerException {
    for (Object e : o) {
      if (e == null) {
        throw new NullPointerException("Element in " + param + " cannot be null");
      }
    }
  }

  public static void notEmpty(String param, Collection<?> o) throws IllegalArgumentException {
    if (o.isEmpty()) {
      throw new IllegalArgumentException(param + " cannot be empty");
    }
  }

  public static void notNull(String param, Object o) throws NullPointerException {
    if (o == null) {
      throw new NullPointerException(param + " cannot be null");
    }
  }

  public static void validCollection(String param, Collection<?> o) throws IllegalArgumentException,
      NullPointerException {
    notNull(param, o);
    notEmpty(param, o);
    noNullElements(param, o);
  }
}
