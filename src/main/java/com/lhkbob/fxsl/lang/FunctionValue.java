package com.lhkbob.fxsl.lang;

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
 * A function value is concrete if its parameter types are concrete and its return value expression is
 * concrete.
 *
 * @author Michael Ludwig
 */
public class FunctionValue implements Expression {
    private final FunctionType type;
    private final List<String> parameterNames;
    private final Expression returnValue;

    /**
     * Create a new function value that has the signature described by `parameterNames` and
     * `parameterTypes`. These two lists must have the same size and the elements are paired to define the
     * name and type of each parameter to the function. `returnValue` is the body of the function to be
     * invoked when the function is called.
     *
     * The type of this expression is a {@link com.lhkbob.fxsl.lang.FunctionType} that is implicitly defined
     * by `parameterTypes` and the type of `returnValue`.
     *
     * @param parameterNames The names of every parameter to the function
     * @param parameterTypes THe paired types of every parameter
     * @param returnValue    The body of the function
     * @throws java.lang.IllegalArgumentException if the size of `parameterNames` is different from
     *                                            `parameterTypes`, or if either is empty
     * @throws java.lang.NullPointerException     if any argument is null or contains a null element
     */
    public FunctionValue(List<String> parameterNames, List<? extends Type> parameterTypes,
                         Expression returnValue) {
        validCollection("parameterNames", parameterNames);
        validCollection("parameterTypes", parameterTypes);
        notNull("returnValue", returnValue);

        if (parameterNames.size() != parameterTypes.size()) {
            throw new IllegalArgumentException("Name and type lists must have equal size");
        }

        type = new FunctionType(parameterTypes, returnValue.getType());
        this.parameterNames = Collections.unmodifiableList(new ArrayList<>(parameterNames));
        this.returnValue = returnValue;
    }

    /**
     * Get the declared name of the `index` parameter, where the first parameter has index 0. The index is
     * the same as with parameter types in {@link com.lhkbob.fxsl.lang.FunctionType}.
     *
     * @param index The parameter index to lookup
     * @return The name associated with the parameter and usable as a parameter expression in the body
     * @throws java.lang.IndexOutOfBoundsException if index is less than 0 or greater than
     *                                             `getType().getParameterCount() - 1`
     */
    public String getParameterName(int index) {
        return parameterNames.get(index);
    }

    /**
     * Get the named parameters for this function value. The returned list cannot be modified.
     *
     * @return All parameter names of the function, ordered the same as how they were declared in FXSL
     */
    public List<String> getParameterNames() {
        return parameterNames;
    }

    /**
     * Get the expression that is evaluated each time the function is invoked. This expression can contain
     * {@link com.lhkbob.fxsl.lang.ParameterExpression} referencing the parameters defined by this function
     * value.
     *
     * @return The body of the function
     */
    public Expression getReturnValue() {
        return returnValue;
    }

    @Override
    public FunctionType getType() {
        return type;
    }

    @Override
    public boolean isConcrete() {
        return type.isConcrete() && returnValue.isConcrete();
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
        return v.type.equals(type) && v.parameterNames.equals(parameterNames) &&
               v.returnValue.equals(returnValue);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result += 31 * result + type.hashCode();
        result += 31 * result + parameterNames.hashCode();
        result += 31 * result + returnValue.hashCode();
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
            sb.append(parameterNames.get(i)).append(':').append(type.getParameterType(i).toString());
        }
        sb.append(" -> ");
        sb.append(returnValue.toString());
        sb.append(')');
        return sb.toString();
    }
}
