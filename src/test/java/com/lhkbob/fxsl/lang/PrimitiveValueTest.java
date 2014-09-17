package com.lhkbob.fxsl.lang;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Basic test cases for {@link com.lhkbob.fxsl.lang.PrimitiveValue}.
 *
 * @author Michael Ludwig
 */
public class PrimitiveValueTest {
    @Test
    public void testFloatConstructorAndGetters() {
        PrimitiveValue pv = new PrimitiveValue(2.0f);
        assertEquals(PrimitiveType.FLOAT, pv.getType());
        assertTrue(pv.getValue() instanceof Float);
        assertEquals(2.0f, pv.getValue());
    }

    @Test
    public void testIntConstructorAndGetters() {
        PrimitiveValue pv = new PrimitiveValue(1);
        assertEquals(PrimitiveType.INT, pv.getType());
        assertTrue(pv.getValue() instanceof Integer);
        assertEquals(1, pv.getValue());
    }

    @Test
    public void testBooleanConstructorAndGetters() {
        PrimitiveValue pv = new PrimitiveValue(false);
        assertEquals(PrimitiveType.BOOL, pv.getType());
        assertTrue(pv.getValue() instanceof Boolean);
        assertEquals(false, pv.getValue());
    }

    @Test
    public void testIsConcrete() {
        // always concrete
        assertTrue(new PrimitiveValue(1).isConcrete());
        assertTrue(new PrimitiveValue(2.0f).isConcrete());
        assertTrue(new PrimitiveValue(false).isConcrete());
    }

    @Test
    public void testEqualsAndHashcode() {
        PrimitiveValue v1a = new PrimitiveValue(1);
        PrimitiveValue v1b = new PrimitiveValue(1);
        PrimitiveValue v2 = new PrimitiveValue(2);
        PrimitiveValue v3 = new PrimitiveValue(2.0f);

        assertEquals(v1a, v1b);
        assertEquals(v1a.hashCode(), v1b.hashCode());
        assertFalse(v1a.equals(v2));
        assertFalse(v1a.hashCode() == v2.hashCode());
        assertFalse(v1a.equals(v3));
        assertFalse(v1a.hashCode() == v3.hashCode());
        assertFalse(v2.equals(v3));
        assertFalse(v2.hashCode() == v3.hashCode());
    }

    @Test
    public void testAccept() {
        PrimitiveValue v = new PrimitiveValue(1);
        assertTrue(v.accept(new PrimitiveValueTestVisitor()));
    }

    private static class PrimitiveValueTestVisitor implements Expression.Visitor<Boolean> {
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
            return false;
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
            return true;
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
