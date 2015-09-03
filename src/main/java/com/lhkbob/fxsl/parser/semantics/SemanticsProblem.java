package com.lhkbob.fxsl.parser.semantics;

import com.lhkbob.fxsl.lang.expr.Expression;
import com.lhkbob.fxsl.lang.type.Type;

/**
 *
 */
public interface SemanticsProblem {
  String getMessage();

  Object getFailingElement();

  class TypeProblem implements SemanticsProblem {
    private final String message;
    private final Type failingType;

    public TypeProblem(String message) {
      this(message, null);
    }

    public TypeProblem(String message, Type failingType) {
      if (message == null && failingType == null) {
        throw new IllegalArgumentException("Both message and type cannot be null");
      }
      this.message = message;
      this.failingType = failingType;
    }

    public TypeProblem(Type failingType) {
      this(null, failingType);
    }

    @Override
    public String getMessage() {
      return message;
    }

    @Override
    public String toString() {
      if (failingType != null && message != null) {
        return String.format("Error in type (%s), message: %s", failingType, message);
      } else if (failingType != null) {
        return String.format("Error in type (%s)", failingType);
      } else if (message != null) {
        return String.format("Type error, message: %s", message);
      } else {
        return ""; // Won't happen
      }
    }

    @Override
    public Type getFailingElement() {
      return failingType;
    }


  }

  class ExpressionProblem implements SemanticsProblem {
    private final String message;
    private final Expression failingExpr;

    public ExpressionProblem(String message) {
      this(message, null);
    }

    public ExpressionProblem(String message, Expression failingExpr) {
      if (message == null && failingExpr == null) {
        throw new IllegalArgumentException("Both message and expression cannot be null");
      }
      this.message = message;
      this.failingExpr = failingExpr;
    }

    public ExpressionProblem(Expression failingExpr) {
      this(null, failingExpr);
    }

    @Override
    public String getMessage() {
      return message;
    }

    @Override
    public Expression getFailingElement() {
      return failingExpr;
    }

    @Override
    public String toString() {
      if (failingExpr != null && message != null) {
        return String.format("Error in expression (%s), message: %s", failingExpr, message);
      } else if (failingExpr != null) {
        return String.format("Error in expression (%s)", failingExpr);
      } else if (message != null) {
        return String.format("Type error, message: %s", message);
      } else {
        return ""; // Won't happen
      }
    }
  }
}
