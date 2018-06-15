package com.lhkbob.fxsl.lang.expr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class DefaultExpressionVisitor<T> implements Expression.Visitor<T> {
  public static class ListExpressionVisitor<E> extends DefaultExpressionVisitor<List<E>> {
    @Override
    protected List<E> initialValue(Expression e) {
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
  public T visitArray(ArrayValue value) {
    T result = initialValue(value);
    for (int i = 0; i < value.getLength(); i++) {
      result = shortCircuitedCombine(result, value.getElement(i));
    }
    return result;
  }

  @Override
  public T visitArrayAccess(ArrayAccess access) {
    T result = initialValue(access);
    result = shortCircuitedCombine(result, access.getIndex());
    return shortCircuitedCombine(result, access.getArray());
  }

  @Override
  public T visitArrayLength(ArrayLength length) {
    T result = initialValue(length);
    return shortCircuitedCombine(result, length.getPathToArrayType().getRoot());
  }

  @Override
  public T visitAttribute(Attribute attr) {
    // Leaf node, return null until overridden
    return initialValue(attr);
  }

  @Override
  public T visitDynamicArray(DynamicArrayValue value) {
    T result = initialValue(value);
    result = shortCircuitedCombine(result, value.getLength());
    return shortCircuitedCombine(result, value.getElementFunction());
  }

  @Override
  public T visitFieldAccess(StructFieldAccess access) {
    T result = initialValue(access);
    return shortCircuitedCombine(result, access.getStruct());
  }

  @Override
  public T visitFunction(FunctionValue function) {
    T result = initialValue(function);
    return shortCircuitedCombine(result, function.getReturnValue());
  }

  @Override
  public T visitFunctionCall(FunctionCall function) {
    T result = initialValue(function);
    for (int i = 0; i < function.getSuppliedParameterCount(); i++) {
      result = shortCircuitedCombine(result, function.getParameterValue(i));
    }
    return shortCircuitedCombine(result, function.getFunction());
  }

  @Override
  public T visitIfThenElse(IfThenElse test) {
    T result = initialValue(test);
    result = shortCircuitedCombine(result, test.getCondition());
    result = shortCircuitedCombine(result, test.getTrueExpression());
    return shortCircuitedCombine(result, test.getFalseExpression());
  }

  @Override
  public T visitNativeExpression(NativeExpression expr) {
    // Leaf node, return null until overridden
    return initialValue(expr);
  }

  @Override
  public T visitParameter(Parameter param) {
    // Leaf node, return null until overridden
    return initialValue(param);
  }

  @Override
  public T visitPrimitive(PrimitiveValue primitive) {
    // Leaf node, return null until overridden
    return initialValue(primitive);
  }

  @Override
  public T visitStruct(StructValue struct) {
    T result = initialValue(struct);
    for (Expression field : struct.getFields().values()) {
      result = shortCircuitedCombine(result, field);
    }
    return result;
  }

  @Override
  public T visitUniform(Uniform uniform) {
    // Leaf node, return null until overridden
    return null;
  }

  @Override
  public T visitUnion(UnionValue union) {
    T result = initialValue(union);
    for (Expression option : union.getOptions()) {
      result = shortCircuitedCombine(result, option);
    }
    return result;
  }

  @Override
  public T visitVariable(VariableReference var) {
    // Leaf node, return null until overridden
    return initialValue(var);
  }

  private T shortCircuitedCombine(T previous, Expression next) {
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

  protected T initialValue(Expression e) {
    return null;
  }

  protected boolean shortCircuit(T value) {
    return false;
  }
}
