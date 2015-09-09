package com.lhkbob.fxsl.parser;

import com.lhkbob.fxsl.lang.expr.Expression;
import com.lhkbob.fxsl.lang.type.Type;

import org.antlr.v4.runtime.misc.NotNull;

import static com.lhkbob.fxsl.util.Preconditions.notNull;

/**
 *
 */
public class DeclarationVisitor extends FXSLBaseVisitor<Void> {
  private final ParseContext context;

  public DeclarationVisitor(ParseContext context) {
    notNull("context", context);
    this.context = context;
  }

  @Override
  public Void visitTypeDef(@NotNull FXSLParser.TypeDefContext ctx) {
    String typeName = ctx.Identifier().getText();
    Type type = ctx.type().accept(context.getTypeVisitor());
    context.getEnvironment().addDeclaredType(context.getCurrentScope(), typeName, type);
    return null;
  }

  @Override
  public Void visitVarDef(@NotNull FXSLParser.VarDefContext ctx) {
    Expression expression = ctx.expr().accept(context.getExpressionVisitor());

    String varName;
    if (ctx.optTypeKeyValue().Identifier() != null) {
      varName = ctx.optTypeKeyValue().Identifier().getText();
    } else {
      varName = ctx.optTypeKeyValue().typeKeyValue().Identifier().getText();
      context.startNewVariableDeclaration(varName);
      try {
        context.getEnvironment().setExpressionType(
            expression,
            ctx.optTypeKeyValue().typeKeyValue().type().accept(context.getTypeVisitor()));
      } finally {
        context.finishVariableDeclaration();
      }
    }

    context.getEnvironment().addDeclaredVariable(context.getCurrentScope(), varName, expression);
    return null;
  }
}
