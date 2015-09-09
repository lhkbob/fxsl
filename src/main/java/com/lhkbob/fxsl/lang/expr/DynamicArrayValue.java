package com.lhkbob.fxsl.lang.expr;

import com.lhkbob.fxsl.lang.Scope;
import com.lhkbob.fxsl.util.EfficientEqualityBase;
import com.lhkbob.fxsl.util.Immutable;

import static com.lhkbob.fxsl.util.Preconditions.notNull;

/**
 *
 */
@Immutable
public final class DynamicArrayValue extends EfficientEqualityBase implements Expression {
  private final Expression elementFunction;
  private final Expression length;
  private final Scope scope;

  public DynamicArrayValue(Scope scope, Expression length, Expression elementFunction) {
    notNull("scope", scope);
    notNull("length", length);
    notNull("elementFunction", elementFunction);
    this.scope = scope;
    this.length = length;
    this.elementFunction = elementFunction;
  }

  @Override
  public <T> T accept(Visitor<T> visitor) {
    return visitor.visitDynamicArray(this);
  }

  @Override
  public boolean equals(Object o) {
    DynamicArrayValue a = compareHashCodes(DynamicArrayValue.class, o);
    return a != null && scope.equals(a.scope) && length.equals(a.length) &&
        elementFunction.equals(a.elementFunction);
  }

  public Expression getElementFunction() {
    return elementFunction;
  }

  public Expression getLength() {
    return length;
  }

  @Override
  public Scope getScope() {
    return scope;
  }

  @Override
  public String toString() {
    return String.format("[%s : %s]", length, elementFunction);
  }

  @Override
  protected int computeHashCode() {
    int hash = 17;
    hash += hash * 31 + scope.hashCode();
    hash += hash * 31 + elementFunction.hashCode();
    hash += hash * 31 + length.hashCode();
    return hash;
  }
}
