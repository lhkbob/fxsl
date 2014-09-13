package com.lhkbob.fxsl.lang;

import static com.lhkbob.fxsl.util.Preconditions.notNull;

/**
 * Parameters
 * ==========
 *
 * Parameter expressions are variable references that resolve to the declared names of function parameters.
 * If a function is inlined the parameter expression may be replaced with the actual parameter expression.
 * Otherwise they dynamically represent the parameter expressions their owning function is invoked with.
 * Because parameters only exist in the declaration of a function value, parameter expressions are only seen
 * referenced in expressions used as function bodies.
 *
 * A parameter expression is concrete if its type is concrete.
 *
 * @author Michael Ludwig
 */
public class ParameterExpression implements Expression {
    private final Type type;
    private final String name;
    private final Scope scope;

    /**
     * Create a new parameter expression that is defined in `scope` and can be referenced by the given
     * `name`. The parameter's value will always be of the given `type`.
     *
     * @param scope The scope that the parameter is defined in (e.g. the owning function's scope)
     * @param name  The name of the parameter
     * @param type  The value type of the parameter
     * @throws java.lang.NullPointerException if any argument is null
     */
    public ParameterExpression(Scope scope, String name, Type type) {
        notNull("name", name);
        notNull("type", type);
        notNull("scope", scope);
        this.type = type;
        this.name = name;
        this.scope = scope;
    }

    /**
     * Get the scope that defines this parameter expression, which should be the scope that the function
     * value creates for its parameters and result body.
     *
     * @return The scope defining this parameter
     */
    public Scope getScope() {
        return scope;
    }

    /**
     * Get the parameter name that can be used in FXSL code to reference the parameter.
     *
     * @return The parameter's name
     */
    public String getParameterName() {
        return name;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public boolean isConcrete() {
        return type.isConcrete();
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visitParameter(this);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ParameterExpression)) {
            return false;
        }
        ParameterExpression t = (ParameterExpression) o;
        return t.name.equals(name) && t.type.equals(type) && t.scope.equals(scope);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result += 31 * result + name.hashCode();
        result += 31 * result + type.hashCode();
        result += 31 * result + scope.hashCode();
        return result;
    }
}
