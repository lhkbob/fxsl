package com.lhkbob.fxsl.parser;

import com.lhkbob.fxsl.lang.Scope;
import com.lhkbob.fxsl.lang.expr.ArrayLength;
import com.lhkbob.fxsl.lang.expr.Expression;
import com.lhkbob.fxsl.lang.type.AliasType;
import com.lhkbob.fxsl.lang.type.ArrayType;
import com.lhkbob.fxsl.lang.type.FunctionType;
import com.lhkbob.fxsl.lang.type.MetaType;
import com.lhkbob.fxsl.lang.type.ParametricType;
import com.lhkbob.fxsl.lang.type.PrimitiveType;
import com.lhkbob.fxsl.lang.type.StructType;
import com.lhkbob.fxsl.lang.type.Type;
import com.lhkbob.fxsl.lang.type.TypePath;
import com.lhkbob.fxsl.lang.type.UnionType;

import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.lhkbob.fxsl.util.Preconditions.notNull;

/**
 *
 */
public class TypeVisitor extends FXSLBaseVisitor<Type> {
  private final ParseContext context;

  private final Map<Scope, Map<String, Integer>> lengthLabelMap;
  private int lengthWildcardCounter;

  public TypeVisitor(ParseContext context) {
    notNull("context", context);
    this.context = context;
    lengthLabelMap = new HashMap<>();
    lengthWildcardCounter = 0;
  }

  @Override
  public Type visitUnionType(@NotNull FXSLParser.UnionTypeContext ctx) {
    List<Type> types = new ArrayList<>();
    int element = 0;
    for (FXSLParser.TypeContext t : ctx.type()) {
      if (context.isInsideVariableDeclaration()) {
        context.getCurrentTypePath().pushUnionElement(element);
      }
      try {
        types.add(t.accept(this));
      } finally {
        if (context.isInsideVariableDeclaration()) {
          context.getCurrentTypePath().pop();
        }
      }
      element++;
    }
    return new UnionType(types);
  }

  @Override
  public Type visitAliasType(@NotNull FXSLParser.AliasTypeContext ctx) {
    // Alias types are not part of type paths
    String name = ctx.Identifier().getText();
    if (name.startsWith("_")) {
      if (!context.isInsideVariableDeclaration()) {
        throw new IllegalStateException(
            "Cannot specify a wildcard type outside of a variable declaration: " + name);
      }

      if (name.length() == 1) {
        // the name is only _, so that marks an anonymous type
        return new MetaType(context.getCurrentScope());
      } else {
        // create a parametric type
        return new ParametricType(context.getCurrentScope(), name.substring(1));
      }
    } else {
      // this is a type name reference
      return new AliasType(context.getCurrentScope(), name);
    }
  }

  @Override
  public Type visitArrayType(@NotNull FXSLParser.ArrayTypeContext ctx) {
    // Get the component type, updating the type path as the type is parsed
    if (context.isInsideVariableDeclaration()) {
      context.getCurrentTypePath().pushArrayComponent();
    }
    Type componentType;
    try {
      componentType = ctx.type().accept(this);
    } finally {
      if (context.isInsideVariableDeclaration()) {
        context.getCurrentTypePath().pop();
      }
    }

    // Complete the array type by parsing the length
    TerminalNode constantLength = ctx.arrayLength().Integer();
    if (constantLength != null) {
      int length = Integer.parseInt(constantLength.getText());
      if (length <= 0) {
        throw new IllegalStateException("Array length must be at least 1 or a wildcard");
      }
      return new ArrayType(componentType, new ArrayType.Length(length));
    } else {
      // wildcard array length
      String lengthName = ctx.arrayLength().Identifier().getText();
      if (context.isInsideVariableDeclaration()) {
        throw new IllegalStateException(
            "Cannot specify a wildcard array length inside a type declaration: " + lengthName);
      }

      int lengthID = getLengthID(lengthName);

      // Create an expression in the current scope for this array, using the current type path
      TypePath path = context.getCurrentTypePath().create();
      Expression length = new ArrayLength(context.getCurrentScope(), path);
      context.getEnvironment().addDeclaredVariable(context.getCurrentScope(), lengthName, length);
      context.getEnvironment().setExpressionType(length, PrimitiveType.INT);
      return new ArrayType(componentType, new ArrayType.Length(lengthID));
    }
  }

  private int getLengthID(String label) {
    Map<String, Integer> map = lengthLabelMap.get(context.getCurrentScope());
    if (map == null) {
      map = new HashMap<>();
      lengthLabelMap.put(context.getCurrentScope(), map);
    }
    Integer id = map.get(label);
    if (id == null) {
      // Assign a new id, this is only ever called regarding a label in the current scope.
      // If there's a higher scope with the same label, this is a new parameter and there's no need to
      // recurse up parent scopes. Type inference may well bind them to the exact same thing, but that
      // is handled later.
      id = lengthWildcardCounter--; // go negative for wildcard ids
      map.put(label, id);
    }

    return id;
  }

  @Override
  public Type visitFunctionType(@NotNull FXSLParser.FunctionTypeContext ctx) {
    int param = 0;
    List<Type> parameters = new ArrayList<>(ctx.params.size());
    for (FXSLParser.TypeContext f : ctx.params) {
      // Record the current type path node as each parameter is parsed.
      if (context.isInsideVariableDeclaration()) {
        context.getCurrentTypePath().pushFunctionParameter(param++);
      }
      try {
        parameters.add(f.accept(this));
      } finally {
        if (context.isInsideVariableDeclaration()) {
          context.getCurrentTypePath().pop();
        }
      }
    }

    // Record the return type in the type path as well.
    if (context.isInsideVariableDeclaration()) {
      context.getCurrentTypePath().pushFunctionReturn();
    }
    try {
      return new FunctionType(parameters, visit(ctx.returnType));
    } finally {
      if (context.isInsideVariableDeclaration()) {
        context.getCurrentTypePath().pop();
      }
    }
  }

  @Override
  public Type visitStructType(@NotNull FXSLParser.StructTypeContext ctx) {
    Map<String, Type> fields = new HashMap<>(ctx.typeKeyValue().size());
    for (FXSLParser.TypeKeyValueContext f : ctx.typeKeyValue()) {
      String name = f.Identifier().getText();

      // Record the current type path node as each field is parsed.
      if (context.isInsideVariableDeclaration()) {
        context.getCurrentTypePath().pushStructField(name);
      }
      try {
        fields.put(name, f.type().accept(this));
      } finally {
        if (!context.isInsideVariableDeclaration()) {
          context.getCurrentTypePath().pop();
        }
      }
    }
    return new StructType(fields);
  }
}
