// FIXME figure out how to do markdown javadoc comments before this project gets too big
/**
 * <h1>FXSL Overview</h1>
 * <p>
 * FXSL is a functional domain-specific language for producing GLSL output. Theoretically its
 * compiler
 * can be extended to export other languages, but as Java only has good support for OpenGL, GLSL is
 * preferred. FXSL defines functions as a map from X -&gt; Y, so technically they are only allowed
 * one input and one output. However, syntactic sugar and currying allows multi-argument functions
 * to be
 * declared, e.g.: X,Y,Z -&gt; W is actually X -&gt; (Y -&gt; (Z -&gt; W)). In addition to functions
 * as
 * values, the type system allows for key-value maps, fixed length arrays, and primitive types.
 * Function
 * declarations can use wildcards and pattern matching, similar to languages like Haskell.
 * </p>
 * <p>
 * FIXME I think this needs to be fixed up and or changed, since I don't think that's how it's going
 * to
 * FIXME be done, but this is the paragraph that should explain final structure
 * A FXSL program may be split across multiple shader units, each of which is assigned a namespace.
 * The shader units are unified at compile time to produce the final GLSL shaders for each stage
 * that
 * was defined. A shader unit may be a file containing FXSL script, or it may be an embedded script
 * defined with Java annotations. After unification, it must produce a function representing each
 * stage of shader execution in GLSL. These functions have a specific, but templated definition that
 * must
 * be met. If it is not met, compiler errors are raised. Optional shader stages can be omitted and
 * are automatically filled in with an identity function.
 * </p>
 * <h1>Details</h1>
 * <h2>Type Definitions</h2>
 * <p>
 * Every type has a name assigned to it. This is done with the syntax:
 * <pre>
 *         type &lt;alias&gt; = &lt;typeDef&gt;
 *     </pre>
 * The type definition may be the name of another type, in which case there are now multiple
 * aliases
 * representing the same type, or it can be the first definition of the type, as described below.
 * When the type is first defined, the name it is assigned is its canonical name. The examples
 * provided
 * below use this syntax to assign an alias. More complex type definitions allow other types to be
 * defined implicitly inside the definition. It is permissible to refer to a type by canonical
 * name,
 * an alias, or the full syntax of its definition.
 * </p>
 * <p>
 * There are four type classes used by FXSL: primitives, structures, arrays, and functions.
 * All state is immutable. Array length is part of array type's definition. It is not possible to
 * define new primitive types, but custom structures, arrays, and functions may be defined.
 * Defining
 * a structure type specifies the field names' and types'. Defining an array type specifies the
 * component
 * type and length of the array. Defining a function specifies the input argument type and return
 * type.
 * Parameterized type labels may be used when specifying a type in the function definition. Type
 * compatibility determines if a value can be passed into a function or assigned when a variable is
 * declared. Types can only be compatible with types in the same class. The class-specific type
 * compatibility rules will be described in each type class section. However, common notation is
 * used
 * to describe the direction of compatibility:
 * <ul>
 * <li>{@code A &lt; B} denotes A is a subtype of B. Anywhere B is required, A can be
 * provided.</li>
 * <li>{@code A &gt; B} denotes A is a supertype of B. B &lt; A is true.</li>
 * <li>{@code A &lt;&gt; B} denotes that A and B have no shared hierarchy and are not
 * compatible.</li>
 * </ul>
 * </p>
 *
 * <h3>Primitives</h3>
 * <p>
 * Primitive types are built into the language and cannot be defined in terms of other types or
 * language elements. It is an error to use a primitive type name as an alias for some other type,
 * although it is allowed to assign an alias to a primitive type. The standard library defines
 * operators
 * and functions that provide the most common operations needed, such as addition, multiplication,
 * etc.
 * The following primitives are available, listed by their canonical names and a short description:
 * <ul>
 * <li><code>float</code> - 32-bit floating point real number.</li>
 * <li><code>int</code> - 32-bit signed integer.</li>
 * <li><code>uint</code> - 32-bit unsigned integer.</li>
 * <li><code>bool</code> - true/false boolean value.</li>
 * <li><code>sampler1d</code> - One dimensional texture.</li>
 * <li><code>sampler2d</code> - Two dimensional texture.</li>
 * <li><code>sampler3d</code> - Three dimensional texture.</li>
 * <li><code>samplerCube</code> - Cube map texture.</li>
 * </ul>
 * The following type relationships exist amongst the primitives. If not specified in the list, the
 * types
 * are not compatible:
 * <ul>
 * <li>{@code int &lt; float}</li>
 * <li>{@code uint &lt; float}</li>
 * <li>{@code int} and {@code uint} are weird FIXME</li>
 * </ul>
 * </p>
 *
 * <h3>Structures</h3>
 * <p>
 * Structures, or <i>structs</i>, are key-value maps. The key names and associated types define
 * type
 * equality and compatibility. The order in which keys are declared is not important. Each key's
 * type
 * can be different. Two struct types are considered equal if their key sets are equal and the
 * types
 * associated with each pair of matching keys is equal. The subtype relationship is similarly
 * defined.
 * {@code A &lt; B} is true if A's key set is a subset of B's key set, and {@code A[key] &lt;
 * B[key]}
 * is true for every key in A. This also illustrates the syntax to access a value from a struct:
 * square brackets are used around the key token. So to access the key 'foo' from a struct instance
 * 'var',
 * you would write <code>var[foo]</code>. It is a semantic error to access an undefined key.
 * Struct declarations cannot be self-referential and cyclical definitions are not allowed.
 * Multiple
 * structured type declaration samples are shown below:
 * <pre>
 *         {&lt;param&gt;: &lt;type&gt;}
 *         type &lt;name&gt; = {&lt;param&gt;: &lt;type&gt;}
 *         type &lt;name&gt; = {&lt;param1&gt;: &lt;type1&gt;, &lt;param2&gt;: &lt;type2&gt;}
 *     </pre>
 * 'param' is the name of the parameter used to access the element of the type. Each type is either
 * a valid unnamed type definition or an already defined type name. 'name' becomes the canonical
 * name for the type.
 * </p>
 *
 * <h3>Arrays</h3>
 * <p>
 * Arrays are ordered containers of a fixed length and all elements have the same component type.
 * An array type specifies the length of all of its instances. Thus an array type holding floats of
 * length
 * three is a different type than one holding four floats. Two array types are considered equal if
 * their component types have type equality, and the lengths are the same. Unlike structures, array
 * types do not have subtyping. This is because arrays are often used to represent mathematical
 * vectors
 * and matrices where the semantics of a 4-dimensional vector a different than a 3-dimensional
 * vector.
 * To access an element from the array, square brackets are used around an integer index. Indices
 * start
 * at 0, so to access the first element you would write <code>var[0]</code>. Expressions that
 * evaluate
 * to 'int' or 'uint' may also be used to access an element. The array type declaration examples
 * are shown below:
 * <pre>
 *         &lt;type&gt;[&lt;length&gt;]
 *         type &lt;name&gt; = &lt;type&gt;[&lt;length&gt;]
 *         type &lt;name&gt; = &lt;type&gt;[&lt;length1&gt;][&lt;length2&gt;]
 *     </pre>
 * Like before 'name' is the canonical name and 'type' must be a type definition or existing type
 * name.
 * 'length' is the fixed length of the array and must be a constant 'uint' or 'int'. The second
 * entry
 * shows how an array of arrays can be defined.
 * </p>
 *
 * <h3>Functions</h3>
 * <p>
 * Functions are the fourth type available in FXSL. A function type declaration only specifies the
 * types of its input parameters and its return type. Fundamentally a function only takes one
 * parameter
 * and outputs one value. However syntax sugar is provided to define multi-arg functions that curry
 * the arguments along as in Haskell. A function type declaration serves as a template or
 * interface,
 * actual function instances or definitions provide the expression that is evaluated (of which
 * there
 * can be multiple). Defining and invoking functions will be discussed more in the Values section.
 * The syntax for a function type declaration is shown below:
 * <pre>
 *         &lt;type&gt; -&gt; &lt;type&gt;
 *         type &lt;name&gt; = &lt;type1&gt;, &lt;type2&gt; -&gt; &lt;type3&gt;
 *         type &lt;name&gt; = &lt;type1&gt; -&gt; &lt;type2&gt; -&gt; &lt;type3&gt;
 *     </pre>
 * The second and third examples represent the equivalent functional signature, but the first
 * shows the syntactic sugar provided for multi-argument functions.  The '-&gt;' notation is right
 * associative, so to declare a function parameter, you would need to surround it in parentheses.
 * </p>
 * <p>
 * It is also possible to declare a special type of function, an infix function. An infix function
 * must have two arguments. An infix type declaration is identical to a regular function except
 * that
 * the keyword {@code infix} comes before the parameter list. Infix functions are invoked in a
 * different
 * manner from regular functions.
 * </p>
 * <p>
 * When declaring the parameter types and return type of a function, the types can contain
 * wildcards
 * and patterns to enable more flexible function signatures. A wildcard type is signified by an
 * underscore
 * ('_') starting its name. The wildcard names are assigned actual types when the function is
 * invoked,
 * based on the input arguments. Every instance of a wildcard with the same name refers to the same
 * type;
 * if the input arguments do not satisfy those constraints that function cannot invoked. Named
 * wildcards
 * may be embedded within other type declarations of the function signature to have more control
 * over
 * the input or output arguments. In addition to named wildcards, the length of an array type can
 * be
 * declared with a variety of wildcard options. Using a named wildcard in place of the constant
 * integer
 * creates a constant expression usable throughout a function definition. A named length wildcard
 * can
 * also have an expression of the form '>= value' appended to it to add a constraint. The minimum
 * value
 * must be a constant integer expression (actual constant or another named wildcard). When an
 * actual
 * constant is used for an array length a '+' can be appended. This is shorthand for {@code _L >=
 * value}
 * when the actual length is not needed in the function definition. FIXME this is old
 * </p>
 * <p>
 * Function type compatibility is more complex than the other types. Function types are equal if
 * the
 * functions have the same parameter type and same return type. Wildcards must have the same
 * constraints.
 * The actual wildcard labels do not affect equality as long as labeling is consistent between the
 * two
 * functions. A function is a subtype of another function if its parameter type is a super type of
 * the
 * other's parameter type and its return type is a subtype of the other's return type (the standard
 * widening of the input and narrowing of the output). All wildcard declarations are merged and if
 * it
 * produces an impossible instantiation then it is not a subtype.
 * </p>
 *
 * <h2>Values</h2>
 * <p>
 * Primitive types are the simplest and are defined by writing the literal numeric value:
 * <ul>
 * <li><code>float</code> - 1.0, 0.4, 1e4, -2.34e-3, etc.</li>
 * <li><code>int</code> - 1, 2, 456, -234, etc.</li>
 * <li><code>uint</code> - 1u, 2u, 456u, etc.</li>
 * <li><code>bool</code> - true or false.</li>
 * </ul>
 * In addition, there are built in functions to construct new primitive values based on the
 * mathematical
 * operations: add, subtract, multiply, divide, mod.
 * </p>
 * <p>
 * Although the sampler types are also primitive, there is no way of writing a literal numeric
 * value.
 * There is no way to declare a sampler's value in FXSL shader code. Instead it is necessary to
 * take the value from an external system, such as OpenGL. These aspects will be discussed later.
 * </p>
 * <p>
 * Struct definitions are constructed similarly to how their types are defined. Instead of
 * specifying
 * key and type pairs within curly braces, you provide the keys and corresponding expressions that
 * evaluate to the required type. The expression can be another instance constructor, variable
 * reference,
 * function call, or constant. An example is shown
 * <pre>
 *         {foo: 4.5, bar: 12, oof: false}
 *     </pre>
 * This produces an instance of the type <code>{foo: float, bar: int, oof: bool}</code>. This type
 * is
 * implicitly created if it wasn't already named before.
 * </p>
 * <p>
 * Array instances can be fully specified as follows:
 * <pre>
 *         [4.0, 3.0, 2.0]
 *     </pre>
 * This produces an instance of <code>float[3]</code>. The constructor <code>[3.0, 4, false]</code>
 * would
 * fail because the elements do not have the same type. The element values do not have to be
 * constant
 * literals, but can also be expressions just like with structs. In addition to this explicit
 * construction
 * you can use the iteration constructor. The iteration constructor takes a length and a function
 * that
 * maps from index to value and returns an array that contains the results of the function for each
 * index.
 * The iteration constructor is defined as <code>iter = (uint -&gt; _T), l:uint -&gt; _T[l]</code>.
 * An expressions of each element must be subtypes of the array's type. If the array's type is
 * being
 * implicitly declared then the highest type present is used as the array's type. Thus a
 * declaration
 * of {@code [4.0, 3]} produces a {@code float[2]} and the second argument is lifted to a float.
 * </p>
 * <p>
 * Function definitions
 * </p>
 */
package com.lhkbob.fxsl;
