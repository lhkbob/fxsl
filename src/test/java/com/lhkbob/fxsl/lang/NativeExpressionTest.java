package com.lhkbob.fxsl.lang;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Basic test cases for {@link com.lhkbob.fxsl.lang.NativeExpression}.
 *
 * @author Michael Ludwig
 */
public class NativeExpressionTest {
    @Test
    public void testGetType() {
        NativeExpression v = new NativeExpression(PrimitiveType.INT);
        assertEquals(PrimitiveType.INT, v.getType());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullType() {
        new NativeExpression(null);
    }

    @Test
    public void testEqualsAndHashcode() {
        NativeExpression v1 = new NativeExpression(PrimitiveType.INT);
        NativeExpression v2 = new NativeExpression(PrimitiveType.INT);

        assertFalse(v1.equals(v2));
        assertEquals(v1, v1);
    }

    @Test
    public void testIsConcrete() {
        assertTrue(new NativeExpression(PrimitiveType.INT).isConcrete());
    }

    @Test
    public void testAccept() {
        NativeExpression t = new NativeExpression(PrimitiveType.INT);
        assertTrue(t.accept(new NativeExpressionTestVisitor()));
    }

    private static class NativeExpressionTestVisitor implements Expression.Visitor<Boolean> {
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
            return true;
        }
    }
}
