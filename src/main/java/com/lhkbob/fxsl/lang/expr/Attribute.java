package com.lhkbob.fxsl.lang.expr;

import com.lhkbob.fxsl.lang.Scope;
import com.lhkbob.fxsl.util.EfficientEqualityBase;

import static com.lhkbob.fxsl.util.Preconditions.notNull;

/**
 *
 */
public class Attribute extends EfficientEqualityBase implements Expression {
  private final Scope scope;
  private final String name;

  public Attribute(Scope scope, String name) {
    notNull("scope", scope);
    notNull("name", name);
    this.scope = scope;
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public Scope getScope() {
    return scope;
  }

  @Override
  public <T> T accept(Expression.Visitor<T> visitor) {
    return visitor.visitAttribute(this);
  }

  @Override
  public String toString() {
    return String.format("attr(%s)", name);
  }

  @Override
  protected int computeHashCode() {
    return scope.hashCode() ^ name.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    Attribute a = compareHashCodes(Attribute.class, o);
    return a != null && a.scope.equals(scope) && a.name.equals(name);
  }
}
