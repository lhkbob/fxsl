package com.lhkbob.fxsl.lang.expr;

import com.lhkbob.fxsl.lang.Scope;
import com.lhkbob.fxsl.lang.type.MetaType;
import com.lhkbob.fxsl.lang.type.PrimitiveType;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Basic test cases for {@link com.lhkbob.fxsl.lang.expr.VariableExpression}.
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
    public void testGetters() {
        Scope scope = new Scope();
        VariableExpression t = new VariableExpression(scope, "a", PrimitiveType.INT);

        assertEquals("a", t.getVariableName());
        assertEquals(scope, t.getScope());
        assertEquals(PrimitiveType.INT, t.getType());
    }

    @Test
    public void testConstructorNullKnownType() {
        VariableExpression t = new VariableExpression(new Scope(), "a");
        assertTrue(t.getType() instanceof MetaType);
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
        VariableExpression visited = t.accept(new Expression.Visitor<VariableExpression>() {
            @Override
            public VariableExpression visitArrayAccess(ArrayAccess access) {
                return null;
            }

            @Override
            public VariableExpression visitArray(ArrayValue value) {
                return null;
            }

            @Override
            public VariableExpression visitFunctionCall(FunctionCall function) {
                return null;
            }

            @Override
            public VariableExpression visitFunction(FunctionValue function) {
                return null;
            }

            @Override
            public VariableExpression visitParameter(ParameterExpression param) {
                return null;
            }

            @Override
            public VariableExpression visitPrimitive(PrimitiveValue primitive) {
                return null;
            }

            @Override
            public VariableExpression visitFieldAccess(StructFieldAccess access) {
                return null;
            }

            @Override
            public VariableExpression visitStruct(StructValue struct) {
                return null;
            }

            @Override
            public VariableExpression visitUnion(UnionValue union) {
                return null;
            }

            @Override
            public VariableExpression visitVariable(VariableExpression var) {
                return var;
            }

            @Override
            public VariableExpression visitNativeExpression(NativeExpression expr) {
                return null;
            }
        });
        assertEquals(t, visited);
    }
}
