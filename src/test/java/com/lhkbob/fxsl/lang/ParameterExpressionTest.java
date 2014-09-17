package com.lhkbob.fxsl.lang;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Basic test cases for {@link com.lhkbob.fxsl.lang.ParameterExpression}.
 *
 * @author Michael Ludwig
 */
public class ParameterExpressionTest {
    @Test
    public void testEqualsAndHashcode() {
        Scope shared = new Scope();
        ParameterExpression t1a = new ParameterExpression(shared, "a", PrimitiveType.INT);
        ParameterExpression t1b = new ParameterExpression(shared, "a", PrimitiveType.INT);
        ParameterExpression t2 = new ParameterExpression(shared, "b", PrimitiveType.INT);
        ParameterExpression t3 = new ParameterExpression(new Scope(), "a", PrimitiveType.INT);

        assertEquals(t1a, t1b);
        assertEquals(t1a.hashCode(), t1b.hashCode());
        assertFalse(t1a.equals(t2));
        assertFalse(t1a.hashCode() == t2.hashCode());
        assertFalse(t1a.equals(t3));
        assertFalse(t1a.hashCode() == t3.hashCode());
    }

    @Test
    public void testIsConcrete() {
        ParameterExpression concrete = new ParameterExpression(new Scope(), "a", PrimitiveType.INT);
        ParameterExpression notConcrete = new ParameterExpression(new Scope(), "a",
                                                                  new WildcardType(new Scope(), "type"));

        assertTrue(concrete.isConcrete());
        assertFalse(notConcrete.isConcrete());
    }

    @Test
    public void testGetters() {
        Scope scope = new Scope();
        ParameterExpression t = new ParameterExpression(scope, "a", PrimitiveType.INT);

        assertEquals("a", t.getParameterName());
        assertEquals(PrimitiveType.INT, t.getType());
        assertEquals(scope, t.getScope());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullScope() {
        new ParameterExpression(null, "a", PrimitiveType.INT);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullLabel() {
        new ParameterExpression(new Scope(), null, PrimitiveType.INT);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullType() {
        new ParameterExpression(new Scope(), "a", null);
    }

    @Test
    public void testAccept() {
        ParameterExpression t = new ParameterExpression(new Scope(), "a", PrimitiveType.INT);
        assertTrue(t.accept(new ParameterExpressionTestVisitor()));
    }

    private static class ParameterExpressionTestVisitor implements Expression.Visitor<Boolean> {
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
            return true;
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
