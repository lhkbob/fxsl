package com.lhkbob.fxsl.lang.expr;

import com.lhkbob.fxsl.lang.Scope;
import com.lhkbob.fxsl.util.EfficientEqualityBase;
import com.lhkbob.fxsl.util.Immutable;

import static com.lhkbob.fxsl.util.Preconditions.notNull;

/**
 * Array Access
 * ============
 *
 * An array access is an expression that takes an expression evaluating to an array value and an
 * expression evaluating to an integer index and returns the element in the array value at the
 * index. Either of these two expressions may be wildcards but doing so imposes the constraint that
 * the wildcard instantiate to an array or integer respectively.
 *
 * @author Michael Ludwig
 */
@Immutable
public final class ArrayAccess extends EfficientEqualityBase implements Expression {
  private final Expression array;
  private final Expression index;
  private final Scope scope;

  /**
   * Create a new array access expression that accesses `array` at the given `index`.
   *
   * @param scope
   *     The scope the expression is defined in
   * @param array
   *     The expression evaluating to an array value
   * @param index
   *     The expression evaluating to the integer index to access
   * @throws java.lang.NullPointerException
   *     if `scope`, `array`, or `index` are null
   */
  public ArrayAccess(Scope scope, Expression array, Expression index) {
    notNull("scope", scope);
    notNull("array", array);
    notNull("index", index);

    this.scope = scope;
    this.array = array;
    this.index = index;
  }

  @Override
  public <T> T accept(Visitor<T> visitor) {
    return visitor.visitArrayAccess(this);
  }

  @Override
  public boolean equals(Object o) {
    ArrayAccess a = compareHashCodes(ArrayAccess.class, o);
    return a != null && a.scope.equals(scope) && a.array.equals(array) && a.index.equals(index);
  }

  /**
   * Get the expression being accessed as an array. Although possibly a complex expression, it
   * must evaluate to an array value or a wildcard that can be instantiated to an array type.
   *
   * @return The array being accessed
   */
  public Expression getArray() {
    return array;
  }

  /**
   * Get the expression that evaluates to the array index being accessed. Array indices are
   * positive integers that start at 0. Although possibly a complex expression, it must evaluate to
   * a primitive integer or a  wildcard that can be instantiated to an int.
   *
   * @return The array index
   */
  public Expression getIndex() {
    return index;
  }

  @Override
  public Scope getScope() {
    return scope;
  }

  @Override
  public String toString() {
    return array.toString() + "[" + index.toString() + "]";
  }

  @Override
  protected int computeHashCode() {
    int result = 17;
    result += 31 * result + scope.hashCode();
    result += 31 * result + array.hashCode();
    result += 31 * result + index.hashCode();
    return result;
  }
}
