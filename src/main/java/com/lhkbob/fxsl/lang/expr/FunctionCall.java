package com.lhkbob.fxsl.lang.expr;

import com.lhkbob.fxsl.lang.Scope;
import com.lhkbob.fxsl.lang.type.*;
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

    private final Expression expression;
    private final List<Expression> parameterValues;

    private final transient Type returnType;

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
     * @param expression      The expression to invoke
     * @param parameterValues The parameter values to invoke the function with
     * @throws java.lang.IllegalArgumentException if `expression` is not a function, union, or wildcard, if
     *                                            the parameters do not match the function, or if
     *                                            `parameterValues` is empty
     * @throws java.lang.NullPointerException     if `expression` is null, or if `parameterValues` is null or
     *                                            contains null elements
     */
    public FunctionCall(Scope scope, Expression expression, List<? extends Expression> parameterValues) {
        notNull("scope", scope);
        notNull("expression", expression);
        validCollection("parameterValues", parameterValues);

        if (!(expression.getType() instanceof UnionType) && !(expression.getType() instanceof FunctionType) &&
            !(expression.getType() instanceof MetaType)) {
            throw new IllegalArgumentException("Can only invoke function values, unions or wildcards, not: " +
                                               expression.getType());
        }
        returnType = computeReturnType(scope, expression, parameterValues);
        if (returnType == null) {
            throw new IllegalArgumentException("No matching function call for signature");
        }

        this.scope = scope;
        this.expression = expression;
        this.parameterValues = Collections.unmodifiableList(new ArrayList<>(parameterValues));
    }

    /**
     * Compute the return type of invoking the given function. This assumes the `function` is a valid type.
     * This will properly create a dependent wildcard type or curry the function as necessary.
     */
    private static Type computeReturnType(Scope scope, Expression function, List<? extends Expression> args) {
        if (function.getType() instanceof MetaType) {
            // the return type is a dependent wildcard type and argument list is irrelevant
            return new MetaType(new Scope());
        } else if (function.getType() instanceof UnionType) {
            // if multiple options are assignable, we use a wildcard at this point
            // if one option is assignable, we use its return type
            Type matchedFunction = null;
            for (Type option : ((UnionType) function.getType()).getOptions()) {
                if (option instanceof MetaType || isSignatureValid((FunctionType) option, args)) {
                    // default match
                    if (matchedFunction != null) {
                        // more than one match so make a wildcard
                        return new MetaType(scope);
                    } else {
                        matchedFunction = option;
                    }
                }
            }

            if (matchedFunction instanceof MetaType) {
                return new MetaType(scope);
            } else if (matchedFunction != null) {
                return getReturnTypeOrCurriedFunction(scope, (FunctionType) matchedFunction, args);
            } else {
                // the union cannot be invoked with the provided arguments
                return null;
            }
        } else {
            FunctionType funcType = (FunctionType) function.getType();
            if (isSignatureValid(funcType, args)) {
                return getReturnTypeOrCurriedFunction(scope, funcType, args);
            } else {
                // function cannot be invoked with provided arguments
                return null;
            }
        }
    }

    /**
     * Return either the function's return type or the appropriate remaining function after currying the
     * given arguments. It can be assumed the arguments are assignable to the function's parameters.
     */
    private static Type getReturnTypeOrCurriedFunction(Scope scope, FunctionType type, List<? extends Expression> args) {
        if (args.size() == type.getParameterCount()) {
            // invoke the function so the type is the function's return type
            return type.getReturnType();
        } else {
            // the function is curried so the return type is a new function with updated signature
            List<Type> signature = new ArrayList<>();
            for (int i = args.size(); i < type.getParameterCount(); i++) {
                signature.add(type.getParameterType(i));
            }
            return new FunctionType(scope, signature, type.getReturnType());
        }
    }

    /**
     * Return true if the arguments are assignable to their matching parameter type and the number of
     * supplied arguments is at most the expected parameter count of the function (e.g. it may still be a
     * curried function invocation, but there won't be more arguments than expected).
     *
     * @param type The type of the function type to compare the argument list with
     * @param args The provided argument list that is to be supplied to the function of `type`
     * @return True if functions of `type` can be invoked with the provided parameter values
     * @throws java.lang.IllegalArgumentException if `args` is empty
     * @throws java.lang.NullPointerException     if `type` is null, if `args` is null or contains null elements
     */
    public static boolean isSignatureValid(FunctionType type, List<? extends Expression> args) {
        notNull("type", type);
        validCollection("args", args);

        if (args.size() > type.getParameterCount()) {
            // definitely incorrect signature if arg count is greater than what function expects
            return false;
        }
        for (int i = 0; i < args.size(); i++) {
            Type argType = type.getParameterType(i);
            if (!Types.isAssignable(argType, args.get(i).getType())) {
                // signature is incompatible
                return false;
            }
        }
        // exact function invocation or a curried function if args is smaller than the parameter count
        return true;
    }

    /**
     * Get the function expression that is invoked. The type of this expression is a {@link
     * com.lhkbob.fxsl.lang.type.FunctionType}, {@link com.lhkbob.fxsl.lang.type.UnionType}, or {@link
     * MetaType}.
     *
     * @return The function being invoked
     */
    public Expression getFunction() {
        return expression;
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
    public Type getType() {
        return returnType;
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
        return v.scope.equals(scope) && v.expression.equals(expression) && v.parameterValues.equals(parameterValues);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result += 31 * result + expression.hashCode();
        result += 31 * result + parameterValues.hashCode();
        result += 31 * result + scope.hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(expression.toString()).append('(');

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
