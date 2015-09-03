package com.lhkbob.fxsl.lang.type;

/**
 *
 */
public class DefaultTypeVisitor<T> implements Type.Visitor<T> {
  @Override
  public T visitArrayType(ArrayType t) {
    return t.getComponentType().accept(this);
  }

  @Override
  public T visitFunctionType(FunctionType t) {
    T childResult = null;
    for (Type param : t.getParameterTypes()) {
      childResult = combine(childResult, param.accept(this));
    }
    return combine(childResult, t.getReturnType().accept(this));
  }

  @Override
  public T visitMetaType(MetaType t) {
    // Leaf type so return null until overridden
    return null;
  }

  @Override
  public T visitParametricType(ParametricType t) {
    // Leaf type so return null until overridden
    return null;
  }

  @Override
  public T visitAliasType(AliasType t) {
    // Leaf type so return null until overridden
    return null;
  }

  @Override
  public T visitPrimitiveType(PrimitiveType t) {
    // Leaf type so return null until overridden
    return null;
  }

  @Override
  public T visitStructType(StructType t) {
    T childResult = null;
    for (Type field : t.getFieldTypes().values()) {
      childResult = combine(childResult, field.accept(this));
    }
    return childResult;
  }

  @Override
  public T visitUnionType(UnionType t) {
    T childResult = null;
    for (Type option : t.getOptions()) {
      childResult = combine(childResult, option.accept(this));
    }
    return childResult;
  }

  protected T combine(T previous, T newest) {
    if (newest != null) {
      return newest;
    } else {
      return previous;
    }
  }
}
