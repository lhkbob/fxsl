package com.lhkbob.fxsl.lang.expr;

/**
 *
 */
public class DefaultExpressionVisitor<T> implements Expression.Visitor<T> {
  @Override
  public T visitArrayAccess(ArrayAccess access) {
    T childResult = access.getIndex().accept(this);
    return combine(childResult, access.getArray().accept(this));
  }

  @Override
  public T visitArray(ArrayValue value) {
    T childResult = null;
    for (int i = 0; i < value.getLength(); i++) {
      childResult = combine(childResult, value.getElement(i).accept(this));
    }
    return childResult;
  }

  @Override
  public T visitFunctionCall(FunctionCall function) {
    T childResult = null;
    for (int i = 0; i < function.getSuppliedParameterCount(); i++) {
      childResult = combine(childResult, function.getParameterValue(i).accept(this));
    }
    return combine(childResult, function.getFunction().accept(this));
  }

  @Override
  public T visitFunction(FunctionValue function) {
    return function.getReturnValue().accept(this);
  }

  @Override
  public T visitParameter(Parameter param) {
    // Leaf node, return null until overridden
    return null;
  }

  @Override
  public T visitPrimitive(PrimitiveValue primitive) {
    // Leaf node, return null until overridden
    return null;
  }

  @Override
  public T visitArrayLength(ArrayLength length) {
    return length.getPathToArrayType().getRoot().accept(this);
  }

  @Override
  public T visitFieldAccess(StructFieldAccess access) {
    return access.getStruct().accept(this);
  }

  @Override
  public T visitStruct(StructValue struct) {
    T childResult = null;
    for (Expression field : struct.getFields().values()) {
      childResult = combine(childResult, field.accept(this));
    }
    return childResult;
  }

  @Override
  public T visitUnion(UnionValue union) {
    T childResult = null;
    for (Expression option : union.getOptions()) {
      childResult = combine(childResult, option.accept(this));
    }
    return childResult;
  }

  @Override
  public T visitVariable(VariableReference var) {
    // Leaf node, return null until overridden
    return null;
  }

  @Override
  public T visitNativeExpression(NativeExpression expr) {
    // Leaf node, return null until overridden
    return null;
  }

  @Override
  public T visitDynamicArray(DynamicArrayValue value) {
    T childResult = value.getLength().accept(this);
    return combine(childResult, value.getElementFunction().accept(this));
  }

  @Override
  public T visitUniform(Uniform uniform) {
    // Leaf node, return null until overridden
    return null;
  }

  @Override
  public T visitAttribute(Attribute attr) {
    // Leaf node, return null until overridden
    return null;
  }

  @Override
  public T visitIfThenElse(IfThenElse test) {
    T childResult = test.getCondition().accept(this);
    childResult = combine(childResult, test.getTrueExpression().accept(this));
    childResult = combine(childResult, test.getFalseExpression().accept(this));
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
