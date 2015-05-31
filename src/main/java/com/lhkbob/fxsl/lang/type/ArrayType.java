package com.lhkbob.fxsl.lang.type;

import com.lhkbob.fxsl.lang.expr.ParameterExpression;
import com.lhkbob.fxsl.util.Immutable;

import static com.lhkbob.fxsl.util.Preconditions.notNull;

/**
 * Array Types
 * ===========
 *
 * Arrays are custom types that can be declared in FXSL programs. They are fixed-length lists of elements,
 * where each element is of the same type. Order within an array instance is well-defined based on how
 * the elements are ordered in the constructor. Elements can be accessed by integer indices, starting from `0`
 * up to `length - 1`. The length of an array is part of its type, thus `float[3]` is a different type
 * than `float[4]`. The component type of an array refers to the type of its elements. The component type
 * can be any other valid type, including other arrays, structs, and wildcards.
 *
 * The length of an array must either be an integer primitive, or an identifier. If an identifier is used,
 * the array type has a wildcard length and the specified identifier declares an implicit parameter of type
 * int that is replaced with any array instance's length when used, which exists in the same scope as the
 * defined array type. Arrays with explicit lengths must have a length greater than or equal to one. Arrays
 * with wildcard lengths can only be declared as the type of function parameters: the length identifier is
 * treated as if it were another argument to the function when it's invoked.
 *
 * @author Michael Ludwig
 */
@Immutable
public final class ArrayType implements Type {
    private final Type componentType;
    private final int constantLength;
    private final ParameterExpression wildcardLength;

    /**
     * Construct a new ArrayType with an explicit length. The length must be at least 1.
     *
     * @param componentType The component type of the array
     * @param length        The explicit length of the array
     * @throws java.lang.IllegalArgumentException if `length` is less than 1
     * @throws java.lang.NullPointerException     if `componentType` is null
     */
    public ArrayType(Type componentType, int length) {
        this(componentType, length, null);
    }

    /**
     * Construct a new ArrayType with a wildcard length. The expression `length` is
     * the variable used to refer to the array length before its made concrete.
     *
     * @param componentType The component type of the array
     * @param length        The wildcard length variable for the array
     * @throws java.lang.IllegalArgumentException if the type of `length` is not `INT`
     * @throws java.lang.NullPointerException     if `length` or `componentType` are null
     */
    public ArrayType(Type componentType, ParameterExpression length) {
        this(componentType, -1, length);
    }

    private ArrayType(Type componentType, int constantLength, ParameterExpression wildcardLength) {
        notNull("componentType", componentType);
        if (wildcardLength != null) {
            if (wildcardLength.getType() != PrimitiveType.INT) {
                throw new IllegalArgumentException("Wildcard length expression must be of type INT, not: " +
                                                   wildcardLength.getType());
            }
        } else {
            if (constantLength < 1) {
                throw new IllegalArgumentException("Constant length must be at least 1, not: " +
                                                   constantLength);
            }
        }

        this.componentType = componentType;
        this.constantLength = constantLength;
        this.wildcardLength = wildcardLength;
    }

    /**
     * @return The component type of the array
     */
    public Type getComponentType() {
        return componentType;
    }

    /**
     * Get the concrete length of the array. If this array was declared with a wildcard length, a negative
     * number is returned. Otherwise, the value will be at least `1`.
     *
     * @return The concrete or explicit length this array was declared with
     */
    public int getConcreteLength() {
        return constantLength;
    }

    /**
     * Get the variable expression that represents the wildcard length of this array type. If the array was
     * not declared with a wildcard length then `null` is returned.
     *
     * @return The wildcard length of the array
     */
    public ParameterExpression getWildcardLength() {
        return wildcardLength;
    }

    @Override
    public <T> T accept(Type.Visitor<T> visitor) {
        return visitor.visitArrayType(this);
    }

    @Override
    public boolean equals(Object t) {
        if (!(t instanceof ArrayType)) {
            return false;
        }
        ArrayType a = (ArrayType) t;
        if (a.componentType.equals(componentType)) {
            if (wildcardLength != null) {
                return wildcardLength.equals(a.wildcardLength);
            } else {
                return constantLength == a.constantLength;
            }
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash += hash * 37 + componentType.hashCode();
        if (wildcardLength == null) {
            hash += hash * 37 + constantLength;
        } else {
            hash += hash * 37 + wildcardLength.hashCode();
        }
        return hash;
    }

    @Override
    public String toString() {
        if (wildcardLength != null) {
            return String.format("%s[%s]", componentType, wildcardLength.getParameterName());
        } else {
            return String.format("%s[%d]", componentType, constantLength);
        }
    }
}
