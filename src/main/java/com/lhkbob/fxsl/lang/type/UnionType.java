package com.lhkbob.fxsl.lang.type;

import com.lhkbob.fxsl.lang.Scope;
import com.lhkbob.fxsl.util.Immutable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.lhkbob.fxsl.util.Preconditions.notNull;
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
 * @author Michael Ludwig
 */
@Immutable
public final class UnionType implements Type {
    private final Scope scope;
    private final Set<Type> functions;

    /**
     * Create a new union type that is the union of the given types. These types may be function types,
     * wildcard types, or other union types. Any union types included within this set are expanded to their
     * function options. If after this flattening occurs there are not two unique options the union type is
     * invalid and an exception is thrown.
     *
     * @param scope     The scope the union was declared in
     * @param functions The function type options making up the union
     * @throws java.lang.IllegalArgumentException if `functions` references a type other than UnionType,
     *                                            FunctionType, and WildcardType, or if the net option size is
     *                                            less than 2.
     * @throws java.lang.NullPointerException     if `functions` is null or contains null elements
     */
    public UnionType(Scope scope, Set<? extends Type> functions) {
        notNull("scope", scope);
        validCollection("functions", functions);

        Set<Type> flattened = new HashSet<>();
        for (Type t : functions) {
            if (!(t instanceof UnionType || t instanceof FunctionType || t instanceof MetaType)) {
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

        this.scope = scope;
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
    public <T> T accept(Type.Visitor<T> visitor) {
        return visitor.visitUnionType(this);
    }

    @Override
    public Scope getScope() {
        return scope;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UnionType)) {
            return false;
        }
        UnionType t = (UnionType) o;
        return t.scope.equals(scope) && t.functions.equals(functions);
    }

    @Override
    public int hashCode() {
        return functions.hashCode() ^ scope.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Type o : functions) {
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
