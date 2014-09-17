package com.lhkbob.fxsl.lang;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * Basic unit tests for {@link com.lhkbob.fxsl.lang.FunctionCall}.
 *
 * @author Michael Ludwig
 */
public class FunctionCallTest {
    @Test
    public void testSimpleFunctionCall() {
        FunctionValue function = new FunctionValue(Arrays.asList("a"), Arrays.asList(PrimitiveType.INT),
                                                   new PrimitiveValue(2));
        FunctionCall call = new FunctionCall(function, Arrays.asList(new PrimitiveValue(1)));

        assertEquals(PrimitiveType.INT, call.getType());
        assertEquals(function, call.getFunction());
        assertEquals(1, call.getSuppliedParameterCount());
        assertEquals(new PrimitiveValue(1), call.getParameterValue(0));
        assertEquals(Arrays.<Expression>asList(new PrimitiveValue(1)), call.getParameterValues());
        assertTrue(call.isConcrete());
    }

    @Test
    public void testWildcardFunctionCall() {
        Expression function = new ParameterExpression(new Scope(), "a",
                                                      new WildcardType(new Scope(), "wildfunction"));
        FunctionCall call = new FunctionCall(function, Arrays.asList(new PrimitiveValue(1)));

        assertTrue(call.getType() instanceof WildcardType);
        String depLabel = ((WildcardType) call.getType()).getLabel();
        assertEquals("wildfunction:function", depLabel);
        assertEquals(function, call.getFunction());
        assertEquals(1, call.getSuppliedParameterCount());
        assertEquals(new PrimitiveValue(1), call.getParameterValue(0));
        assertEquals(Arrays.<Expression>asList(new PrimitiveValue(1)), call.getParameterValues());
        assertFalse(call.isConcrete());
    }

    @Test
    public void testSingleOptionUnionCall() {
        FunctionValue matched = new FunctionValue(Arrays.asList("a"), Arrays.asList(PrimitiveType.INT),
                                                  new PrimitiveValue(2));
        FunctionValue notMatched = new FunctionValue(Arrays.asList("a"), Arrays.asList(PrimitiveType.BOOL),
                                                     new PrimitiveValue(2.0f));
        UnionValue function = UnionValueTest.makeValue(matched, notMatched);
        FunctionCall call = new FunctionCall(function, Arrays.asList(new PrimitiveValue(1)));

        assertEquals(PrimitiveType.INT, call.getType());
        assertEquals(function, call.getFunction());
        assertEquals(1, call.getSuppliedParameterCount());
        assertEquals(new PrimitiveValue(1), call.getParameterValue(0));
        assertEquals(Arrays.<Expression>asList(new PrimitiveValue(1)), call.getParameterValues());
        assertFalse(call.isConcrete());
    }

    @Test
    public void testMultipleOptionUnionCall() {
        FunctionValue matched = new FunctionValue(Arrays.asList("a"), Arrays.asList(PrimitiveType.INT),
                                                  new PrimitiveValue(2));
        FunctionValue notMatched = new FunctionValue(Arrays.asList("a"), Arrays.asList(PrimitiveType.FLOAT),
                                                     new PrimitiveValue(2.0f));
        UnionValue function = UnionValueTest.makeValue(matched, notMatched);
        FunctionCall call = new FunctionCall(function, Arrays.asList(new PrimitiveValue(1)));

        String depLabel = ((WildcardType) call.getType()).getLabel();
        assertEquals("union", depLabel);
        assertEquals(function, call.getFunction());
        assertEquals(1, call.getSuppliedParameterCount());
        assertEquals(new PrimitiveValue(1), call.getParameterValue(0));
        assertEquals(Arrays.<Expression>asList(new PrimitiveValue(1)), call.getParameterValues());
        assertFalse(call.isConcrete());
    }

    @Test
    public void testCurriedFunctionCall() {
        FunctionValue function = new FunctionValue(Arrays.asList("a", "b"),
                                                   Arrays.asList(PrimitiveType.INT, PrimitiveType.FLOAT),
                                                   new PrimitiveValue(2));
        FunctionCall curry = new FunctionCall(function, Arrays.asList(new PrimitiveValue(1)));

        FunctionType expectedType = FunctionTypeTest.makeType(PrimitiveType.INT, PrimitiveType.FLOAT);

        assertTrue(curry.getType() instanceof FunctionType);
        assertEquals(expectedType, curry.getType());

        assertEquals(function, curry.getFunction());
        assertEquals(1, curry.getSuppliedParameterCount());
        assertEquals(new PrimitiveValue(1), curry.getParameterValue(0));
        assertEquals(Arrays.<Expression>asList(new PrimitiveValue(1)), curry.getParameterValues());
        assertTrue(curry.isConcrete());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoMatchingSignature() {
        FunctionValue function = new FunctionValue(Arrays.asList("a", "b"),
                                                   Arrays.asList(PrimitiveType.INT, PrimitiveType.FLOAT),
                                                   new PrimitiveValue(2));
        new FunctionCall(function, Arrays.asList(new PrimitiveValue(1), new PrimitiveValue(false)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTooManyArguments() {
        FunctionValue function = new FunctionValue(Arrays.asList("a", "b"),
                                                   Arrays.asList(PrimitiveType.INT, PrimitiveType.FLOAT),
                                                   new PrimitiveValue(2));
        new FunctionCall(function, Arrays.asList(new PrimitiveValue(1), new PrimitiveValue(3.0f),
                                                 new PrimitiveValue(1)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorBadFunctionType() {
        new FunctionCall(new PrimitiveValue(2), Arrays.asList(new PrimitiveValue(1)));
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullFunction() {
        new FunctionCall(null, Arrays.asList(new PrimitiveValue(1)));
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullParameterList() {
        FunctionValue function = new FunctionValue(Arrays.asList("a", "b"),
                                                   Arrays.asList(PrimitiveType.INT, PrimitiveType.FLOAT),
                                                   new PrimitiveValue(2));
        new FunctionCall(function, null);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullParameter() {
        FunctionValue function = new FunctionValue(Arrays.asList("a", "b"),
                                                   Arrays.asList(PrimitiveType.INT, PrimitiveType.FLOAT),
                                                   new PrimitiveValue(2));
        new FunctionCall(function, Arrays.asList(new PrimitiveValue(1), null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmptyParameters() {
        FunctionValue function = new FunctionValue(Arrays.asList("a", "b"),
                                                   Arrays.asList(PrimitiveType.INT, PrimitiveType.FLOAT),
                                                   new PrimitiveValue(2));
        new FunctionCall(function, Collections.<Expression>emptyList());
    }

    @Test
    public void testEqualsAndHashcode() {
        FunctionValue function1 = new FunctionValue(Arrays.asList("a", "b"),
                                                    Arrays.asList(PrimitiveType.INT, PrimitiveType.FLOAT),
                                                    new PrimitiveValue(2));
        FunctionValue function2 = new FunctionValue(Arrays.asList("a"), Arrays.asList(PrimitiveType.INT),
                                                    new PrimitiveValue(2));

        FunctionCall v1a = new FunctionCall(function1,
                                            Arrays.asList(new PrimitiveValue(1), new PrimitiveValue(1f)));
        FunctionCall v1b = new FunctionCall(function1,
                                            Arrays.asList(new PrimitiveValue(1), new PrimitiveValue(1f)));
        FunctionCall v2 = new FunctionCall(function2, Arrays.asList(new PrimitiveValue(1)));
        FunctionCall v3 = new FunctionCall(function1, Arrays.asList(new PrimitiveValue(1)));
        FunctionCall v4 = new FunctionCall(function1,
                                           Arrays.asList(new PrimitiveValue(1), new PrimitiveValue(1)));

        assertEquals(v1a, v1b);
        assertEquals(v1a.hashCode(), v1b.hashCode());

        assertFalse(v1a.equals(v2));
        assertFalse(v1a.hashCode() == v2.hashCode());
        assertFalse(v1a.equals(v3));
        assertFalse(v1a.hashCode() == v3.hashCode());
        assertFalse(v1a.equals(v4));
        assertFalse(v1a.hashCode() == v4.hashCode());
    }

    @Test
    public void testAccept() {
        FunctionValue function = new FunctionValue(Arrays.asList("a"), Arrays.asList(PrimitiveType.INT),
                                                   new PrimitiveValue(2));
        FunctionCall call = new FunctionCall(function, Arrays.asList(new PrimitiveValue(1)));
        assertTrue(call.accept(new FunctionCallTestVisitor()));
    }

    private static class FunctionCallTestVisitor implements Expression.Visitor<Boolean> {
        @Override
        public Boolean visitArrayAccess(ArrayAccess access) {
            return false;
        }

        @Override
        public Boolean visitArray(ArrayValue value) {
            return false;
        }

        @Override
        public Boolean visitFunctionCall(FunctionCall function) {
            return true;
        }

        @Override
        public Boolean visitFunction(FunctionValue function) {
            return false;
        }

        @Override
        public Boolean visitParameter(ParameterExpression param) {
            return false;
        }

        @Override
        public Boolean visitPrimitive(PrimitiveValue primitive) {
            return false;
        }

        @Override
        public Boolean visitFieldAccess(StructFieldAccess access) {
            return false;
        }

        @Override
        public Boolean visitStruct(StructValue struct) {
            return false;
        }

        @Override
        public Boolean visitUnion(UnionValue union) {
            return false;
        }

        @Override
        public Boolean visitVariable(VariableExpression var) {
            return false;
        }

        @Override
        public Boolean visitNativeExpression(NativeExpression expr) {
            return false;
        }
    }
}
