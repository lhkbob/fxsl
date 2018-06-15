package com.lhkbob.fxsl.parser.semantics;

import com.lhkbob.fxsl.lang.Declaration;
import com.lhkbob.fxsl.lang.Environment;
import com.lhkbob.fxsl.lang.EnvironmentUtils;
import com.lhkbob.fxsl.lang.expr.Expression;
import com.lhkbob.fxsl.lang.type.DefaultTypeVisitor;
import com.lhkbob.fxsl.lang.type.ParametricType;
import com.lhkbob.fxsl.lang.type.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class ReservedNameChecker implements SemanticsChecker {
  @Override
  public boolean continueOnFailure() {
    // Although we don't want people to declare variables with reserved names, because unintended
    // consequences can occur, it's pretty rare so continue forwards and accumulate more errors.
    return true;
  }

  @Override
  public void validate(Environment environment) throws SemanticsException {
    ParametricNameVisitor visitor = new ParametricNameVisitor();
    List<SemanticsProblem> problems = new ArrayList<>();
    for (Declaration<Expression> var : EnvironmentUtils.getAllVariables(environment)) {
      if (var.getName().startsWith(Environment.RESERVED_NAME_PREFIX)) {
        problems.add(
            new SemanticsProblem.ExpressionProblem(
                "Variable name uses reserved prefix: " + var.getName(), var.getValue()));
      }
      problems.addAll(environment.getExpressionType(var.getValue()).accept(visitor));
    }
    for (Declaration<Type> type : EnvironmentUtils.getAllTypes(environment)) {
      if (type.getName().startsWith(Environment.RESERVED_NAME_PREFIX)) {
        problems.add(
            new SemanticsProblem.TypeProblem(
                "Type name uses reserved prefix: " + type.getName(), type.getValue()));
      }
      problems.addAll(type.getValue().accept(visitor));
    }

    if (!problems.isEmpty()) {
      throw new SemanticsException("Reserved names check failed", problems);
    }
  }

  private static class ParametricNameVisitor extends DefaultTypeVisitor.ListTypeVisitor<SemanticsProblem.TypeProblem> {
    @Override
    public List<SemanticsProblem.TypeProblem> visitParametricType(ParametricType type) {
      // User created parameter labels in the source should start with a single _, which
      // is removed during processing and is not part of the label. If there is an _
      // remaining then the effective label of the parametric type starts with the __
      // in RESERVED_NAME_PREFIX.
      if (type.getLabel().startsWith("_")) {
        return Collections.singletonList(new SemanticsProblem.TypeProblem("Parametric type label starts with reserved prefix", type));
      } else {
        return Collections.emptyList();
      }
    }
  }
}
