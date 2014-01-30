/**
 * <h1>FXSL Overview</h1>
 * <p>
 *     FXSL is a functional domain-specific language for producing GLSL output. Theoretically its compiler
 *     can be extended to export other languages, but as Java only has good support for OpenGL, GLSL is
 *     preferred. FXSL defines functions as a map from X -&gt; Y, so technically they are only allowed
 *     one input and one output. However, syntactic sugar and currying allows multi-argument functions to be
 *     declared, e.g.: X,Y,Z -&gt; W is actually X -&gt; (Y -&gt; (Z -&gt; W)). In addition to functions as
 *     values, the type system allows for key-value maps, fixed length arrays, and primitive types. Function
 *     declarations can use wildcards and pattern matching, similar to languages like Haskell.
 * </p>
 * <p>
 *     FIXME I think this needs to be fixed up and or changed, since I don't think that's how it's going to
 *     FIXME be done, but this is the paragraph that should explain final structure
 *     A FXSL program may be split across multiple shader units, each of which is assigned a namespace.
 *     The shader units are unified at compile time to produce the final GLSL shaders for each stage that
 *     was defined. A shader unit may be a file containing FXSL script, or it may be an embedded script
 *     defined with Java annotations. After unification, it must produce a function representing each
 *     stage of shader execution in GLSL. These functions have a specific, but templated definition that must
 *     be met. If it is not met, compiler errors are raised. Optional shader stages can be omitted and
 *     are automatically filled in with an identity function.
 * </p>
 * <h1>Details</h1>
 * <h2>Type Definitions</h2>
 * <p>
 *     Every type has a name assigned to it. This is done with the syntax:
 *     <pre>
 *         type &lt;alias&gt; = &lt;typeDef&gt;
 *     </pre>
 *     The type definition may be the name of another type, in which case there are now multiple aliases
 *     representing the same type, or it can be the first definition of the type, as described below.
 *     When the type is first defined, the name it is assigned is its canonical name. In the examples listed
 *     below, they include the syntax to declare the alias. For more complex types, other types can be
 *     defined implicitly inside the outer definition and do not receive a name. The syntax used for
 *     inner types is just the block to the right of <code>type &lt;alias&gt; =</code>.
 * </p>
 * <p>
 *     There are four type representations used by FXSL. The first and most foundational are the primitive
 *     types. Primitive types are built into the language and cannot be defined in terms of other types or
 *     language elements. The following primitive types are available, listed with their canonical names:
 *     <ul>
 *         <li><code>float</code> - 32-bit floating point real number.</li>
 *         <li><code>int</code> - 32-bit signed integer.</li>
 *         <li><code>uint</code> - 32-bit unsigned integer.</li>
 *         <li><code>bool</code> - true/false boolean value.</li>
 *         <li><code>sampler1d</code> - One dimensional texture.</li>
 *         <li><code>sampler2d</code> - Two dimensional texture.</li>
 *         <li><code>sampler3d</code> - Three dimensional texture.</li>
 *         <li><code>samplerCube</code> - Cube map texture.</li>
 *     </ul>
 * </p>
 * <p>
 *     The second type representation is a structured type, or <i>struct</i>, which is a key-value map. Two
 *     structs are compatible if they have the same keys and the types for each key are compatible. The
 *     order of the keys does not affect compatibility. To access a value from a struct, square brackets are
 *     used around the key token. So to access the key 'foo' from a struct instance 'var', you would write
 *     <code>var[foo]</code>. It is an error to access an undefined key. Struct declarations cannot be
 *     self-referential and cyclical definitions are not allowed. Multiple structured type templates
 *     are shown below:
 *     <pre>
 *         {&lt;param&gt;: &lt;type&gt;}
 *         type &lt;name&gt; = {&lt;param&gt;: &lt;type&gt;}
 *         type &lt;name&gt; = {&lt;param1&gt;: &lt;type1&gt;, &lt;param2&gt;: &lt;type2&gt;}
 *     </pre>
 *     'param' is the name of the parameter used to access the element of the type. Each type is either
 *     a valid unnamed type definition or an already defined type name. 'name' becomes the canonical
 *     name for the type.
 * </p>
 * <p>
 *     The third type representation is a fixed-length array. Two arrays are compatible if they have the
 *     same element type and the same length. The length of the array is part of the type declaration. To
 *     access an element from the array, square brackets are used around an integer index. Indices start
 *     at 0, so to access the first element you would write <code>var[0]</code>. Expressions that evaluate
 *     to 'int' or 'uint' may also be used to access an element. All elements of an array have the same
 *     type. The syntax is shown below:
 *     <pre>
 *         &lt;type&gt;[&lt;length&gt;]
 *         type &lt;name&gt; = &lt;type&gt;[&lt;length&gt;]
 *         type &lt;name&gt; = &lt;type&gt;[&lt;length1&gt;][&lt;length2&gt;]
 *     </pre>
 *     Like before 'name' is the canonical name and 'type' must be a type definition or existing type name.
 *     'length' is the fixed length of the array and must be a constant 'uint' or 'int'. The second entry
 *     shows how an array of arrays can be defined.
 * </p>
 * <p>
 *     The fourth type representation is a function. You can declare a function as well as define it.
 *     Declaring a function signature merely specifies the parameter and return type signatures without
 *     specifying its implementation and does not define a function with that signature. Thus it is
 *     permissible for multiple functions to share the same declaration; the declaration can be thought of
 *     as a template. The syntax is shown below:
 *     <pre>
 *         &lt;type&gt; -&gt; &lt;type&gt;
 *         type &lt;name&gt; = &lt;type1&gt;, &lt;type2&gt; -&gt; &lt;type3&gt;
 *         type &lt;name&gt; = &lt;type1&gt; -&gt; &lt;type2&gt; -&gt; &lt;type3&gt;
 *     </pre>
 *     The second and third examples represent the equivalent functional signature, but the first
 *     shows the syntactic sugar provided for multi-argument functions.  The '-&gt;' notation is right
 *     associative, so to have a function parameter, you would need to surround it in parentheses.
 * </p>
 *
 * <h2>Value Instances</h2>
 * <p>
 *     Value instances are immutable. They are constructed in different manners depending on their type.
 *     Primitive types are the simplest and are constructed by writing the literal numeric value:
 *     <ul>
 *         <li><code>float</code> - 1.0, 0.4, 1e4, -2.34e-3, etc.</li>
 *         <li><code>int</code> - 1, 2, 456, -234, etc.</li>
 *         <li><code>uint</code> - 1u, 2u, 456u, etc.</li>
 *         <li><code>bool</code> - true or false.</li>
 *     </ul>
 *     In addition, there are built in functions to construct new primitive values based on the mathematical
 *     operations: add, subtract, multiply, divide, mod.
 * </p>
 * <p>
 *     Although the sampler types are also primitive, there is no way of writing a literal numeric value.
 *     There is no way to declare a sampler's value in FXSL shader code. Instead it is necessary to
 *     take the value from an external system, such as OpenGL. These aspects will be discussed later.
 * </p>
 * <p>
 *     Struct instances are constructed similarly to how their types are defined. Instead of specifying
 *     key and type pairs within curly braces, you provide the keys and corresponding expressions that
 *     evaluate to the required type. The expression can be another instance constructor, variable reference,
 *     function call, or constant. An example is shown
 *     <pre>
 *         {foo: 4.5, bar: 12, oof: false}
 *     </pre>
 *     This produces an instance of the type <code>{foo: float, bar: int, oof: bool}</code>. This type is
 *     implicitly created if it wasn't already named before.
 * </p>
 * <p>
 *     Array instances can be fully specified, as follows:
 *     <pre>
 *         [4.0, 3.0, 2.0]
 *     </pre>
 *     This produces an instance of <code>float[3]</code>. The constructor <code>[3.0, 4, false]</code> would
 *     fail because the elements do not have the same type. The element values do not have to be constant
 *     literals, but can also be expressions just like with structs. In addition to this explicit construction
 *     you can use the iteration constructor. The iteration constructor takes a length and a function that
 *     maps from index to value and returns an array that contains the results of the function for each index.
 *     The iteration constructor is defined as <code>iter = (uint -&gt; _T), l:uint -&gt; _T[l]</code>.
 * </p>
 */
package com.lhkbob.fxsl;
