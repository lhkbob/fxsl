package com.lhkbob.fxsl.lang.expr;

import com.lhkbob.fxsl.lang.Scope;
import com.lhkbob.fxsl.lang.type.MetaType;
import com.lhkbob.fxsl.lang.type.PrimitiveType;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Test cases for {@link com.lhkbob.fxsl.lang.expr.ArrayAccess}.
 *
 * @author Michael Ludwig
 */
public class ArrayAccessTest {
    @Test
    public void testWildcardArrayExpression() {
        // this tests the constructor with a wildcard array expression, getArray(), getIndex()
        Scope s = new Scope();
        Expression array = new ParameterExpression(s, "value", new MetaType(s));
        Expression index = new PrimitiveValue(4);
        ArrayAccess access = new ArrayAccess(s, array, index);

        assertTrue(access.getType() instanceof MetaType);
        assertEquals(array, access.getArray());
        assertEquals(index, access.getIndex());
    }

    @Test
    public void testConcreteArrayExpression() {
        // this tests the constructor with an array value and index expression, getArray(), getIndex()
        Scope s = new Scope();
        Expression array = new ArrayValue(s, PrimitiveType.FLOAT, Arrays.asList(new PrimitiveValue(4.0f)));
        Expression index = new PrimitiveValue(0);
        ArrayAccess access = new ArrayAccess(s, array, index);

        assertEquals(PrimitiveType.FLOAT, access.getType());
        assertEquals(array, access.getArray());
        assertEquals(index, access.getIndex());
    }

    @Test
    public void testWildcardIndexExpression() {
        // this tests the constructor with a wildcard index expression and getIndex()
        Scope s = new Scope();
        Expression array = new ArrayValue(s, PrimitiveType.FLOAT, Arrays.asList(new PrimitiveValue(4.0f)));
        Expression index = new ParameterExpression(s, "index", new MetaType(s));
        ArrayAccess access = new ArrayAccess(s, array, index);

        assertEquals(PrimitiveType.FLOAT, access.getType());
        assertEquals(array, access.getArray());
        assertEquals(index, access.getIndex());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullArray() {
        new ArrayAccess(new Scope(), null, new PrimitiveValue(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorBadArrayExpression() {
        new ArrayAccess(new Scope(), new PrimitiveValue(2), new PrimitiveValue(2));
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullIndex() {
        Scope s = new Scope();
        new ArrayAccess(s, new ParameterExpression(s, "array", new MetaType(s)), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorBadIndexType() {
        Scope s = new Scope();
        new ArrayAccess(s, new ParameterExpression(s, "array", new MetaType(s)), new PrimitiveValue(2.0f));
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullScope() {
        new ArrayAccess(null, new ParameterExpression(new Scope(), "array", new MetaType(new Scope())),
                        new PrimitiveValue(1));
    }

    @Test
    public void testEqualsAndHashcode() {
        Scope s = new Scope();
        ArrayAccess t1a = new ArrayAccess(s, new ArrayValue(s, PrimitiveType.FLOAT,
                                                            Arrays.asList(new PrimitiveValue(4.0f))),
                                          new PrimitiveValue(0));
        ArrayAccess t1b = new ArrayAccess(s, new ArrayValue(s, PrimitiveType.FLOAT,
                                                            Arrays.asList(new PrimitiveValue(4.0f))),
                                          new PrimitiveValue(0));
        ArrayAccess t2 = new ArrayAccess(s, new ArrayValue(s, PrimitiveType.FLOAT,
                                                           Arrays.asList(new PrimitiveValue(4.0f),
                                                                         new PrimitiveValue(2.0f))),
                                         new PrimitiveValue(0));
        ArrayAccess t3 = new ArrayAccess(s, new ArrayValue(s, PrimitiveType.FLOAT,
                                                           Arrays.asList(new PrimitiveValue(4.0f))),
                                         new PrimitiveValue(1));
        ArrayAccess t4 = new ArrayAccess(new Scope(), new ArrayValue(s, PrimitiveType.FLOAT,
                                                                     Arrays.asList(new PrimitiveValue(4.0f))),
                                         new PrimitiveValue(1));

        assertEquals(t1a, t1b);
        assertEquals(t1a.hashCode(), t1b.hashCode());
        assertFalse(t1a.equals(t2));
        assertFalse(t1a.hashCode() == t2.hashCode());
        assertFalse(t1a.equals(t3));
        assertFalse(t1a.hashCode() == t3.hashCode());
        assertFalse(t1a.equals(t4));
        assertFalse(t1a.hashCode() == t4.hashCode());
    }

    @Test
    public void testAccept() {
        Scope s = new Scope();
        ArrayAccess access = new ArrayAccess(s, new ArrayValue(s, PrimitiveType.FLOAT,
                                                               Arrays.asList(new PrimitiveValue(4.0f))),
                                             new PrimitiveValue(0));

        ArrayAccess visited = access.accept(new Expression.Visitor<ArrayAccess>() {
            @Override
            public ArrayAccess visitArrayAccess(ArrayAccess access) {
                return access;
            }

            @Override
            public ArrayAccess visitArray(ArrayValue value) {
                return null;
            }

            @Override
            public ArrayAccess visitFunctionCall(FunctionCall function) {
                return null;
            }

            @Override
            public ArrayAccess visitFunction(FunctionValue function) {
                return null;
            }

            @Override
            public ArrayAccess visitParameter(ParameterExpression param) {
                return null;
            }

            @Override
            public ArrayAccess visitPrimitive(PrimitiveValue primitive) {
                return null;
            }

            @Override
            public ArrayAccess visitFieldAccess(StructFieldAccess access) {
                return null;
            }

            @Override
            public ArrayAccess visitStruct(StructValue struct) {
                return null;
            }

            @Override
            public ArrayAccess visitUnion(UnionValue union) {
                return null;
            }

            @Override
            public ArrayAccess visitVariable(VariableExpression var) {
                return null;
            }

            @Override
            public ArrayAccess visitNativeExpression(NativeExpression expr) {
                return null;
            }
        });

        assertEquals(visited, access);
    }
}
