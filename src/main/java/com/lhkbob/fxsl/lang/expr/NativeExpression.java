package com.lhkbob.fxsl.lang.expr;

import com.lhkbob.fxsl.lang.Scope;
import com.lhkbob.fxsl.lang.type.Type;
import com.lhkbob.fxsl.util.Immutable;

import static com.lhkbob.fxsl.util.Preconditions.notNull;

/**
 * Native Expressions
 * ==================
 *
 * Native expressions are special purpose expressions created by the compiler and parser to
 * represent the base functionality of FXSL that cannot be itself be written in FXSL. Examples of
 * this are the actual math operations provided for primitive types, or creating a functional union.
 *
 * Native expressions are always concrete.
 *
 * Note that native expression's logical equality is equivalent to reference equality. This is
 * because there are many native expressions that may evaluate to the same type, which is the only
 * internal data available to a native expression.
 *
 * @author Michael Ludwig
 */
@Immutable
public final class NativeExpression implements Expression {
  private final String name;
  private final Type type;

  /**
   * Create a new native expression that results in a value of the given `type`.
   *
   * @param name
   *     A readable name of what the expression performs (does not define equality)
   * @param type
   *     The type of the native expression
   * @throws java.lang.NullPointerException
   *     if `name` or `type` is null
   */
  public NativeExpression(String name, Type type) {
    notNull("name", name);
    notNull("type", type);
    this.type = type;
    this.name = name;
  }

  @Override
  public <T> T accept(Visitor<T> visitor) {
    return visitor.visitNativeExpression(this);
  }

  @Override
  public boolean equals(Object o) {
    return o == this;
  }

  /**
   * @return The readable name/description of this native expression.
   */
  public String getName() {
    return name;
  }

  @Override
  public Scope getScope() {
    return Scope.NATIVE_SCOPE;
  }

  // Although these are the same definitions in Object, we implement them here to bypass any
  // annotation validation performed based on @LogicalEquality

  /**
   * @return The known type of the expression, which will only refer to primitive types and concrete
   * compositions of those types.
   */
  public Type getType() {
    return type;
  }

  @Override
  public int hashCode() {
    return System.identityHashCode(this);
  }

  @Override
  public String toString() {
    return "native(" + name + ")";
  }
}
