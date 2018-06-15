package com.lhkbob.fxsl.lang.type;

import com.lhkbob.fxsl.lang.Environment;
import com.lhkbob.fxsl.lang.InvalidExpressionException;
import com.lhkbob.fxsl.lang.Scope;
import com.lhkbob.fxsl.lang.expr.ArrayAccess;
import com.lhkbob.fxsl.lang.expr.ArrayLength;
import com.lhkbob.fxsl.lang.expr.ArrayValue;
import com.lhkbob.fxsl.lang.expr.Expression;
import com.lhkbob.fxsl.lang.expr.FunctionCall;
import com.lhkbob.fxsl.lang.expr.FunctionValue;
import com.lhkbob.fxsl.lang.expr.NativeExpression;
import com.lhkbob.fxsl.lang.expr.Parameter;
import com.lhkbob.fxsl.lang.expr.PrimitiveValue;
import com.lhkbob.fxsl.lang.expr.StructFieldAccess;
import com.lhkbob.fxsl.lang.expr.StructValue;
import com.lhkbob.fxsl.lang.expr.UnionValue;
import com.lhkbob.fxsl.lang.expr.VariableReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public final class Types {
  private Types() {
  }

  public static Type generalize(Type type, final Environment env) {
    return type.accept(
        new Type.Visitor<Type>() {
          @Override
          public Type visitAliasType(AliasType t) {
            // FIXME where do we handle type cycle checking? can it be done so that it solves meta type cycles as well?
            Type lookup = env.getType(t.getScope(), t.getLabel());
            if (lookup != null) {
              return generalize(lookup, env);
            } else {
              return t;
            }
          }

          @Override
          public Type visitArrayType(ArrayType t) {
            Type component = generalize(t.getComponentType(), env);
            if (component != t.getComponentType()) {
              // meta type got lifted up
              if (t.getWildcardLength() == null) {
                return new ArrayType(t.getComponentType(), t.getConcreteLength());
              } else {
                return new ArrayType(t.getComponentType(), t.getWildcardLength());
              }
            } else {
              // no metas to change
              return t;
            }
          }

          @Override
          public Type visitFunctionType(FunctionType t) {
            boolean changed = false;
            List<Type> pTypes = new ArrayList<>();
            for (Type p : t.getParameterTypes()) {
              Type newP = generalize(p, env);
              if (newP != p) {
                // meta was lifted up
                changed = true;
              }
              pTypes.add(newP);
            }

            Type returnType = generalize(t.getReturnType(), env);
            if (changed || returnType != t.getReturnType()) {
              return new FunctionType(pTypes, returnType);
            } else {
              return t;
            }
          }

          @Override
          public Type visitMetaType(MetaType t) {
            Type bound = env.getMetaTypeBinding(t);
            if (bound != null) {
              return generalize(bound, env);
              // FIXME think about this, can this create an infinite loop?
            } else {
              // this is a leaf meta type, need a new label
              ParametricType label = new ParametricType(t.getScope(), "FIXME");
              env.bindMetaType(t, label);
              return label;
            }
          }

          @Override
          public Type visitParametricType(ParametricType t) {
            // do nothing, definitely not a meta type and has no child types
            return t;
          }

          @Override
          public Type visitPrimitiveType(PrimitiveType t) {
            // do nothing, definitely not a meta type and has no child types
            return t;
          }

          @Override
          public Type visitStructType(StructType t) {
            boolean changed = false;
            Map<String, Type> fTypes = new HashMap<>();
            for (Map.Entry<String, Type> f : t.getFieldTypes().entrySet()) {
              Type newF = generalize(f.getValue(), env);
              if (newF != f.getValue()) {
                changed = true;
              }
              fTypes.put(f.getKey(), newF);
            }

            if (changed) {
              return new StructType(fTypes);
            } else {
              return t;
            }
          }

          @Override
          public Type visitUnionType(UnionType t) {
            boolean changed = false;
            Set<Type> options = new HashSet<>();
            for (Type u : t.getOptions()) {
              Type newU = generalize(u, env);
              if (newU != u) {
                changed = true;
              }
              options.add(newU);
            }

            if (changed) {
              return new UnionType(options);
            } else {
              return t;
            }
          }
        });
  }

  public static Type infer(Expression expr, final Environment env) {
    // FIXME everywhere that does unification and then generalization is going to need create a child
    // environment and commit it upon success
    final Scope scope = expr.getScope();
    return expr.accept(
        new Expression.Visitor<Type>() {
          @Override
          public Type visitArray(ArrayValue value) {
            // arrays cannot be empty so the first element always exists
            Type componentType = infer(value.getElement(0), env);
            for (int i = 1; i < value.getLength(); i++) {
              componentType = unify(infer(value.getElement(i), env), componentType, env);
              if (componentType == null) {
                throw new InvalidExpressionException(
                    value, "Array element expression types cannot be unified");
              }
            }
            // FIXME generalize out the component meta types?
            return new ArrayType(componentType, value.getLength());
          }

          @Override
          public Type visitArrayAccess(ArrayAccess access) {
            Type indexType = unify(infer(access.getIndex(), env), PrimitiveType.INT, env);
            if (indexType == null) {
              throw new InvalidExpressionException(access, "Index must be of type INT");
            }
            Type metaArrayType = new ArrayType(
                new MetaType(scope), new Parameter(
                scope, "FIXME",
                PrimitiveType.INT)); // FIXME the type here should be an array-length meta expression
            Type arrayType = unify(infer(access.getArray(), env), metaArrayType, env);
            if (arrayType == null || !(arrayType instanceof ArrayType)) {
              throw new InvalidExpressionException(access, "Value must be an array");
            }
            // FIXME generalize out meta types?
            return ((ArrayType) arrayType).getComponentType();
          }

          @Override
          public Type visitArrayLength(ArrayLength length) {
            // FIXME haven't figured out how to handle array lengths or parameters yet
            return null;
          }

          @Override
          public Type visitFieldAccess(StructFieldAccess access) {
            Type metaStructType = new StructType(
                Collections.singletonMap(
                    access.getField(), new MetaType(scope)));
            Type structType = unify(infer(access.getStruct(), env), metaStructType, env);
            if (structType == null || !(structType instanceof StructType)) {
              throw new InvalidExpressionException(
                  access, "Cannot use field access on a non-struct expression");
            }
            // FIXME generalize?
            return ((StructType) structType).getFieldType(access.getField());
          }

          @Override
          public Type visitFunction(FunctionValue function) {
            List<Type> paramTypes = new ArrayList<>(function.getParameters().size());
            for (Parameter p : function.getParameters()) {
              // FIXME should this include both explicit and meta types? what about type parameters?
              // FIXME should this call infer(p, env) or should that assume that we're within the
              // scope where p has been bound to some type?
              paramTypes.add(p.getType());
            }
            Type bodyType = infer(function.getReturnValue(), env);
            return new FunctionType(paramTypes, bodyType);
          }

          @Override
          public Type visitFunctionCall(FunctionCall function) {
            List<Type> paramTypes = new ArrayList<>(function.getSuppliedParameterCount());
            for (Expression p : function.getParameterValues()) {
              paramTypes.add(infer(p, env));
            }
            Type metaFunctionType = new FunctionType(paramTypes, new MetaType(scope));
            // FIXME I think this is where I should instantiate parameter types as well?
            // FIXME what about unions? is that part of unify? can we even handle it now or do we need to wait until actual parameters are passed in?
            Type functionType = unify(infer(function.getFunction(), env), metaFunctionType, env);
            if (functionType == null || !(functionType instanceof FunctionType)) {
              throw new InvalidExpressionException(
                  function,
                  "Cannot invoke function with provided arguments, or target was not invokeable");
            }
            // FIXME generalize?
            return ((FunctionType) functionType).getReturnType();
          }

          @Override
          public Type visitNativeExpression(NativeExpression expr) {
            // native expressions have known types that were configured within the native scope
            return expr.getType();
          }

          @Override
          public Type visitParameter(Parameter param) {
            // FIXME does this return param.infer()? does it return what the param has been instantiated with
            // during a function call? or otherwise resolve aliases etc?
            return null;
          }

          @Override
          public Type visitPrimitive(PrimitiveValue primitive) {
            // primitive types are known a priori based on their compiled value
            return primitive.getType();
          }

          @Override
          public Type visitStruct(StructValue struct) {
            Map<String, Type> fieldTypes = new HashMap<>();
            for (Map.Entry<String, Expression> field : struct.getFields().entrySet()) {
              fieldTypes.put(field.getKey(), infer(field.getValue(), env));
            }
            return new StructType(fieldTypes);
          }

          @Override
          public Type visitUnion(UnionValue union) {
            // FIXME don't even know what to do here
            return null;
          }

          @Override
          public Type visitVariable(VariableReference var) {
            Expression e = env.getVariable(scope, var.getVariableName());
            if (e == null) {
              throw new InvalidExpressionException(var, "Variable is undefined in scope");
            }
            return infer(e, env);
          }
        });
  }

  public static boolean occursIn(final Type a, final Type b, final Environment environment) {
    if (a.equals(b)) {
      return true;
    }

    return b.accept(
        new Type.Visitor<Boolean>() {
          @Override
          public Boolean visitAliasType(AliasType t) {
            Type alias = environment.getType(t.getScope(), t.getLabel());
            return alias != null && occursIn(a, alias, environment);
          }

          @Override
          public Boolean visitArrayType(ArrayType t) {
            return occursIn(a, t.getComponentType(), environment);
          }

          @Override
          public Boolean visitFunctionType(FunctionType t) {
            for (Type p : t.getParameterTypes()) {
              if (occursIn(a, p, environment)) {
                return true;
              }
            }
            return occursIn(a, t.getReturnType(), environment);
          }

          @Override
          public Boolean visitMetaType(MetaType t) {
            Type bound = environment.getMetaTypeBinding(t);
            return bound != null && occursIn(a, bound, environment);
          }

          @Override
          public Boolean visitParametricType(ParametricType t) {
            return false;
          }

          @Override
          public Boolean visitPrimitiveType(PrimitiveType t) {
            return false;
          }

          @Override
          public Boolean visitStructType(StructType t) {
            for (Type f : t.getFieldTypes().values()) {
              if (occursIn(a, f, environment)) {
                return true;
              }
            }
            return false;
          }

          @Override
          public Boolean visitUnionType(UnionType t) {
            for (Type u : t.getOptions()) {
              if (occursIn(a, u, environment)) {
                return true;
              }
            }
            return false;
          }
        });
  }

  public static Type resolveAlias(Type t, Environment env) {
    while (t instanceof AliasType) {
      AliasType a = (AliasType) t;
      Type b = env.getType(a.getScope(), a.getLabel());
      if (b != null) {
        t = b;
      } else {
        // return the most specific alias
        break;
      }
    }

    return t;
  }

  public static Type substitute(
      final Type a, final Type b, final Type replaceWith, final Environment environment) {
    if (a.equals(b)) {
      return replaceWith;
    }
    return b.accept(
        new Type.Visitor<Type>() {
          @Override
          public Type visitAliasType(AliasType t) {
            Type bound = environment.getType(t.getScope(), t.getLabel());
            if (bound != null) {
              return substitute(a, bound, replaceWith, environment);
            } else {
              // this is a leaf type not equal to a, so it can't be substituted
              return t;
            }
          }

          @Override
          public Type visitArrayType(ArrayType t) {
            Type component = substitute(a, t.getComponentType(), replaceWith, environment);
            if (t.getWildcardLength() != null) {
              return new ArrayType(component, t.getWildcardLength());
            } else {
              return new ArrayType(component, t.getConcreteLength());
            }
          }

          @Override
          public Type visitFunctionType(FunctionType t) {
            Type returnType = substitute(a, t.getReturnType(), replaceWith, environment);
            List<Type> paramTypes = new ArrayList<>();
            for (Type p : t.getParameterTypes()) {
              paramTypes.add(substitute(a, p, replaceWith, environment));
            }
            return new FunctionType(paramTypes, returnType);
          }

          @Override
          public Type visitMetaType(MetaType t) {
            Type bound = environment.getMetaTypeBinding(t);
            if (bound != null) {
              return substitute(a, bound, replaceWith, environment);
            } else {
              // leaf type not equal to a, so it can't be substituted
              return t;
            }
          }

          @Override
          public Type visitParametricType(ParametricType t) {
            // this is a leaf type not equal to a, so it can't be substituted
            return t;
          }

          @Override
          public Type visitPrimitiveType(PrimitiveType t) {
            // this is a leaf type not equal to a, so it can't be substituted
            return t;
          }

          @Override
          public Type visitStructType(StructType t) {
            Map<String, Type> fields = new HashMap<>();
            for (Map.Entry<String, Type> f : t.getFieldTypes().entrySet()) {
              fields.put(f.getKey(), substitute(a, f.getValue(), replaceWith, environment));
            }
            return new StructType(fields);
          }

          @Override
          public Type visitUnionType(UnionType t) {
            Set<Type> union = new HashSet<>();
            for (Type u : t.getOptions()) {
              union.add(substitute(a, u, replaceWith, environment));
            }
            return new UnionType(union);
          }
        });
  }

  public static Type unify(Type a, Type b, Environment environment) {
    return unifyImpl(resolveAlias(a, environment), resolveAlias(b, environment), environment);
  }

  private static Type unifyImpl(final Type a, final Type b, final Environment environment) {
    if (a.equals(b)) {
      // simple case, if a type is equal to itself it's definitely unified
      return a;
    } else if (!(a instanceof MetaType) && b instanceof MetaType) {
      // swap around the types
      return unify(b, a, environment);
    }

    // the following code assumes that all meaningful aliases have been resolved
    return a.accept(
        new Type.Visitor<Type>() {
          @Override
          public Type visitAliasType(AliasType t) {
            // meaningful aliases should have been resolved to real type at this point
            // if we got here then there's some alias that hasn't been defined so it doesn't make
            // sense to unify it with anything
            return null;
          }

          @Override
          public Type visitArrayType(ArrayType t) {
            if (!(b instanceof ArrayType)) {
              // can't unify anything other than arrays
              return null;
            }
            ArrayType o = (ArrayType) b;

            Type componentType = unify(t.getComponentType(), o.getComponentType(), environment);
            if (componentType == null) {
              return null;
            }

            if (t.getWildcardLength() == null && o.getWildcardLength() == null) {
              // both are concrete arrays so they must have the same length
              if (t.getConcreteLength() != o.getConcreteLength()) {
                return null;
              }
              // otherwise lengths agree
              return new ArrayType(componentType, t.getConcreteLength());
            } else if (t.getWildcardLength() != null && o.getWildcardLength() != null) {
              // both are wildcards, so collapse b's length expression into a's (this assumes that
              // any direct references to b's length are updated later)
              // FIXME I'm not entirely sure yet but I can't help but think this is going to cause
              // problems later on. Multiple array types are going to reference the exact same
              // parameter expression; that's a little scary and I feel like we could easily
              // run into issues with cyclic parameters. We need to either treat parameters specially,
              // e.g. a reference to the type that owns them (hard since those types want the parameters
              // first, unless we go back to strings/types in the ctors)
              environment.addVariable(
                  o.getWildcardLength().getScope(), o.getWildcardLength().getParameterName(),
                  PrimitiveType.INT, t.getWildcardLength());
              return new ArrayType(componentType, t.getWildcardLength());
            } else if (o.getWildcardLength() != null) {
              // the wildcard goes away (but must record in the environment that it maps to a constant now)
              // FIXME figure out how to handle the recording of such a binding to a concrete length
              return new ArrayType(componentType, t.getConcreteLength());
            } else { // t.getWildcardLength() != null
              // as above but o is the concrete length provider
              return new ArrayType(componentType, o.getConcreteLength());
            }
          }

          @Override
          public Type visitFunctionType(FunctionType t) {
            // FIXME make sure this handles currying, that's the expectation of the infer function
            if (b instanceof UnionType) {
              // FIXME what does this look like?
              // 1. unification is a new union including t
              // 2. " is a the unification of any element in the union
              // 3. " is the unification of the best element in the union
              //
              // Does this depend on the scenario that is calling unify?
            } else if (!(b instanceof FunctionType)) {
              // function types only unify with other functions (and unions of functions)
            }

            // two functions unify if WLOG a's parameter types are assignable to b's, and
            // unification of return types is assignable to a's return type

            // what is the unified function type? b's parameters or the unification of a's and b's?
            // return type is a's return type or unification of a's and b's
            // FIXME check the compilers book to see what they do for functions
            return null;
          }

          @Override
          public Type visitMetaType(MetaType t) {
            Type bound = environment.getMetaTypeBinding(t);
            if (bound != null) {
              // unify the pointed to type with b
              return unify(bound, b, environment);
            } else if (b instanceof MetaType) {
              Type bBound = environment.getMetaTypeBinding((MetaType) b);
              if (bBound != null) {
                // unify a with the type pointed to by b
                return unify(a, bBound, environment);
              }
            } else if (occursIn(t, b, environment)) {
              return null;
            }
            // otherwise bind t to b and move on
            environment.bindMetaType(t, b);
            return b;
          }

          @Override
          public Type visitParametricType(ParametricType t) {
            // type parameters don't unify with anything but themselves
            // type inference algorithms must first replace type parameters with instantiated meta types
            // and then generalize
            return null;
          }

          @Override
          public Type visitPrimitiveType(PrimitiveType t) {
            if (!(b instanceof PrimitiveType)) {
              return null;
            }
            if (t == PrimitiveType.INT) {
              if (b == PrimitiveType.FLOAT) {
                // upgrade to a float from an int
                return PrimitiveType.FLOAT;
              }
            } else if (t == PrimitiveType.FLOAT) {
              if (b == PrimitiveType.INT) {
                // upgrade from an int to a float
                return PrimitiveType.FLOAT;
              }
            }

            if (t == b) {
              // all other combinations of primitive types only unify with themselves
              return t;
            } else {
              return null;
            }
          }

          @Override
          public Type visitStructType(StructType t) {
            if (!(b instanceof StructType)) {
              // structs only unify with other structs (and meta's handled above)
              return null;
            }

            StructType o = (StructType) b;
            Map<String, Type> unifiedFields = new HashMap<>();

            // can iterate over one type arbitrarily since we care about intersection of field names
            for (String fieldName : t.getFieldTypes().keySet()) {
              Type fieldA = t.getFieldType(fieldName);
              Type fieldB = o.getFieldType(fieldName);
              if (fieldB != null) {
                // shared field name, make sure the types unify as well
                Type fieldType = unify(fieldA, fieldB, environment);
                if (fieldType != null) {
                  unifiedFields.put(fieldName, fieldType);
                } else {
                  // type mismatch on common field name so the structs are incompatible
                  return null;
                }
              }
            }

            // need at least one shared field
            if (unifiedFields.isEmpty()) {
              return null;
            } else {
              return new StructType(unifiedFields);
            }
          }

          @Override
          public Type visitUnionType(UnionType t) {
            return null;
          }
        });
  }
}
