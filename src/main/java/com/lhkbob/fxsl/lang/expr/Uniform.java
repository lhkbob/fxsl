package com.lhkbob.fxsl.lang.expr;

import com.lhkbob.fxsl.lang.Scope;
import com.lhkbob.fxsl.util.EfficientEqualityBase;

import static com.lhkbob.fxsl.util.Preconditions.notNull;

/**
 * Uniform
 * =======
 */
public class Uniform extends EfficientEqualityBase implements Expression {
  private final Scope scope;
  private final String name;

  public Uniform(Scope scope, String name) {
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
  public <T> T accept(Visitor<T> visitor) {
    return visitor.visitUniform(this);
  }

  @Override
  public String toString() {
    return String.format("uniform(%s)", name);
  }

  @Override
  protected int computeHashCode() {
    return scope.hashCode() ^ name.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    Uniform u = compareHashCodes(Uniform.class, o);
    return u != null && u.scope.equals(scope) && u.name.equals(name);
  }
}
