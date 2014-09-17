package com.lhkbob.fxsl.lang;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Test cases for {@link com.lhkbob.fxsl.lang.ArrayAccess}.
 *
 * @author Michael Ludwig
 */
public class ArrayAccessTest {
    @Test
    public void testWildcardArrayExpression() {
        // this tests the constructor with a wildcard array expression, getArray(), getIndex(), and isConcrete()
        Expression array = new ParameterExpression(new Scope(), "value",
                                                   new WildcardType(new Scope(), "arrayType"));
        Expression index = new PrimitiveValue(4);
        ArrayAccess access = new ArrayAccess(array, index);

        assertTrue(access.getType() instanceof WildcardType);
        String depLabel = ((WildcardType) access.getType()).getLabel();
        assertEquals("arrayType:array", depLabel);

        assertFalse(access.isConcrete());
        assertEquals(array, access.getArray());
        assertEquals(index, access.getIndex());
    }

    @Test
    public void testConcreteArrayExpression() {
        // this tests the constructor with an array value and index expression, getArray(), getIndex(), and isConcrete()
        Expression array = new ArrayValue(Arrays.asList(new PrimitiveValue(4.0f)));
        Expression index = new PrimitiveValue(0);
        ArrayAccess access = new ArrayAccess(array, index);

        assertEquals(PrimitiveType.FLOAT, access.getType());
        assertTrue(access.isConcrete());
        assertEquals(array, access.getArray());
        assertEquals(index, access.getIndex());
    }

    @Test
    public void testWildcardIndexExpression() {
        // this tests the constructor with a wildcard index expression and getIndex()
        Expression array = new ArrayValue(Arrays.asList(new PrimitiveValue(4.0f)));
        Expression index = new ParameterExpression(new Scope(), "index",
                                                   new WildcardType(new Scope(), "intType"));
        ArrayAccess access = new ArrayAccess(array, index);

        assertEquals(PrimitiveType.FLOAT, access.getType());
        assertFalse(access.isConcrete());
        assertEquals(array, access.getArray());
        assertEquals(index, access.getIndex());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullArray() {
        new ArrayAccess(null, new PrimitiveValue(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorBadArrayType() {
        new ArrayAccess(new PrimitiveValue(2), new PrimitiveValue(2));
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullIndex() {
        new ArrayAccess(new ArrayValue(Arrays.asList(new PrimitiveValue(1))), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorBadIndexType() {
        new ArrayAccess(new ArrayValue(Arrays.asList(new PrimitiveValue(1))), new PrimitiveValue(2.0f));
    }

    @Test
    public void testEqualsAndHashcode() {
        ArrayAccess t1a = new ArrayAccess(new ArrayValue(Arrays.asList(new PrimitiveValue(4.0f))),
                                          new PrimitiveValue(0));
        ArrayAccess t1b = new ArrayAccess(new ArrayValue(Arrays.asList(new PrimitiveValue(4.0f))),
                                          new PrimitiveValue(0));
        ArrayAccess t2 = new ArrayAccess(new ArrayValue(Arrays.asList(new PrimitiveValue(4.0f),
                                                                      new PrimitiveValue(2.0f))),
                                         new PrimitiveValue(0));
        ArrayAccess t3 = new ArrayAccess(new ArrayValue(Arrays.asList(new PrimitiveValue(4.0f))),
                                         new PrimitiveValue(1));

        assertEquals(t1a, t1b);
        assertEquals(t1a.hashCode(), t1b.hashCode());
        assertFalse(t1a.equals(t2));
        assertFalse(t1a.hashCode() == t2.hashCode());
        assertFalse(t1a.equals(t3));
        assertFalse(t1a.hashCode() == t3.hashCode());
    }

    @Test
    public void testAccept() {
        ArrayAccess access = new ArrayAccess(new ArrayValue(Arrays.asList(new PrimitiveValue(4.0f))),
                                             new PrimitiveValue(0));

        assertTrue(access.accept(new ArrayAccessTestVisitor()));
    }

    private static class ArrayAccessTestVisitor implements Expression.Visitor<Boolean> {
        @Override
        public Boolean visitArrayAccess(ArrayAccess access) {
            return true;
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
            return false;
        }
    }
}
