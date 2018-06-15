package com.lhkbob.fxsl.lang.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class TypeRewritingVisitor implements Type.Visitor<Type> {
  @Override
  public Type visitAliasType(AliasType t) {
    return t;
  }

  @Override
  public Type visitArrayType(ArrayType t) {
    Type componentType = t.getComponentType().accept(this);
    if (!t.getComponentType().equals(componentType)) {
      return new ArrayType(componentType, t.getLength());
    } else {
      return t;
    }
  }

  @Override
  public Type visitFunctionType(FunctionType t) {
    List<Type> paramTypes = new ArrayList<>();
    Type returnType = t.getReturnType().accept(this);
    boolean changed = !t.getReturnType().equals(returnType);
    for (Type p : t.getParameterTypes()) {
      paramTypes.add(p.accept(this));
      changed |= !p.equals(paramTypes.get(paramTypes.size() - 1));
    }

    if (changed) {
      return new FunctionType(paramTypes, returnType);
    } else {
      return t;
    }
  }

  @Override
  public Type visitMetaType(MetaType t) {
    return t;
  }

  @Override
  public Type visitParametricType(ParametricType t) {
    return t;
  }

  @Override
  public Type visitPrimitiveType(PrimitiveType t) {
    return t;
  }

  @Override
  public Type visitStructType(StructType t) {
    Map<String, Type> fieldTypes = new HashMap<>();
    boolean changed = false;
    for (Map.Entry<String, Type> f : t.getFieldTypes().entrySet()) {
      Type fieldType = f.getValue().accept(this);
      changed |= !f.getValue().equals(fieldType);
      fieldTypes.put(f.getKey(), fieldType);
    }

    if (changed) {
      return new StructType(fieldTypes);
    } else {
      return t;
    }
  }

  @Override
  public Type visitUnionType(UnionType t) {
    List<Type> unionTypes = new ArrayList<>();
    boolean changed = false;
    for (Type u : t.getOptions()) {
      Type optionType = u.accept(this);
      changed |= !u.equals(optionType);
      unionTypes.add(optionType);
    }

    if (changed) {
      return new UnionType(unionTypes);
    } else {
      return t;
    }
  }
}
