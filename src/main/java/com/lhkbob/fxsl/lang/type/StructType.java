package com.lhkbob.fxsl.lang.type;

import com.lhkbob.fxsl.util.Immutable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.lhkbob.fxsl.util.Preconditions.*;

/**
 * Structure Types
 * ===============
 *
 * Structures, or *structs*, are composite objects made up of labeled fields. Each field can have its own
 * type, which may be any type supported by FXSL including other composite types and wildcards. Structs form
 * the counterpart to {@link ArrayType arrays}, which are homogeneously typed lists,
 * while structs are heterogeneous maps. The field labels and types define a struct; two struct declarations
 * with the exact same labeled types, regardless of order, are considered equal. Thus, if two structs have the
 * same field but have different types, the structs are different. Similarly, two structs with the same typed
 * field are not equal if the label is not the same.
 *
 * @author Michael Ludwig
 */
@Immutable
public final class StructType implements Type {
    private final Map<String, Type> fields;

    /**
     * Create a new StructType instance that is made up of the given fields. The map is copied, so no
     * modifications to it will affect the created type.
     *
     * @param fields The fields of the structure
     * @throws java.lang.IllegalArgumentException if `fields` is empty
     * @throws java.lang.NullPointerException     if `fields` is null or contains null elements
     */
    public StructType( Map<String, ? extends Type> fields) {
        notNull("fields", fields);
        notEmpty("fields", fields.keySet());
        noNullElements("fields", fields.keySet());
        noNullElements("fields", fields.values());

        this.fields = Collections.unmodifiableMap(new HashMap<>(fields));
    }

    /**
     * Get the type of the field specified by `name`. If the field name is not a valid field for this type
     * then `null` is returned.
     *
     * @param name The field name
     * @return The type of the field, or null
     */
    public Type getFieldType(String name) {
        return fields.get(name);
    }

    /**
     * Get every field defined by this type. The keys are the field names and the corresponding map values
     * are the field types. The returned map cannot be modified.
     *
     * @return All fields in this type
     */
    public Map<String, Type> getFieldTypes() {
        return fields;
    }

    @Override
    public <T> T accept(Type.Visitor<T> visitor) {
        return visitor.visitStructType(this);
    }

    @Override
    public boolean equals(Object t) {
        if (!(t instanceof StructType)) {
            return false;
        }
        StructType o = (StructType) t;
        return o.fields.equals(fields);
    }

    @Override
    public int hashCode() {
        return fields.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean first = true;
        for (Map.Entry<String, Type> f : fields.entrySet()) {
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
}
