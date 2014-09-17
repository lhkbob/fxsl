package com.lhkbob.fxsl.lang;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Validate the contract of {@link com.lhkbob.fxsl.lang.ArrayType}:
 *
 * 1. Array types can convert and assign from other arrays (within limits) and wildcards.
 * 2. One array can only be assigned to another if its component type is assignable, and their lengths are
 * equal or either has a wildcard length.
 * 3. Two arrays can be converted together if their component types are convertible, and their lengths are
 * equal or either is a wildcard. In the case of a wildcard the concrete length is chosen if available.
 *
 * @author Michael Ludwig
 */
public class ArrayTypeTest {
    @Test
    public void testEqualsAndHashCode() {
        ArrayType t1a = new ArrayType(PrimitiveType.INT, 5);
        ArrayType t1b = new ArrayType(PrimitiveType.INT, 5);
        Scope scope = new Scope();
        ArrayType t2a = new ArrayType(PrimitiveType.FLOAT, new ParameterExpression(scope, "a", PrimitiveType.INT));
        ArrayType t2b = new ArrayType(PrimitiveType.FLOAT, new ParameterExpression(scope, "a", PrimitiveType.INT));

        assertEquals(t1a, t1b);
        assertEquals(t2a, t2b);
        assertFalse(t1a.equals(t2a));

        assertEquals(t1a.hashCode(), t1b.hashCode());
        assertEquals(t2a.hashCode(), t2b.hashCode());
    }

    @Test
    public void testComponentTypeNotEquals() {
        ArrayType t1 = new ArrayType(PrimitiveType.INT, 4);
        ArrayType t2 = new ArrayType(PrimitiveType.FLOAT, 4);
        assertFalse(t1.equals(t2));
        assertFalse(t1.hashCode() == t2.hashCode());
    }

    @Test
    public void testDifferentConcreteLengthNotEquals() {
        ArrayType t1 = new ArrayType(PrimitiveType.INT, 4);
        ArrayType t2 = new ArrayType(PrimitiveType.INT, 5);
        assertFalse(t1.equals(t2));
        assertFalse(t1.hashCode() == t2.hashCode());
    }

    @Test
    public void testDifferentWildcardLengthsNotEquals() {
        Scope scope = new Scope();
        ArrayType t1 = new ArrayType(PrimitiveType.INT, new ParameterExpression(scope, "a", PrimitiveType.INT));
        ArrayType t2 = new ArrayType(PrimitiveType.INT, new ParameterExpression(scope, "b", PrimitiveType.INT));
        assertFalse(t1.equals(t2));
        assertFalse(t1.hashCode() == t2.hashCode());
    }

    @Test
    public void testOtherTypeAssignabilityAndSharedTypes() {
        assertNotAssignable(PrimitiveType.INT);
        assertNotAssignable(new FunctionType(Arrays.asList(PrimitiveType.INT), PrimitiveType.FLOAT));

        Map<String, Type> fields = new HashMap<>();
        fields.put("test", PrimitiveType.INT);
        assertNotAssignable(new StructType(fields));

        assertNotAssignable(new UnionType(new HashSet<>(Arrays.asList(new FunctionType(Arrays.asList(PrimitiveType.INT),
                                                                                       PrimitiveType.INT),
                                                                      new FunctionType(Arrays.asList(PrimitiveType.INT),
                                                                                       PrimitiveType.FLOAT)))));
    }

    private void assertNotAssignable(Type other) {
        ArrayType t = new ArrayType(PrimitiveType.INT, 4);
        assertNull(t.getSharedType(other));
        assertFalse(t.isAssignableFrom(other));
    }

    @Test
    public void testWildcardAssignabilityAndSharedTypes() {
        ArrayType t1 = new ArrayType(PrimitiveType.INT, 4);
        WildcardType t2 = new WildcardType(new Scope(), "a");

        assertTrue(t1.isAssignableFrom(t2));

        Type conversion = t1.getSharedType(t2);
        assertEquals(t1, conversion);
    }

    @Test
    public void testSelfAssignabilityAndSharedTypes() {
        ArrayType t = new ArrayType(PrimitiveType.INT, 4);
        assertTrue(t.isAssignableFrom(t));
        assertEquals(t, t.getSharedType(t));
    }

    @Test
    public void testComponentTypeAssignabilityAndSharedTypes() {
        ArrayType t1 = new ArrayType(PrimitiveType.FLOAT, 4);
        ArrayType t2 = new ArrayType(PrimitiveType.INT, 4);

        assertTrue(t1.isAssignableFrom(t2));
        assertFalse(t2.isAssignableFrom(t1));

        assertEquals(t1, t1.getSharedType(t2));
        assertEquals(t1, t2.getSharedType(t1));
    }

    @Test
    public void testLengthAssignabilityAndSharedTypes() {
        ArrayType t1 = new ArrayType(PrimitiveType.INT, 4);
        ArrayType t2 = new ArrayType(PrimitiveType.INT, 5);
        ArrayType t3 = new ArrayType(PrimitiveType.INT, new ParameterExpression(new Scope(), "a", PrimitiveType.INT));

        // test when both arrays have concrete lengths
        assertFalse(t1.isAssignableFrom(t2));
        assertFalse(t2.isAssignableFrom(t1));
        assertNull(t1.getSharedType(t2));
        assertNull(t2.getSharedType(t1));

        // test when argument array has wildcard length
        assertTrue(t1.isAssignableFrom(t3));
        assertTrue(t3.isAssignableFrom(t1));
        assertEquals(t1, t1.getSharedType(t3));
        assertEquals(t1, t3.getSharedType(t1));
    }

    @Test
    public void testIsConcrete() {
        // concrete
        ArrayType t1 = new ArrayType(PrimitiveType.INT, 4);
        assertTrue(t1.isConcrete());

        // not concrete
        ArrayType t2 = new ArrayType(PrimitiveType.INT, new ParameterExpression(new Scope(), "a", PrimitiveType.INT));
        ArrayType t3 = new ArrayType(new WildcardType(new Scope(), "a"), 4);
        ArrayType t4 = new ArrayType(new WildcardType(new Scope(), "a"),
                                     new ParameterExpression(new Scope(), "b", PrimitiveType.INT));

        assertFalse(t2.isConcrete());
        assertFalse(t3.isConcrete());
        assertFalse(t4.isConcrete());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWildcardLengthBadType() {
        new ArrayType(PrimitiveType.INT, new ParameterExpression(new Scope(), "a", PrimitiveType.FLOAT));
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorWildcardLengthNull() {
        new ArrayType(PrimitiveType.INT, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorConcreteLengthNegative() {
        new ArrayType(PrimitiveType.INT, 0);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorComponentTypeNull() {
        new ArrayType(null, 1);
    }

    @Test
    public void testGetComponentType() {
        ArrayType t = new ArrayType(PrimitiveType.INT, 4);
        assertEquals(PrimitiveType.INT, t.getComponentType());
    }

    @Test
    public void testGetConcreteLength() {
        ArrayType t1 = new ArrayType(PrimitiveType.INT, 4);
        ArrayType t2 = new ArrayType(PrimitiveType.INT, new ParameterExpression(new Scope(), "a", PrimitiveType.INT));
        assertEquals(4, t1.getConcreteLength());
        assertTrue(t2.getConcreteLength() < 0);
    }

    @Test
    public void testGetWildcardLength() {
        Scope scope = new Scope();
        ArrayType t1 = new ArrayType(PrimitiveType.INT, 4);
        ArrayType t2 = new ArrayType(PrimitiveType.INT, new ParameterExpression(scope, "a", PrimitiveType.INT));

        assertNull(t1.getWildcardLength());
        assertEquals(new ParameterExpression(scope, "a", PrimitiveType.INT), t2.getWildcardLength());
    }
}
