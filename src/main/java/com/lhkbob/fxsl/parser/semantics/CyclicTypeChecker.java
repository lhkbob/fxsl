package com.lhkbob.fxsl.parser.semantics;

import com.lhkbob.fxsl.lang.Declaration;
import com.lhkbob.fxsl.lang.Environment;
import com.lhkbob.fxsl.lang.EnvironmentUtils;
import com.lhkbob.fxsl.lang.type.AliasType;
import com.lhkbob.fxsl.lang.type.DefaultTypeVisitor;
import com.lhkbob.fxsl.lang.type.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

/**
 *
 */
public class CyclicTypeChecker implements SemanticsChecker {
  @Override
  public boolean continueOnFailure() {
    return false;
  }

  @Override
  public void validate(Environment environment) throws SemanticsException {
    CyclicVisitor visitor = new CyclicVisitor(environment);
    List<SemanticsProblem.TypeProblem> allProblems = new ArrayList<>();
    for (Declaration<Type> type : EnvironmentUtils.getAllTypes(environment)) {
      visitor.typeStack.push(type);
      try {
        allProblems.addAll(type.getValue().accept(visitor));
      } finally {
        visitor.typeStack.pop();
      }
    }

    if (!allProblems.isEmpty()) {
      throw new SemanticsException("Declared types cannot be cyclic", allProblems);
    }
  }

  private static class CyclicVisitor extends DefaultTypeVisitor.ListTypeVisitor<SemanticsProblem.TypeProblem> {
    private final Stack<Declaration<Type>> typeStack;
    private final Environment environment;

    public CyclicVisitor(Environment env) {
      environment = env;
      typeStack = new Stack<>();
    }

    @Override
    public List<SemanticsProblem.TypeProblem> visitAliasType(AliasType alias) {
      Declaration<Type> link = environment.getDeclaredType(alias.getScope(), alias.getLabel());
      if (link == null) {
        return Collections.singletonList(
            new SemanticsProblem.TypeProblem("Aliased type is undefined", alias));
      }

      if (typeStack.contains(link)) {
        // A cycle exists in a type's definition
        return Collections.singletonList(
            new SemanticsProblem.TypeProblem("Type is cyclic", alias));
      }

      // Mark that this alias as being processed
      typeStack.push(link);
      try {
        return link.getValue().accept(this);
      } finally {
        typeStack.pop();
      }
    }
  }
}
