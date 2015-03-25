package com.lhkbob.fxsl.lang.expr;

import com.lhkbob.fxsl.lang.Scope;
import com.lhkbob.fxsl.lang.type.ArrayType;
import com.lhkbob.fxsl.lang.type.PrimitiveType;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test cases for {@link com.lhkbob.fxsl.lang.expr.ArrayValue}.
 *
 * @author Michael Ludwig
 */
public class ArrayValueTest {
    @Test
    public void testSimpleArrayGetters() {
        Scope s = new Scope();
        ArrayValue value = new ArrayValue(s, PrimitiveType.INT,
                                          Arrays.asList(new PrimitiveValue(2), new PrimitiveValue(4)));

        assertEquals(s, value.getScope());
        assertEquals(new ArrayType(s, PrimitiveType.INT, 2), value.getType());
        assertEquals(new PrimitiveValue(2), value.getElement(0));
        assertEquals(new PrimitiveValue(4), value.getElement(1));
        assertEquals(Arrays.<Expression>asList(new PrimitiveValue(2), new PrimitiveValue(4)),
                     value.getElements());
        assertEquals(2, value.getLength());
    }

    @Test
    public void testAssignableComponentType() {
        Scope s = new Scope();
        ArrayValue value = new ArrayValue(s, PrimitiveType.FLOAT,
                                          Arrays.asList(new PrimitiveValue(2), new PrimitiveValue(4.0f)));

        assertEquals(new ArrayType(s, PrimitiveType.FLOAT, 2), value.getType());
        assertEquals(new PrimitiveValue(2), value.getElement(0));
        assertEquals(new PrimitiveValue(4.0f), value.getElement(1));
        assertEquals(Arrays.<Expression>asList(new PrimitiveValue(2), new PrimitiveValue(4.0f)),
                     value.getElements());
        assertEquals(2, value.getLength());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotAssignableComponentType() {
        new ArrayValue(new Scope(), PrimitiveType.INT, Arrays.asList(new PrimitiveValue(4.0f)));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testElementAccessOutOfBounds() {
        ArrayValue value = new ArrayValue(new Scope(), PrimitiveType.INT,
                                          Arrays.asList(new PrimitiveValue(2), new PrimitiveValue(4)));

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
        ArrayValue value = new ArrayValue(new Scope(), PrimitiveType.INT,
                                          Arrays.asList(new PrimitiveValue(2), new PrimitiveValue(4)));
        value.getElements().set(0, new PrimitiveValue(3));
    }

    @Test
    public void testConstructorCloneElementsList() {
        List<PrimitiveValue> expr = Arrays.asList(new PrimitiveValue(2), new PrimitiveValue(4));
        ArrayValue value = new ArrayValue(new Scope(), PrimitiveType.INT, expr);

        expr.set(0, new PrimitiveValue(3));
        assertEquals(new PrimitiveValue(2), value.getElement(0));
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullElement() {
        new ArrayValue(new Scope(), PrimitiveType.INT, Arrays.asList(new PrimitiveValue(2), null));
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullList() {
        new ArrayValue(new Scope(), PrimitiveType.INT, null);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullScope() {
        new ArrayValue(null, PrimitiveType.INT, Arrays.asList(new PrimitiveValue(1)));
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullComponentType() {
        new ArrayValue(new Scope(), null, Arrays.asList(new PrimitiveValue(1)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmptyList() {
        new ArrayValue(new Scope(), PrimitiveType.INT, Collections.<Expression>emptyList());
    }

    @Test
    public void testEqualsAndHashcode() {
        Scope s = new Scope();
        ArrayValue a1a = new ArrayValue(s, PrimitiveType.INT, Arrays.asList(new PrimitiveValue(2), new PrimitiveValue(4)));
        ArrayValue a1b = new ArrayValue(s, PrimitiveType.INT, Arrays.asList(new PrimitiveValue(2), new PrimitiveValue(4)));
        ArrayValue a2 = new ArrayValue(s, PrimitiveType.INT, Arrays.asList(new PrimitiveValue(2f), new PrimitiveValue(4)));
        ArrayValue a3 = new ArrayValue(s, PrimitiveType.INT, Arrays.asList(new PrimitiveValue(2), new PrimitiveValue(4),
                                                     new PrimitiveValue(3)));
        ArrayValue a4 = new ArrayValue(new Scope(), PrimitiveType.INT, Arrays.asList(new PrimitiveValue(2), new PrimitiveValue(4)));

        assertEquals(a1a, a1b);
        assertEquals(a1a.hashCode(), a1b.hashCode());
        assertFalse(a1a.equals(a2));
        assertFalse(a1a.hashCode() == a2.hashCode());
        assertFalse(a1a.equals(a3));
        assertFalse(a1a.hashCode() == a3.hashCode());
        assertFalse(a1a.equals(a4));
        assertFalse(a1a.hashCode() == a4.hashCode());
    }

    @Test
    public void testAccept() {
        ArrayValue v = new ArrayValue(new Scope(), PrimitiveType.INT, Arrays.asList(new PrimitiveValue(2), new PrimitiveValue(4)));
        ArrayValue visited = v.accept(new Expression.Visitor<ArrayValue>() {
            @Override
            public ArrayValue visitArrayAccess(ArrayAccess access) {
                return null;
            }

            @Override
            public ArrayValue visitArray(ArrayValue value) {
                return value;
            }

            @Override
            public ArrayValue visitFunctionCall(FunctionCall function) {
                return null;
            }

            @Override
            public ArrayValue visitFunction(FunctionValue function) {
                return null;
            }

            @Override
            public ArrayValue visitParameter(ParameterExpression param) {
                return null;
            }

            @Override
            public ArrayValue visitPrimitive(PrimitiveValue primitive) {
                return null;
            }

            @Override
            public ArrayValue visitFieldAccess(StructFieldAccess access) {
                return null;
            }

            @Override
            public ArrayValue visitStruct(StructValue struct) {
                return null;
            }

            @Override
            public ArrayValue visitUnion(UnionValue union) {
                return null;
            }

            @Override
            public ArrayValue visitVariable(VariableExpression var) {
                return null;
            }

            @Override
            public ArrayValue visitNativeExpression(NativeExpression expr) {
                return null;
            }
        });
        assertEquals(v, visited);
    }
}
