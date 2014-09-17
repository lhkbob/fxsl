package com.lhkbob.fxsl.lang;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test cases for {@link com.lhkbob.fxsl.lang.ArrayValue}.
 *
 * @author Michael Ludwig
 */
public class ArrayValueTest {
    @Test
    public void testSimpleArrayGetters() {
        ArrayValue value = new ArrayValue(Arrays.asList(new PrimitiveValue(2), new PrimitiveValue(4)));

        assertEquals(new ArrayType(PrimitiveType.INT, 2), value.getType());
        assertEquals(new PrimitiveValue(2), value.getElement(0));
        assertEquals(new PrimitiveValue(4), value.getElement(1));
        assertEquals(Arrays.<Expression>asList(new PrimitiveValue(2), new PrimitiveValue(4)),
                     value.getElements());
        assertEquals(2, value.getLength());
    }

    @Test
    public void testSharedComponentType() {
        ArrayValue value = new ArrayValue(Arrays.asList(new PrimitiveValue(2), new PrimitiveValue(4.0f)));

        assertEquals(new ArrayType(PrimitiveType.FLOAT, 2), value.getType());
        assertEquals(new PrimitiveValue(2), value.getElement(0));
        assertEquals(new PrimitiveValue(4.0f), value.getElement(1));
        assertEquals(Arrays.<Expression>asList(new PrimitiveValue(2), new PrimitiveValue(4.0f)),
                     value.getElements());
        assertEquals(2, value.getLength());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testElementAccessOutOfBounds() {
        ArrayValue value = new ArrayValue(Arrays.asList(new PrimitiveValue(2), new PrimitiveValue(4)));

        try {
            value.getElement(-1);
            fail();
        } catch (IndexOutOfBoundsException e) {
            value.getElement(2);
            fail();
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnmodifiableElementsList() {
        ArrayValue value = new ArrayValue(Arrays.asList(new PrimitiveValue(2), new PrimitiveValue(4)));
        value.getElements().set(0, new PrimitiveValue(3));
    }

    @Test
    public void testConstructorCloneElementsList() {
        List<PrimitiveValue> expr = Arrays.asList(new PrimitiveValue(2), new PrimitiveValue(4));
        ArrayValue value = new ArrayValue(expr);

        expr.set(0, new PrimitiveValue(3));
        assertEquals(new PrimitiveValue(2), value.getElement(0));
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullElement() {
        new ArrayValue(Arrays.asList(new PrimitiveValue(2), null));
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullList() {
        new ArrayValue(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNoSharableComponentType() {
        new ArrayValue(Arrays.asList(new PrimitiveValue(1), new PrimitiveValue(false)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmptyList() {
        new ArrayValue(Collections.<Expression>emptyList());
    }

    @Test
    public void testEqualsAndHashcode() {
        ArrayValue a1a = new ArrayValue(Arrays.asList(new PrimitiveValue(2), new PrimitiveValue(4)));
        ArrayValue a1b = new ArrayValue(Arrays.asList(new PrimitiveValue(2), new PrimitiveValue(4)));
        ArrayValue a2 = new ArrayValue(Arrays.asList(new PrimitiveValue(2f), new PrimitiveValue(4)));
        ArrayValue a3 = new ArrayValue(Arrays.asList(new PrimitiveValue(2), new PrimitiveValue(4),
                                                     new PrimitiveValue(3)));

        assertEquals(a1a, a1b);
        assertEquals(a1a.hashCode(), a1b.hashCode());
        assertFalse(a1a.equals(a2));
        assertFalse(a1a.hashCode() == a2.hashCode());
        assertFalse(a1a.equals(a3));
        assertFalse(a1a.hashCode() == a3.hashCode());
    }

    @Test
    public void testIsConcrete() {
        ArrayValue v1 = new ArrayValue(Arrays.asList(new PrimitiveValue(2), new PrimitiveValue(4)));
        ArrayValue v2 = new ArrayValue(Arrays.asList(new ParameterExpression(new Scope(), "a",
                                                                             new WildcardType(new Scope(),
                                                                                              "a"))));

        assertTrue(v1.isConcrete());
        assertFalse(v2.isConcrete());
    }

    @Test
    public void testAccept() {
        ArrayValue v = new ArrayValue(Arrays.asList(new PrimitiveValue(2), new PrimitiveValue(4)));

        assertTrue(v.accept(new ArrayValueTestVisitor()));
    }

    private static class ArrayValueTestVisitor implements Expression.Visitor<Boolean> {
        @Override
        public Boolean visitArrayAccess(ArrayAccess access) {
            return false;
        }

        @Override
        public Boolean visitArray(ArrayValue value) {
            return true;
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
