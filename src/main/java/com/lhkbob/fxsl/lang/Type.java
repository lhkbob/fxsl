package com.lhkbob.fxsl.lang;

import com.lhkbob.fxsl.util.LogicalEquality;

/**
 * Types in FXSL
 * =============
 *
 * FXSL has six fundamental types, each with dedicated classes that implement this interface. Where possible
 * the type implementations enforce validity at the time of instantiation. This is possible for all concrete
 * type formulations but when wildcards and unions are involved it must be deferred until the code tree has
 * been processed. They are listed below:
 *
 * 1. Primitives ({@link com.lhkbob.fxsl.lang.PrimitiveType}): `int`, `float`, `bool`, `sampler1d`,
 * `sampler2d`, and `sampler3d`.
 * 2. Arrays ({@link com.lhkbob.fxsl.lang.ArrayType}): Fixed-length lists of instances of another type.
 * 3. Structs ({@link com.lhkbob.fxsl.lang.StructType}): Keyed maps with fields of varying types.
 * 4. Functions ({@link com.lhkbob.fxsl.lang.FunctionType}): Invokable routines that are high-level objects.
 * 5. Unions ({@link com.lhkbob.fxsl.lang.UnionType}): Collection of functions that supports limited ad-hoc
 * polymorphism.
 * 6. Wildcards ({@link com.lhkbob.fxsl.lang.WildcardType}): Wildcards that are inferred later based on usage.
 *
 * ## Assignability and shared types
 *
 * FXSL has no concept of type inheritance but it does have built in support for implicitly converting one
 * type to another. The nature of this conversion depends on the types involved. Except for when wildcards and
 * unions are involved conversions never cross the six class boundaries. A value cannot be assigned to a
 * variable definition or passed into a function parameter if it is not assignable to the target type. A type
 * is assignable if there exists a straight-forward way in which the value could be converted that is so
 * mundane it is performed implicitly. The details are specific to each type and documented by each.
 *
 * Shared types represents the related goal of finding a new type that two initial types are both
 * assignable to. This is primarily used during array creation when it is necessary to find an array component
 * type that all elements can be assigned to.
 *
 * ## Concreteness
 *
 * When FXSL code is initially processed functions and expressions may contain references to wildcard types
 * or unions. The code is not concrete until all wildcard types have been inferred and all unions have been
 * removed by selecting the appropriate function to invoke based on the concrete input arguments. Composite
 * types such as arrays and structs are concrete unless their encapsulated types are not concrete. There is a
 * related notion of expression concreteness described by {@link com.lhkbob.fxsl.lang.Expression}.
 *
 * ### Implementation note
 *
 * Type subclasses should be immutable, provide logical implementations of equals() and hashCode(), and
 * will throw exceptions if `null` values are provided.
 *
 * @author Michael Ludwig
 */
@LogicalEquality
public interface Type {
    /**
     * Return whether or not `t` can be assigned to a variable or parameter of this type. This should
     * return true if the types are equal, or if there is a type-class specific means of implicitly converting
     * values in `t` to values of this type. Lastly, implementations should treat wildcard types as being
     * assignable; type inference is used to determine if constraints would make the assignment invalid.
     *
     * @param t The other type
     * @return True if `t` can be assigned to this type
     * @throws java.lang.NullPointerException if `t` is null
     */
    public boolean isAssignableFrom(Type t);

    /**
     * Get the cost of assigning the type `t` to this type.  If `t` equals this type then the cost must
     * return zero because no changes must be made to values of `t` to make it assignable.  Otherwise the
     * returned value is the net type complexity lost by the assignment based on type specific rules for how
     * the type is changed and any subtype's implementation of {@link #getTypeComplexity()}.
     *
     * Note that this cost has no bearing on runtime performance but is used to determine the best function
     * selection when using ad hoc polymorphism. If either type involved is a wildcard type, the assignment
     * cost should be the type complexity of the other type.
     *
     * If `isAssignableFrom(t)` returns false this should return {@link Double#POSITIVE_INFINITY}.
     *
     * @param t The type that is being assigned to this type
     * @return The cost of the assigning `t` to this type
     * @throws java.lang.NullPointerException if `t` is null
     */
    public double getAssignmentCost(Type t);

    /**
     * Get the type complexity of this type. The exact quantity is only meaningful when comparing between
     * assignable types otherwise the spaces are completely independent. Primitive types will have fixed
     * constant values. Compound types will have a based fixed complexity plus the type complexity of all
     * referenced types. The value must be greater than 0.
     *
     * @return The type complexity measure of this type
     */
    public double getTypeComplexity();

    /**
     * Return whether or not this type is concrete. A value of false indicates the type is or contains
     * references to wildcard or union types.
     *
     * @return True if this type is concrete
     */
    public boolean isConcrete();

    /**
     * Determine a type that both `this` and `t` are assignable to. If `isAssignableFrom` returns true when
     * invoked by either type, that type should be returned because it represents a type both are assignable
     * to. Similarly, if the types are logically equal it returns the type unmodified. However, specific type
     * classes have more complex scenarios where conversions are possible. If no conversion exists, either
     * because the type classes are incompatible or type-specific details cause the failure, then a `null`
     * value is returned.
     *
     * This function is commutative, namely `this.getSharedType(t)` should produce the same result as
     * `t.getSharedType(this)`. Since conversions only cross class boundaries in a limited number of
     * circumstances this requirement should not complicate implementations greatly.
     *
     * @param t The other type
     * @return A type that both types can be assigned to, or null if no such type exists
     * @throws java.lang.NullPointerException if `t` is null
     */
    public Type getSharedType(Type t);
}
