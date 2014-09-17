package com.lhkbob.fxsl.lang;

import static com.lhkbob.fxsl.util.Preconditions.notNull;

/**
 * Struct Fields
 * =============
 *
 * As a collection of labeled expressions, structs are useful collections of data. Accessing a field from a
 * struct is an expression that evaluates to the value of that field. Accessing a field that does not exist is
 * a compile time failure in FXSL. See {@link com.lhkbob.fxsl.lang.StructType} for more details.
 *
 * A struct field access is concrete if the entire struct expression is concrete, or when the struct value
 * is deterministic, if the field expression is concrete.
 *
 * @author Michael Ludwig
 */
public class StructFieldAccess implements Expression {
    private final Expression struct;
    private final String field;

    private final transient Type fieldType;

    /**
     * Create a new struct field access expression that accesses `field` from the given expression. The
     * expression must evaluate to a struct type or a wildcard type. The type of this expression is the type
     * of the struct's field's expression, or a dependent wildcard if accessing a wildcard type.
     *
     * @param struct The struct being accessed
     * @param field  The name of the field to access
     * @throws java.lang.IllegalArgumentException if the field does not exist in the struct type, or if
     *                                            `struct` does not have a struct or wildcard type
     * @throws java.lang.NullPointerException     if `struct` or `field` are null
     */
    public StructFieldAccess(Expression struct, String field) {
        notNull("struct", struct);
        notNull("field", field);

        if (struct.getType() instanceof StructType) {
            Type t = ((StructType) struct.getType()).getFieldType(field);
            if (t == null) {
                throw new IllegalArgumentException("Field does not exist in struct: " + field);
            }
            fieldType = t;
        } else if (struct.getType() instanceof WildcardType) {
            fieldType = ((WildcardType) struct.getType()).createDependentType(field);
        } else {
            throw new IllegalArgumentException("Field access must operate on structs or wildcards, not " +
                                               struct.getType());
        }

        this.struct = struct;
        this.field = field;
    }

    /**
     * Get the expression that evaluates to the struct value having one of its fields accessed. The type of
     * this expression will be a {@link com.lhkbob.fxsl.lang.StructType} or a {@link
     * com.lhkbob.fxsl.lang.WildcardType}.
     *
     * @return The struct expression
     */
    public Expression getStruct() {
        return struct;
    }

    /**
     * Get the field label that is being accessed. If the struct expression is a StructType, the returned
     * label will be defined field in that type. If the struct expression is a WildcardType, the returned
     * label is the dependent wildcard label used for the type of this expression.
     *
     * @return The field label to access
     */
    public String getField() {
        return field;
    }

    @Override
    public Type getType() {
        return fieldType;
    }

    @Override
    public boolean isConcrete() {
        if (struct instanceof StructValue) {
            return ((StructValue) struct).getField(field).isConcrete();
        } else {
            // e.g. a function call that returns a struct
            return struct.isConcrete();
        }
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitFieldAccess(this);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof StructFieldAccess)) {
            return false;
        }
        StructFieldAccess a = (StructFieldAccess) o;
        return a.struct.equals(struct) && a.field.equals(field);
    }

    @Override
    public int hashCode() {
        return struct.hashCode() ^ field.hashCode();
    }
}
