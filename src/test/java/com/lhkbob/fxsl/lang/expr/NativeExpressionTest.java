package com.lhkbob.fxsl.lang.expr;

import com.lhkbob.fxsl.lang.Scope;
import com.lhkbob.fxsl.lang.type.PrimitiveType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Basic test cases for {@link com.lhkbob.fxsl.lang.expr.NativeExpression}.
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
        assertFalse(v1.hashCode() == v2.hashCode());
        assertEquals(v1, v1);
        assertEquals(v1.hashCode(), v1.hashCode());
    }

    @Test
    public void testGetScope() {
        NativeExpression v = new NativeExpression(PrimitiveType.INT);
        assertEquals(Scope.NATIVE_SCOPE, v.getScope());
    }

    @Test
    public void testAccept() {
        NativeExpression t = new NativeExpression(PrimitiveType.INT);
        NativeExpression visited = t.accept(new Expression.Visitor<NativeExpression>() {
            @Override
            public NativeExpression visitArrayAccess(ArrayAccess access) {
                return null;
            }

            @Override
            public NativeExpression visitArray(ArrayValue value) {
                return null;
            }

            @Override
            public NativeExpression visitFunctionCall(FunctionCall function) {
                return null;
            }

            @Override
            public NativeExpression visitFunction(FunctionValue function) {
                return null;
            }

            @Override
            public NativeExpression visitParameter(ParameterExpression param) {
                return null;
            }

            @Override
            public NativeExpression visitPrimitive(PrimitiveValue primitive) {
                return null;
            }

            @Override
            public NativeExpression visitFieldAccess(StructFieldAccess access) {
                return null;
            }

            @Override
            public NativeExpression visitStruct(StructValue struct) {
                return null;
            }

            @Override
            public NativeExpression visitUnion(UnionValue union) {
                return null;
            }

            @Override
            public NativeExpression visitVariable(VariableExpression var) {
                return null;
            }

            @Override
            public NativeExpression visitNativeExpression(NativeExpression expr) {
                return expr;
            }
        });
        assertEquals(t, visited);
    }
}
