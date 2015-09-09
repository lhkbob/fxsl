package com.lhkbob.fxsl.lang.expr;

import com.lhkbob.fxsl.lang.Scope;
import com.lhkbob.fxsl.util.EfficientEqualityBase;

import static com.lhkbob.fxsl.util.Preconditions.notNull;

/**
 *
 */
public class IfThenElse extends EfficientEqualityBase implements Expression {
  private final Scope scope;
  private final Expression condition;
  private final Expression trueExpr;
  private final Expression falseExpr;

  public IfThenElse(Scope scope, Expression condition, Expression trueExpr, Expression falseExpr) {
    notNull("scope", scope);
    notNull("condition", condition);
    notNull("trueExpr", trueExpr);
    notNull("falseExpr", falseExpr);

    this.scope = scope;
    this.condition = condition;
    this.trueExpr = trueExpr;
    this.falseExpr = falseExpr;
  }

  public Expression getCondition() {
    return condition;
  }

  public Expression getTrueExpression() {
    return trueExpr;
  }

  public Expression getFalseExpression() {
    return falseExpr;
  }

  @Override
  public Scope getScope() {
    return scope;
  }

  @Override
  public <T> T accept(Visitor<T> visitor) {
    return visitor.visitIfThenElse(this);
  }

  @Override
  protected int computeHashCode() {
    int hash = 17;
    hash += 31 * hash + scope.hashCode();
    hash += 31 * hash + condition.hashCode();
    hash += 31 * hash + trueExpr.hashCode();
    hash += 31 * hash + falseExpr.hashCode();
    return hash;
  }

  @Override
  public boolean equals(Object o) {
    IfThenElse t = compareHashCodes(IfThenElse.class, o);
    return t != null && t.scope.equals(scope) && t.condition.equals(condition) && t.trueExpr.equals(trueExpr) && t.falseExpr.equals(falseExpr);
  }

  @Override
  public String toString() {
    return String.format("if %s then %s else %s", condition, trueExpr, falseExpr);
  }
}
