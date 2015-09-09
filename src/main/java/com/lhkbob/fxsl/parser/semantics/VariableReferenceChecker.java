package com.lhkbob.fxsl.parser.semantics;

import com.lhkbob.fxsl.lang.Declaration;
import com.lhkbob.fxsl.lang.Environment;
import com.lhkbob.fxsl.lang.EnvironmentUtils;
import com.lhkbob.fxsl.lang.expr.DefaultExpressionVisitor;
import com.lhkbob.fxsl.lang.expr.Expression;
import com.lhkbob.fxsl.lang.expr.VariableReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class VariableReferenceChecker implements SemanticsChecker {
  @Override
  public boolean continueOnFailure() {
    return false;
  }

  @Override
  public void validate(Environment environment) throws SemanticsException {
    VariableVisitor visitor = new VariableVisitor(environment);
    List<SemanticsProblem> problems = new ArrayList<>();
    for (Declaration<Expression> expr : EnvironmentUtils.getAllVariables(environment)) {
      List<SemanticsProblem> forExpr = expr.getValue().accept(visitor);
      if (forExpr != null) {
        problems.addAll(forExpr);
      }
    }

    if (!problems.isEmpty()) {
      throw new SemanticsException("Undefined variables", problems);
    }
  }

  private static class VariableVisitor extends DefaultExpressionVisitor<List<SemanticsProblem>> {
    private final Environment env;

    public VariableVisitor(Environment env) {
      this.env = env;
    }

    @Override
    public List<SemanticsProblem> visitVariable(VariableReference var) {
      if (env.getDeclaredVariable(var.getScope(), var.getVariableName()) == null) {
        return Collections.<SemanticsProblem>singletonList(
            new SemanticsProblem.ExpressionProblem("Reference is undefined", var));
      } else {
        return null;
      }
    }

    @Override
    protected List<SemanticsProblem> combine(
        List<SemanticsProblem> old, List<SemanticsProblem> next) {
      return SemanticsException.combineProblems(old, next);
    }
  }
}
