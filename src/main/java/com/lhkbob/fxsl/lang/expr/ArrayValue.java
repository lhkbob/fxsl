package com.lhkbob.fxsl.lang.expr;

import com.lhkbob.fxsl.lang.Scope;
import com.lhkbob.fxsl.util.Immutable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.lhkbob.fxsl.util.Preconditions.notNull;
import static com.lhkbob.fxsl.util.Preconditions.validCollection;

/**
 * Array Values
 * ============
 *
 * Array values are constructor expressions that turn a list of compatibly-typed expressions into an array,
 * which is described in {@link com.lhkbob.fxsl.lang.type.ArrayType}. An array value's component type is the
 * union type of all expressions. As arrays have finite lengths, most cases the array's elements can be
 * written directly in FXSL.
 *
 * However, a functional array constructor is also available that invokes a function to produce values for
 * each index. This constructor is also usable when the length of the array is an unknown dynamic variable.
 * FIXME implement this feature and determine its syntax.
 *
 * @author Michael Ludwig
 */
@Immutable
public final class ArrayValue implements Expression {
    private final Scope scope;
    private final List<Expression> elements;

    /**
     * Create a new array value with element values taken directly from the list `elements`. The list is
     * copied so modifications to `elements` after the constructor completes will not affect the array value.
     *
     * @param scope    The scope the array value is constructed within
     * @param elements The array elements
     * @throws IllegalArgumentException       if `elements` is empty
     * @throws java.lang.NullPointerException if `scope` or `componentType` are null, or `elements` is
     *                                        null or contains null elements
     */
    public ArrayValue(Scope scope, List<? extends Expression> elements) {
        validCollection("elements", elements);
        notNull("scope", scope);

        this.scope = scope;
        this.elements = Collections.unmodifiableList(new ArrayList<>(elements));
    }

    /**
     * Get the length of the array value, which is equal to the length of the associated array type.
     *
     * @return The length of the array value, will be at least 1
     */
    public int getLength() {
        return elements.size();
    }

    /**
     * Get the element value at `index` in this array value. The returned expression's type will be
     * assignable to the component type of this array's type, but it may not be the identical type of all
     * element expressions. It will not be a null value.
     *
     * @param index The index to lookup
     * @return The array value at `index`
     * @throws java.lang.IndexOutOfBoundsException if `index` is less than 0 or greater than or equal to
     *                                             `getLength()`
     */
    public Expression getElement(int index) {
        return elements.get(index);
    }

    /**
     * Get all element expressions in this array value constructor. The order of the returned list is the
     * element order in the array value. The returned array cannot be modified.
     *
     * @return The array's expressions
     */
    public List<Expression> getElements() {
        return elements;
    }

    @Override
    public Scope getScope() {
        return scope;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitArray(this);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ArrayValue)) {
            return false;
        }
        ArrayValue v = (ArrayValue) o;
        return v.scope.equals(scope) && v.elements.equals(elements);
    }

    @Override
    public int hashCode() {
        return elements.hashCode() ^ scope.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        boolean first = true;
        for (Expression e : elements) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            sb.append(e.toString());
        }
        sb.append(']');
        return sb.toString();
    }
}
