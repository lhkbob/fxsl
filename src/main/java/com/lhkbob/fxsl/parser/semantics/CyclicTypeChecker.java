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
    List<SemanticsProblem> allProblems = new ArrayList<>();
    for (Declaration<Type> type : EnvironmentUtils.getAllTypes(environment)) {
      allProblems.addAll(type.getValue().accept(visitor));
    }

    if (!allProblems.isEmpty()) {
      throw new SemanticsException("Declared types cannot be cyclic", allProblems);
    }
  }

  private static class CyclicVisitor extends DefaultTypeVisitor<List<SemanticsProblem>> {
    private final Stack<AliasType> aliasStack;
    private final Environment environment;

    public CyclicVisitor(Environment env) {
      environment = env;
      aliasStack = new Stack<>();
    }

    @Override
    public List<SemanticsProblem> visitAliasType(AliasType alias) {
      if (aliasStack.contains(alias)) {
        // A cycle exists in a type's definition
        return Collections.<SemanticsProblem>singletonList(
            new SemanticsProblem.TypeProblem("Type is cyclic", alias));
      }

      // Mark that this alias as being processed
      aliasStack.push(alias);

      Declaration<Type> link = environment.getDeclaredType(alias.getScope(), alias.getLabel());
      if (link == null) {
        return Collections.<SemanticsProblem>singletonList(
            new SemanticsProblem.TypeProblem("Aliased type is undefined", alias));
      }

      try {
        return link.getValue().accept(this);
      } finally {
        aliasStack.pop();
      }
    }

    @Override
    protected List<SemanticsProblem> combine(
        List<SemanticsProblem> old, List<SemanticsProblem> newer) {
      return SemanticsException.combineProblems(old, newer);
    }
  }
}
