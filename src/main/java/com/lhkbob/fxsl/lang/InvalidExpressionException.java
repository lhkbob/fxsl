package com.lhkbob.fxsl.lang;

import com.lhkbob.fxsl.lang.expr.Expression;

/**
 *
 */
public class InvalidExpressionException extends RuntimeException {
  private final Expression e;

  public InvalidExpressionException() {
    this("");
  }

  public InvalidExpressionException(String msg) {
    this(msg, null);
  }

  public InvalidExpressionException(String msg, Throwable t) {
    this(null, msg, t);
  }

  public InvalidExpressionException(Expression e, String msg, Throwable t) {
    super(getMessage(e, msg), t);
    this.e = e;
  }

  public InvalidExpressionException(Throwable t) {
    this("", t);
  }

  public InvalidExpressionException(Expression e) {
    this(e, "", null);
  }

  public InvalidExpressionException(Expression e, String msg) {
    this(e, msg, null);
  }

  public InvalidExpressionException(Expression e, Throwable t) {
    this(e, "", t);
  }

  public Expression getExpression() {
    return e;
  }

  private static String getMessage(Expression e, String msg) {
    StringBuilder sb = new StringBuilder();
    if (msg != null && msg.length() > 0) {
      sb.append(msg);
    }
    if (e != null) {
      if (sb.length() > 0) {
        sb.append(", ");
      }
      sb.append("expr: ").append(e);
    }
    return sb.toString();
  }
}
