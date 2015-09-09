package com.lhkbob.fxsl.lang;

import com.lhkbob.fxsl.lang.expr.Expression;
import com.lhkbob.fxsl.lang.type.Type;

import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public final class EnvironmentUtils {
  private EnvironmentUtils() {}

  public static Set<Declaration<Type>> getAllTypes(Environment environment) {
    return getTypesUnderScope(environment, Scope.NATIVE_SCOPE);
  }

  public static Set<Declaration<Expression>> getAllVariables(Environment environment) {
    return getVariablesUnderScope(environment, Scope.NATIVE_SCOPE);
  }

  public static Set<Declaration<Type>> getTypesInScope(Environment environment, Scope scope) {
    Set<Declaration<Type>> allVars = new HashSet<>();
    while (scope != null) {
      allVars.addAll(environment.getDeclaredTypes(scope));
      scope = scope.getParent();
    }
    return allVars;
  }

  public static Set<Declaration<Type>> getTypesUnderScope(Environment environment, Scope scope) {
    Set<Declaration<Type>> allVars = new HashSet<>();
    allVars.addAll(environment.getDeclaredTypes(scope));
    for (Scope child : environment.getChildScopes(scope)) {
      allVars.addAll(getTypesUnderScope(environment, child));
    }
    return allVars;
  }

  public static Set<Declaration<Expression>> getVariablesInScope(
      Environment environment, Scope scope) {
    Set<Declaration<Expression>> allVars = new HashSet<>();
    while (scope != null) {
      allVars.addAll(environment.getDeclaredVariables(scope));
      scope = scope.getParent();
    }
    return allVars;
  }

  public static Set<Declaration<Expression>> getVariablesUnderScope(
      Environment environment, Scope scope) {
    Set<Declaration<Expression>> allVars = new HashSet<>();
    allVars.addAll(environment.getDeclaredVariables(scope));
    for (Scope child : environment.getChildScopes(scope)) {
      allVars.addAll(getVariablesUnderScope(environment, child));
    }
    return allVars;
  }
}
