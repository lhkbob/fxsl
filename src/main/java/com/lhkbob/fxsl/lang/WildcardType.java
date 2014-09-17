package com.lhkbob.fxsl.lang;

import com.lhkbob.fxsl.util.Immutable;

import static com.lhkbob.fxsl.util.Preconditions.notNull;

/**
 * Wildcard Types
 * ==============
 *
 * Wildcard types are special types that can match any other type in an FXSL program. As part of the
 * compilation process all variables and parameters with wildcards have their types inferred or resolved at
 * the point of invocation. This effectively creates a new program with the proper explicit types declared in
 * place of the wildcard types.
 *
 * Parameters and variables defined without an explicit type declaration implicitly create a new wildcard
 * type for that value. It is also possible to declare types to have labeled wildcards by using a type name
 * prefixed with '_'. This can be used to constrain the relationship between multiple parameters in a function
 * while still maintaining flexibility. A named wildcard is valid within the scope that it was defined. All
 * references to that type label will use the same wildcard type so any constraints imposed by possibly
 * multiple references must validate in order for compilation to succeed.
 *
 * ## Assignability and shared types
 *
 * Ignoring the issue of constraints upon type resolution, any type can be assigned to a wildcard. The
 * shared type between a wildcard and any other type is that other type. If both are wildcards then one is
 * chosen arbitrarily. Technically constraints affect assignability and shared types but they are a more
 * complex system and are handled elsewhere.
 *
 * ## Concreteness
 *
 * Wildcard types are never concrete.
 *
 * @author Michael Ludwig
 */
@Immutable
public class WildcardType implements Type {
    private final Scope scope;
    private final String label;

    /**
     * Create a new wildcard type that is to exist within `scope` and be named `label`. Wildcards with the
     * same label in the same scope are equal. The constructor does *not* register the type with the scope.
     *
     * @param scope The scope this wildcard is declared in
     * @param label The label for the wildcard
     * @throws java.lang.NullPointerException if `scope` or `label` are null
     */
    public WildcardType(Scope scope, String label) {
        notNull("scope", scope);
        notNull("label", label);
        this.label = label;
        this.scope = scope;
    }

    /**
     * Create a wildcard type that represents some dependent result of this type, such as accessing values
     * as an array index or struct field, or invoking them as a function invoking. The new wildcard type
     * exists in the same scope as this type. Its label is this type's base label appended by a colon and
     * `access`. Thus, given a label of a wildcard type, all dependent accesses can be determined by
     * separating it by ":". A colon is not an allowed character in identifiers so there is no risk of
     * contaminating this  property from the FXSL code.
     *
     * @param access The dependent access label
     * @return A new dependent wildcard type
     * @throws java.lang.NullPointerException if `access` is null
     */
    public WildcardType createDependentType(String access) {
        notNull("access", access);
        return new WildcardType(scope, label + ":" + access);
    }

    /**
     * Get the scope that this wildcard type is valid within.
     *
     * @return The wildcard's scope
     */
    public Scope getScope() {
        return scope;
    }

    /**
     * Get the label of this wildcard type.
     *
     * @return The wildcard's label
     */
    public String getLabel() {
        return label;
    }

    @Override
    public double getTypeComplexity() {
        return 0.0;
    }

    @Override
    public double getAssignmentCost(Type t) {
        return t.getTypeComplexity();
    }

    @Override
    public boolean isAssignableFrom(Type t) {
        // any type can be converted to the wild card's type by instantiating the wild card
        // to be that type (which will be handled at a different semantic level than this function)
        return true;
    }

    @Override
    public boolean isConcrete() {
        // wildcards are never concrete
        return false;
    }

    @Override
    public Type getSharedType(Type t) {
        // the valid conversion is the other type since we instantiate the wildcard as t
        return t;
    }

    @Override
    public boolean equals(Object t) {
        if (!(t instanceof WildcardType)) {
            return false;
        }
        return ((WildcardType) t).label.equals(label) && ((WildcardType) t).scope.equals(scope);
    }

    @Override
    public int hashCode() {
        return label.hashCode() ^ scope.hashCode();
    }

    @Override
    public String toString() {
        return label;
    }
}
