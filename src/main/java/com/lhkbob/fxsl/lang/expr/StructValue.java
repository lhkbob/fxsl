package com.lhkbob.fxsl.lang.expr;

import com.lhkbob.fxsl.lang.Scope;
import com.lhkbob.fxsl.lang.type.StructType;
import com.lhkbob.fxsl.util.EfficientEqualityBase;
import com.lhkbob.fxsl.util.Immutable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.lhkbob.fxsl.util.Preconditions.noNullElements;
import static com.lhkbob.fxsl.util.Preconditions.notEmpty;
import static com.lhkbob.fxsl.util.Preconditions.notNull;

/**
 * Struct Values
 * =============
 *
 * Struct values are the constructor expressions that form instances of {@link
 * com.lhkbob.fxsl.lang.type.StructType}. The expression type of a struct value is implicitly
 * defined by the fields specified and their  corresponding expressions' types.
 *
 * @author Michael Ludwig
 */
@Immutable
public final class StructValue extends EfficientEqualityBase implements Expression {
  private final Map<String, Expression> fields;
  private final Scope scope;

  /**
   * Create a new struct value that is described completely by the map. The keys in the map are
   * the field names of the struct, and the corresponding map values are the expressions that must
   * be evaluated to determine the field value. The map `fields` is cloned so future modifications
   * to it will not affect the definition of this struct value.
   *
   * The expression type of this struct value is a {@link StructType} with the exact same field
   * set with types based on the declared types of the fields' expressions.
   *
   * @param scope
   *     The scope this struct was declared in
   * @param fields
   *     The map of field values defining the struct
   * @throws java.lang.IllegalArgumentException
   *     if `fields` is empty
   * @throws java.lang.NullPointerException
   *     if `fields` is null or contains null elements
   */
  public StructValue(Scope scope, Map<String, Expression> fields) {
    notNull("scope", scope);
    notNull("fields", fields);
    notEmpty("fields", fields.keySet());
    noNullElements("fields", fields.keySet());
    noNullElements("fields", fields.values());

    this.scope = scope;
    this.fields = Collections.unmodifiableMap(new HashMap<>(fields));
  }

  @Override
  public <T> T accept(Visitor<T> visitor) {
    return visitor.visitStruct(this);
  }

  @Override
  public boolean equals(Object o) {
    StructValue v = compareHashCodes(StructValue.class, o);
    return v != null && v.scope.equals(scope) && v.fields.equals(fields);
  }

  /**
   * Get the value of the field given by `name`. If `name` is not a defined field for the struct
   * then `null` is returned. Otherwise the returned value is the expression that computes the value
   * assigned to the given field for the struct.
   *
   * @param name
   *     The field name to look up
   * @return The field's value
   */
  public Expression getField(String name) {
    return fields.get(name);
  }

  /**
   * Get every value of this struct, with the associated field names as the keys to the map. The
   * returned map cannot be modified.
   *
   * @return All values of the struct
   */
  public Map<String, Expression> getFields() {
    return fields;
  }

  @Override
  public Scope getScope() {
    return scope;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("{");
    boolean first = true;
    for (Map.Entry<String, Expression> f : fields.entrySet()) {
      if (first) {
        first = false;
      } else {
        sb.append(", ");
      }
      sb.append(f.getKey()).append(":").append(f.getValue().toString());
    }
    sb.append("}");
    return sb.toString();
  }

  @Override
  protected int computeHashCode() {
    return fields.hashCode() ^ scope.hashCode();
  }
}
