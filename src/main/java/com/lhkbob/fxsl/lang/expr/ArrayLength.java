package com.lhkbob.fxsl.lang.expr;

import com.lhkbob.fxsl.lang.Scope;
import com.lhkbob.fxsl.lang.type.TypePath;
import com.lhkbob.fxsl.util.EfficientEqualityBase;
import com.lhkbob.fxsl.util.Immutable;

import static com.lhkbob.fxsl.util.Preconditions.notNull;

/**

 */
@Immutable
public final class ArrayLength extends EfficientEqualityBase implements Expression {
  private final Scope scope;
  private final TypePath pathToArrayType;

  public ArrayLength(Scope scope, TypePath toArrayType) {
    notNull("scope", scope);
    notNull("toArrayType", toArrayType);
    this.scope = scope;
    pathToArrayType = toArrayType;
  }

  public TypePath getPathToArrayType() {
    return pathToArrayType;
  }

  @Override
  public Scope getScope() {
    return scope;
  }

  @Override
  public <T> T accept(Visitor<T> visitor) {
    return visitor.visitArrayLength(this);
  }

  @Override
  public String toString() {
    return String.format("__length(%s)", pathToArrayType);
  }

  @Override
  protected int computeHashCode() {
    int hash = 17;
    hash += 37 * hash + ArrayLength.class.hashCode();
    hash += 37 * hash + scope.hashCode();
    hash += 37 * hash + pathToArrayType.hashCode();
    return hash;
  }

  @Override
  public boolean equals(Object o) {
    ArrayLength a = compareHashCodes(ArrayLength.class, o);
    return a != null && a.scope.equals(scope) && a.pathToArrayType.equals(pathToArrayType);
  }
}
