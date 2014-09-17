package com.lhkbob.fxsl.lang;

import com.lhkbob.fxsl.util.Immutable;

import static com.lhkbob.fxsl.util.Preconditions.notNull;

/**
 * Variables
 * ==========
 *
 * Variable expressions are name references to other expressions defined in the current scope, or one of the
 * current scope's parent scopes. Variables can be referenced in FXSL code before they are defined, so long as
 * the variable name is defined within an available scope once parsing has completed.
 *
 * Variables are never concrete. One of the first processing steps after parsing is resolving variables to
 * their defined values. If a variable name is not defined then the FXSL code fails to compile. Because of
 * this resolution step, a variable expression actually has an implicit wildcard type until its been resolved
 * to the correct value.
 *
 * Although structurally very similar to {@link com.lhkbob.fxsl.lang.ParameterExpression}, these two
 * reference expressions are semantically very different.
 *
 * @author Michael Ludwig
 */
@Immutable
public class VariableExpression implements Expression {
    private final transient WildcardType type;
    private final String name;
    private final Scope scope;

    /**
     * Create a new variable expression that is defined in `scope` and can be referenced by the given
     * `name`. The variable's type will be a new wildcard type defined within the scope whose label is equal
     * to the provided `name`.
     *
     * @param scope The scope that the variable is referenced (e.g. the current scope)
     * @param name  The name of the variable
     * @throws java.lang.NullPointerException if any argument is null
     */
    public VariableExpression(Scope scope, String name) {
        notNull("name", name);
        notNull("scope", scope);
        // FIXME what happens if a wildcard label has been created in the same scope with the exact same name?
        this.type = new WildcardType(scope, name);
        this.name = name;
        this.scope = scope;
    }

    /**
     * Get the scope that this variable is referenced within. The reference scope determines which actual
     * value these expressions resolve to based on the variable name.
     *
     * @return The scope  this variable was referenced within
     */
    public Scope getScope() {
        return scope;
    }

    /**
     * Get the variable name that determines the actual value of this expression.
     *
     * @return The variable's name
     */
    public String getVariableName() {
        return name;
    }

    @Override
    public WildcardType getType() {
        return type;
    }

    @Override
    public boolean isConcrete() {
        // never concrete
        return false;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitVariable(this);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof VariableExpression)) {
            return false;
        }
        VariableExpression t = (VariableExpression) o;
        return t.name.equals(name) && t.scope.equals(scope);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result += 31 * result + name.hashCode();
        result += 31 * result + scope.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return name;
    }
}
