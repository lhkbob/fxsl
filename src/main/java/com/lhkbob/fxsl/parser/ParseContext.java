package com.lhkbob.fxsl.parser;

import com.lhkbob.fxsl.lang.Environment;
import com.lhkbob.fxsl.lang.Scope;
import com.lhkbob.fxsl.lang.expr.VariableReference;
import com.lhkbob.fxsl.lang.type.TypePath;

/**
 *
 */
public class ParseContext {
  private final DeclarationVisitor declVisitor;
  private final Environment environment;
  private final ExpressionVisitor exprVisitor;
  private final TypeVisitor typeVisitor;

  private Scope currentScope;

  private TypePath.Builder currentTypePath; // null implies no variable source, e.g. type declaration

  public ParseContext() {
    environment = new Environment();
    currentScope = environment.getRootScope();

    declVisitor = new DeclarationVisitor(this);
    exprVisitor = new ExpressionVisitor(this);
    typeVisitor = new TypeVisitor(this);

    currentTypePath = null;
  }

  public void finishVariableDeclaration() {
    if (currentTypePath == null) {
      throw new IllegalStateException("No variable declaration to finish");
    }
    currentTypePath = null;
  }

  public Scope getCurrentScope() {
    return currentScope;
  }

  public TypePath.Builder getCurrentTypePath() {
    return currentTypePath;
  }

  public DeclarationVisitor getDeclarationVisitor() {
    return declVisitor;
  }

  public Environment getEnvironment() {
    return environment;
  }

  public ExpressionVisitor getExpressionVisitor() {
    return exprVisitor;
  }

  public TypeVisitor getTypeVisitor() {
    return typeVisitor;
  }

  public boolean isInsideVariableDeclaration() {
    return currentTypePath != null;
  }

  public void pop() {
    if (currentScope.equals(environment.getRootScope())) {
      throw new IllegalStateException("Cannot pop past the root scope");
    }
    currentScope = currentScope.getParent();
  }

  public Scope push() {
    currentScope = new Scope(currentScope);
    return currentScope;
  }

  public void startNewVariableDeclaration(String name) {
    if (currentTypePath != null) {
      throw new IllegalStateException("Cannot start a new variable before previous is finished");
    }
    currentTypePath = TypePath.newPath(new VariableReference(currentScope, name));
  }

}
