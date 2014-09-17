package com.lhkbob.fxsl.lang;

import static com.lhkbob.fxsl.util.Preconditions.notNull;

/**
 * Native Expressions
 * ==================
 *
 * Native expressions are special purpose expressions created by the compiler and parser to represent the
 * base functionality of FXSL that cannot be itself be written in FXSL. Examples of this are the actual math
 * operations provided for primitive types, or creating a functional union.
 *
 * Native expressions are always concrete.
 *
 * @author Michael Ludwig
 */
public class NativeExpression implements Expression {
    private final Type type;

    /**
     * Create a new native expression that results in a value of the given `type`.
     *
     * @param type The type of the native expression
     * @throws java.lang.NullPointerException if `type` is null
     */
    public NativeExpression(Type type) {
        notNull("type", type);
        this.type = type;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public boolean isConcrete() {
        return true;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitNativeExpression(this);
    }

    // Although these are the same definitions in Object, we implement them here to bypass any
    // annotation validation performed based on @LogicalEquality

    @Override
    public boolean equals(Object o) {
        return o == this;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }
}
