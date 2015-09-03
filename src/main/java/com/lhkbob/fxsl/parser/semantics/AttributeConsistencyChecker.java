package com.lhkbob.fxsl.parser.semantics;

import com.lhkbob.fxsl.lang.Environment;
import com.lhkbob.fxsl.lang.expr.Attribute;
import com.lhkbob.fxsl.lang.expr.DefaultExpressionVisitor;
import com.lhkbob.fxsl.lang.type.Type;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class AttributeConsistencyChecker implements SemanticsChecker {
  @Override
  public void validate(Environment environment) throws SemanticsException {

  }

  @Override
  public boolean continueOnFailure() {
    // Although all attributes with the same name need to have the same type, the code may otherwise
    // be valid and can be processed.
    return true;
  }

  private static class AttributeVisitor extends DefaultExpressionVisitor<List<SemanticsProblem>> {
    private final Map<String, Type> attributeTypes;
    private final Environment environment;

    public AttributeVisitor(Environment env) {
      attributeTypes = new HashMap<>();
      environment = env;
    }

    @Override
    public List<SemanticsProblem> visitAttribute(Attribute attr) {
      Type actualType = environment.getExpressionType(attr);
      Type existingType = attributeTypes.get(attr.getName());
      if (existingType != null) {
        // FIXME Is strict equality necessary, or can we allow different types as long as they unify?
        if (!existingType.equals(actualType)) {
          return Collections.<SemanticsProblem>singletonList(
              new SemanticsProblem.ExpressionProblem(
                  String.format(
                      "Attribute has conflicting types (%s vs. %s)", actualType, existingType),
                  attr));
        }
      } else {
        attributeTypes.put(attr.getName(), actualType);
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
