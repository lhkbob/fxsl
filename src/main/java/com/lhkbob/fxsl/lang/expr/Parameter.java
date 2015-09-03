package com.lhkbob.fxsl.lang.expr;

import com.lhkbob.fxsl.lang.Scope;
import com.lhkbob.fxsl.util.EfficientEqualityBase;
import com.lhkbob.fxsl.util.Immutable;

import static com.lhkbob.fxsl.util.Preconditions.notNull;

/**
 * Parameters
 * ==========
 *
 * Parameter expressions are variable references that resolved to the declared names of function
 * parameters. If a function is inlined the parameter expression may be replaced with the actual
 * parameter expression. Otherwise they dynamically represent the parameter expressions their owning
 * function is invoked with. Because parameters only exist in the declaration of a function value,
 * parameter expressions are only seen referenced in expressions used as function bodies. This is
 * the expression analog of a {@link com.lhkbob.fxsl.lang.type.ParametricType}.
 *
 * A parameter expression is concrete if its type is concrete.
 *
 * @author Michael Ludwig
 */
@Immutable
public final class Parameter extends EfficientEqualityBase implements Expression {
  private final String name;
  private final FunctionValue function;

  /**
   * @param function
   *     The function that defines this parameter
   * @param name
   *     The name of the parameter
   * @throws java.lang.NullPointerException
   *     if any argument is null
   */
  public Parameter(FunctionValue function, String name) {
    notNull("name", name);
    notNull("function", function);
    if (!function.getParameters().contains(name)) {
      throw new IllegalArgumentException("Parameter name is not defined by the function:  " + name);
    }
    this.function = function;
    this.name = name;
  }

  @Override
  public Scope getScope() {
    return function.getBodyScope();
  }

  @Override
  public <T> T accept(Visitor<T> visitor) {
    return visitor.visitParameter(this);
  }

  /**
   * Get the parameter name that can be used in FXSL code to reference the parameter.
   *
   * @return The parameter's name
   */
  public String getParameterName() {
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
    result += 31 * result + function.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object o) {
    Parameter t = compareHashCodes(Parameter.class, o);
    return t != null && t.name.equals(name) && t.function.equals(function);
  }
}
