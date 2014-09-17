package com.lhkbob.fxsl.lang;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.lhkbob.fxsl.util.Preconditions.*;

/**
 * Struct Values
 * =============
 *
 * Struct values are the constructor expressions that form instances of {@link
 * com.lhkbob.fxsl.lang.StructType}. The expression type of a struct value is implicitly defined by the fields
 * specified and their  corresponding expressions' types.
 *
 * @author Michael Ludwig
 */
public class StructValue implements Expression {
    private final StructType type;
    private final Map<String, Expression> fields;

    /**
     * Create a new struct value that is described completely by the map. The keys in the map are the field
     * names of the struct, and the corresponding map values are the expressions that must be evaluated to
     * determine the field value. The map `fields` is cloned so future modifications to it will not affect the
     * definition of this struct value.
     *
     * The expression type of this struct value is a {@link StructType} with the exact same field set with
     * types based on the declared types of the fields' expressions.
     *
     * @param fields The map of field values defining the struct
     * @throws java.lang.IllegalArgumentException if `fields` is empty
     * @throws java.lang.NullPointerException     if `fields` is null or contains null elements
     */
    public StructValue(Map<String, Expression> fields) {
        notNull("fields", fields);
        notEmpty("fields", fields.keySet());
        noNullElements("fields", fields.values());

        Map<String, Type> fieldTypes = new HashMap<>();
        for (Map.Entry<String, Expression> e : fields.entrySet()) {
            fieldTypes.put(e.getKey(), e.getValue().getType());
        }
        type = new StructType(fieldTypes);
        this.fields = Collections.unmodifiableMap(new HashMap<>(fields));
    }

    /**
     * Get the value of the field given by `name`. If `name` is not a defined field for the struct then
     * `null` is returned. Otherwise the returned value is the expression that computes the value assigned to
     * the given field for the struct.
     *
     * @param name The field name to look up
     * @return The field's value
     */
    public Expression getField(String name) {
        return fields.get(name);
    }

    /**
     * Get every value of this struct, with the associated field names as the keys to the map. The returned
     * map cannot be modified.
     *
     * @return All values of the struct
     */
    public Map<String, Expression> getFields() {
        return fields;
    }

    @Override
    public StructType getType() {
        return type;
    }

    @Override
    public boolean isConcrete() {
        if (!type.isConcrete()) {
            return false;
        }
        for (Expression f : fields.values()) {
            if (!f.isConcrete()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitStruct(this);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof StructValue)) {
            return false;
        }
        StructValue v = (StructValue) o;
        return v.type.equals(type) && v.fields.equals(fields);
    }

    @Override
    public int hashCode() {
        return type.hashCode() ^ fields.hashCode();
    }
}
