package com.lhkbob.fxsl.parser;

import com.lhkbob.fxsl.lang.Expression;

/**
 *
 */
public class ExpressionVisitor extends FXSLBaseVisitor<Expression> {
    /*private final Scope scope;
    private final TypeVisitor typeVisitor;

    // FIXME must somehow insert the array wildcard length into the variable scope
    // could just be done by the type visitor since that has access to the scope
    // FIXME how do we implement recursive functions? how does that impact the idea that we generate a code tree instead of a graph?

    public ExpressionVisitor(Scope scope) {
        notNull("scope", scope);
        this.scope = scope;
        typeVisitor = new TypeVisitor(scope);
    }

    @Override
    public Expression visitStmList(@NotNull FXSLParser.StmListContext ctx) {
        Expression lastExpr = null;
        for (FXSLParser.StmContext stm : ctx.stm()) {
            lastExpr = visit(stm);
        }
        return lastExpr;
    }

    @Override
    public Expression visitBaseDef(@NotNull FXSLParser.BaseDefContext ctx) {
        FXSLParser.OptTypeKeyValueContext var = ctx.optTypeKeyValue();
        String pName;
        Type pType;
        if (var.typeKeyValue() != null) {
            // typed definition
            pType = typeVisitor.visit(var.typeKeyValue().type());
            pName = var.typeKeyValue().Identifier().getText();
        } else {
            // implicit def type (add anonymous wildcard)
            pName = var.Identifier().getText();
            pType = scope.newAnonymousWildcard();
        }

        Expression value = visit(ctx.expr());
        if (!pType.isAssignableFrom(value.getType())) {
            throw new IllegalStateException("Expression's type cannot be assigned to declared variable type");
        }
        // FIXME don't want to lose pType in case the assigned value needs to be stripped down
        scope.addVariable(pName, value);
        return value;
    }

    @Override
    public Expression visitTypeDef(@NotNull FXSLParser.TypeDefContext ctx) {
        scope.addType(ctx.Identifier().getText(), typeVisitor.visit(ctx.type()));
        return null;
    }

    @Override
    public Expression visitFieldAccess(@NotNull FXSLParser.FieldAccessContext ctx) {
        Expression value = visit(ctx.value);

        if (value.getType() instanceof ArrayType) {
            Expression field = visit(ctx.field);
            return new ArrayAccess(value, field);
        } else {
            // assumed field name is identifier, but we can't visit it like an expression because the
            // identifier is an undefined variable, etc.
            String fieldName = ctx.field.getText();
            return new StructFieldAccess(value, fieldName);
        }
    }

    @Override
    public Expression visitBinaryExpression(@NotNull FXSLParser.BinaryExpressionContext ctx) {
        Expression left = visit(ctx.left);
        Expression right = visit(ctx.right);

        Expression function = scope.getVariable(ctx.op.getText());
        return new FunctionCall(function, Arrays.asList(left, right));
    }

    @Override
    public Expression visitVariable(@NotNull FXSLParser.VariableContext ctx) {
        String varName = ctx.Identifier().getText();
        Expression value = scope.getVariable(varName);
        if (value == null) {
            throw new IllegalStateException("Variable is not defined: " + varName);
        }
        return new VariableExpression(varName, value);
    }

    @Override
    public Expression visitLet(@NotNull FXSLParser.LetContext ctx) {
        Scope letScope = new Scope(scope);
        ExpressionVisitor letVisitor = new ExpressionVisitor(letScope);

        for (FXSLParser.BaseDefContext def: ctx.defList().baseDef()) {
            letVisitor.visit(def);
        }

        return letVisitor.visit(ctx.expr());
    }

    @Override
    public Expression visitFunctionCall(@NotNull FXSLParser.FunctionCallContext ctx) {
        Expression function = visit(ctx.func);
        List<Expression> args = new ArrayList<>();
        for (FXSLParser.ExprContext p : ctx.params) {
            args.add(visit(p));
        }
        return new FunctionCall(function, args);
    }

    @Override
    public Expression visitUnaryExpression(@NotNull FXSLParser.UnaryExpressionContext ctx) {
        Expression arg = visit(ctx.expr());
        Expression function = scope.getVariable(ctx.op.getText());
        return new FunctionCall(function, Arrays.asList(arg));
    }

    @Override
    public Expression visitStruct(@NotNull FXSLParser.StructContext ctx) {
        Map<String, Expression> fields = new HashMap<>();
        for (FXSLParser.ExprKeyValueContext f : ctx.exprKeyValue()) {
            Expression value = visit(f.expr());
            fields.put(f.Identifier().getText(), value);
        }
        return new StructValue(fields);
    }

    @Override
    public Expression visitFunction(@NotNull FXSLParser.FunctionContext ctx) {
        Scope funcScope = new Scope(scope);

        List<String> paramNames = new ArrayList<>();
        List<Type> paramTypes = new ArrayList<>();
        for (FXSLParser.OptTypeKeyValueContext p : ctx.params) {
            String pName;
            Type pType;
            if (p.typeKeyValue() != null) {
                // typed parameter
                pType = typeVisitor.visit(p.typeKeyValue().type());
                pName = p.typeKeyValue().Identifier().getText();
            } else {
                // implicit parameter (add anonymous wildcard)
                pName = p.Identifier().getText();
                pType = funcScope.newAnonymousWildcard();
            }

            funcScope.addVariable(pName, new ParameterExpression(pName, pType));
        }

        Expression body = new ExpressionVisitor(funcScope).visit(ctx.returnExpr);

        return new FunctionValue(paramNames, paramTypes, body);
    }

    @Override
    public Expression visitArray(@NotNull FXSLParser.ArrayContext ctx) {
        List<Expression> elems = new ArrayList<>(ctx.expr().size());
        for (FXSLParser.ExprContext e : ctx.expr()) {
            elems.add(visit(e));
        }

        return new ArrayValue(elems);
    }

    @Override
    public Expression visitPrimitive(@NotNull FXSLParser.PrimitiveContext ctx) {
        if (ctx.Boolean() != null) {
            return new PrimitiveValue(Boolean.parseBoolean(ctx.Boolean().getText()));
        } else if (ctx.Integer() != null) {
            return new PrimitiveValue(Integer.parseInt(ctx.Integer().getText()));
        } else if (ctx.Float() != null) {
            return new PrimitiveValue(Float.parseFloat(ctx.Float().getText()));
        } else {
            throw new IllegalStateException("No real primitive value");
        }
    }*/
}
