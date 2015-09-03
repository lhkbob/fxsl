package com.lhkbob.fxsl.lang.expr;

import com.lhkbob.fxsl.lang.Scope;
import com.lhkbob.fxsl.util.EfficientEqualityBase;
import com.lhkbob.fxsl.util.Immutable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.lhkbob.fxsl.util.Preconditions.notNull;
import static com.lhkbob.fxsl.util.Preconditions.validCollection;

/**
 * Function Values
 * ===============
 *
 * Function values are expressions that define functions that can then be invoked later. A
 * function value defines a new scope that by default includes the parameters of the function. The
 * function body is a single expression, but the `let ... in` syntax can be used in FXSL to compute
 * multiple values based on the parameters and form more complex expressions cleanly.
 *
 * @author Michael Ludwig
 */
@Immutable
public final class FunctionValue extends EfficientEqualityBase implements Expression {
  private final Scope scope;

  private final List<String> parameters;
  private final Expression returnValue;

  /**
   * Create a new function value that has the signature described by `parameterNames`
   * `returnValue` is the body of the function to be invoked when the function is called. The
   * environment must be updated to include {@link Parameter} expressions corresponding to the names
   * in `parameterNames`.
   *
   * @param scope
   *     The scope this function is defined within (not the body's scope)
   * @param parameterNames
   *     The parameter names accessible within the body's scope for this function
   * @param returnValue
   *     The body of the function
   * @throws IllegalArgumentException
   *     if `parameters` is empty
   * @throws java.lang.NullPointerException
   *     if any argument is null or contains a null element
   */
  public FunctionValue(Scope scope, List<String> parameterNames, Expression returnValue) {
    notNull("scope", scope);
    validCollection("parameterNames", parameterNames);
    notNull("returnValue", returnValue);

    this.parameters = Collections.unmodifiableList(new ArrayList<>(parameterNames));
    this.returnValue = returnValue;
    this.scope = scope;
  }

  /**
   * Get the declared parameter name of the `index` parameter, where the first parameter has index
   * 0. The index is the same as with parameter types in {@link
   * com.lhkbob.fxsl.lang.type.FunctionType}.
   *
   * @param index
   *     The parameter index to lookup
   * @return The name of the parameter
   *
   * @throws java.lang.IndexOutOfBoundsException
   *     if index is less than 0 or greater than
   *     `infer().getParameterCount() - 1`
   */
  public String getParameter(int index) {
    return parameters.get(index);
  }

  /**
   * Get the named parameters for this function value. The returned list cannot be modified.
   *
   * @return All parameter names of the function, ordered the same as how they were declared in FXSL
   */
  public List<String> getParameters() {
    return parameters;
  }

  /**
   * Get the expression that is evaluated each time the function is invoked. This expression can
   * contain {@link Parameter} referencing the parameters defined by this function value.
   *
   * @return The body of the function
   */
  public Expression getReturnValue() {
    return returnValue;
  }

  /**
   * @return The scope of the function body, which includes the parameter expression definitions.
   */
  public Scope getBodyScope() {
    return returnValue.getScope();
  }

  @Override
  public Scope getScope() {
    return scope;
  }

  @Override
  public <T> T accept(Visitor<T> visitor) {
    return visitor.visitFunction(this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append('(');
    for (int i = 0; i < parameters.size(); i++) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append(parameters.get(i));
    }
    sb.append(" -> ");
    sb.append(returnValue.toString());
    sb.append(')');
    return sb.toString();
  }

  @Override
  protected int computeHashCode() {
    int result = 17;
    result += 31 * result + parameters.hashCode();
    result += 31 * result + returnValue.hashCode();
    result += 31 * result + scope.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object o) {
    FunctionValue v = compareHashCodes(FunctionValue.class, o);
    return v != null && v.scope.equals(scope) && v.parameters.equals(parameters) &&
        v.returnValue.equals(returnValue);
  }
}
