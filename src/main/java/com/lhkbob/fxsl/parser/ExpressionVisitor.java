package com.lhkbob.fxsl.parser;

import com.lhkbob.fxsl.lang.Scope;
import com.lhkbob.fxsl.lang.expr.ArrayAccess;
import com.lhkbob.fxsl.lang.expr.ArrayValue;
import com.lhkbob.fxsl.lang.expr.Attribute;
import com.lhkbob.fxsl.lang.expr.DynamicArrayValue;
import com.lhkbob.fxsl.lang.expr.Expression;
import com.lhkbob.fxsl.lang.expr.FunctionCall;
import com.lhkbob.fxsl.lang.expr.FunctionValue;
import com.lhkbob.fxsl.lang.expr.Parameter;
import com.lhkbob.fxsl.lang.expr.PrimitiveValue;
import com.lhkbob.fxsl.lang.expr.StructFieldAccess;
import com.lhkbob.fxsl.lang.expr.StructValue;
import com.lhkbob.fxsl.lang.expr.Uniform;
import com.lhkbob.fxsl.lang.expr.VariableReference;
import com.lhkbob.fxsl.lang.type.Type;

import org.antlr.v4.runtime.misc.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.lhkbob.fxsl.util.Preconditions.notNull;

/**
 *
 */
public class ExpressionVisitor extends FXSLBaseVisitor<Expression> {
  private final ParseContext context;

  public ExpressionVisitor(ParseContext context) {
    notNull("context", context);
    this.context = context;
  }

  @Override
  public Attribute visitAttr(@NotNull FXSLParser.AttrContext ctx) {
    String name;
    Type declaredType;
    if (ctx.optTypeKeyValue().typeKeyValue() != null) {
      name = ctx.optTypeKeyValue().typeKeyValue().Identifier().getText();
      if (context.isInsideVariableDeclaration()) {
        throw new IllegalStateException(
            "Should not be possible to have attribute expression in a variable's declared type");
      }
      declaredType = ctx.optTypeKeyValue().typeKeyValue().type().accept(context.getTypeVisitor());
    } else {
      name = ctx.optTypeKeyValue().Identifier().getText();
      declaredType = null;
    }

    Attribute attr = new Attribute(context.getCurrentScope(), name);
    if (declaredType != null) {
      context.getEnvironment().setExpressionType(attr, declaredType);
    }
    return attr;
  }

  @Override
  public Expression visitStmList(@NotNull FXSLParser.StmListContext ctx) {
    context.push();
    try {
      Expression lastExpr = null;
      for (FXSLParser.StmContext stm : ctx.stm()) {
        if (stm.def() != null) {
          stm.def().accept(context.getDeclarationVisitor());
        } else {
          lastExpr = stm.expr().accept(this);
        }
      }
      return lastExpr;
    } finally {
      context.pop();
    }
  }

  @Override
  public ArrayValue visitArray(@NotNull FXSLParser.ArrayContext ctx) {
    List<Expression> elems = new ArrayList<>(ctx.expr().size());
    for (FXSLParser.ExprContext e : ctx.expr()) {
      elems.add(e.accept(this));
    }

    return new ArrayValue(context.getCurrentScope(), elems);
  }

  @Override
  public StructFieldAccess visitFieldAccess(@NotNull FXSLParser.FieldAccessContext ctx) {
    Expression value = ctx.value.accept(this);
    String fieldName = ctx.field.getText();
    return new StructFieldAccess(context.getCurrentScope(), value, fieldName);
  }

  @Override
  public ArrayAccess visitArrayAccess(@NotNull FXSLParser.ArrayAccessContext ctx) {
    Expression value = ctx.value.accept(this);
    Expression index = ctx.index.accept(this);
    return new ArrayAccess(context.getCurrentScope(), value, index);
  }

  @Override
  public FunctionCall visitBinaryExpression(@NotNull FXSLParser.BinaryExpressionContext ctx) {
    Expression left = ctx.left.accept(this);
    Expression right = ctx.right.accept(this);

    Expression function = new VariableReference(context.getCurrentScope(), ctx.op.getText());
    return new FunctionCall(context.getCurrentScope(), function, Arrays.asList(left, right));
  }

  @Override
  public VariableReference visitVariable(@NotNull FXSLParser.VariableContext ctx) {
    String varName = ctx.Identifier().getText();
    return new VariableReference(context.getCurrentScope(), varName);
  }

  @Override
  public Expression visitLet(@NotNull FXSLParser.LetContext ctx) {
    context.push();
    try {
      for (FXSLParser.DefContext def : ctx.def()) {
        def.accept(context.getDeclarationVisitor());
      }
      return ctx.expr().accept(this);
    } finally {
      context.pop();
    }
  }

  @Override
  public FunctionValue visitFunction(@NotNull FXSLParser.FunctionContext ctx) {
    List<String> parameterNames = new ArrayList<>();
    List<FXSLParser.TypeContext> parameterTypes = new ArrayList<>();
    for (FXSLParser.OptTypeKeyValueContext p : ctx.params) {
      if (p.typeKeyValue() != null) {
        parameterNames.add(p.typeKeyValue().Identifier().getText());
        parameterTypes.add(p.typeKeyValue().type());
      } else {
        parameterNames.add(p.Identifier().getText());
        parameterTypes.add(null);
      }
    }

    Scope funcScope = context.push();
    try {
      Expression body = ctx.returnExpr.accept(this);
      // The function itself is in the parent scope, but don't pop off the current scope yet since
      // it will need to be active when the parameters are created.
      FunctionValue function = new FunctionValue(funcScope.getParent(), parameterNames, body);

      // Now add all the parameter expressions to the body scope
      for (int i = 0; i < parameterNames.size(); i++) {
        Parameter param = new Parameter(function, parameterNames.get(i));
        if (parameterTypes.get(i) != null) {
          context.startNewVariableDeclaration(parameterNames.get(i));
          try {
            context.getEnvironment()
                .setExpressionType(param, parameterTypes.get(i).accept(context.getTypeVisitor()));
          } finally {
            context.finishVariableDeclaration();
          }
        }
        context.getEnvironment().addVariable(funcScope, parameterNames.get(i), param);
      }

      return function;
    } finally {
      context.pop();
    }
  }

  @Override
  public FunctionCall visitFunctionCall(@NotNull FXSLParser.FunctionCallContext ctx) {
    Expression function = visit(ctx.func);
    List<Expression> args = new ArrayList<>();
    for (FXSLParser.ExprContext p : ctx.params) {
      args.add(p.accept(this));
    }
    return new FunctionCall(context.getCurrentScope(), function, args);
  }

  @Override
  public Uniform visitUniform(@NotNull FXSLParser.UniformContext ctx) {
    String name;
    Type declaredType;
    if (ctx.optTypeKeyValue().typeKeyValue() != null) {
      name = ctx.optTypeKeyValue().typeKeyValue().Identifier().getText();
      if (context.isInsideVariableDeclaration()) {
        throw new IllegalStateException(
            "Should not be possible to have uniform expression in a variable's declared type");
      }
      declaredType = ctx.optTypeKeyValue().typeKeyValue().type().accept(context.getTypeVisitor());
    } else {
      name = ctx.optTypeKeyValue().Identifier().getText();
      declaredType = null;
    }

    Uniform uniform = new Uniform(context.getCurrentScope(), name);
    if (declaredType != null) {
      context.getEnvironment().setExpressionType(uniform, declaredType);
    }
    return uniform;
  }

  @Override
  public FunctionCall visitUnaryExpression(@NotNull FXSLParser.UnaryExpressionContext ctx) {
    Expression arg = ctx.expr().accept(this);
    Expression function = new VariableReference(context.getCurrentScope(), ctx.op.getText());
    return new FunctionCall(context.getCurrentScope(), function, Collections.singletonList(arg));
  }

  @Override
  public StructValue visitStruct(@NotNull FXSLParser.StructContext ctx) {
    Map<String, Expression> fields = new HashMap<>();
    for (FXSLParser.ExprKeyValueContext f : ctx.exprKeyValue()) {
      Expression value = f.expr().accept(this);
      fields.put(f.Identifier().getText(), value);
    }
    return new StructValue(context.getCurrentScope(), fields);
  }

  @Override
  public PrimitiveValue visitPrimitive(@NotNull FXSLParser.PrimitiveContext ctx) {
    if (ctx.Boolean() != null) {
      return new PrimitiveValue(Boolean.parseBoolean(ctx.Boolean().getText()));
    } else if (ctx.Integer() != null) {
      return new PrimitiveValue(Integer.parseInt(ctx.Integer().getText()));
    } else if (ctx.Float() != null) {
      return new PrimitiveValue(Float.parseFloat(ctx.Float().getText()));
    } else {
      throw new IllegalStateException("Not a real primitive value");
    }
  }

  @Override
  public Expression visitDynamicArray(@NotNull FXSLParser.DynamicArrayContext ctx) {
    Expression length = ctx.length.accept(this);
    Expression elements = ctx.func.accept(this);
    return new DynamicArrayValue(context.getCurrentScope(), length, elements);
  }

}
