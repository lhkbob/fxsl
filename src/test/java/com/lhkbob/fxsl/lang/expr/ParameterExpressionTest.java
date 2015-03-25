package com.lhkbob.fxsl.lang.expr;

import com.lhkbob.fxsl.lang.Scope;
import com.lhkbob.fxsl.lang.type.PrimitiveType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Basic test cases for {@link com.lhkbob.fxsl.lang.expr.ParameterExpression}.
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
        ParameterExpression visited = t.accept(new Expression.Visitor<ParameterExpression>() {
            @Override
            public ParameterExpression visitArrayAccess(ArrayAccess access) {
                return null;
            }

            @Override
            public ParameterExpression visitArray(ArrayValue value) {
                return null;
            }

            @Override
            public ParameterExpression visitFunctionCall(FunctionCall function) {
                return null;
            }

            @Override
            public ParameterExpression visitFunction(FunctionValue function) {
                return null;
            }

            @Override
            public ParameterExpression visitParameter(ParameterExpression param) {
                return param;
            }

            @Override
            public ParameterExpression visitPrimitive(PrimitiveValue primitive) {
                return null;
            }

            @Override
            public ParameterExpression visitFieldAccess(StructFieldAccess access) {
                return null;
            }

            @Override
            public ParameterExpression visitStruct(StructValue struct) {
                return null;
            }

            @Override
            public ParameterExpression visitUnion(UnionValue union) {
                return null;
            }

            @Override
            public ParameterExpression visitVariable(VariableExpression var) {
                return null;
            }

            @Override
            public ParameterExpression visitNativeExpression(NativeExpression expr) {
                return null;
            }
        });
        assertEquals(t, visited);
    }
}
