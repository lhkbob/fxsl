package com.lhkbob.fxsl.lang.expr;

import com.lhkbob.fxsl.lang.Scope;
import com.lhkbob.fxsl.lang.type.PrimitiveType;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Basic test cases for {@link com.lhkbob.fxsl.lang.expr.PrimitiveValue}.
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
    public void testGetScope() {
        PrimitiveValue v = new PrimitiveValue(1);
        Assert.assertEquals(Scope.NATIVE_SCOPE, v.getScope());
    }

    @Test
    public void testAccept() {
        PrimitiveValue v = new PrimitiveValue(1);
        PrimitiveValue visited = v.accept(new Expression.Visitor<PrimitiveValue>() {
            @Override
            public PrimitiveValue visitArrayAccess(ArrayAccess access) {
                return null;
            }

            @Override
            public PrimitiveValue visitArray(ArrayValue value) {
                return null;
            }

            @Override
            public PrimitiveValue visitFunctionCall(FunctionCall function) {
                return null;
            }

            @Override
            public PrimitiveValue visitFunction(FunctionValue function) {
                return null;
            }

            @Override
            public PrimitiveValue visitParameter(Parameter param) {
                return null;
            }

            @Override
            public PrimitiveValue visitPrimitive(PrimitiveValue primitive) {
                return primitive;
            }

            @Override
            public PrimitiveValue visitFieldAccess(StructFieldAccess access) {
                return null;
            }

            @Override
            public PrimitiveValue visitStruct(StructValue struct) {
                return null;
            }

            @Override
            public PrimitiveValue visitUnion(UnionValue union) {
                return null;
            }

            @Override
            public PrimitiveValue visitVariable(VariableReference var) {
                return null;
            }

            @Override
            public PrimitiveValue visitNativeExpression(NativeExpression expr) {
                return null;
            }
        });
        assertEquals(v, visited);
    }
}
