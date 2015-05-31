package com.lhkbob.fxsl.lang.expr;

import com.lhkbob.fxsl.lang.Scope;
import com.lhkbob.fxsl.lang.type.MetaType;
import com.lhkbob.fxsl.util.Immutable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.lhkbob.fxsl.util.Preconditions.notNull;
import static com.lhkbob.fxsl.util.Preconditions.validCollection;

/**
 * Function Calls
 * ==============
 *
 * Function calls are expressions that invoke another expression with a list of expressions to replace the
 * parameters of the invoked expression. Only certain expression types can be invoked, namely functions,
 * wildcards, and unions. See {@link com.lhkbob.fxsl.lang.type.UnionType} for details about how the specific
 * function is selected in a union. Ignoring function recursion, a function call can be thought of as
 * replacing the call with the function return value with every parameter expression replaced by the supplied
 * argument values.
 *
 * The type of a function call expression is the return type of the invoked function. This is deterministic
 * if a function typed expression is invoked. If a wildcard is invoked, the return type is a new dependent
 * wildcard in the same scope as the invoked wildcard. If a union is invoked and there is more than one
 * function that matches then a new wildcard type is used. When a union has one option matching the provided
 * argument list then that option determines the return type.
 *
 * If a function is invoked with fewer arguments than the function expects, the type of the call expression
 * is a new function that has the remaining parameter types as its arguments and the original return type.
 * This is a curried function.
 *
 * @author Michael Ludwig
 */
@Immutable
public final class FunctionCall implements Expression {
    private final Scope scope;

    private final Expression function;
    private final List<Expression> parameterValues;

    /**
     * Create a new function call expression. `expression` will be invoked with each expression in
     * `parameterValues`. Each expression in `parameterValues` must be assignable to the parameter type for
     * its index. There cannot be more parameters than expected parameters. If there are fewer, but are
     * assignable, then the function call curries the function instead of invoking it completely.
     *
     * The invoked expression must have a function type, union type, or wildcard type. All invocations must
     * have at least one argument.
     *
     * @param scope           The scope of the function call
     * @param function        The expression to invoke
     * @param parameterValues The parameter values to invoke the function with
     * @throws IllegalArgumentException       if `parameterValues` is empty
     * @throws java.lang.NullPointerException if `expression` is null, or if `parameterValues` is null or
     *                                        contains null elements
     */
    public FunctionCall(Scope scope, Expression function, List<? extends Expression> parameterValues) {
        notNull("scope", scope);
        notNull("function", function);
        validCollection("parameterValues", parameterValues);

        this.scope = scope;
        this.function = function;
        this.parameterValues = Collections.unmodifiableList(new ArrayList<>(parameterValues));
    }

    /**
     * Get the function expression that is invoked. The type of this expression is a {@link
     * com.lhkbob.fxsl.lang.type.FunctionType}, {@link com.lhkbob.fxsl.lang.type.UnionType}, or {@link
     * MetaType}.
     *
     * @return The function being invoked
     */
    public Expression getFunction() {
        return function;
    }

    /**
     * Get the value provided for the `index` parameter that will replace that parameter expression within
     * the function body. `index` must be at least 0 (representing the first argument) and must be less than
     * `getSuppliedParameterCount()`, which may be less than the maximum parameter count of the function.
     *
     * @param index The parameter index to lookup
     * @return The argument for `index` when invoking the function call
     * @throws java.lang.IndexOutOfBoundsException if `index` is less than 0 or greater than or equal to
     *                                             `getSuppliedParameterCount()`
     * @see #getSuppliedParameterCount()
     */
    public Expression getParameterValue(int index) {
        return parameterValues.get(index);
    }

    /**
     * Get the number of parameters the function is invoked with. This will be at least 1 and at most the
     * parameter count of the function. If it is less than the function's parameter count then this invocation
     * curries the function, producing a new function that requires the remaining arguments before invoking
     * the base function.
     *
     * The returned value is equal to `getParameterValues().size()`.
     *
     * @return The number of supplied arguments
     */
    public int getSuppliedParameterCount() {
        return parameterValues.size();
    }

    /**
     * Get the argument values that the function is invoked with. This list will have at least one element
     * and at most as many elements as the parameter count of the function. If it is fewer than that, the
     * function call curries the base function. The list is ordered from first argument to last.
     *
     * The returned list cannot be modified.
     *
     * @return Every supplied parameter value for the function call
     * @see #getSuppliedParameterCount()
     */
    public List<Expression> getParameterValues() {
        return parameterValues;
    }

    @Override
    public Scope getScope() {
        return scope;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitFunctionCall(this);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FunctionCall)) {
            return false;
        }
        FunctionCall v = (FunctionCall) o;
        return v.scope.equals(scope) && v.function.equals(function) &&
               v.parameterValues.equals(parameterValues);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result += 31 * result + function.hashCode();
        result += 31 * result + parameterValues.hashCode();
        result += 31 * result + scope.hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(function.toString()).append('(');

        boolean first = true;
        for (Expression e : parameterValues) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            sb.append(e.toString());
        }
        sb.append(')');
        return sb.toString();
    }
}
