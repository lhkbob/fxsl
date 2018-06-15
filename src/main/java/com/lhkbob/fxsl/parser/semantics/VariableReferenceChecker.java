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
    List<SemanticsProblem.ExpressionProblem> problems = new ArrayList<>();
    for (Declaration<Expression> expr : EnvironmentUtils.getAllVariables(environment)) {
        problems.addAll(expr.getValue().accept(visitor));
    }

    if (!problems.isEmpty()) {
      throw new SemanticsException("Undefined variables", problems);
    }
  }

  private static class VariableVisitor extends DefaultExpressionVisitor.ListExpressionVisitor<SemanticsProblem.ExpressionProblem> {
    private final Environment env;

    public VariableVisitor(Environment env) {
      this.env = env;
    }

    @Override
    public List<SemanticsProblem.ExpressionProblem> visitVariable(VariableReference var) {
      if (env.getDeclaredVariable(var.getScope(), var.getVariableName()) == null) {
        return Collections.singletonList(
            new SemanticsProblem.ExpressionProblem("Reference is undefined", var));
      } else {
        return null;
      }
    }
  }
}
