package com.lhkbob.fxsl.lang.expr;

import com.lhkbob.fxsl.lang.Scope;
import com.lhkbob.fxsl.lang.type.FunctionType;
import com.lhkbob.fxsl.lang.type.Type;
import com.lhkbob.fxsl.util.Immutable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.lhkbob.fxsl.util.Preconditions.notNull;
import static com.lhkbob.fxsl.util.Preconditions.validCollection;

/**
 * Function Values
 * ===============
 *
 * Function values are expressions that define functions that can then be invoked later. A function value
 * defines a new scope that by default includes the parameters of the function. The function body is a single
 * expression, but the `let ... in` syntax can be used in FXSL to compute multiple values based on the
 * parameters and form more complex expressions cleanly.
 *
 * @author Michael Ludwig
 */
@Immutable
public final class FunctionValue implements Expression {
    private final Scope scope;

    private final FunctionType type;
    private final List<ParameterExpression> parameters;
    private final Expression returnValue;

    /**
     * Create a new function value that has the signature described by `parameterNames` and
     * `parameterTypes`. These two lists must have the same size and the elements are paired to define the
     * name and type of each parameter to the function. `returnValue` is the body of the function to be
     * invoked when the function is called.
     *
     * The type of this expression is a {@link com.lhkbob.fxsl.lang.type.FunctionType} that is implicitly defined
     * by `parameterTypes` and the type of `returnValue`.
     *
     * @param scope       The scope this function is defined within (not the body's scope)
     * @param parameters  The parameter expressions accessible within the body's scope for this function
     * @param returnValue The body of the function
     * @throws java.lang.IllegalArgumentException if the size of `parameterNames` is different from
     *                                            `parameterTypes`, or if either is empty
     * @throws java.lang.NullPointerException     if any argument is null or contains a null element
     */
    public FunctionValue(Scope scope, List<ParameterExpression> parameters, Expression returnValue) {
        notNull("scope", scope);
        validCollection("parameters", parameters);
        notNull("returnValue", returnValue);

        List<Type> parameterTypes = new ArrayList<>(parameters.size());
        for (ParameterExpression p : parameters) {
            parameterTypes.add(p.getType());
        }

        type = new FunctionType(scope, parameterTypes, returnValue.getType());
        this.parameters = Collections.unmodifiableList(new ArrayList<>(parameters));
        this.returnValue = returnValue;
        this.scope = scope;
    }

    /**
     * Get the declared parameter expression of the `index` parameter, where the first parameter has index
     * 0.  The index is the same as with parameter types in {@link com.lhkbob.fxsl.lang.type.FunctionType}.
     *
     * @param index The parameter index to lookup
     * @return The name expression with the parameter and usable as a variable in the body
     * @throws java.lang.IndexOutOfBoundsException if index is less than 0 or greater than
     *                                             `getType().getParameterCount() - 1`
     */
    public ParameterExpression getParameter(int index) {
        return parameters.get(index);
    }

    /**
     * Get the named parameters for this function value. The returned list cannot be modified.
     *
     * @return All parameter names of the function, ordered the same as how they were declared in FXSL
     */
    public List<ParameterExpression> getParameters() {
        return parameters;
    }

    /**
     * Get the expression that is evaluated each time the function is invoked. This expression can contain
     * {@link ParameterExpression} referencing the parameters defined by this function
     * value.
     *
     * @return The body of the function
     */
    public Expression getReturnValue() {
        return returnValue;
    }

    /**
     * @return The scope of the function body, which includes the parameter expression definitions.
     */
    public Scope getBodyScope() {
        return returnValue.getScope();
    }

    @Override
    public FunctionType getType() {
        return type;
    }

    @Override
    public Scope getScope() {
        return scope;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitFunction(this);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FunctionValue)) {
            return false;
        }
        FunctionValue v = (FunctionValue) o;
        return v.scope.equals(scope) && v.type.equals(type) && v.parameters.equals(parameters) &&
               v.returnValue.equals(returnValue);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result += 31 * result + type.hashCode();
        result += 31 * result + parameters.hashCode();
        result += 31 * result + returnValue.hashCode();
        result += 31 * result + scope.hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        for (int i = 0; i < type.getParameterCount(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(parameters.get(i)).append(':').append(type.getParameterType(i).toString());
        }
        sb.append(" -> ");
        sb.append(returnValue.toString());
        sb.append(')');
        return sb.toString();
    }
}
