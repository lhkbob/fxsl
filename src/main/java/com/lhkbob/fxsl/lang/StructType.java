package com.lhkbob.fxsl.lang;

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
 * the counterpart to {@link com.lhkbob.fxsl.lang.ArrayType arrays}, which are homogeneously typed lists,
 * while structs are heterogeneous maps. The field labels and types define a struct; two struct declarations
 * with the exact same labeled types, regardless of order, are considered equal. Thus, if two structs have the
 * same field but have different types, the structs are different. Similarly, two structs with the same typed
 * field are not equal if the label is not the same.
 *
 * ## Assignability and shared types
 *
 * A type can only be assigned to a struct value if that type is a wildcard or another struct subject to
 * restrictions listed below. If the type is a wildcard, the assignment or conversion creates a constraint
 * upon the wildcard instantiation that must be satisfied or a compiler error will be raised. Given two struct
 * types `A` and `B`, instances of `B` are assignable to `A` if `B` has a field name for every field in `A`
 * and the types of `B`'s matching fields are assignable to the corresponding fields in `A`.
 *
 * The shared type between two structs is a new struct type made of the field intersection between the
 * two structs, and the types of the fields the conversion of each corresponding field type. If a field's type
 * conversion is invalid that field is excluded. If the field intersection is empty the shared type is
 * invalid.
 *
 * Like other types, a struct with a wildcard produces the struct unmodified and adds a
 * constraint on the wildcard's instantiation.
 *
 * ## Concreteness
 *
 * A struct type is concrete if and only if the types of its fields are concrete.
 *
 * @author Michael Ludwig
 */
@Immutable
public class StructType implements Type {
    private static final double BASE_COST = 10.0;
    private static final double FIELD_COST = 1.0;

    private final Map<String, Type> fields;

    /**
     * Create a new StructType instance that is made up of the given fields. The map is copied, so no
     * modifications to it will affect the created type.
     *
     * @param fields The fields of the structure
     * @throws java.lang.IllegalArgumentException if `fields` is empty
     * @throws java.lang.NullPointerException     if `fields` is null or contains null elements
     */
    public StructType(Map<String, ? extends Type> fields) {
        notNull("fields", fields);
        notEmpty("fields", fields.keySet());
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
    public double getTypeComplexity() {
        double fieldCost = 0.0;
        for (Type field: fields.values()) {
            fieldCost += field.getTypeComplexity();
        }
        fieldCost += fields.size() * FIELD_COST;
        return BASE_COST + fieldCost;
    }

    @Override
    public double getAssignmentCost(Type t) {
        if (!isAssignableFrom(t)) {
            return Double.POSITIVE_INFINITY;
        } else if (t instanceof WildcardType) {
            return getTypeComplexity();
        } else {
            double cost = 0.0;

            // this type's fields are a subset of t's fields
            Map<String, Type> otherFields = ((StructType) t).fields;
            for (Map.Entry<String, Type> f : otherFields.entrySet()) {
                Type orig = fields.get(f.getKey());
                if (orig == null) {
                    // field from the other type must be removed completely
                    cost += FIELD_COST + f.getValue().getTypeComplexity();
                } else {
                    // field from the other type can be assigned to this type
                    cost += orig.getAssignmentCost(f.getValue());
                }
            }

            return cost;
        }
    }

    @Override
    public boolean isAssignableFrom(Type t) {
        if (!(t instanceof StructType)) {
            return t instanceof WildcardType;
        }
        Map<String, Type> otherFields = ((StructType) t).fields;
        for (Map.Entry<String, Type> f : fields.entrySet()) {
            Type otherType = otherFields.get(f.getKey());
            if (otherType == null) {
                // this struct does not have a subset of field names
                return false;
            }
            if (!f.getValue().isAssignableFrom(otherType)) {
                // the other type's field cannot be converted to this field type
                return false;
            }
        }
        return true;
    }

    @Override
    public Type getSharedType(Type t) {
        if (!(t instanceof StructType)) {
            return (t instanceof WildcardType ? this : null);
        }

        Map<String, Type> validFields = new HashMap<>();
        Map<String, Type> otherFields = ((StructType) t).fields;
        for (Map.Entry<String, Type> f : fields.entrySet()) {
            Type otherType = otherFields.get(f.getKey());
            if (otherType != null) {
                Type fieldConversion = f.getValue().getSharedType(otherType);
                if (fieldConversion == null) {
                    // common key with inconvertible types makes the whole thing inconvertible
                    return null;
                } else {
                    validFields.put(f.getKey(), fieldConversion);
                }
            }
        }
        if (validFields.isEmpty()) {
            // no shared fields and structs can't be empty
            return null;
        } else {
            return new StructType(validFields);
        }
    }

    @Override
    public boolean isConcrete() {
        for (Type f : fields.values()) {
            if (!f.isConcrete()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object t) {
        return t instanceof StructType && fields.equals(((StructType) t).fields);
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
        for (Map.Entry<String, Type> f: fields.entrySet()) {
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
