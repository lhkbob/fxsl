package com.lhkbob.fxsl.lang;

import static com.lhkbob.fxsl.util.Preconditions.notNull;

/**
 * Array Access
 * ============
 *
 * An array access is an expression that takes an expression evaluating to an array value and an expression
 * evaluating to an integer index and returns the element in the array value at the index. Either of these two
 * expressions may be wildcards but doing so imposes the constraint that the wildcard instantiate to an array
 * or integer respectively.
 *
 * An array access is concrete if and only if the dependent array expression and index expression are
 * concrete.
 *
 * @author Michael Ludwig
 */
public class ArrayAccess implements Expression {
    /**
     * The dependent access label used if the array expression being accessed is a wildcard type and the
     * component array type must be generated.
     */
    public static final String DEPENDENT_WILDCARD_LABEL = "array";

    private final Expression array;
    private final Expression index;

    private final Type componentType;

    /**
     * Create a new array access expression that accesses `array` at the given `index`.
     *
     * @param array The expression evaluating to an array value
     * @param index The expression evaluating to the integer index to access
     * @throws java.lang.IllegalArgumentException if `array` is not an array or wildcard, or if `index` is not
     *                                            an int or wildcard
     * @throws java.lang.NullPointerException     if `array` or `index` are null
     */
    public ArrayAccess(Expression array, Expression index) {
        notNull("array", array);
        notNull("index", index);

        if (!index.getType().equals(PrimitiveType.INT) && !(index.getType() instanceof WildcardType)) {
            throw new IllegalArgumentException("Array index must evaluate to an int or wildcard, not " +
                                               index.getType());
        }

        if (array.getType() instanceof ArrayType) {
            componentType = ((ArrayType) array.getType()).getComponentType();
        } else if (array.getType() instanceof WildcardType) {
            // the array being accessed is a wildcard, so the returned type is a new wildcard within the same scope
            componentType = ((WildcardType) array.getType()).createDependentType(DEPENDENT_WILDCARD_LABEL);
        } else {
            throw new IllegalArgumentException("Array access must operate on arrays, not " + array.getType());
        }

        this.array = array;
        this.index = index;
    }

    /**
     * Get the expression being accessed as an array. Although possibly a complex expression, it must evaluate
     * to an array value or a wildcard that can be instantiated to an array type.
     *
     * @return The array being accessed
     */
    public Expression getArray() {
        return array;
    }

    /**
     * Get the expression that evaluates to the array index being accessed. Array indices are positive
     * integers that start at 0. Although possibly a complex expression, it must evaluate to a primitive
     * integer or a  wildcard that can be instantiated to an int.
     *
     * @return The array index
     */
    public Expression getIndex() {
        return index;
    }

    @Override
    public Type getType() {
        return componentType;
    }

    @Override
    public boolean isConcrete() {
        return array.isConcrete() && index.isConcrete();
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitArrayAccess(this);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ArrayAccess)) {
            return false;
        }
        ArrayAccess a = (ArrayAccess) o;
        return a.array.equals(array) && a.index.equals(index);
    }

    @Override
    public int hashCode() {
        return array.hashCode() ^ index.hashCode();
    }
}
