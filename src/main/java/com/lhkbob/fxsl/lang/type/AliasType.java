package com.lhkbob.fxsl.lang.type;

import com.lhkbob.fxsl.lang.Scope;

import static com.lhkbob.fxsl.util.Preconditions.notNull;

/**
 * Type Alias's
 * ============
 *
 * Within FXSL, types may be declared outside of an expression and assigned an alias or name for convenient
 * referral later on. This is done using the `type <name> = ...` syntax within an FXSL shader. Anywhere `name`
 * is used in place of an explicit type specification, it is replaced with the type specification given in the
 * declaration. This declaration may itself reference aliases to build complex but easy to read types. Alias
 * references are lazily evaluated so the order within which the aliases are declared is not important as long
 * as the alias exists in the same scope.
 *
 * Types cannot be cyclical. This is due to the limitations of the graphics hardware upon which FXSL must be
 * executed. Syntactically, it is possible to create cyclic type definitions using alias types. However, they
 * are semantically invalid.
 *
 * @author Michael Ludwig
 */
public class AliasType implements Type {
    private final String label;
    private final Scope scope;

    /**
     * Create a new alias type named `label`.
     *
     * @param scope The scope the alias was referenced from
     * @param label The label for the wildcard
     * @throws NullPointerException if `scope` or `label` are null
     */
    public AliasType(Scope scope, String label) {
        notNull("scope", scope);
        notNull("label", label);
        this.label = label;
        this.scope = scope;
    }

    /**
     * Get the scope the alias was referenced from, which is also the defining scope for the instantiation
     * of this alias.
     *
     * @return The scope that defined this type
     */
    @Override
    public Scope getScope() {
        return scope;
    }

    /**
     * Get the label of this type.
     *
     * @return The type's label
     */
    public String getLabel() {
        return label;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitAliasType(this);
    }

    @Override
    public boolean equals(Object t) {
        if (!(t instanceof AliasType)) {
            return false;
        }
        AliasType a = (AliasType) t;
        return a.scope.equals(scope) && a.label.equals(label);
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
