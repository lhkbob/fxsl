package com.lhkbob.fxsl.lang.expr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class ExpressionRewritingVisitor implements Expression.Visitor<Expression> {
  @Override
  public Expression visitArray(ArrayValue value) {
    List<Expression> elements = new ArrayList<>();
    boolean changed = false;
    for (Expression v : value.getElements()) {
      Expression newV = v.accept(this);
      changed |= !v.equals(newV);
      elements.add(newV);
    }

    if (changed) {
      return new ArrayValue(value.getScope(), elements);
    } else {
      return value;
    }
  }

  @Override
  public Expression visitArrayAccess(ArrayAccess access) {
    Expression array = access.getArray().accept(this);
    Expression index = access.getIndex().accept(this);
    if (!access.getArray().equals(array) || !access.getIndex().equals(index)) {
      return new ArrayAccess(access.getScope(), array, index);
    } else {
      return access;
    }
  }

  @Override
  public Expression visitArrayLength(ArrayLength length) {
    // Although array lengths are composed with a VariableReference in their type path, this is not
    // the same as other composed types and the length expression is basically a primitive.
    return length;
  }

  @Override
  public Expression visitAttribute(Attribute attr) {
    return attr;
  }

  @Override
  public Expression visitDynamicArray(DynamicArrayValue value) {
    Expression length = value.getLength().accept(this);
    Expression element = value.getElementFunction().accept(this);
    if (!value.getLength().equals(length) || !value.getElementFunction().equals(element)) {
      return new DynamicArrayValue(value.getScope(), length, element);
    } else {
      return value;
    }
  }

  @Override
  public Expression visitFieldAccess(StructFieldAccess access) {
    Expression newStruct = access.getStruct().accept(this);
    if (!access.getStruct().equals(newStruct)) {
      return new StructFieldAccess(access.getScope(), newStruct, access.getField());
    } else {
      return access;
    }
  }

  @Override
  public Expression visitFunction(FunctionValue function) {
    Expression returnValue = function.getReturnValue().accept(this);
    if (!function.getReturnValue().equals(returnValue)) {
      // FIXME how does this affect parameter expressions that exist within the function scope
      // but must now be updated to reflect the new function?
      // Likely we need to separate and have an object that represents a function declaration that
      // exists without a return value that can be shared by rewritten functions.
      // This is inline with what has to be done for ArrayLength
      return new FunctionValue(function.getScope(), function.getParameters(), returnValue);
    } else {
      return function;
    }
  }

  @Override
  public Expression visitFunctionCall(FunctionCall function) {
    Expression functionValue = function.getFunction().accept(this);
    List<Expression> params = new ArrayList<>();
    boolean changed = !function.getFunction().equals(functionValue);
    for (Expression v : function.getParameterValues()) {
      Expression newV = v.accept(this);
      changed |= !v.equals(newV);
      params.add(newV);
    }

    if (changed) {
      return new FunctionCall(function.getScope(), functionValue, params);
    } else {
      return function;
    }
  }

  @Override
  public Expression visitIfThenElse(IfThenElse test) {
    Expression condition = test.getCondition().accept(this);
    Expression trueExpr = test.getTrueExpression().accept(this);
    Expression falseExpr = test.getFalseExpression().accept(this);
    if (!test.getCondition().equals(condition) || !test.getTrueExpression().equals(trueExpr)
        || !test.getFalseExpression().equals(falseExpr)) {
      return new IfThenElse(test.getScope(), condition, trueExpr, falseExpr);
    } else {
      return test;
    }
  }

  @Override
  public Expression visitNativeExpression(NativeExpression expr) {
    return expr;
  }

  @Override
  public Expression visitParameter(Parameter param) {
    return null;
  }

  @Override
  public Expression visitPrimitive(PrimitiveValue primitive) {
    return primitive;
  }

  @Override
  public Expression visitStruct(StructValue struct) {
    Map<String, Expression> structValues = new HashMap<>();
    boolean changed = false;
    for (Map.Entry<String, Expression> f : struct.getFields().entrySet()) {
      Expression newField = f.getValue().accept(this);
      changed |= !f.getValue().equals(newField);
      structValues.put(f.getKey(), newField);
    }

    if (changed) {
      return new StructValue(struct.getScope(), structValues);
    } else {
      return struct;
    }
  }

  @Override
  public Expression visitUniform(Uniform uniform) {
    return uniform;
  }

  @Override
  public Expression visitUnion(UnionValue union) {
    List<Expression> options = new ArrayList<>();
    boolean changed = false;
    for (Expression u : union.getOptions()) {
      Expression newU = u.accept(this);
      changed |= !u.equals(newU);
      options.add(newU);
    }

    if (changed) {
      return new ArrayValue(union.getScope(), options);
    } else {
      return union;
    }
  }

  @Override
  public Expression visitVariable(VariableReference var) {
    return var;
  }
}
