package com.lhkbob.fxsl.lang.expr;

import com.lhkbob.fxsl.lang.Scope;
import com.lhkbob.fxsl.lang.type.PrimitiveType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Basic test cases for {@link Parameter}.
 *
 * @author Michael Ludwig
 */
public class ParameterExpressionTest {
    @Test
    public void testEqualsAndHashcode() {
        Scope shared = new Scope();
        Parameter t1a = new Parameter(shared, "a", PrimitiveType.INT);
        Parameter t1b = new Parameter(shared, "a", PrimitiveType.INT);
        Parameter t2 = new Parameter(shared, "b", PrimitiveType.INT);
        Parameter t3 = new Parameter(new Scope(), "a", PrimitiveType.INT);

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
        Parameter t = new Parameter(scope, "a", PrimitiveType.INT);

        assertEquals("a", t.getParameterName());
        assertEquals(PrimitiveType.INT, t.getType());
        assertEquals(scope, t.getScope());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullScope() {
        new Parameter(null, "a", PrimitiveType.INT);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullLabel() {
        new Parameter(new Scope(), null, PrimitiveType.INT);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullType() {
        new Parameter(new Scope(), "a", null);
    }

    @Test
    public void testAccept() {
        Parameter t = new Parameter(new Scope(), "a", PrimitiveType.INT);
        Parameter visited = t.accept(new Expression.Visitor<Parameter>() {
            @Override
            public Parameter visitArrayAccess(ArrayAccess access) {
                return null;
            }

            @Override
            public Parameter visitArray(ArrayValue value) {
                return null;
            }

            @Override
            public Parameter visitFunctionCall(FunctionCall function) {
                return null;
            }

            @Override
            public Parameter visitFunction(FunctionValue function) {
                return null;
            }

            @Override
            public Parameter visitParameter(Parameter param) {
                return param;
            }

            @Override
            public Parameter visitPrimitive(PrimitiveValue primitive) {
                return null;
            }

            @Override
            public Parameter visitFieldAccess(StructFieldAccess access) {
                return null;
            }

            @Override
            public Parameter visitStruct(StructValue struct) {
                return null;
            }

            @Override
            public Parameter visitUnion(UnionValue union) {
                return null;
            }

            @Override
            public Parameter visitVariable(VariableReference var) {
                return null;
            }

            @Override
            public Parameter visitNativeExpression(NativeExpression expr) {
                return null;
            }
        });
        assertEquals(t, visited);
    }
}
