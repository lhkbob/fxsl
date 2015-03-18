package com.lhkbob.fxsl.lang;

import com.lhkbob.fxsl.util.Immutable;

/**
 * Scopes
 * ======
 *
 * A scope represents an abstract, hierarchical set of definitions and is essentially the same notion of
 * scope in many languages. A scope contains variable name and type name definitions to resolve when referred
 * to by name in FXSL instead of duplicating their definitions each time. To be immutable, Scope, does not
 * actually maintain these variable and type definitions. Instead Scope instances can be used as keys in some
 * external storage mechanism used by the compiler and processor. The logical equality of a Scope instance
 * is equivalent to reference equality.
 *
 * Scopes exist within a hierarchy. A name lookup starts at the closest scope containing the expression and
 * proceeds to the root scope. If no name is found then the variable or type is undefined and the FXSL code
 * cannot be compiled. The root scope contains default name aliases for the primitive types and function
 * definitions for all operators on the primitive types.
 *
 * Each FXSL code file has its own scope that is a child of the root scope. Function values have their own
 * scope that contains the parameter definitions, and any scope created by their body is a child of the
 * function scope. The `let...in` expression similarly creates a scope that contains all defined variables and
 * the final expression operates in the child scope.
 *
 * @author Michael Ludwig
 */
@Immutable
public class Scope {
    /**
     * A special scope independent of any scope hierarchy that's part of a parsed program that contains
     * all native-level language features.
     */
    public static final Scope NATIVE_SCOPE = new Scope();

    /**
     * A special scope, that's a child of {@link #NATIVE_SCOPE} that contains all primitive types and values.
     */
    public static final Scope PRIMITIVE_SCOPE = new Scope(NATIVE_SCOPE);

    private final Scope parent;

    /**
     * Create a new root scope that has no parent.
     */
    public Scope() {
        this(null);
    }

    /**
     * Create a new scope that is a child of the specified `parent`. If `parent` is null then the created
     * scope is a root scope with no parent.
     *
     * @param parent The parent scope
     */
    public Scope(Scope parent) {
        this.parent = parent;
    }

    /**
     * Get the parent scope of this scope. If this returns null then the scope represents a root scope.
     *
     * @return The parent scope
     */
    public Scope getParent() {
        return parent;
    }

    @Override
    public boolean equals(Object o) {
        return o == this;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }
}
