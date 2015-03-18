package com.lhkbob.fxsl.lang.type;

import com.lhkbob.fxsl.lang.Scope;
import com.lhkbob.fxsl.util.Immutable;

import static com.lhkbob.fxsl.util.Preconditions.notNull;

/**
 * Meta Types
 * ==========
 *
 * Meta types are special types that can match any other type in an FXSL program. As part of the
 * compilation process all variables and parameters with wildcards have their types inferred or resolved at
 * the point of invocation. This effectively creates a new program with the proper explicit types declared in
 * place of the wildcard types.
 *
 * Parameters and variables defined without an explicit type declaration implicitly create a new wildcard
 * type for that value. It is also possible to declare types to have labeled wildcards by using a type name
 * prefixed with '_'. This can be used to constrain the relationship between multiple parameters in a function
 * while still maintaining flexibility. Explicitly labeled wild cards are handled by the {@link
 * com.lhkbob.fxsl.lang.type.ParametricType}. A named wildcard is valid within the scope that it was defined.
 * All meta type instances are eventually replaced with specific types, or possibly uniquely named parametric
 * types after type inference is completed.
 *
 * @author Michael Ludwig
 */
@Immutable
public class MetaType implements Type {
    private final Scope scope;

    /**
     * Create a new meta type that is to exist within `scope`. Meta types are only equal to themselves.
     *
     * @param scope The scope of the expression which required the meta type
     * @throws java.lang.NullPointerException if `scope` is null
     */
    public MetaType(Scope scope) {
        notNull("scope", scope);
        this.scope = scope;
    }

    @Override
    public <T> T accept(Type.Visitor<T> visitor) {
        return visitor.visitMetaType(this);
    }

    /**
     * Get the scope that this meta type is valid within.
     *
     * @return The meta type's scope
     */
    public Scope getScope() {
        return scope;
    }

    @Override
    public boolean equals(Object t) {
        return t == this;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public String toString() {
        return "__m" + Integer.toHexString(System.identityHashCode(this));
    }
}
