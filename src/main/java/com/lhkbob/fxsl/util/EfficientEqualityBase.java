package com.lhkbob.fxsl.util;

/**
 * EfficientEqualityBase
 * =====================
 *
 * This utility super class is designed for immutable classes (or at least classes whose equality
 * and hashcode state does not change over an instance's lifetime). The implementation of
 * `hashCode()` caches a hash provided by subclasses' implementations of {@link #computeHashCode()}.
 * This abstract class mandates that `equals(Object)` be implemented by the subclass, but provides a
 * utility method {@link #compareHashCodes(Class, Object)} that uses the cached hashes to
 * efficiently rule out many false positives.
 *
 * @author Michael Ludwig
 */
public abstract class EfficientEqualityBase {
  @Override
  public abstract boolean equals(Object o);

  @Override
  public int hashCode() {
    if (hashcode == INVALID_HASHCODE) {
      hashcode = computeHashCode();
    }
    return hashcode;
  }

  /**
   * Compare the hashcode of this instance to the hashcode of the other instance. If `other` is
   * not null, is an instance of type `cls`, and its hashcode is equal to this instances hashcode
   * then `other` is returned cast into type T. Otherwise null is returned.
   *
   * When null is returned, it is guaranteed that `other` is not equal to this instance. If a
   * non-null instance is returned the equals implementation should compare all equals-defining
   * state as normal.
   *
   * @param cls
   *     The required class of `other` for an equals check to be meaningful
   * @param other
   *     The instance being compared
   * @return `other` cast to type T if its not null, is a T, and has a matching hashcode as this
   * instance.
   */
  protected <T extends EfficientEqualityBase> T compareHashCodes(Class<T> cls, Object other) {
    // this if also checks if other is null
    if (!cls.isInstance(other)) {
      return null;
    }

    T o = cls.cast(other);
    if (o.hashCode() == hashCode()) {
      // possibly equal at this point and eliminates expensive comparisons for instances that
      // are definitely not equal
      return o;
    } else {
      return null;
    }
  }

  /**
   * Compute the hashcode that should be returned by `hashCode()`. All requirements and
   * expectations described in the documentation of {@link Object#hashCode()} apply here. A special
   * value is used to indicate that this method hasn't been invoked yet. If somehow that is returned
   * by this method, the caching behavior of this class is disabled for that instance. `hashCode()`
   * will still return that value but it will be computed every time. Under normal operation, you
   * can expect this to be called once the first time `hashCode` or `equals` are invoked.
   *
   * @return The hashcode for the immutable object
   */
  protected abstract int computeHashCode();

  // An unlikely hashcode to occur in nature. The code works fine even when the subclasses produce
  // this as a valid hashcode, it just won't be cached.
  private static final int INVALID_HASHCODE = ~0;
  private transient int hashcode = INVALID_HASHCODE;
}
