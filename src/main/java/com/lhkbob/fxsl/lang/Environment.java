package com.lhkbob.fxsl.lang;

import com.lhkbob.fxsl.lang.expr.Expression;
import com.lhkbob.fxsl.lang.expr.NativeExpression;
import com.lhkbob.fxsl.lang.expr.UnionValue;
import com.lhkbob.fxsl.lang.type.ArrayType;
import com.lhkbob.fxsl.lang.type.FunctionType;
import com.lhkbob.fxsl.lang.type.MetaType;
import com.lhkbob.fxsl.lang.type.ParametricType;
import com.lhkbob.fxsl.lang.type.PrimitiveType;
import com.lhkbob.fxsl.lang.type.Type;
import com.lhkbob.fxsl.lang.type.UnionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 *
 */
public class Environment {
  public static final NativeExpression UNARY_SUB_INT = nativeFunction(
      "int negate", PrimitiveType.INT, PrimitiveType.INT);
  public static final NativeExpression UNARY_SUB_FLOAT = nativeFunction(
      "float negate", PrimitiveType.FLOAT, PrimitiveType.FLOAT);
  public static final NativeExpression UNARY_BANG = nativeFunction(
      "bool negate", PrimitiveType.BOOL, PrimitiveType.BOOL);
  public static final NativeExpression UNARY_TILDE = nativeFunction(
      "bitwise negate", PrimitiveType.INT, PrimitiveType.INT);
  public static final NativeExpression BINARY_ADD_INT = nativeFunction(
      "int add", PrimitiveType.INT, PrimitiveType.INT, PrimitiveType.INT);
  public static final NativeExpression BINARY_SUB_INT = nativeFunction(
      "int subtract", PrimitiveType.INT, PrimitiveType.INT, PrimitiveType.INT);
  public static final NativeExpression BINARY_MUL_INT = nativeFunction(
      "int multiply", PrimitiveType.INT, PrimitiveType.INT, PrimitiveType.INT);
  public static final NativeExpression BINARY_DIV_INT = nativeFunction(
      "int divide", PrimitiveType.INT, PrimitiveType.INT, PrimitiveType.INT);
  public static final NativeExpression BINARY_MOD_INT = nativeFunction(
      "int modulo", PrimitiveType.INT, PrimitiveType.INT, PrimitiveType.INT);
  public static final NativeExpression BINARY_BITAND_INT = nativeFunction(
      "bitwise and", PrimitiveType.INT, PrimitiveType.INT, PrimitiveType.INT);
  public static final NativeExpression BINARY_BITOR_INT = nativeFunction(
      "bitwise or", PrimitiveType.INT, PrimitiveType.INT, PrimitiveType.INT);
  public static final NativeExpression BINARY_BITXOR_INT = nativeFunction(
      "bitwise xor", PrimitiveType.INT, PrimitiveType.INT, PrimitiveType.INT);
  public static final NativeExpression BINARY_ADD_FLOAT = nativeFunction(
      "float add", PrimitiveType.FLOAT, PrimitiveType.FLOAT, PrimitiveType.FLOAT);
  public static final NativeExpression BINARY_SUB_FLOAT = nativeFunction(
      "float subtract", PrimitiveType.FLOAT, PrimitiveType.FLOAT, PrimitiveType.FLOAT);
  public static final NativeExpression BINARY_MUL_FLOAT = nativeFunction(
      "float multiply", PrimitiveType.FLOAT, PrimitiveType.FLOAT, PrimitiveType.FLOAT);
  public static final NativeExpression BINARY_DIV_FLOAT = nativeFunction(
      "float divide", PrimitiveType.FLOAT, PrimitiveType.FLOAT, PrimitiveType.FLOAT);
  public static final NativeExpression BINARY_POW_FLOAT = nativeFunction(
      "float power", PrimitiveType.FLOAT, PrimitiveType.FLOAT, PrimitiveType.FLOAT);
  public static final NativeExpression BINARY_EQ_BOOL = nativeFunction(
      "bool equals", PrimitiveType.BOOL, PrimitiveType.BOOL, PrimitiveType.BOOL);
  public static final NativeExpression BINARY_EQ_INT = nativeFunction(
      "int equals", PrimitiveType.BOOL, PrimitiveType.INT, PrimitiveType.INT);
  public static final NativeExpression BINARY_GT_INT = nativeFunction(
      "int greater-than", PrimitiveType.BOOL, PrimitiveType.INT, PrimitiveType.INT);
  public static final NativeExpression BINARY_LT_INT = nativeFunction(
      "int less-than", PrimitiveType.BOOL, PrimitiveType.INT, PrimitiveType.INT);
  public static final NativeExpression BINARY_GE_INT = nativeFunction(
      "int greater-than-or-equal", PrimitiveType.BOOL, PrimitiveType.INT, PrimitiveType.INT);
  public static final NativeExpression BINARY_LE_INT = nativeFunction(
      "int less-than-or-equal", PrimitiveType.BOOL, PrimitiveType.INT, PrimitiveType.INT);
  public static final NativeExpression BINARY_EQ_FLOAT = nativeFunction(
      "float equals", PrimitiveType.BOOL, PrimitiveType.FLOAT, PrimitiveType.FLOAT);
  public static final NativeExpression BINARY_GT_FLOAT = nativeFunction(
      "float greater-than", PrimitiveType.BOOL, PrimitiveType.FLOAT, PrimitiveType.FLOAT);
  public static final NativeExpression BINARY_LT_FLOAT = nativeFunction(
      "float less-than", PrimitiveType.BOOL, PrimitiveType.FLOAT, PrimitiveType.FLOAT);
  public static final NativeExpression BINARY_GE_FLOAT = nativeFunction(
      "float greater-than-or-equal", PrimitiveType.BOOL, PrimitiveType.FLOAT, PrimitiveType.FLOAT);
  public static final NativeExpression BINARY_LE_FLOAT = nativeFunction(
      "float less-than-or-equal", PrimitiveType.BOOL, PrimitiveType.FLOAT, PrimitiveType.FLOAT);
  public static final NativeExpression BINARY_UNION_FUNCTION = functionUnion();
  // since expressions are immutable we can record their inferred and/or declared types
  private final WeakHashMap<Expression, Type> expressionTypeCache;
  private final Map<Scope, ScopeRules> scopeRules;
  private final WeakHashMap<Object, Object> metaBindings; // Includes both meta types and wildcard array lengths
  private final Scope rootScope;
  private final Environment parent;
  private State state;

  public Environment() {
    this(null);
  }

  private Environment(Environment parent) {
    this.parent = parent;

    expressionTypeCache = new WeakHashMap<>();
    scopeRules = new HashMap<>();
    metaBindings = new WeakHashMap<>();

    if (parent == null) {
      ScopeRules nativeRules = new ScopeRules();
      configureNativeScope(nativeRules);
      scopeRules.put(Scope.NATIVE_SCOPE, nativeRules);
      rootScope = new Scope(Scope.NATIVE_SCOPE);
    } else {
      rootScope = parent.rootScope;
    }

    state = State.READY;
  }

  private void configureNativeScope(ScopeRules nativeScope) {
    // default operators
    addNativeVariable(nativeScope, "!", UNARY_BANG);
    addNativeVariable(nativeScope, "~", UNARY_TILDE);

    addNativeVariable(nativeScope, "*^", BINARY_POW_FLOAT);
    addNativeVariable(nativeScope, "%", BINARY_MOD_INT);
    addNativeVariable(nativeScope, "&", BINARY_BITAND_INT);
    addNativeVariable(nativeScope, "^", BINARY_BITXOR_INT);

    addNativeVariable(nativeScope, "|", BINARY_BITOR_INT, BINARY_UNION_FUNCTION);

    addNativeVariable(nativeScope, "+", BINARY_ADD_INT, BINARY_ADD_FLOAT);
    addNativeVariable(
        nativeScope, "-", UNARY_SUB_INT, UNARY_SUB_FLOAT, BINARY_SUB_INT, BINARY_SUB_FLOAT);
    addNativeVariable(nativeScope, "*", BINARY_MUL_INT, BINARY_MUL_FLOAT);
    addNativeVariable(nativeScope, "/", BINARY_DIV_INT, BINARY_DIV_FLOAT);

    addNativeVariable(nativeScope, "=", BINARY_EQ_BOOL, BINARY_EQ_INT, BINARY_EQ_FLOAT);
    addNativeVariable(nativeScope, "<", BINARY_LT_INT, BINARY_LT_FLOAT);
    addNativeVariable(nativeScope, "<=", BINARY_LE_INT, BINARY_LE_FLOAT);
    addNativeVariable(nativeScope, ">", BINARY_GT_INT, BINARY_GT_FLOAT);
    addNativeVariable(nativeScope, ">=", BINARY_GE_INT, BINARY_GE_FLOAT);

    // primitive types
    for (PrimitiveType type : PrimitiveType.values()) {
      nativeScope.setDefinition(Type.class, type.toString(), type);
    }
  }

  private void addNativeVariable(ScopeRules nativeScope, String name, NativeExpression... exprs) {
    if (exprs.length == 1) {
      nativeScope.setDefinition(Expression.class, name, exprs[0]);
      // Don't use setExpressionType since that fails if the scope is native
      expressionTypeCache.put(exprs[0], exprs[0].getType());
    } else {
      // create a union type, and since they are all native expressions the union type will be
      // known and concrete without using inference
      List<Type> types = new ArrayList<>();
      for (NativeExpression e : exprs) {
        types.add(e.getType());
      }
      Type finalType = new UnionType(types);
      Expression finalExpr = new UnionValue(Scope.NATIVE_SCOPE, Arrays.asList(exprs));
      nativeScope.setDefinition(Expression.class, name, finalExpr);
      // Don't use setExpressionType since that fails if the scope is native
      expressionTypeCache.put(finalExpr, finalType);
    }
  }

  private static NativeExpression nativeFunction(String name, Type returnType, Type... paramTypes) {
    return new NativeExpression(name, new FunctionType(Arrays.asList(paramTypes), returnType));
  }

  private static NativeExpression functionUnion() {
    ParametricType leftType = new ParametricType(Scope.NATIVE_SCOPE, "__union_left");
    ParametricType rightType = new ParametricType(Scope.NATIVE_SCOPE, "__union_right");

    List<? extends Type> argTypes = Arrays.asList(leftType, rightType);
    Type returnType = new UnionType(argTypes);

    return new NativeExpression("union", new FunctionType(argTypes, returnType));
  }

  public Environment push() {
    checkLockStatus();
    state = State.LOCKED;
    return new Environment(this);
  }

  private void checkLockStatus() {
    switch (state) {
    case LOCKED:
      throw new IllegalStateException(
          "Environment is locked, modify the child environment instead");
    case COMMITTED:
      throw new IllegalStateException(
          "Environment is has been committed to parent and cannot be modified any further");
    case ABORTED:
      throw new IllegalStateException(
          "Environment has been aborted and cannot be modified any further");
    }
    // anything else and it's fine
  }

  public Environment pop(boolean commit) {
    return (commit ? commit() : abort());
  }

  public Environment commit() {
    checkCommitStatus();

    // push all changes in this environment back into parent
    // - either the context prevents putAll() from overriding the parent's value, or
    //   overrides are allowed when they occur and putAll() behaves correctly.
    parent.metaBindings.putAll(metaBindings);
    parent.expressionTypeCache.putAll(expressionTypeCache);

    for (Scope scope : scopeRules.keySet()) {
      ScopeRules child = scopeRules.get(scope);
      ScopeRules parent = this.parent.scopeRules.get(scope);

      if (child != null) {
        if (parent == null) {
          parent = new ScopeRules();
          this.parent.scopeRules.put(scope, parent);
        }

        parent.defs.putAll(child.defs);
      }
    }

    state = State.COMMITTED;

    // a parent cannot be aborted or committed without its children having been aborted or
    // committed and there can only be 1 child at a time, so we know the parent's state was locked
    // and should be changed to ready
    parent.state = State.READY;
    return parent;
  }

  public Environment abort() {
    checkAbortStatus();
    // only update state, no need to push in new mappings, etc.
    state = State.ABORTED;

    // see comment in commit()
    parent.state = State.READY;
    return parent;
  }

  private void checkCommitStatus() {
    if (parent == null) {
      throw new IllegalStateException("Cannot commit environment with no parent");
    }

    switch (state) {
    case LOCKED:
      throw new IllegalStateException("Environment is locked, cannot commit environment to parent");
    case COMMITTED:
      throw new IllegalStateException("Environment has already been committed");
    case ABORTED:
      throw new IllegalStateException("Environment has been aborted and cannot be committed");
    }
    // anything else and it's fine
  }

  private void checkAbortStatus() {
    if (parent == null) {
      throw new IllegalStateException("Cannot abort environment with no parent");
    }

    switch (state) {
    case LOCKED:
      throw new IllegalStateException(
          "Environment is locked, cannot abort environment from parent");
    case COMMITTED:
      throw new IllegalStateException("Environment has been committed and cannot be aborted");
    case ABORTED:
      throw new IllegalStateException("Environment has already been aborted");
    }
    // anything else and it's fine
  }

  public Set<Declaration<Expression>> getDeclaredVariables(Scope scope) {
    Set<Declaration<Expression>> defs = new HashSet<>();
    getDefinitions(Expression.class, scope, defs);
    return defs;
  }

  public Set<Declaration<Type>> getDeclaredTypes(Scope scope) {
    Set<Declaration<Type>> defs = new HashSet<>();
    getDefinitions(Type.class, scope, defs);
    return defs;
  }

  public Set<Scope> getChildScopes(Scope scope) {
    if (scope == null) {
      return Collections.singleton(Scope.NATIVE_SCOPE);
    }

    Set<Scope> children = new HashSet<>();
    accumulateChildScopes(scope, children);
    return children;
  }

  public Scope getRootScope() {
    return rootScope;
  }

  public Type getAliasedType(Scope scope, String name) {
    return getDefinition(Type.class, scope, name, true);
  }

  public void addAliasedType(Scope scope, String name, Type type) {
    addDefinition(Type.class, scope, name, type);
  }

  public void setAliasedType(Scope scope, String name, Type type) {
    setDefinition(Type.class, scope, name, type);
  }

  private <T> void setDefinition(Class<T> defnType, Scope scope, String name, T definition) {
    checkLockStatus();
    validateScope(scope);

    ScopeRules rules = scopeRules.get(scope);
    if (rules == null) {
      rules = new ScopeRules();
      scopeRules.put(scope, rules);
    }

    rules.setDefinition(defnType, name, definition);
  }

  private void validateScope(Scope s) {
    if (Scope.NATIVE_SCOPE.equals(s)) {
      throw new IllegalArgumentException("Cannot modify the native scope");
    }

    while (s != null && !s.equals(rootScope)) {
      s = s.getParent();
    }

    if (s == null) {
      throw new IllegalArgumentException("Scope is not a child of the root scope");
    }
  }

  public Expression getVariable(Scope scope, String name) {
    return getDefinition(Expression.class, scope, name, true);
  }

  public void addVariable(Scope scope, String name, Expression expression) {
    addDefinition(Expression.class, scope, name, expression);
  }

  public void setVariable(Scope scope, String name, Expression expression) {
    setDefinition(Expression.class, scope, name, expression);
  }

  public Type getExpressionType(Expression expr) {
    Type cached = expressionTypeCache.get(expr);
    if (cached == null) {
      // The expression hasn't been processed, so its type is a new meta type
      cached = new MetaType(expr.getScope());
      expressionTypeCache.put(expr, cached);
    }

    return cached;
  }

  public void setExpressionType(Expression expr, Type type) {
    checkLockStatus();
    validateScope(expr.getScope());
    expressionTypeCache.put(expr, type);
  }

  public Type getBoundMetaType(MetaType meta) {
    return getMetaBinding(Type.class, meta);
  }

  public void bindMetaType(MetaType wildcard, Type toType) {
    addMetaBinding(wildcard, toType);
  }

  public ArrayType.Length getBoundArrayLength(ArrayType.Length wildcard) {
    return getMetaBinding(ArrayType.Length.class, wildcard);
  }

  public void bindArrayLength(ArrayType.Length wildcard, ArrayType.Length toLength) {
    if (!wildcard.isWildcard()) {
      throw new IllegalArgumentException("Wildcard length is not a wildcard");
    }
    addMetaBinding(wildcard, toLength);
  }

  private <T> void getDefinitions(Class<T> defType, Scope scope, Set<Declaration<T>> defs) {
    ScopeRules rules = scopeRules.get(scope);
    if (rules != null) {
      for (Map.Entry<String, Object> def : rules.defs.entrySet()) {
        if (defType.isInstance(def.getValue())) {
          defs.add(new Declaration<>(scope, def.getKey(), defType.cast(def.getValue())));
        }
      }
    }

    if (parent != null) {
      parent.getDefinitions(defType, scope, defs);
    }
  }

  private void accumulateChildScopes(Scope parent, Set<Scope> children) {
    for (Scope scope : scopeRules.keySet()) {
      if (parent.equals(scope.getParent())) {
        children.add(scope);
      }
    }
    if (this.parent != null) {
      this.parent.accumulateChildScopes(parent, children);
    }
  }

  private <T> T getDefinitionInEnvironment(
      Class<T> defnType, Scope scope, String name, boolean recurseScopes) {
    ScopeRules rules = scopeRules.get(scope);
    T def = (rules != null ? rules.getDefinition(defnType, name) : null);
    if (recurseScopes && def == null && scope.getParent() != null) {
      // stay within the current environment
      return getDefinitionInEnvironment(defnType, scope.getParent(), name, true);
    } else {
      return def;
    }
  }

  private <T> T getDefinition(Class<T> defnType, Scope scope, String name, boolean recurseScopes) {
    T def = getDefinitionInEnvironment(defnType, scope, name, recurseScopes);
    if (def == null && parent != null) {
      return parent.getDefinition(defnType, scope, name, recurseScopes);
    } else {
      return def;
    }
  }

  private <T> void addDefinition(Class<T> defnType, Scope scope, String name, T definition) {
    // Don't recurse to the higher scopes (but do query all environments); it's okay to shadow a
    // higher definition but it is not okay to redefine a variable in the same scope
    if (getDefinition(defnType, scope, name, false) != null) {
      throw new IllegalStateException(name + " is already defined in this scope");
    }
    setDefinition(defnType, scope, name, definition);
  }

  private <T> T getMetaBinding(Class<T> metaClass, T wildcard) {
    T bound = metaClass.cast(metaBindings.get(wildcard));
    if (bound != null) {
      // Follow the bound type to resolve any binding chain; if bound isn't chained to
      // anything else then it's the end of the current bind chain and can be returned.
      T chained = getMetaBinding(metaClass, bound);
      return chained == null ? bound : chained;
    } else if (parent != null) {
      // There was nothing bound at this environment level, so query the parent
      return parent.getMetaBinding(metaClass, wildcard);
    } else {
      // No binding at this level, and no parent to query
      return null;
    }
  }

  private <T> void addMetaBinding(T wildcard, T toTarget) {
    checkLockStatus();
    validateMetaBinding(wildcard);
    metaBindings.put(wildcard, toTarget);
  }

  private void validateMetaBinding(Object key) {
    if (key == null) {
      throw new NullPointerException("Wildcard cannot be null");
    }
    if (metaBindings.containsKey(key)) {
      throw new IllegalStateException("Meta has already been bound to another type");
    }
    if (parent != null) {
      parent.validateMetaBinding(key);
    }
  }

  // FIXME This class needs to be updated to include generating new unique names for paramatric types,
  // new names for implicit parameter expressions (however I decided to do that). It needs to maintain
  // an alias stack that the type unifier and inferrer, generalizer can use to detect cycles as its
  // processing types.
  private enum State {
    READY,
    LOCKED,
    COMMITTED,
    ABORTED
  }

  private static class ScopeRules {
    private final Map<String, Object> defs;

    public ScopeRules() {
      defs = new HashMap<>();
    }

    public <T> void setDefinition(Class<T> defnType, String name, T defined) {
      // Confirm that higher functions aren't abusing the API
      if (!defnType.isInstance(defined)) {
        throw new IllegalArgumentException(
            "Defined instance of incompatible type; expected " +
                defnType + " but was " + defined.getClass());
      }

      Object oldDefinition = defs.get(name);
      if (oldDefinition != null) {
        // Confirm that the old definition is a subtype of defnType
        if (!defnType.isInstance(oldDefinition)) {
          throw new IllegalStateException(
              name +
                  " has already been defined as an incompatible type; expected " +
                  defnType.getSimpleName().toLowerCase() + " but was " +
                  oldDefinition.getClass());
        }
      }

      // Store in definition map
      defs.put(name, defined);
    }

    public <T> T getDefinition(Class<T> defnType, String name) {
      Object oldDefinition = defs.get(name);
      if (oldDefinition != null) {
        // Confirm that the old definition is of the expected type
        if (!defnType.isInstance(oldDefinition)) {
          // One option would be to fail here, but that would make it possible to shadow a higher
          // scope's variable with a type and then not be able to reference it. If there is no
          // higher scoped variable returning null will still cause code to fail if it's trying to
          // reference a defined type as if it were a variable.
          return null;
        }

        return defnType.cast(oldDefinition);
      } else {
        // Nothing defined
        return null;
      }
    }
  }
}
