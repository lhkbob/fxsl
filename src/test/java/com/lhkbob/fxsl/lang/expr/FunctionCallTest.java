package com.lhkbob.fxsl.lang.expr;

import com.lhkbob.fxsl.lang.Scope;
import com.lhkbob.fxsl.lang.type.FunctionType;
import com.lhkbob.fxsl.lang.type.FunctionTypeTest;
import com.lhkbob.fxsl.lang.type.MetaType;
import com.lhkbob.fxsl.lang.type.PrimitiveType;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * Basic unit tests for {@link com.lhkbob.fxsl.lang.expr.FunctionCall}.
 *
 * @author Michael Ludwig
 */
public class FunctionCallTest {
    @Test
    public void testSimpleFunctionCall() {
        FunctionValue function = new FunctionValue(new Scope(),
                                                   Arrays.asList(new ParameterExpression(new Scope(), "a",
                                                                                         PrimitiveType.INT)),
                                                   new PrimitiveValue(2));
        Scope scope = new Scope();
        FunctionCall call = new FunctionCall(scope, function, Arrays.asList(new PrimitiveValue(1)));

        assertEquals(scope, call.getScope());
        assertEquals(PrimitiveType.INT, call.getType());
        assertEquals(function, call.getFunction());
        assertEquals(1, call.getSuppliedParameterCount());
        assertEquals(new PrimitiveValue(1), call.getParameterValue(0));
        assertEquals(Arrays.<Expression>asList(new PrimitiveValue(1)), call.getParameterValues());
    }

    @Test
    public void testWildcardFunctionCall() {
        Expression function = new ParameterExpression(new Scope(), "a", new MetaType(new Scope()));
        Scope scope = new Scope();
        FunctionCall call = new FunctionCall(scope, function, Arrays.asList(new PrimitiveValue(1)));

        assertEquals(scope, call.getScope());
        assertTrue(call.getType() instanceof MetaType);
        assertEquals(function, call.getFunction());
        assertEquals(1, call.getSuppliedParameterCount());
        assertEquals(new PrimitiveValue(1), call.getParameterValue(0));
        assertEquals(Arrays.<Expression>asList(new PrimitiveValue(1)), call.getParameterValues());
    }

    @Test
    public void testSingleOptionUnionCall() {
        FunctionValue matched = new FunctionValue(new Scope(),
                                                  Arrays.asList(new ParameterExpression(new Scope(), "a",
                                                                                        PrimitiveType.INT)),
                                                  new PrimitiveValue(2));
        FunctionValue notMatched = new FunctionValue(new Scope(),
                                                     Arrays.asList(new ParameterExpression(new Scope(), "a",
                                                                                           PrimitiveType.BOOL)),
                                                     new PrimitiveValue(2.0f));
        UnionValue function = UnionValueTest.makeValue(new Scope(), matched, notMatched);
        FunctionCall call = new FunctionCall(new Scope(), function, Arrays.asList(new PrimitiveValue(1)));

        assertEquals(PrimitiveType.INT, call.getType());
        assertEquals(function, call.getFunction());
        assertEquals(1, call.getSuppliedParameterCount());
        assertEquals(new PrimitiveValue(1), call.getParameterValue(0));
        assertEquals(Arrays.<Expression>asList(new PrimitiveValue(1)), call.getParameterValues());
    }

    @Test
    public void testMultipleOptionUnionCall() {
        FunctionValue matched = new FunctionValue(new Scope(),
                                                  Arrays.asList(new ParameterExpression(new Scope(), "a",
                                                                                        PrimitiveType.INT)),
                                                  new PrimitiveValue(2));
        FunctionValue notMatched = new FunctionValue(new Scope(),
                                                     Arrays.asList(new ParameterExpression(new Scope(), "a",
                                                                                           PrimitiveType.BOOL)),
                                                     new PrimitiveValue(2.0f));
        UnionValue function = UnionValueTest.makeValue(new Scope(), matched, notMatched);
        FunctionCall call = new FunctionCall(new Scope(), function, Arrays.asList(new PrimitiveValue(1)));

        assertEquals(function, call.getFunction());
        assertEquals(1, call.getSuppliedParameterCount());
        assertEquals(new PrimitiveValue(1), call.getParameterValue(0));
        assertEquals(Arrays.<Expression>asList(new PrimitiveValue(1)), call.getParameterValues());
    }

    @Test
    public void testCurriedFunctionCall() {
        FunctionValue function = new FunctionValue(new Scope(),
                                                   Arrays.asList(new ParameterExpression(new Scope(), "a",
                                                                                         PrimitiveType.INT),
                                                                 new ParameterExpression(new Scope(), "b",
                                                                                         PrimitiveType.FLOAT)),
                                                   new PrimitiveValue(2));
        FunctionCall curry = new FunctionCall(new Scope(), function, Arrays.asList(new PrimitiveValue(1)));

        FunctionType expectedType = FunctionTypeTest.makeType(curry.getScope(), PrimitiveType.INT,
                                                              PrimitiveType.FLOAT);

        assertTrue(curry.getType() instanceof FunctionType);
        assertEquals(expectedType, curry.getType());

        assertEquals(function, curry.getFunction());
        assertEquals(1, curry.getSuppliedParameterCount());
        assertEquals(new PrimitiveValue(1), curry.getParameterValue(0));
        assertEquals(Arrays.<Expression>asList(new PrimitiveValue(1)), curry.getParameterValues());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoMatchingSignature() {
        FunctionValue function = new FunctionValue(new Scope(),
                                                   Arrays.asList(new ParameterExpression(new Scope(), "a",
                                                                                         PrimitiveType.INT),
                                                                 new ParameterExpression(new Scope(), "b",
                                                                                         PrimitiveType.FLOAT)),
                                                   new PrimitiveValue(2));
        new FunctionCall(new Scope(), function,
                         Arrays.asList(new PrimitiveValue(1), new PrimitiveValue(false)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTooManyArguments() {
        FunctionValue function = new FunctionValue(new Scope(),
                                                   Arrays.asList(new ParameterExpression(new Scope(), "a",
                                                                                         PrimitiveType.INT),
                                                                 new ParameterExpression(new Scope(), "b",
                                                                                         PrimitiveType.FLOAT)),
                                                   new PrimitiveValue(2));
        new FunctionCall(new Scope(), function, Arrays.asList(new PrimitiveValue(1), new PrimitiveValue(3.0f),
                                                              new PrimitiveValue(1)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorBadFunctionType() {
        new FunctionCall(new Scope(), new PrimitiveValue(2), Arrays.asList(new PrimitiveValue(1)));
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullFunction() {
        new FunctionCall(new Scope(), null, Arrays.asList(new PrimitiveValue(1)));
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullParameterList() {
        FunctionValue function = new FunctionValue(new Scope(),
                                                   Arrays.asList(new ParameterExpression(new Scope(), "a",
                                                                                         PrimitiveType.INT),
                                                                 new ParameterExpression(new Scope(), "b",
                                                                                         PrimitiveType.FLOAT)),
                                                   new PrimitiveValue(2));
        new FunctionCall(new Scope(), function, null);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullParameter() {
        FunctionValue function = new FunctionValue(new Scope(),
                                                   Arrays.asList(new ParameterExpression(new Scope(), "a",
                                                                                         PrimitiveType.INT),
                                                                 new ParameterExpression(new Scope(), "b",
                                                                                         PrimitiveType.FLOAT)),
                                                   new PrimitiveValue(2));
        new FunctionCall(new Scope(), function, Arrays.asList(new PrimitiveValue(1), null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmptyParameters() {
        FunctionValue function = new FunctionValue(new Scope(),
                                                   Arrays.asList(new ParameterExpression(new Scope(), "a",
                                                                                         PrimitiveType.INT),
                                                                 new ParameterExpression(new Scope(), "b",
                                                                                         PrimitiveType.FLOAT)),
                                                   new PrimitiveValue(2));
        new FunctionCall(new Scope(), function, Collections.<Expression>emptyList());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullScope() {
        FunctionValue function = new FunctionValue(new Scope(),
                                                   Arrays.asList(new ParameterExpression(new Scope(), "a",
                                                                                         PrimitiveType.INT),
                                                                 new ParameterExpression(new Scope(), "b",
                                                                                         PrimitiveType.FLOAT)),
                                                   new PrimitiveValue(2));
        new FunctionCall(null, function, Arrays.asList(new PrimitiveValue(1), new PrimitiveValue(2)));
    }

    @Test
    public void testEqualsAndHashcode() {
        FunctionValue function1 = new FunctionValue(new Scope(),
                                                    Arrays.asList(new ParameterExpression(new Scope(), "a",
                                                                                          PrimitiveType.INT),
                                                                  new ParameterExpression(new Scope(), "b",
                                                                                          PrimitiveType.FLOAT)),
                                                    new PrimitiveValue(2));
        FunctionValue function2 = new FunctionValue(new Scope(),
                                                    Arrays.asList(new ParameterExpression(new Scope(), "a",
                                                                                          PrimitiveType.INT)),
                                                    new PrimitiveValue(2));

        Scope s = new Scope();
        FunctionCall v1a = new FunctionCall(s, function1,
                                            Arrays.asList(new PrimitiveValue(1), new PrimitiveValue(1f)));
        FunctionCall v1b = new FunctionCall(s, function1,
                                            Arrays.asList(new PrimitiveValue(1), new PrimitiveValue(1f)));
        FunctionCall v2 = new FunctionCall(s, function2, Arrays.asList(new PrimitiveValue(1)));
        FunctionCall v3 = new FunctionCall(s, function1, Arrays.asList(new PrimitiveValue(1)));
        FunctionCall v4 = new FunctionCall(s, function1,
                                           Arrays.asList(new PrimitiveValue(1), new PrimitiveValue(1)));
        FunctionCall v5 = new FunctionCall(new Scope(), function1, Arrays.asList(new PrimitiveValue(1), new PrimitiveValue(2)));

        assertEquals(v1a, v1b);
        assertEquals(v1a.hashCode(), v1b.hashCode());

        assertFalse(v1a.equals(v2));
        assertFalse(v1a.hashCode() == v2.hashCode());
        assertFalse(v1a.equals(v3));
        assertFalse(v1a.hashCode() == v3.hashCode());
        assertFalse(v1a.equals(v4));
        assertFalse(v1a.hashCode() == v4.hashCode());
        assertFalse(v1a.equals(v5));
        assertFalse(v1a.hashCode() == v5.hashCode());
    }

    @Test
    public void testAccept() {
        FunctionValue function = new FunctionValue(new Scope(),
                                                   Arrays.asList(new ParameterExpression(new Scope(), "a",
                                                                                         PrimitiveType.INT),
                                                                 new ParameterExpression(new Scope(), "b",
                                                                                         PrimitiveType.FLOAT)),
                                                   new PrimitiveValue(2));
        FunctionCall call = new FunctionCall(new Scope(), function, Arrays.asList(new PrimitiveValue(1)));
        FunctionCall visited = call.accept(new Expression.Visitor<FunctionCall>() {
            @Override
            public FunctionCall visitArrayAccess(ArrayAccess access) {
                return null;
            }

            @Override
            public FunctionCall visitArray(ArrayValue value) {
                return null;
            }

            @Override
            public FunctionCall visitFunctionCall(FunctionCall function) {
                return function;
            }

            @Override
            public FunctionCall visitFunction(FunctionValue function) {
                return null;
            }

            @Override
            public FunctionCall visitParameter(ParameterExpression param) {
                return null;
            }

            @Override
            public FunctionCall visitPrimitive(PrimitiveValue primitive) {
                return null;
            }

            @Override
            public FunctionCall visitFieldAccess(StructFieldAccess access) {
                return null;
            }

            @Override
            public FunctionCall visitStruct(StructValue struct) {
                return null;
            }

            @Override
            public FunctionCall visitUnion(UnionValue union) {
                return null;
            }

            @Override
            public FunctionCall visitVariable(VariableExpression var) {
                return null;
            }

            @Override
            public FunctionCall visitNativeExpression(NativeExpression expr) {
                return null;
            }
        });
        assertEquals(call, visited);
    }
}
