package com.lhkbob.fxsl.lang.type;

import com.lhkbob.fxsl.util.LogicalEquality;

/**
 * Types in FXSL
 * =============
 *
 * FXSL has six fundamental types, each with dedicated classes that implement this interface.
 * Where possible the type implementations enforce validity at the time of instantiation. This is
 * possible for all concrete type formulations but when wildcards and unions are involved it must be
 * deferred until the code tree has been processed. They are listed below:
 *
 * 1. Primitives ({@link PrimitiveType}): `int`, `float`, `bool`, `sampler1d`, `sampler2d`, and
 * `sampler3d`.
 * 2. Arrays ({@link ArrayType}): Fixed-length lists of instances of another type.
 * 3. Structs ({@link StructType}): Keyed maps with fields of varying types.
 * 4. Functions ({@link FunctionType}): Invokable routines that are high-level objects.
 * 5. Unions ({@link UnionType}): Collection of functions that supports limited ad-hoc
 * polymorphism.
 * 6. Wildcards ({@link MetaType}): Wildcards that are inferred later based on usage.
 *
 * See the {@link Types} static function library for utilities when working with types.
 *
 * FIXME rewrite this section to be about assignability and type unification
 * ## Assignability and shared types
 *
 * FXSL has no concept of type inheritance but it does have built in support for implicitly
 * converting one type to another. The nature of this conversion depends on the types involved.
 * Except for when wildcards and unions are involved conversions never cross the six class
 * boundaries. A value cannot be assigned to a variable definition or passed into a function
 * parameter if it is not assignable to the target type. A type is assignable if there exists a
 * straight-forward way in which the value could be converted that is so mundane it is performed
 * implicitly. The details are specific to each type and documented by each.
 *
 * Shared types represents the related goal of finding a new type that two initial types are both
 * assignable to. This is primarily used during array creation when it is necessary to find an array
 * component type that all elements can be assigned to.
 *
 * ## Concreteness
 *
 * When FXSL code is initially processed functions and expressions may contain references to
 * wildcard types or unions. The code is not concrete until all wildcard types have been inferred
 * and all unions have been removed by selecting the appropriate function to invoke based on the
 * concrete input arguments. Composite types such as arrays and structs are concrete unless their
 * encapsulated types are not concrete. There is a related notion of expression concreteness
 * described by {@link com.lhkbob.fxsl.lang.expr.Expression}.
 *
 * ## Implementation note
 *
 * Type subclasses should be immutable, provide logical implementations of equals() and
 * hashCode(), and will throw exceptions if `null` values are provided.
 *
 * @author Michael Ludwig
 */
@LogicalEquality(def =
    "Two types are equal if they represent the same category of FXSL type, all child "
        + "child types are equal, and all other type-specific information are equal.")
public interface Type {
  /**
   * The Type Visitor provides the visitor pattern for walking parsed type trees.
   *
   * @param <T>
   *     The type returned by the visitor, often a Type
   */
  interface Visitor<T> {
    T visitAliasType(AliasType t);

    T visitArrayType(ArrayType t);

    T visitFunctionType(FunctionType t);

    T visitMetaType(MetaType t);

    T visitParametricType(ParametricType t);

    T visitPrimitiveType(PrimitiveType t);

    T visitStructType(StructType t);

    T visitUnionType(UnionType t);
  }

  /**
   * Invoke the appropriate `visit` method of the visitor based on the concrete class type of this
   * FXSL type and return the result of that visitation.
   *
   * @param visitor
   *     The visitor to visit
   * @param <T>
   *     The return type of the visitor
   * @return The result of invoking the appropriate `visit` method
   *
   * @throws java.lang.NullPointerException
   *     if `visitor` is null
   */
  <T> T accept(Visitor<T> visitor);
}
