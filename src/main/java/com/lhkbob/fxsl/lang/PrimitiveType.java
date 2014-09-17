package com.lhkbob.fxsl.lang;

import com.lhkbob.fxsl.util.Immutable;

/**
 * Primitive Types
 * ===============
 *
 * FXSL has several primitive types that are fundamental and cannot be decomposed further. Because of their
 * simplicity they are represented as an enumeration instead of a full class. The sampler types could
 * technically be represented as arrays of the other primitive types except that their dimensions and
 * operating semantics are so specialized it makes sense to treat them as unique types. Sampler arrays,
 * supported in newer versions of OpenGL, are represented as arrays of the base samplers.
 *
 * ## Assignability and shared types
 *
 * In almost all cases, primitive types are only assignable to variables and parameters of the exact same
 * type. The exception to this is that integer primitives can be assigned to float types and can be converted
 * to floats. It does *not* go the other direction. This is consistent behavior with other languages like
 * Java.
 *
 * A primitive type can be assigned to a wildcard, but it adds the constraint that the wildcard be resolved
 * to the primitive type. If this is not possible then a compilation error results. Similarly, the shared type
 * between a primitive type and a wildcard type is the primitive type.
 *
 * ## Concreteness
 *
 * All primitive types are concrete.
 *
 * @author Michael Ludwig
 */
@Immutable
public enum PrimitiveType implements Type {
    /**
     * `BOOL` represents a boolean true or false value. By default the `bool` type name refers to this
     * primitive type. FXSL supports all of the common boolean operators. Although all operators are
     * represented as functions, the FXSL compiler adds short-circuit evaluation for the generally assumed
     * behavior. Boolean values are declared in FXSL using the `true` or `false` keywords.
     */
    BOOL(0.5),
    /**
     * `INT` represents a 32-bit signed integer. By default the `int` type name in FXSL refers to this
     * primitive type. Values of this type are automatically converted to the `FLOAT` type if necessary.
     * The syntax to write a integer primitive is equivalent to that in Java or C.
     */
    INT(1.0),
    /**
     * `FLOAT` represents a 32-bit floating point number. By default the `float` type name in FXSL refers to
     * this primitive type. Most graphics cards follow the IEEE standard. The syntax to write a float is
     * equivalent to that in Java or C for their `double` types (i.e. no trailing 'f'). Integer values are
     * automatically converted to floats, making it unnecessary to add a decimal for simple values.
     */
    FLOAT(2.0) {
        @Override
        public boolean isAssignableFrom(Type t) {
            // int's can be converted to floats
            return super.isAssignableFrom(t) || t == INT;
        }
    },
    /**
     * `SAMPLER1D` refers to the `sampler1d` FXSL data type, which represents a one dimensional texture in
     * OpenGL. A sampler value cannot be instantiated directly in FXSL code but must be constructed through
     * the usual OpenGL process and then linked using uniforms.
     */
    SAMPLER1D(3.0),
    /**
     * `SAMPLER2D` refers to the `sampler2d` FXSL data type, which represents a two dimensional texture in
     * OpenGL. A sampler value cannot be instantiated directly in FXSL code but must be constructed through
     * the usual OpenGL process and then linked using uniforms.
     */
    SAMPLER2D(3.0),
    /**
     * `SAMPLER3D` refers to the `sampler3d` FXSL data type, which represents a three dimensional texture in
     * OpenGL. A sampler value cannot be instantiated directly in FXSL code but must be constructed through
     * the usual OpenGL process and then linked using uniforms.
     */
    SAMPLER3D(3.0),
    /**
     * `SAMPLERCUBE` refers to the `samplerCube` FXSL data type, which represents a cubemap texture in
     * OpenGL. A sampler value cannot be instantiated directly in FXSL code but must be constructed through
     * the usual OpenGL process and then linked using uniforms.
     */
    SAMPLERCUBE(3.0);

    private final double complexity;

    private PrimitiveType(double complexity) {
        this.complexity = complexity;
    }

    @Override
    public double getTypeComplexity() {
        return complexity;
    }

    @Override
    public double getAssignmentCost(Type t) {
        if (!isAssignableFrom(t)) {
            return Double.POSITIVE_INFINITY;
        } else if (t instanceof WildcardType) {
            return getTypeComplexity();
        } else {
            return getTypeComplexity() - t.getTypeComplexity();
        }
    }

    @Override
    public boolean isAssignableFrom(Type t) {
        return equals(t) || t instanceof WildcardType;
    }

    @Override
    public boolean isConcrete() {
        return true;
    }

    @Override
    public Type getSharedType(Type t) {
        if (t instanceof WildcardType) {
            // conversion with wildcard type is the primitive type unmodified
            return this;
        } else if (t instanceof PrimitiveType) {
            if (isAssignableFrom(t)) {
                // types are equal, or we upgrade t from int to float (this)
                return this;
            } else if (t.isAssignableFrom(this)) {
                // we upgrade this (int) to t (float)
                return t;
            } else {
                // no primitive conversion available
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
