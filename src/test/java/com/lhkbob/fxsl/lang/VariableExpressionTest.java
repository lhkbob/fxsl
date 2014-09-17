package com.lhkbob.fxsl.lang;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Basic test cases for {@link VariableExpression}.
 *
 * @author Michael Ludwig
 */
public class VariableExpressionTest {
    @Test
    public void testEqualsAndHashcode() {
        Scope shared = new Scope();
        VariableExpression t1a = new VariableExpression(shared, "a");
        VariableExpression t1b = new VariableExpression(shared, "a");
        VariableExpression t2 = new VariableExpression(shared, "b");
        VariableExpression t3 = new VariableExpression(new Scope(), "a");

        assertEquals(t1a, t1b);
        assertEquals(t1a.hashCode(), t1b.hashCode());
        assertFalse(t1a.equals(t2));
        assertFalse(t1a.hashCode() == t2.hashCode());
        assertFalse(t1a.equals(t3));
        assertFalse(t1a.hashCode() == t3.hashCode());
    }

    @Test
    public void testIsConcrete() {
        assertFalse(new VariableExpression(new Scope(), "a").isConcrete());
    }

    @Test
    public void testGetters() {
        Scope scope = new Scope();
        VariableExpression t = new VariableExpression(scope, "a");

        assertEquals("a", t.getVariableName());
        String depLabel = t.getType().getLabel();
        assertEquals("a", depLabel);
        assertEquals(scope, t.getScope());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullScope() {
        new VariableExpression(null, "a");
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullLabel() {
        new VariableExpression(new Scope(), null);
    }

    @Test
    public void testAccept() {
        VariableExpression t = new VariableExpression(new Scope(), "a");
        assertTrue(t.accept(new VariableExpressionTestVisitor()));
    }

    private static class VariableExpressionTestVisitor implements Expression.Visitor<Boolean> {
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
            return true;
        }

        @Override
        public Boolean visitNativeExpression(NativeExpression expr) {
            return false;
        }
    }
}
