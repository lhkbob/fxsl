package com.lhkbob.fxsl.lang.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class DefaultTypeVisitor<T> implements Type.Visitor<T> {
  public static class ListTypeVisitor<E> extends DefaultTypeVisitor<List<E>> {
    @Override
    protected List<E> initialValue(Type t) {
      return Collections.emptyList();
    }

    @Override
    protected List<E> combine(List<E> previous, List<E> next) {
      if (previous != null && !previous.isEmpty()) {
        if (next != null && !next.isEmpty()) {
          // Add all of next into previous, but join them into a new list because
          // we can't necessarily trust where previous was created and if it was mutable
          List<E> joined = new ArrayList<>(previous.size() + next.size());
          joined.addAll(previous);
          joined.addAll(next);
          return joined;
        } else {
          // Previous list is unmodified
          return previous;
        }
      } else {
        // No prior value, so choose next
        return next;
      }
    }
  }

  @Override
  public T visitAliasType(AliasType t) {
    // Leaf type so return null until overridden
    return initialValue(t);
  }

  @Override
  public T visitArrayType(ArrayType t) {
    T result = initialValue(t);
    return shortCircuitedCombine(result, t.getComponentType());
  }

  @Override
  public T visitFunctionType(FunctionType t) {
    T result = initialValue(t);
    for (Type param : t.getParameterTypes()) {
      result = shortCircuitedCombine(result, param);
    }
    return shortCircuitedCombine(result, t.getReturnType());
  }

  @Override
  public T visitMetaType(MetaType t) {
    // Leaf type so return null until overridden
    return initialValue(t);
  }

  @Override
  public T visitParametricType(ParametricType t) {
    // Leaf type so return null until overridden
    return initialValue(t);
  }

  @Override
  public T visitPrimitiveType(PrimitiveType t) {
    // Leaf type so return null until overridden
    return initialValue(t);

  }

  @Override
  public T visitStructType(StructType t) {
    T result = initialValue(t);
    for (Type field : t.getFieldTypes().values()) {
      result = shortCircuitedCombine(result, field);
    }
    return result;
  }

  @Override
  public T visitUnionType(UnionType t) {
    T result = initialValue(t);
    for (Type option : t.getOptions()) {
      result = shortCircuitedCombine(result, option);
    }
    return result;
  }

  private T shortCircuitedCombine(T previous, Type next) {
    if (shortCircuit(previous)) {
      return previous;
    } else {
      return combine(previous, next.accept(this));
    }
  }

  protected T combine(T previous, T next) {
    if (next != null) {
      return next;
    } else {
      return previous;
    }
  }

  protected boolean shortCircuit(T value) {
    return false;
  }

  protected T initialValue(Type t) {
    return null;
  }
}
