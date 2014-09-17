package com.lhkbob.fxsl.lang;

import com.lhkbob.fxsl.util.Immutable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.lhkbob.fxsl.util.Preconditions.validCollection;

/**
 * Union Values
 * ============
 *
 * Union values are expressions that form instances of {@link UnionType} within FXSL. In order for a  union
 * value expression to be valid, the expressions combined must have types that form a valid union type. Thus,
 * even if two different functions with the exact same signature or union'ed together, a compiler failure will
 * arise because there is only a single function type involved in the union. Not every instance of functions
 * with equivalent signatures being union'ed can be caught by the UnionValue's constructor, but it can be
 * verified completely when function selection is performed.
 *
 * If valid, the type of union value expressions is implicitly defined by the types of the expressions being
 * combined. For convenience, a union made with other unions will flatten out the options from its child
 * unions to present the most concrete options. If the union is the result of a function call or parameter,
 * this may not be possible so this flattening does not preclude the presence of expressions that still have
 * union types in the set of function options.
 *
 * A union value is never concrete. The compiler must select a particular option and rewrite the expression
 * tree to transform a union function call to a concrete expression.
 *
 * @author Michael Ludwig
 */
@Immutable
public class UnionValue implements Expression {
    private final Set<Expression> functions;
    private final transient UnionType type;

    /**
     * Create a new union value that is the union of the given `functions`. The type of this expression is
     * implicitly defined by the types of the function expressions. If the expression types produce an invalid
     * union type, the union value cannot be constructed.
     *
     * Like {@link com.lhkbob.fxsl.lang.UnionType}, this flattens the `functions` set when any other
     * UnionValues are encountered. However, not every expression with a union type can be flattened if the
     * actual option expressions cannot be determined.
     *
     * @param functions The function options of the union
     * @throws java.lang.IllegalArgumentException if `functions` is empty or contains an expression that is
     *                                            not a union, function, or wildcard, or if the set of
     *                                            expressions forms an invalid union type
     * @throws java.lang.NullPointerException     if `functions` is null or contains null elements
     */
    public UnionValue(Set<? extends Expression> functions) {
        validCollection("functions", functions);

        Set<Type> types = new HashSet<>();
        Set<Expression> flattened = new HashSet<>();
        for (Expression t : functions) {
            Type type = t.getType();
            if (!(type instanceof UnionType || type instanceof FunctionType ||
                  type instanceof WildcardType)) {
                throw new IllegalArgumentException("Functional union only supports functions, unions, and wildcards, not " +
                                                   type);
            } else if (t instanceof UnionValue) {
                flattened.addAll(((UnionValue) t).getOptions());
            } else {
                flattened.add(t);
            }

            types.add(t.getType());
        }

        type = new UnionType(types);
        this.functions = Collections.unmodifiableSet(flattened);
    }

    /**
     * Get all function options this union embodies. Note that the size of this set is not necessarily equal
     * to the type set returned by `getType().getOptions()`. This is because a union value can only be
     * flattened if the expression explicitly declares the options. If a function call returns a union, or a
     * parameter is a union, the actual options are not available until the function is inlined.
     *
     * The returned set cannot be modified and will have at least two elements.
     *
     * @return The union'ed function options of this expression
     */
    public Set<Expression> getOptions() {
        return functions;
    }

    @Override
    public UnionType getType() {
        return type;
    }

    @Override
    public boolean isConcrete() {
        // never concrete
        return false;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitUnion(this);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UnionValue)) {
            return false;
        }
        return ((UnionValue) o).functions.equals(functions);
    }

    @Override
    public int hashCode() {
        return functions.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Expression o : functions) {
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
