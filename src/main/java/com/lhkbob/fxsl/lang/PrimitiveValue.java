package com.lhkbob.fxsl.lang;

/**
 * Primitive Values
 * ================
 *
 * Primitive value expressions are constructors for instances of {@link com.lhkbob.fxsl.lang.PrimitiveType}.
 * FXSL does not allow the sampler primitive types from being constructed within FXSL. As such it is not
 * possible to have a primitive value constructor for values of that class of types. With this constraint,
 * PrimitiveValue represents float, int, and bool constants defined within FXSL code.
 *
 * A primitive value is always concrete.
 *
 * @author Michael Ludwig
 */
public class PrimitiveValue implements Expression {
    private final Object value;
    private final transient PrimitiveType type;

    /**
     * Create a new primitive value that will have the FLOAT primitive type and has the given `value`.
     *
     * @param value The float value this value represents
     */
    public PrimitiveValue(float value) {
        this.value = value;
        type = PrimitiveType.FLOAT;
    }

    /**
     * Create a new primitive value that will have the INT primitive type and has the given `value`.
     *
     * @param value The int value this value represents
     */
    public PrimitiveValue(int value) {
        this.value = value;
        type = PrimitiveType.INT;
    }

    /**
     * Create a new primitive value that will have the BOOLEAN primitive type and has the given `value`.
     *
     * @param value The boolean value this value will represent
     */
    public PrimitiveValue(boolean value) {
        this.value = value;
        type = PrimitiveType.BOOL;
    }

    /**
     * Get the concrete value of this primitive. This will be an instance of {@link java.lang.Integer} if
     * the expression's type is INT, it will be an instance of {@link java.lang.Float} if the type is FLOAT,
     * or it will be an instance of {@link java.lang.Boolean} if the type is BOOLEAN.
     *
     * The other primitive types cannot be represented by instances of this class, so there is no need to
     * worry about how to represent them in this method.
     *
     * @return The actual value this value is constructed with
     */
    public Object getValue() {
        return value;
    }

    @Override
    public PrimitiveType getType() {
        return type;
    }

    @Override
    public boolean isConcrete() {
        // always concrete
        return true;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitPrimitive(this);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PrimitiveValue)) {
            return false;
        }
        PrimitiveValue v = (PrimitiveValue) o;
        return v.value.equals(value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
