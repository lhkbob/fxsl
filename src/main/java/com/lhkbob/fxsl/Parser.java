package com.lhkbob.fxsl;

import com.lhkbob.fxsl.lang.expr.Expression;
import com.lhkbob.fxsl.lang.type.Types;
import com.lhkbob.fxsl.parser.ExpressionVisitor;
import com.lhkbob.fxsl.parser.FXSLLexer;
import com.lhkbob.fxsl.parser.FXSLParser;
import com.lhkbob.fxsl.parser.ParseContext;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 */
public class Parser {
  private final ParseContext context;

  public Parser() {
    context = new ParseContext();
  }

  public static void main(String[] args) {
    Parser p = new Parser();
    Expression e = p.parse(
        "var jar:(float,float -> float) = (foo, bar -> foo + bar); var foo = 5; var bar = foo + 3 + jar(3,5);foo+12");
    System.out.println(e);
    System.out.println(Types.infer(e, p.context.getEnvironment()));
  }

  public Expression parse(String content) {
    return parse(new ANTLRInputStream(content));
  }

  public Expression parseFile(File file) throws IOException {
    try (FileInputStream in = new FileInputStream(file)) {
      return parseStream(in);
    }
  }

  public Expression parseFile(String file) throws IOException {
    try (FileInputStream in = new FileInputStream(file)) {
      return parseStream(in);
    }
  }

  public Expression parseStream(InputStream stream) throws IOException {
    BufferedInputStream buffer = new BufferedInputStream(stream);
    return parse(new ANTLRInputStream(buffer));
  }

  private Expression parse(ANTLRInputStream in) {
    FXSLLexer lexer = new FXSLLexer(in);
    FXSLParser parser = new FXSLParser(new CommonTokenStream(lexer));

    ExpressionVisitor visitor = new ExpressionVisitor(context);
    return visitor.visit(parser.stmList());
  }
}
