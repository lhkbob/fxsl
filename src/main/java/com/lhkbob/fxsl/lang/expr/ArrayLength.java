package com.lhkbob.fxsl.lang.expr;

import com.lhkbob.fxsl.lang.Environment;
import com.lhkbob.fxsl.lang.Scope;
import com.lhkbob.fxsl.lang.type.TypePath;
import com.lhkbob.fxsl.util.EfficientEqualityBase;
import com.lhkbob.fxsl.util.Immutable;

import static com.lhkbob.fxsl.util.Preconditions.notNull;

/**

 */
@Immutable
public final class ArrayLength extends EfficientEqualityBase implements Expression {
  private final TypePath pathToArrayType;
  private final Scope scope;

  public ArrayLength(Scope scope, TypePath toArrayType) {
    notNull("scope", scope);
    notNull("toArrayType", toArrayType);
    this.scope = scope;
    pathToArrayType = toArrayType;
  }

  @Override
  public <T> T accept(Visitor<T> visitor) {
    return visitor.visitArrayLength(this);
  }

  @Override
  public boolean equals(Object o) {
    ArrayLength a = compareHashCodes(ArrayLength.class, o);
    return a != null && a.scope.equals(scope) && a.pathToArrayType.equals(pathToArrayType);
  }

  public TypePath getPathToArrayType() {
    return pathToArrayType;
  }

  @Override
  public Scope getScope() {
    return scope;
  }

  @Override
  public String toString() {
    return String.format("%slength(%s)", Environment.RESERVED_NAME_PREFIX, pathToArrayType);
  }

  @Override
  protected int computeHashCode() {
    int hash = 17;
    hash += 37 * hash + ArrayLength.class.hashCode();
    hash += 37 * hash + scope.hashCode();
    hash += 37 * hash + pathToArrayType.hashCode();
    return hash;
  }
}
