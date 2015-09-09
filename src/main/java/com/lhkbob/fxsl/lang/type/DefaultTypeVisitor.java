package com.lhkbob.fxsl.lang.type;

/**
 *
 */
public class DefaultTypeVisitor<T> implements Type.Visitor<T> {
  @Override
  public T visitAliasType(AliasType t) {
    // Leaf type so return null until overridden
    return typeValue(t);
  }

  @Override
  public T visitArrayType(ArrayType t) {
    T result = typeValue(t);
    if (shortCircuit(result)) {
      return result;
    }
    return combine(result, t.getComponentType().accept(this));
  }

  @Override
  public T visitFunctionType(FunctionType t) {
    T result = typeValue(t);
    if (shortCircuit(result)) {
      return result;
    }
    for (Type param : t.getParameterTypes()) {
      result = combine(result, param.accept(this));
      if (shortCircuit(result)) {
        return result;
      }
    }
    return combine(result, t.getReturnType().accept(this));
  }

  @Override
  public T visitMetaType(MetaType t) {
    // Leaf type so return null until overridden
    return typeValue(t);
  }

  @Override
  public T visitParametricType(ParametricType t) {
    // Leaf type so return null until overridden
    return typeValue(t);
  }

  @Override
  public T visitPrimitiveType(PrimitiveType t) {
    // Leaf type so return null until overridden
    return typeValue(t);

  }

  @Override
  public T visitStructType(StructType t) {
    T result = typeValue(t);
    if (shortCircuit(result)) {
      return result;
    }
    for (Type field : t.getFieldTypes().values()) {
      result = combine(result, field.accept(this));
      if (shortCircuit(result)) {
        return result;
      }
    }
    return result;
  }

  @Override
  public T visitUnionType(UnionType t) {
    T result = typeValue(t);
    if (shortCircuit(result)) {
      return result;
    }
    for (Type option : t.getOptions()) {
      result = combine(result, option.accept(this));
      if (shortCircuit(result)) {
        return result;
      }
    }
    return result;
  }

  protected T combine(T previous, T newest) {
    if (newest != null) {
      return newest;
    } else {
      return previous;
    }
  }

  protected boolean shortCircuit(T value) {
    return false;
  }

  protected T typeValue(Type t) {
    return null;
  }
}
