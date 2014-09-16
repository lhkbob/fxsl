package com.lhkbob.fxsl.lang;

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
 * defined array type. Arrays with explicit lengths must have a length greater than or equal to one.
 *
 * ## Assignability and shared types
 *
 * Array types are only assignable to other array types so long as certain rules apply, and to wildcards.
 * For an array to be assignable to another array, the component type of the first array must be assignable to
 * the component type of the second array. If the first array has an explicit length, the target type must
 * have the exact same length or have a wildcard length (in which case a constraint is added that the wildcard
 * length must equal the explicit length, possibly resulting in a compilation error). If the first array has a
 * wildcard length then a constraint is added that its length be equal to the second's length (valid
 * regardless of the second's length type).
 *
 * The shared type between two arrays is a new array type that has a component type equal to the type
 * conversion between the two arrays' component types. If the component type conversion is invalid, the two
 * arrays cannot be converted together. If both input arrays have explicit lengths, they must be equal and the
 * same value is used for the converted array; otherwise the conversion is invalid. If only one input array
 * has an explicit length, that value is used and a constraint is added to the other wildcard length. If both
 * have wildcard lengths, one is chosen arbitrarily and a constraint is added to the other.
 *
 * When assigned to a wildcard, the wildcard must resolve to the array type or a compilation error results.
 * Similarly, shared type between an array and a wildcard results in the array type.
 *
 * ## Concreteness
 *
 * An array type is considered concrete if its component type is concrete and it has an explicit length.
 *
 * @author Michael Ludwig
 */
@Immutable
public final class ArrayType implements Type {
    private static final double BASE_COMPLEXITY = 10.0;
    private static final double CONST_LEN_COMPLEXITY = 1.0;

    private final Type componentType;
    private final Integer constantLength;
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
        this(componentType, null, length);
    }

    private ArrayType(Type componentType, Integer constantLength, ParameterExpression wildcardLength) {
        notNull("componentType", componentType);
        if (constantLength == null && wildcardLength == null) {
            throw new NullPointerException("Length cannot be null");
        }
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
        if (constantLength != null) {
            return constantLength;
        } else {
            return -1;
        }
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
    public double getTypeComplexity() {
        return BASE_COMPLEXITY + componentType.getTypeComplexity() +
               (constantLength != null ? CONST_LEN_COMPLEXITY : 0.0);
    }

    @Override
    public double getAssignmentCost(Type t) {
        if (!isAssignableFrom(t)) {
            return Double.POSITIVE_INFINITY;
        } else if (t instanceof WildcardType) {
            return getTypeComplexity();
        } else {
            ArrayType other = (ArrayType) t;
            double componentCost = componentType.getAssignmentCost(other.componentType);
            double lenCost = ((constantLength != null && other.constantLength != null) ||
                              (wildcardLength != null && other.wildcardLength != null) ? 0.0
                                                                                       : CONST_LEN_COMPLEXITY);
            return componentCost + lenCost;
        }
    }

    @Override
    public boolean isAssignableFrom(Type t) {
        if (!(t instanceof ArrayType)) {
            // wildcards can be assigned to this type
            return t instanceof WildcardType;
        }

        ArrayType a = (ArrayType) t;
        if (componentType.isAssignableFrom(a.componentType)) {
            if (constantLength != null) {
                // both arrays must have the same size or the second must be a wildcard
                return a.wildcardLength != null || constantLength.equals(a.constantLength);
            } else {
                // since this type has a wildcard length, the other length doesn't matter
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isConcrete() {
        return constantLength != null && componentType.isConcrete();
    }

    @Override
    public Type getSharedType(Type t) {
        if (!(t instanceof ArrayType)) {
            // the conversion with a wildcard type is the array type unmodified, otherwise no other types are supported
            return t instanceof WildcardType ? this : null;
        }

        ArrayType other = (ArrayType) t;

        Type superComponentType = componentType.getSharedType(other.componentType);
        if (superComponentType == null) {
            // component type conversion does not exist so array type conversion is impossible
            return null;
        }

        if (constantLength != null) {
            if (other.wildcardLength != null || constantLength.equals(other.constantLength)) {
                // conversion is this array's length (either they have same length or we constrain the wildcard value)
                return new ArrayType(superComponentType, constantLength);
            } else {
                // length is invalid
                return null;
            }
        } else {
            // conversion is the other array's concrete length (if possible) or arbitrary choice of wildcard
            // lengths, so to keep things simple we just pick the caller's length
            if (other.constantLength == null) {
                return new ArrayType(superComponentType, wildcardLength);
            } else {
                return new ArrayType(superComponentType, other.constantLength);
            }
        }
    }

    @Override
    public boolean equals(Object t) {
        if (!(t instanceof ArrayType)) {
            return false;
        }
        ArrayType a = (ArrayType) t;
        if (a.componentType.equals(componentType)) {
            if (constantLength != null) {
                return constantLength.equals(a.constantLength);
            } else {
                return wildcardLength.equals(a.wildcardLength);
            }
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash += hash * 37 + componentType.hashCode();
        if (constantLength != null) {
            hash += hash * 37 + constantLength.hashCode();
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
