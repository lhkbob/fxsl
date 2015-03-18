package com.lhkbob.fxsl.lang.expr;

import com.lhkbob.fxsl.lang.Scope;
import com.lhkbob.fxsl.lang.type.Type;
import com.lhkbob.fxsl.util.LogicalEquality;

/**
 * Expressions
 * ===========
 *
 * Expressions are the fundamental computational unit within FXSL. As a functional language most complex
 * operations can be represented as a complex expression that is the composition of multiple simpler concepts.
 * There are many different kinds of expressions, many of which correspond or are determined by the various
 * {@link com.lhkbob.fxsl.lang.type.Type types} in FXSL. Broadly speaking expressions can be classified into the
 * following categories:
 *
 * 1. Constructors
 * 2. Selectors
 * 3. References
 * 4. Function invocations
 *
 * ## Expression classes
 *
 * ### Constructors
 *
 * Constructors are explicitly constructed values in FXSL. Each complex type (arrays, structs,  functions,
 * and function unions) have constructors that syntactically resemble the way their corresponding types are
 * declared. The primary difference is they reference child expressions instead of child types. A constructor
 * implicitly defines its type based on the type of constructor and the types of any child expressions.
 *
 * ### Selectors
 *
 * For arrays and struct values it is possible to select the values of their child expressions. For arrays
 * this is the index selector, which must be an integer expression starting at 0 and less than the length of
 * the array type. For structs the selector is the name of the field that is returned. The type of a selector
 * expression is determined either by the component type of the array or the field's type as declared in the
 * struct.
 *
 * ### References
 *
 * Beyond expressions, a compilation unit allows you to define multiple expressions or fragments and assign
 * names to them to refer to their values more easily in subsequent parts of the program. A reference
 * expression is simply one that evaluates to one of these other defined fragments. A reference expression may
 * also represent a function parameter variable name or the wildcard length of an array type. The type of a
 * reference is equal to the type of expression which it references.
 *
 * References are not concrete expressions. A fragment can reference a value that has not yet been declared
 * sequentially, so long as it is defined within the scope by the time the scope has been closed. FXSL uses
 * lazy evaluation to achieve this. This can be used to implement recursion when invoking a function
 * that is referenced by name.
 *
 * ### Function invocations
 *
 * Function invocations effectively inline the function's body expression and replacing all of its parameter
 * expressions with the list of expressions the function is invoked with. If the value of the expression being
 * invoked is a function union ({@link com.lhkbob.fxsl.lang.type.UnionType}), the actual function is selected to
 * minimize the conversion cost of the parameters. If the provided parameter values cannot be assigned to the
 * parameter expressions of the function a compilation error is raised.
 *
 * A function can be invoked with fewer than required arguments. This curries the function and instead of
 * invoking the actual function body it returns a new function value that has a parameter list consisting of
 * the remaining parameters that must be specified.
 *
 * ### Implementation note
 *
 * Expression subclasses should be immutable, provide logical implementations of equals() and hashCode(), and
 * will throw exceptions if `null` values are provided.
 *
 * @author Michael Ludwig
 */
@LogicalEquality(def = "Two expressions are equal if they are the same category of expression, have equal " +
                       "child expressions (respecting any order defined by the expression), and are defined" +
                       " within the same scope")
public interface Expression {
    /**
     * The Expression Visitor provides the visitor pattern for walking parsed expression trees.
     *
     * @param <T> The class type that a visitor returns (generally an Expression)
     */
    public static interface Visitor<T> {
        public T visitArrayAccess(ArrayAccess access);

        public T visitArray(ArrayValue value);

        public T visitFunctionCall(FunctionCall function);

        public T visitFunction(FunctionValue function);

        public T visitParameter(ParameterExpression param);

        public T visitPrimitive(PrimitiveValue primitive);

        public T visitFieldAccess(StructFieldAccess access);

        public T visitStruct(StructValue struct);

        public T visitUnion(UnionValue union);

        public T visitVariable(VariableExpression var);

        public T visitNativeExpression(NativeExpression expr);
    }

    /**
     * Get the type of this expression, which may not be concrete. However, implementations should return
     * the most concrete type possible given available information. This cannot return null.
     *
     * @return The type this expression evaluates to
     */
    public Type getType();

    /**
     * @return The scope that this expression was defined in
     */
    public Scope getScope();

    /**
     * Invoke the appropriate `visit` method of the visitor based on the concrete class type of this
     * expression and return the result of that visitation.
     *
     * @param visitor The visitor to visit
     * @param <T>     The return type of the visitor
     * @return The result of invoking the appropriate `visit` method
     * @throws java.lang.NullPointerException if `visitor` is null
     */
    public <T> T accept(Visitor<T> visitor);
}
