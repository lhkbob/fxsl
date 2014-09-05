package com.lhkbob.fxsl.lang;

import static com.lhkbob.fxsl.util.Preconditions.notNull;

/**
 * WildcardType
 * ============
 *
 * # Banner Lord
 *
 * How is everyone going?
 *
 * ## Extra test
 *
 * 1. `List 1`
 * 1. List 2 [with a link][link]
 * 1. List 3 {@link WildcardType}
 * 1. List 4 [WildcardType]
 *
 * This is inline preformatted text.
 * So much nice things for formatting!
 *
 * > Block quote?
 * > Hall back yall
 *
 * [link]: http://google.com
 *
 * Now for some actual real *code*:Now for some actual real *code*:Now for some actual real *code*:Now for
 * some actual real *code*:Now for some actual real *code*:Now for some actual real *code*:Now for some actual
 * real *code*:Now for some actual real *code*:Now for some actual real *code*:Now for some actual real
 * *code*:Now for some actual real *code*:Now for some actual real *code*:Now for some actual real *code*:Now
 * for some actual real *code*:Now for some actual real *code*:Now for some actual real *code*:Now for some
 * actual real *code*:
 *
 * ```java
 * WildcardType var = new WildcardType(new Scope(), "hello");
 * var.isConcrete();
 * ```
 *
 * Boom!
 *
 * @author Michael Ludwig
 */
public class WildcardType implements Type {
    private final Scope scope;
    private final String label;

    public WildcardType(Scope scope, String label) {
        notNull("scope", scope);
        notNull("label", label);
        this.label = label;
        this.scope = scope;
    }

    public Scope getScope() {
        return scope;
    }

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
        return "_" + label;
    }
}
