package com.lhkbob.fxsl.lang;

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
 * com.lhkbob.fxsl.lang.UnionType}. Function unions are a way to support ad hoc polymorphism that allows
 * multiple function definitions to be referenced by the same symbol.
 *
 * ## Assignability and shared types
 *
 * A type can only be assigned to a function type value if that type is a wildcard or a function or union
 * that meets specific criteria that depends on the target function type. If there are two types, `A` and `B`
 * where `A` is a functional type, the behavior of assignment and type conversion will be explained below for
 * when `B` is a wildcard, another function type, or a union.
 *
 * If `B` is a wildcard then it can be assigned to `A` by creating a constraint on the types `B` may be
 * instantiated as when wildcards are removed. The conversion between `A` and `B` is `A`.
 *
 * If `B` is another function type then it can be assigned to `A` if its return type is assignable to the
 * return type of `A` and every parameter type of `A` is assignable to the corresponding parameter of `B`.
 * Essentially if `B` has wider parameter types and a narrower return type compared to `A` it can be assigned
 * to `A`. It must also have the exact same number of parameters. Parameters are compared based on their index
 * within the parameter list.
 *
 * The shared function type between two function types `A` and `B` is another function type. The new
 * function type has a return type that is the shared type of `A`'s and `B`'s return types. If this doesn't
 * exist the shared function type does not exist. The two function types must have the same number of
 * parameters, and the parameter list is the narrowest parameter list. Specifically if all of `A`'s parameters
 * are assignable to the corresponding ones in `B` then `A`'s list is used. If all of `B`'s parameters are
 * assignable to their match in `A` then `B`'s list is used. If this relationship does not hold for all
 * parameters then the shared function type does not exist.
 *
 * When `B` is a function union type, it may be assigned to `A` if any of its functional options are
 * assignable to `A`. The shared type between a function type and a union is a new union made of all valid
 * shared types between the function type and union options. If there are no valid shared types the overall
 * shared type does not exist. If there is only a single valid shared option that function type is used as the
 * shared type since a union must have at least two options.
 *
 * ## Concreteness
 *
 * A function type is concrete if all of its parameter types are concrete and its return type is concrete.
 *
 * @author Michael Ludwig
 */
@Immutable
public class FunctionType implements Type {
    private static final double BASE_COST = 20.0;
    private static final double PARAM_COST = 2.0;

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
    public double getTypeComplexity() {
        double cost = BASE_COST + returnType.getTypeComplexity();
        for (Type p: parameters) {
            cost += PARAM_COST + p.getTypeComplexity();
        }
        return cost;
    }

    @Override
    public double getAssignmentCost(Type t) {
        if (!isAssignableFrom(t)) {
            return Double.POSITIVE_INFINITY;
        } else if (t instanceof WildcardType) {
            return getTypeComplexity();
        } else if (t instanceof UnionType) {
            double cost = Double.POSITIVE_INFINITY;
            for (Type o: ((UnionType) t).getOptions()) {
                cost = Math.min(cost, getAssignmentCost(o));
            }
            return cost;
        } else {
            FunctionType other = (FunctionType) t;
            double cost = returnType.getAssignmentCost(other.returnType);
            for (int i = 0; i < parameters.size(); i++) {
                cost += other.parameters.get(i).getAssignmentCost(parameters.get(i));
            }
            return cost;
        }
    }

    @Override
    public boolean isAssignableFrom(Type type) {
        if (!(type instanceof FunctionType)) {
            if (type instanceof UnionType) {
                // if any of the options is assignable then the whole type is
                for (Type o : ((UnionType) type).getOptions()) {
                    if (isAssignableFrom(o)) {
                        return true;
                    }
                }
            }
            // otherwise not a union, or union had no assignable function option
            return type instanceof WildcardType;
        }

        FunctionType other = (FunctionType) type;
        if (!returnType.isAssignableFrom(other.returnType)) {
            // cannot convert the other function's result to this type
            return false;
        }
        // must be able to convert all parameters of this function type to other
        // (e.g. other is being invoked as a delegate)
        if (parameters.size() != other.parameters.size()) {
            return false;
        }
        for (int i = 0; i < parameters.size(); i++) {
            if (!other.parameters.get(i).isAssignableFrom(parameters.get(i))) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Type getSharedType(Type type) {
        if (!(type instanceof FunctionType)) {
            // special case for unions of function types, use its implementation
            if (type instanceof UnionType) {
                return type.getSharedType(this);
            } else if (type instanceof WildcardType) {
                return this;
            } else {
                return null;
            }
        }
        // A valid function conversion uses the converted return type, and the union/most-specific-subtype of each
        // parameter type. However, that operation doesn't exist, so we enforce parameter type equality.
        FunctionType other = (FunctionType) type;

        Type convertedReturnType = returnType.getSharedType(other.returnType);
        if (convertedReturnType == null) {
            return null;
        }

        if (parameters.size() != other.parameters.size()) {
            return null;
        }

        boolean thisAssignFromOther = true;
        for (int i = 0; i < parameters.size(); i++) {
            if (!parameters.get(i).isAssignableFrom(other.parameters.get(i))) {
                // if other's parameter does not assign to this parameter then other's parameter list is
                // not the narrowest to choose from
                thisAssignFromOther = false;
                break;
            }
        }

        if (thisAssignFromOther) {
            // other is the narrowest function parameter list
            return new FunctionType(other.parameters, convertedReturnType);
        } else {
            for (int i = 0; i < parameters.size(); i++) {
                if (!other.parameters.get(i).isAssignableFrom(parameters.get(i))) {
                    // this parameter does not assign to other's so this parameter list is not the
                    // narrowest to choose from, but neither is other so the conversion is invalid
                    return null;
                }
            }
            return new FunctionType(parameters, convertedReturnType);
        }
    }

    @Override
    public boolean isConcrete() {
        if (!returnType.isConcrete()) {
            return false;
        }

        for (Type p : parameters) {
            if (!p.isConcrete()) {
                return false;
            }
        }
        return true;
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
        return parameters.hashCode() ^ returnType.hashCode();
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
