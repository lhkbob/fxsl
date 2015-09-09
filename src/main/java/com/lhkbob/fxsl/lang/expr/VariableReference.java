package com.lhkbob.fxsl.lang.expr;

import com.lhkbob.fxsl.lang.Scope;
import com.lhkbob.fxsl.util.EfficientEqualityBase;
import com.lhkbob.fxsl.util.Immutable;

import static com.lhkbob.fxsl.util.Preconditions.notNull;

/**
 * Variables
 * ==========
 *
 * Variable expressions are name references to other expressions defined in the current scope, or
 * one of the current scope's parent scopes. Variables can be referenced in FXSL code before they
 * are defined, so long as the variable name is defined within an available scope once parsing has
 * completed.
 *
 * Variables are never concrete. One of the first processing steps after parsing is resolving
 * variables to their defined values. If a variable name is not defined then the FXSL code fails to
 * compile. Because of this resolution step, a variable expression actually has an implicit wildcard
 * type until its been resolved to the correct value.
 *
 * Although structurally very similar to {@link Parameter}, these two reference expressions are
 * semantically very different. This is the expression analog of {@link
 * com.lhkbob.fxsl.lang.type.AliasType}.
 *
 * @author Michael Ludwig
 */
@Immutable
public final class VariableReference extends EfficientEqualityBase implements Expression {
  private final String name;
  private final Scope scope;

  /**
   * Create a new variable expression that is defined in `scope` and can be referenced by the given
   * `name`.
   *
   * @param scope
   *     The scope that the variable is referenced (e.g. the current scope)
   * @param name
   *     The name of the variable
   * @throws java.lang.NullPointerException
   *     if any argument is null
   */
  public VariableReference(Scope scope, String name) {
    notNull("name", name);
    notNull("scope", scope);

    this.name = name;
    this.scope = scope;
  }

  @Override
  public <T> T accept(Visitor<T> visitor) {
    return visitor.visitVariable(this);
  }

  @Override
  public boolean equals(Object o) {
    VariableReference t = compareHashCodes(VariableReference.class, o);
    return t != null && t.name.equals(name) && t.scope.equals(scope);
  }

  /**
   * Get the scope that this variable is referenced within. The reference scope determines which
   * actual value these expressions resolve to based on the variable name.
   *
   * @return The scope  this variable was referenced within
   */
  @Override
  public Scope getScope() {
    return scope;
  }

  /**
   * Get the variable name that determines the actual value of this expression.
   *
   * @return The variable's name
   */
  public String getVariableName() {
    return name;
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  protected int computeHashCode() {
    int result = 17;
    result += 31 * result + name.hashCode();
    result += 31 * result + scope.hashCode();
    return result;
  }
}
