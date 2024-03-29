package com.lhkbob.fxsl.lang.expr;

import com.lhkbob.fxsl.lang.Scope;
import com.lhkbob.fxsl.lang.type.PrimitiveType;
import com.lhkbob.fxsl.util.EfficientEqualityBase;
import com.lhkbob.fxsl.util.Immutable;

/**
 * Primitive Values
 * ================
 *
 * Primitive value expressions are constructors for instances of {@link
 * com.lhkbob.fxsl.lang.type.PrimitiveType}. FXSL does not allow the sampler primitive types from
 * being constructed within FXSL. As such it is not possible to have a primitive value constructor
 * for values of that class of types. With this constraint, PrimitiveValue represents float, int,
 * and bool constants defined within FXSL code.
 *
 * @author Michael Ludwig
 */
@Immutable
public final class PrimitiveValue extends EfficientEqualityBase implements Expression {
  private final transient PrimitiveType type;
  private final Object value;

  /**
   * Create a new primitive value that will have the FLOAT primitive type and has the given
   * `value`.
   *
   * @param value
   *     The float value this value represents
   */
  public PrimitiveValue(float value) {
    this.value = value;
    type = PrimitiveType.FLOAT;
  }

  /**
   * Create a new primitive value that will have the INT primitive type and has the given `value`.
   *
   * @param value
   *     The int value this value represents
   */
  public PrimitiveValue(int value) {
    this.value = value;
    type = PrimitiveType.INT;
  }

  /**
   * Create a new primitive value that will have the BOOLEAN primitive type and has the given
   * `value`.
   *
   * @param value
   *     The boolean value this value will represent
   */
  public PrimitiveValue(boolean value) {
    this.value = value;
    type = PrimitiveType.BOOL;
  }

  @Override
  public <T> T accept(Visitor<T> visitor) {
    return visitor.visitPrimitive(this);
  }

  @Override
  public boolean equals(Object o) {
    PrimitiveValue v = compareHashCodes(PrimitiveValue.class, o);
    return v.value.equals(value);
  }

  @Override
  public Scope getScope() {
    return Scope.NATIVE_SCOPE;
  }

  /**
   * @return The known primitive type of this value, determined by the constructor that created this
   * instance.
   */
  public PrimitiveType getType() {
    return type;
  }

  /**
   * Get the concrete value of this primitive. This will be an instance of {@link
   * java.lang.Integer} if the expression's type is INT, it will be an instance of {@link
   * java.lang.Float} if the type is FLOAT, or it will be an instance of {@link java.lang.Boolean}
   * if the type is BOOLEAN.
   *
   * The other primitive types cannot be represented by instances of this class, so there is no
   * need to worry about how to represent them in this method.
   *
   * @return The actual value this value is constructed with
   */
  public Object getValue() {
    return value;
  }

  @Override
  public String toString() {
    return value.toString();
  }

  @Override
  protected int computeHashCode() {
    return value.hashCode();
  }
}
