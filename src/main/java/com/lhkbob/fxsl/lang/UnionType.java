package com.lhkbob.fxsl.lang;

import com.lhkbob.fxsl.util.Immutable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.lhkbob.fxsl.util.Preconditions.validCollection;

/**
 * Function Union Types
 * ====================
 *
 * Function unions provide ad hoc polymorphism to FXSL. A union type is a set of types that can resolve to
 * function types, namely actual function types, wildcards, and other union types. Defining a new union type
 * based on other union types makes it easy to overload the common operators to support custom types. In
 * general it should not be necessary to declare union types in FXSL but rely on implicit type inference;
 * otherwise they can become quite unwieldy.
 *
 * ## Invoking function unions
 *
 * A function union can be invoked just like a function. However, only a single function within its set of
 * options is actually invoked. The specific function is selected based on the types of the input arguments of
 * the actual invocation. If no function option can be made to match then the union cannot be invoked in the
 * requested manner and a compilation error is raised. Because FXSL has no type inheritance a heuristic based
 * on edit distance is used to determine what the best function is to invoke.
 *
 * First only function options that have parameters which the argument values are assignable to are
 * considered. Then the net assignment cost is computed for the arguments to each function. The function
 * options are ordered by this cost and the minimal costing function is selected. If a function has more
 * parameters than arguments provided (so the invocation creates a curried function), it is ordered after the
 * other functions that have fewer remaining parameters. This makes currying within a union only possible when
 * no other option matches with a smaller argument list.
 *
 * ## Assignability and shared types
 *
 * One union type can only be assigned to another union type if it contains an assignable option for each
 * option in the second type. The shared type between two unions is a new union made of valid shared types
 * between their options. The shared type between a union type and a function type is described in {@link
 * com.lhkbob.fxsl.lang.FunctionType}. Note that a function type cannot be assigned to a union type, only the
 * opposite direction is valid.
 *
 * Wildcards can still be assigned to unions, but impose a constraint upon the wildcard's instantiated type.
 * Similarly the shared type between a union and a wildcard is the union.
 *
 * ## Concreteness
 *
 * A union type is concrete if all of its function type options are concrete. This is different from the
 * expression concreteness of invoking a union, which is not concrete because a function must be selected to
 * be a concrete expression.
 *
 * @author Michael Ludwig
 */
@Immutable
public class UnionType implements Type {
    private static final double BASE_COST = 20.0;
    private static final double OPTION_COST = 3.0;

    private final Set<Type> functions;

    /**
     * Create a new union type that is the union of the given types. These types may be function types,
     * wildcard types, or other union types. Any union types included within this set are expanded to their
     * function options. If after this flattening occurs there are not two unique options the union type is
     * invalid and an exception is thrown.
     *
     * @param functions The function type options making up the union
     * @throws java.lang.IllegalArgumentException if `functions` references a type other than UnionType,
     *                                            FunctionType, and WildcardType, or if the net option size is
     *                                            less than 2.
     * @throws java.lang.NullPointerException     if `functions` is null or contains null elements
     */
    public UnionType(Set<? extends Type> functions) {
        validCollection("functions", functions);

        Set<Type> flattened = new HashSet<>();
        for (Type t : functions) {
            if (!(t instanceof UnionType || t instanceof FunctionType || t instanceof WildcardType)) {
                throw new IllegalArgumentException("Type union only supports function types, not: " + t);
            }
            if (t instanceof UnionType) {
                flattened.addAll(((UnionType) t).getOptions());
            } else {
                flattened.add(t);
            }
        }

        if (flattened.size() < 2) {
            throw new IllegalArgumentException("Union requires at least two unique types");
        }
        this.functions = Collections.unmodifiableSet(flattened);
    }

    /**
     * Get the set of function options represented by this union type. The returned set will only contain
     * function types and wildcard types. It will have at least two elements. The returned set is immutable.
     *
     * @return The function options of this type
     */
    public Set<Type> getOptions() {
        return functions;
    }

    @Override
    public double getTypeComplexity() {
        double complexity = BASE_COST;
        for (Type o : functions) {
            complexity += OPTION_COST + o.getTypeComplexity();
        }
        return complexity;
    }

    @Override
    public double getAssignmentCost(Type t) {
        if (!isAssignableFrom(t)) {
            return Double.POSITIVE_INFINITY;
        } else if (t instanceof WildcardType) {
            return getTypeComplexity();
        } else {
            UnionType other = (UnionType) t;
            double cost = 0.0;

            for (Type f : functions) {
                cost += f.getAssignmentCost(other);
            }

            return cost;
        }
    }

    @Override
    public boolean isAssignableFrom(Type t) {
        if (!(t instanceof UnionType)) {
            // NOTE: a FunctionType can't be assigned to a UnionType, all options must be provided to be assignable
            return t instanceof WildcardType;
        }

        UnionType otherType = (UnionType) t;
        // every one of this type's options must have an option in other that is assignable to it
        for (Type f : functions) {
            if (!f.isAssignableFrom(otherType)) {
                // no function option in other could be used as f
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isConcrete() {
        for (Type f : functions) {
            if (!f.isConcrete()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Type getSharedType(Type t) {
        if (t instanceof UnionType || t instanceof FunctionType) {
            // the valid conversion is the cross product of both option sets, with only valid conversions;
            // since getSharedType is commutative the product is simpler to compute
            Set<Type> conversion = new HashSet<>();
            for (Type f : functions) {
                Type c = f.getSharedType(t);
                if (c != null) {
                    conversion.add(c);
                }
            }
            if (conversion.isEmpty()) {
                // no useful overlap
                return null;
            } else if (conversion.size() == 1) {
                // single intersection
                return conversion.iterator().next();
            } else {
                return new UnionType(conversion);
            }
        } else if (t instanceof WildcardType) {
            return this;
        } else {
            // incompatible types
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UnionType)) {
            return false;
        }
        return functions.equals(((UnionType) o).functions);
    }

    @Override
    public int hashCode() {
        return functions.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Type o: functions) {
            if (first) {
                first = false;
            } else {
                sb.append(" | ");
            }
            sb.append(o.toString());
        }
        return sb.toString();
    }
}
