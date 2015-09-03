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
 * Union Values
 * ============
 *
 * Union values are expressions that form instances of {@link com.lhkbob.fxsl.lang.type.UnionType}
 * within FXSL. In order for a  union value expression to be valid, the expressions combined must
 * have types that form a valid union type. Thus, even if two different functions with the exact
 * same signature or union'ed together, a compiler failure will arise because there is only a single
 * function type involved in the union. Not every instance of functions with equivalent signatures
 * being union'ed can be caught by the UnionValue's constructor, but it can be verified completely
 * when function selection is performed.
 *
 * If valid, the type of union value expressions is implicitly defined by the types of the
 * expressions being combined. For convenience, a union made with other unions will flatten out the
 * options from its child unions to present the most concrete options. If the union is the result of
 * a function call or parameter, this may not be possible so this flattening does not preclude the
 * presence of expressions that still have union types in the set of function options.
 *
 * @author Michael Ludwig
 */
@Immutable
public final class UnionValue extends EfficientEqualityBase implements Expression {
  private final Scope scope;
  private final List<Expression> functions;

  /**
   * Create a new union value that is the union of the given `functions`. The type of this
   * expression is implicitly defined by the types of the function expressions. If the expression
   * types produce an invalid union type, the union value cannot be constructed.
   *
   * Like {@link com.lhkbob.fxsl.lang.type.UnionType}, this flattens the `functions` set when any
   * other UnionValues are encountered. However, not every expression with a union type can be
   * flattened if the actual option expressions cannot be determined.
   *
   * @param scope
   *     The scope within which the union is declared
   * @param functions
   *     The function options of the union
   * @throws java.lang.IllegalArgumentException
   *     if `functions` is empty
   * @throws java.lang.NullPointerException
   *     if `functions` is null or contains null elements
   */
  public UnionValue(Scope scope, List<? extends Expression> functions) {
    notNull("scope", scope);
    validCollection("functions", functions);

    this.scope = scope;
    this.functions = Collections.unmodifiableList(new ArrayList<>(functions));
  }

  /**
   * Get all function options this union embodies. Note that the size of this set is not
   * necessarily equal to the type set returned by `infer().getOptions()`. This is because a union
   * value can only be flattened if the expression explicitly declares the options. If a function
   * call returns a union, or a parameter is a union, the actual options are not available until the
   * function is inlined.
   *
   * The returned set cannot be modified and will have at least two elements.
   *
   * @return The union'ed function options of this expression
   */
  public List<Expression> getOptions() {
    return functions;
  }

  public Expression getOption(int index) {
    return functions.get(index);
  }

  public int getOptionCount() {
    return functions.size();
  }

  @Override
  public Scope getScope() {
    return scope;
  }

  @Override
  public <T> T accept(Visitor<T> visitor) {
    return visitor.visitUnion(this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (Expression o : functions) {
      if (first) {
        first = false;
      } else {
        sb.append(" | ");
      }
      sb.append(o.toString());
    }
    return sb.toString();
  }

  @Override
  protected int computeHashCode() {
    return functions.hashCode() ^ scope.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    UnionValue v = compareHashCodes(UnionValue.class, o);
    return v != null && v.scope.equals(scope) && v.functions.equals(functions);
  }
}
