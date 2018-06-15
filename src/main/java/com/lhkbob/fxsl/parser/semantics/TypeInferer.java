package com.lhkbob.fxsl.parser.semantics;

import com.lhkbob.fxsl.lang.Declaration;
import com.lhkbob.fxsl.lang.Environment;
import com.lhkbob.fxsl.lang.Scope;
import com.lhkbob.fxsl.lang.expr.ArrayAccess;
import com.lhkbob.fxsl.lang.expr.ArrayLength;
import com.lhkbob.fxsl.lang.expr.ArrayValue;
import com.lhkbob.fxsl.lang.expr.Attribute;
import com.lhkbob.fxsl.lang.expr.DynamicArrayValue;
import com.lhkbob.fxsl.lang.expr.Expression;
import com.lhkbob.fxsl.lang.expr.FunctionCall;
import com.lhkbob.fxsl.lang.expr.FunctionValue;
import com.lhkbob.fxsl.lang.expr.IfThenElse;
import com.lhkbob.fxsl.lang.expr.NativeExpression;
import com.lhkbob.fxsl.lang.expr.Parameter;
import com.lhkbob.fxsl.lang.expr.PrimitiveValue;
import com.lhkbob.fxsl.lang.expr.StructFieldAccess;
import com.lhkbob.fxsl.lang.expr.StructValue;
import com.lhkbob.fxsl.lang.expr.Uniform;
import com.lhkbob.fxsl.lang.expr.UnionValue;
import com.lhkbob.fxsl.lang.expr.VariableReference;
import com.lhkbob.fxsl.lang.type.AliasType;
import com.lhkbob.fxsl.lang.type.ArrayType;
import com.lhkbob.fxsl.lang.type.DefaultTypeVisitor;
import com.lhkbob.fxsl.lang.type.FunctionType;
import com.lhkbob.fxsl.lang.type.MetaType;
import com.lhkbob.fxsl.lang.type.ParametricType;
import com.lhkbob.fxsl.lang.type.PrimitiveType;
import com.lhkbob.fxsl.lang.type.StructType;
import com.lhkbob.fxsl.lang.type.Type;
import com.lhkbob.fxsl.lang.type.TypeRewritingVisitor;
import com.lhkbob.fxsl.lang.type.UnionType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 *
 */
public class TypeInferer implements SemanticsChecker {
  @Override
  public boolean continueOnFailure() {
    return false;
  }

  @Override
  public void validate(Environment environment) throws SemanticsException {

  }

  private static Type infer(Expression expr, Environment env, Stack<Declaration<Expression>> variableStack) {
    Type inferred = env.getExpressionType(expr);
    if (inferred instanceof MetaType) {
      // Type has not yet been inferred for the expression
      Environment workingEnv = env.push();
      inferred = inferImpl(expr, workingEnv, variableStack);
      if (inferred == null) {
        workingEnv.abort();
        throw new SemanticsException(
            "Type inference failed",
            Collections.singletonList(new SemanticsProblem.ExpressionProblem(expr)));
      } else {
        workingEnv.setExpressionType(expr, inferred);
        workingEnv.commit();
      }
    }

    return inferred;
  }

  private static Type inferImpl(Expression expr, final Environment env, final Stack<Declaration<Expression>> variableStack) {
    final Scope scope = expr.getScope();
    return expr.accept(
        new Expression.Visitor<Type>() {
          @Override
          public Type visitArray(ArrayValue expr) {
            // Arrays cannot be empty so the first element always exists.
            Type componentType = infer(expr.getElement(0), env, variableStack);
            for (int i = 1; i < expr.getLength(); i++) {
              componentType = unify(infer(expr.getElement(i), env, variableStack), componentType, env);
              if (componentType == null) {
                // FIXME find a way of reporting a semantics problem from here
                return null;
              }
            }
            componentType = generalize(componentType, env);
            return new ArrayType(componentType, new ArrayType.Length(expr.getLength()));
          }

          @Override
          public Type visitArrayAccess(ArrayAccess expr) {
            Type indexType = unify(infer(expr.getIndex(), env, variableStack), PrimitiveType.INT, env);
            if (indexType == null) {
              // FIXME
              return null;
            }
            Type arrayType = unify(infer(expr.getArray(), env, variableStack),
                new ArrayType(new MetaType(scope), env.newLengthWildcard()), env);
            if (arrayType == null) {
              // FIXME
              return null;
            }
            return generalize(((ArrayType) arrayType).getComponentType(), env);
          }

          @Override
          public Type visitArrayLength(ArrayLength length) {
            // FIXME walk the type path until the end, and verify that it is in
            // fact an array. Then return INT
            return PrimitiveType.INT;
          }

          @Override
          public Type visitAttribute(Attribute expr) {
            // Attributes were either declared with a type, or are completely
            // dependent on their context to infer their type. The environment
            // contains this information already for us (it either returns the
            // declared type or a meta type if none was given).
            return generalize(env.getExpressionType(expr), env);
          }

          @Override
          public Type visitDynamicArray(DynamicArrayValue value) {
            return null;
          }

          @Override
          public Type visitFieldAccess(StructFieldAccess access) {
            // FIXME let's say there's a parameter that is accessed as a struct
            // twice in a function with two different field names.
            // If we infer its type from each field access, we will end up binding
            // the parameters meta type first to a struct with a single field of the
            // first access and a meta type (which then unifies with whatever is necessary
            // for the access). But then we do the second access and it sees that the
            // parameter is now a struct with a single field, not the same field as the second
            // so there's no intersection.
            //
            // In this particular circumstance, we actually want to have the parameter's type
            // be updated to include the new field. Could this be handled uniquely as part of
            // the field access unification?
            //
            // Under what circumstances are we not allowed to add a field to a struct type?
            // - At least when the struct value was created by an actual StructValue expresison and
            // not a reference to another instance.
            return null;
          }

          @Override
          public Type visitFunction(FunctionValue function) {
            return null;
          }

          @Override
          public Type visitFunctionCall(FunctionCall function) {
            return null;
          }

          @Override
          public Type visitIfThenElse(IfThenElse expr) {
            Type conditionType = unify(infer(expr.getCondition(), env, variableStack),
                PrimitiveType.BOOL, env);
            if (conditionType == null) {
              // FIXME
              return null;
            }
            Type trueExpr = infer(expr.getTrueExpression(), env, variableStack);
            Type falseExpr = infer(expr.getFalseExpression(), env, variableStack);
            Type unified = unify(trueExpr, falseExpr, env);
            if (unified == null) {
              // FIXME
              return null;
            }
            return generalize(unified, env);
          }

          @Override
          public Type visitNativeExpression(NativeExpression expr) {
            // Native expressions have explicitly declared concrete types
            return expr.getType();
          }

          @Override
          public Type visitParameter(Parameter param) {
            // FIXME if the parameter is being used within the function body, how do we resolve that?
            // Does it get treated like a normal variable declaration? Without any special handling here
            // that I think will cause us to process the function body twice; but if we stick the
            // parameter declaraiton into the stack now before asking to infer the function body then
            // we will hopefully skip that?
            //
            // Perhaps there needs to be something special done inside visitFunction() that
            // first visits all declared variables in its scope?
            int index = param.getFunction().getParameters().indexOf(param.getName());
            Type functionType = infer(param.getFunction(), env, variableStack);
            return ((FunctionType) functionType).getParameterType(index);
          }

          @Override
          public Type visitPrimitive(PrimitiveValue expr) {
            // Primitive types do not need to be inferred
            return expr.getType();
          }

          @Override
          public Type visitStruct(StructValue struct) {
            return null;
          }

          @Override
          public Type visitUniform(Uniform expr) {
            // Uniforms were either declared with a type, or are completely
            // dependent on their context to infer their type. The environment
            // contains this information already for us (it either returns the
            // declared type or a meta type if none was given).
            return generalize(env.getExpressionType(expr), env);
          }

          @Override
          public Type visitUnion(UnionValue union) {
          }

          @Override
          public Type visitVariable(VariableReference var) {
            return null;
          }
        });
  }

  private static Type generalize(Type type, final Environment env) {
    return type.accept(
        new TypeRewritingVisitor() {
          @Override
          public Type visitAliasType(AliasType type) {
            // This assumes that the alias has no cycles and that it is defined
            // e.g. this must be run after CyclicTypeChecker.
            Type declared = env.getDeclaredType(type.getScope(), type.getLabel()).getValue();
            return declared.accept(this);
          }

          @Override
          public Type visitMetaType(MetaType meta) {
            Type bound = env.getBoundMetaType(meta);
            if (bound != null) {
              return bound.accept(this);
            } else {
              // A leaf meta type gets generalized into a parameter
              ParametricType param = env.newParametricType(meta.getScope());
              // Binding the meta type to param ensures that future encounters with meta
              // map to the same parameter type.
              env.bindMetaType(meta, param);
              return param;
            }
          }
        });
  }

  private static boolean occursIn(final Type target, Type containing, final Environment env) {
    return containing.accept(
        new DefaultTypeVisitor<Boolean>() {
          @Override
          public Boolean visitAliasType(AliasType type) {
            if (super.visitAliasType(type)) {
              return true;
            } else {
              // Assumes CyclicTypeChecker has been run.
              Type mapped = env.getDeclaredType(type.getScope(), type.getLabel()).getValue();
              return mapped != null && mapped.accept(this);
            }
          }

          @Override
          public Boolean visitMetaType(MetaType type) {
            if (super.visitMetaType(type)) {
              return true;
            } else {
              Type bound = env.getBoundMetaType(type);
              return bound != null && bound.accept(this);
            }
          }

          @Override
          protected boolean shortCircuit(Boolean value) {
            return value;
          }

          @Override
          protected Boolean initialValue(Type t) {
            return target.equals(t);
          }
        });
  }

  private static Type unify(final Type a, final Type b, final Environment env) {
    if (a.equals(b)) {
      // When the types are equal, they must obviously unify
      return a;
    } else if (!(a instanceof MetaType) && b instanceof MetaType) {
      // Keep meta types on the left hand side if possible
      return unify(b, a, env);
    } else {
      return a.accept(
          new Type.Visitor<Type>() {
            @Override
            public Type visitAliasType(AliasType a) {
              // Just follow the type alias, assumes its processed by CyclicTypeChecker already
              Type mapped = env.getDeclaredType(a.getScope(), a.getLabel()).getValue();
              return unify(mapped, b, env);
            }

            @Override
            public Type visitArrayType(ArrayType a) {
              // Array types can only unify with other array types, and only if their component
              // types unify and lengths are equal.
              if (!(b instanceof ArrayType)) {
                return null;
              }

              ArrayType o = (ArrayType) b;
              // Make sure component types unify.
              Type componentType = unify(a.getComponentType(), o.getComponentType(), env);
              if (componentType == null) {
                return null;
              }

              // Make sure the two array lengths unify.
              ArrayType.Length arrayLength = unifyLength(a.getLength(), o.getLength(), env);
              if (arrayLength == null) {
                return null;
              }
              return new ArrayType(componentType, arrayLength);
            }

            @Override
            public Type visitFunctionType(FunctionType a) {
              return null;
            }

            @Override
            public Type visitMetaType(MetaType a) {
              // Bind a to b, with proper care if b is also a meta type or if it would create
              // cyclic types.
              Type bound = env.getBoundMetaType(a);
              if (bound != null) {
                // Unify a's bound type with b.
                return unify(bound, b, env);
              } else if (b instanceof MetaType) {
                Type bBound = env.getBoundMetaType((MetaType) b);
                if (bBound != null) {
                  // Unify a with the type bound to b.
                  return unify(a, bBound, env);
                }
              } else if (occursIn(a, b, env)) {
                return null;
              }
              // Otherwise bind a to be and move on.
              env.bindMetaType(a, b);
              return b;
            }

            @Override
            public Type visitParametricType(ParametricType a) {
              // Type parameters don't unify with anything but themselves, which
              // is already handled by the root of the function.
              return null;
            }

            @Override
            public Type visitPrimitiveType(PrimitiveType a) {
              // Primitive types unify with themselves, and ints can be lifted to floats.
              if (!(b instanceof PrimitiveType)) {
                return null;
              }
              if (a == PrimitiveType.INT) {
                if (b == PrimitiveType.FLOAT) {
                  // Upgrade to a float from an int
                  return PrimitiveType.FLOAT;
                }
              } else if (a == PrimitiveType.FLOAT) {
                if (b == PrimitiveType.INT) {
                  // Upgrade from an int to a float
                  return PrimitiveType.INT;
                }
              }

              if (a == b) {
                // All other combinations of primitive types unify only with themselves
                return a;
              } else {
                return null;
              }
            }

            @Override
            public Type visitStructType(StructType a) {
              // Struct types unify with other struct types that have a non-empty
              // intersection of fields with unifiable types.
              if (!(b instanceof StructType)) {
                return null;
              }

              StructType o = (StructType) b;
              Map<String, Type> unifiedFields = new HashMap<>();
              for (String fieldName : a.getFieldTypes().keySet()) {
                Type fieldA = a.getFieldType(fieldName);
                Type fieldB = o.getFieldType(fieldName);
                if (fieldB != null) {
                  // Both a and b have a field of the same name.
                  Type fieldType = unify(fieldA, fieldB, env);
                  if (fieldType != null) {
                    unifiedFields.put(fieldName, fieldType);
                  } else {
                    // Although they have a shared field name, types do not match.
                    return null;
                  }
                }
              }

              // The intersection must be non-empty to be a valid unification.
              if (unifiedFields.isEmpty()) {
                return null;
              } else {
                return new StructType(unifiedFields);
              }
            }

            @Override
            public Type visitUnionType(UnionType a) {
              return null;
            }
          });
    }
  }

  private static ArrayType.Length unifyLength(
      ArrayType.Length a, ArrayType.Length b, Environment env) {
    if (a.equals(b)) {
      // If the lengths are equal, they unify with one instance selected arbitrarily.
      return a;
    } else if (!a.isWildcard() && b.isWildcard()) {
      // Keep wildcard lengths on the same side
      return unifyLength(b, a, env);
    } else if (a.isWildcard()) {
      // Resolve a's reference and continue
      ArrayType.Length bound = env.getBoundArrayLength(a);
      if (bound != null) {
        // Unify to b's length
        return unifyLength(bound, b, env);
      } else if (b.isWildcard()) {
        ArrayType.Length bBound = env.getBoundArrayLength(b);
        if (bBound != null) {
          // Unify a with what's bound to b
          return unifyLength(a, bBound, env);
        }
      } // No occurs-in check for lengths

      // Bind a to b
      env.bindArrayLength(a, b);
      return b;
    } else {
      // Both a and b are concrete lengths. Concrete lengths only unify if they are equal, but
      // since that's the necessary condition of equals(), if we got here these are two concrete
      // lengths that are NOT equal so they do not unify.
      return null;
    }
  }
}

