package com.lhkbob.fxsl.lang.expr;

import com.lhkbob.fxsl.lang.Scope;
import com.lhkbob.fxsl.lang.type.MetaType;
import com.lhkbob.fxsl.lang.type.StructType;
import com.lhkbob.fxsl.lang.type.Type;
import com.lhkbob.fxsl.util.Immutable;

import static com.lhkbob.fxsl.util.Preconditions.notNull;

/**
 * Struct Fields
 * =============
 *
 * As a collection of labeled expressions, structs are useful collections of data. Accessing a field from a
 * struct is an expression that evaluates to the value of that field. Accessing a field that does not exist is
 * a compile time failure in FXSL. See {@link com.lhkbob.fxsl.lang.type.StructType} for more details.
 *
 * @author Michael Ludwig
 */
@Immutable
public final class StructFieldAccess implements Expression {
    private final Scope scope;
    private final Expression struct;
    private final String field;

    private final transient Type fieldType;

    /**
     * Create a new struct field access expression that accesses `field` from the given expression. The
     * expression must evaluate to a struct type or a wildcard type. The type of this expression is the type
     * of the struct's field's expression, or a dependent wildcard if accessing a wildcard type.
     *
     * @param scope The scope of this expression
     * @param struct The struct being accessed
     * @param field  The name of the field to access
     * @throws java.lang.IllegalArgumentException if the field does not exist in the struct type, or if
     *                                            `struct` does not have a struct or wildcard type
     * @throws java.lang.NullPointerException     if `struct` or `field` are null
     */
    public StructFieldAccess(Scope scope, Expression struct, String field) {
        notNull("scope", scope);
        notNull("struct", struct);
        notNull("field", field);

        if (struct.getType() instanceof StructType) {
            Type t = ((StructType) struct.getType()).getFieldType(field);
            if (t == null) {
                throw new IllegalArgumentException("Field does not exist in struct: " + field);
            }
            fieldType = t;
        } else if (struct.getType() instanceof MetaType) {
            fieldType = new MetaType(new Scope());
        } else {
            throw new IllegalArgumentException("Field access must operate on structs or wildcards, not " +
                                               struct.getType());
        }

        this.scope = scope;
        this.struct = struct;
        this.field = field;
    }

    /**
     * Get the expression that evaluates to the struct value having one of its fields accessed. The type of
     * this expression will be a {@link com.lhkbob.fxsl.lang.type.StructType} or a {@link
     * MetaType}.
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
    public Scope getScope() {
        return scope;
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
        return a.scope.equals(scope) && a.struct.equals(struct) && a.field.equals(field);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result += 31 * result + struct.hashCode();
        result += 31 * result + field.hashCode();
        result += 31 * result + scope.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return struct.toString() + "[" + field + "]";
    }
}
