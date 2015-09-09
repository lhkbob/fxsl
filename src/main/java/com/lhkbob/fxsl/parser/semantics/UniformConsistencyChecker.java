package com.lhkbob.fxsl.parser.semantics;

import com.lhkbob.fxsl.lang.Declaration;
import com.lhkbob.fxsl.lang.Environment;
import com.lhkbob.fxsl.lang.EnvironmentUtils;
import com.lhkbob.fxsl.lang.expr.DefaultExpressionVisitor;
import com.lhkbob.fxsl.lang.expr.Expression;
import com.lhkbob.fxsl.lang.expr.Uniform;
import com.lhkbob.fxsl.lang.type.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class UniformConsistencyChecker implements SemanticsChecker {
  @Override
  public boolean continueOnFailure() {
    // Although all uniforms with the same name need to have the same type, the code may otherwise
    // be valid and can be processed for further semantic errors.
    return true;
  }

  @Override
  public void validate(Environment environment) throws SemanticsException {
    List<SemanticsProblem> problems = new ArrayList<>();
    UniformVisitor visitor = new UniformVisitor(environment);
    for (Declaration<Expression> var : EnvironmentUtils.getAllVariables(environment)) {
      problems.addAll(var.getValue().accept(visitor));
    }

    if (!problems.isEmpty()) {
      throw new SemanticsException(
          "References to named uniforms have inconsistent types", problems);
    }
  }

  private static class UniformVisitor extends DefaultExpressionVisitor<List<SemanticsProblem>> {
    private final Environment environment;
    private final Map<String, Type> uniformTypes;

    public UniformVisitor(Environment env) {
      uniformTypes = new HashMap<>();
      environment = env;
    }

    @Override
    public List<SemanticsProblem> visitUniform(Uniform uniform) {
      Type actualType = environment.getExpressionType(uniform);
      Type existingType = uniformTypes.get(uniform.getName());
      if (existingType != null) {
        // FIXME Is strict equality necessary, or can we allow different types as long as they unify?
        if (!existingType.equals(actualType)) {
          return Collections.<SemanticsProblem>singletonList(
              new SemanticsProblem.ExpressionProblem(
                  String.format(
                      "Uniform has conflicting types (%s vs. %s)", actualType, existingType),
                  uniform));
        }
      } else {
        uniformTypes.put(uniform.getName(), actualType);
      }
      return Collections.emptyList();
    }

    @Override
    protected List<SemanticsProblem> combine(
        List<SemanticsProblem> old, List<SemanticsProblem> next) {
      return SemanticsException.combineProblems(old, next);
    }
  }
}
