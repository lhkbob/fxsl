package com.lhkbob.fxsl.lang.type;

import com.lhkbob.fxsl.util.Immutable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.lhkbob.fxsl.util.Preconditions.notNull;
import static com.lhkbob.fxsl.util.Preconditions.validCollection;

/**
 * Function Types
 * ==============
 *
 * FXSL is a functional language, and as such functions can be typed and referenced just like any other
 * value. Like other languages a function consists of a list of parameters and an invoked body that may use
 * those parameters to return some new value. A function type describes the ordered types within the parameter
 * list and the return type of the function when it is invoked.
 *
 * Functions may be curried, making it possible to invoke a function with fewer than the total arguments.
 * When this occurs a new function value is returned that takes the remaining parameters as its input and
 * returns the original return type.  Once the full parameter list is completed the original function body is
 * be invoked.
 *
 * FXSL has the related concept of a function union, described in more detail in {@link
 * UnionType}. Function unions are a way to support ad hoc polymorphism that allows
 * multiple function definitions to be referenced by the same symbol.
 *
 * @author Michael Ludwig
 */
@Immutable
public class FunctionType implements Type {
    private final List<Type> parameters;
    private final Type returnType;

    /**
     * Create a new function type that takes the given parameter types (`parameters`) and will return the
     * type specified by `returnType`. The parameter list is copied so modifications to `parameters` will not
     * affect the created type.
     *
     * @param parameters The parameter list
     * @param returnType The return type
     * @throws java.lang.IllegalArgumentException if `parameters` is empty
     * @throws java.lang.NullPointerException     if `parameters` is null, contains null elements, or if
     *                                            `returnType` is null
     */
    public FunctionType(List<? extends Type> parameters, Type returnType) {
        validCollection("parameters", parameters);
        notNull("returnType", returnType);

        this.parameters = Collections.unmodifiableList(new ArrayList<>(parameters));
        this.returnType = returnType;
    }

    /**
     * Get the return type of this function, e.g. the type of any invocations on instances of this type.
     *
     * @return The return type of the function
     */
    public Type getReturnType() {
        return returnType;
    }

    /**
     * Get all parameter types of this function, in the order they were declared in the shader. The list
     * will have at least one type. The returned list cannot be modified.
     *
     * @return The parameter types of the function
     */
    public List<Type> getParameterTypes() {
        return parameters;
    }

    /**
     * Get the type of the parameter specified by `index`, which must be between `0` and
     * `getParameterCount() - 1`. This is equivalent to getting the same index from the list returned by
     * `getParameterTypes()`.
     *
     * @param index The parameter index to lookup
     * @return The parameter type for `index`
     * @throws java.lang.IndexOutOfBoundsException if `index` is outside the number of parameters
     */
    public Type getParameterType(int index) {
        return parameters.get(index);
    }

    /**
     * Get the maximum number of parameters instances of this function type can be invoked with. If invoked
     * with fewer a curried function value is created instead of actually invoking the base function.
     *
     * @return The parameter count
     */
    public int getParameterCount() {
        return parameters.size();
    }

    @Override
    public <T> T accept(Type.Visitor<T> visitor) {
        return visitor.visitFunctionType(this);
    }

    @Override
    public boolean equals(Object t) {
        if (!(t instanceof FunctionType)) {
            return false;
        }
        FunctionType f = (FunctionType) t;
        return f.returnType.equals(returnType) && f.parameters.equals(parameters);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result += 31 * result + returnType.hashCode();
        result += 31 * result + parameters.hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        boolean first = true;
        for (Type p: parameters) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            sb.append(p.toString());
        }
        sb.append(" -> ").append(returnType.toString()).append(")");
        return sb.toString();
    }
}
