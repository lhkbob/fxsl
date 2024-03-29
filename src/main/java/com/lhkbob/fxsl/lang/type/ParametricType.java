package com.lhkbob.fxsl.lang.type;

import com.lhkbob.fxsl.lang.Scope;
import com.lhkbob.fxsl.util.EfficientEqualityBase;

import static com.lhkbob.fxsl.util.Preconditions.notNull;

/**
 *
 */
public class ParametricType extends EfficientEqualityBase implements Type {
  private final String label;
  private final Scope scope;

  /**
   * Create a new parametric type named `label`. Parametric types with the same label are equal.
   * The constructor does *not* register the type with the scope.
   *
   * @param label
   *     The label for the wildcard
   * @throws java.lang.NullPointerException
   *     if `scope` or `label` are null
   */
  public ParametricType(Scope scope, String label) {
    notNull("scope", scope);
    notNull("label", label);
    this.label = label;
    this.scope = scope;
  }

  @Override
  public <T> T accept(Type.Visitor<T> visitor) {
    return visitor.visitParametricType(this);
  }

  @Override
  public boolean equals(Object t) {
    ParametricType p = compareHashCodes(ParametricType.class, t);
    return p != null && p.label.equals(label) && p.scope.equals(scope);
  }

  /**
   * Get the label of this type.
   *
   * @return The type's label
   */
  public String getLabel() {
    return label;
  }

  /**
   * Get the scope this parameter is valid within.
   *
   * @return The scope that defined this type
   */
  public Scope getScope() {
    return scope;
  }

  @Override
  public String toString() {
    if (label.startsWith("_")) {
      // This was a generalized parametric type, so its intended to be unique over the
      // environment so no need to append the (in scope) business
      return "_" + label;
    } else {
      return "_" + label + "(in " + scope + ")";
    }
  }

  @Override
  protected int computeHashCode() {
    return label.hashCode() ^ scope.hashCode();
  }
}
