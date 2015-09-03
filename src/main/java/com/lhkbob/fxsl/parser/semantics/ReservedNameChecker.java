package com.lhkbob.fxsl.parser.semantics;

import com.lhkbob.fxsl.lang.Declaration;
import com.lhkbob.fxsl.lang.Environment;
import com.lhkbob.fxsl.lang.EnvironmentUtils;
import com.lhkbob.fxsl.lang.expr.Expression;
import com.lhkbob.fxsl.lang.type.Type;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ReservedNameChecker implements SemanticsChecker {
  public static final String RESERVED_PREFIX = "__";

  @Override
  public void validate(Environment environment) throws SemanticsException {
    List<SemanticsProblem> problems = new ArrayList<>();
    for (Declaration<Expression> var : EnvironmentUtils.getAllVariables(environment)) {
      if (var.getName().startsWith(RESERVED_PREFIX)) {
        problems.add(
            new SemanticsProblem.ExpressionProblem(
                "Variable name uses reserved prefix: " + var.getName(), var.getValue()));
      }
    }
    for (Declaration<Type> type : EnvironmentUtils.getAllTypes(environment)) {
      if (type.getName().startsWith(RESERVED_PREFIX)) {
        problems.add(
            new SemanticsProblem.TypeProblem(
                "Type name uses reserved prefix: " + type.getName(), type.getValue()));
      }
    }

    if (!problems.isEmpty()) {
      throw new SemanticsException("Reserved names check failed", problems);
    }
  }

  @Override
  public boolean continueOnFailure() {
    // Although we don't want people to declare variables with reserved names, because unintended
    // consequences can occur, it's pretty rare so continue forwards and accumulate more errors.
    return true;
  }
}
