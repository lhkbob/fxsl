package com.lhkbob.fxsl.lang;

import com.lhkbob.fxsl.util.EfficientEqualityBase;
import com.lhkbob.fxsl.util.Immutable;
import com.lhkbob.fxsl.util.LogicalEquality;

import static com.lhkbob.fxsl.util.Preconditions.notNull;

/**
 *
 */
@Immutable
@LogicalEquality(def = "Declarations are logically equal if the declared name and value are equal and the scopes are equal.")
public final class Declaration<T> extends EfficientEqualityBase {
  private final String name;
  private final Scope scope;
  private final T value;

  public Declaration(Scope scope, String name, T value) {
    notNull("scope", scope);
    notNull("name", name);
    notNull("value", value);

    this.value = value;
    this.name = name;
    this.scope = scope;
  }

  @Override
  public boolean equals(Object o) {
    Declaration d = compareHashCodes(Declaration.class, o);
    return d != null && d.scope.equals(scope) && d.name.equals(name) && d.value.equals(value);
  }

  public String getName() {
    return name;
  }

  public Scope getScope() {
    return scope;
  }

  public T getValue() {
    return value;
  }

  @Override
  public String toString() {
    return String.format("%s -> %s = %s", scope, name, value);
  }

  @Override
  protected int computeHashCode() {
    int hash = 17;
    hash += 31 * hash + scope.hashCode();
    hash += 31 * hash + name.hashCode();
    hash += 31 * hash + value.hashCode();
    return hash;
  }
}
