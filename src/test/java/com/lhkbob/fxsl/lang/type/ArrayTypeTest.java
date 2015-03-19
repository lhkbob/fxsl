package com.lhkbob.fxsl.lang.type;

import com.lhkbob.fxsl.lang.Scope;
import com.lhkbob.fxsl.lang.expr.ParameterExpression;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Validate the contract of {@link com.lhkbob.fxsl.lang.type.ArrayType}.
 *
 * @author Michael Ludwig
 */
public class ArrayTypeTest {
    @Test
    public void testEqualsAndHashCode() {
        Scope scope = new Scope();
        ArrayType t1a = new ArrayType(scope, PrimitiveType.INT, 5);
        ArrayType t1b = new ArrayType(scope, PrimitiveType.INT, 5);
        ArrayType t2a = new ArrayType(scope, PrimitiveType.FLOAT,
                                      new ParameterExpression(scope, "a", PrimitiveType.INT));
        ArrayType t2b = new ArrayType(scope, PrimitiveType.FLOAT,
                                      new ParameterExpression(scope, "a", PrimitiveType.INT));

        assertEquals(t1a, t1b);
        assertEquals(t2a, t2b);
        assertFalse(t1a.equals(t2a));

        assertEquals(t1a.hashCode(), t1b.hashCode());
        assertEquals(t2a.hashCode(), t2b.hashCode());
    }

    @Test
    public void testScopeNotEquals() {
        Scope s1 = new Scope();
        Scope s2 = new Scope();
        ArrayType t1 = new ArrayType(s1, PrimitiveType.INT, 1);
        ArrayType t2 = new ArrayType(s2, PrimitiveType.INT, 1);

        assertFalse(t1.equals(t2));
        assertFalse(t1.hashCode() == t2.hashCode());
    }

    @Test
    public void testComponentTypeNotEquals() {
        Scope s = new Scope();
        ArrayType t1 = new ArrayType(s, PrimitiveType.INT, 4);
        ArrayType t2 = new ArrayType(s, PrimitiveType.FLOAT, 4);
        assertFalse(t1.equals(t2));
        assertFalse(t1.hashCode() == t2.hashCode());
    }

    @Test
    public void testDifferentConcreteLengthNotEquals() {
        Scope s = new Scope();
        ArrayType t1 = new ArrayType(s, PrimitiveType.INT, 4);
        ArrayType t2 = new ArrayType(s, PrimitiveType.INT, 5);
        assertFalse(t1.equals(t2));
        assertFalse(t1.hashCode() == t2.hashCode());
    }

    @Test
    public void testDifferentWildcardLengthsNotEquals() {
        Scope scope = new Scope();
        ArrayType t1 = new ArrayType(scope, PrimitiveType.INT,
                                     new ParameterExpression(scope, "a", PrimitiveType.INT));
        ArrayType t2 = new ArrayType(scope, PrimitiveType.INT,
                                     new ParameterExpression(scope, "b", PrimitiveType.INT));
        assertFalse(t1.equals(t2));
        assertFalse(t1.hashCode() == t2.hashCode());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWildcardLengthBadType() {
        new ArrayType(new Scope(), PrimitiveType.INT,
                      new ParameterExpression(new Scope(), "a", PrimitiveType.FLOAT));
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorWildcardLengthNull() {
        new ArrayType(new Scope(), PrimitiveType.INT, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorConcreteLengthNegative() {
        new ArrayType(new Scope(), PrimitiveType.INT, 0);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorComponentTypeNull() {
        new ArrayType(new Scope(), null, 1);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorScopeNull() {
        new ArrayType(null, PrimitiveType.BOOL, 1);
    }

    @Test
    public void testGetScope() {
        Scope s = new Scope();
        ArrayType t = new ArrayType(s, PrimitiveType.BOOL, 4);
        assertEquals(s, t.getScope());
    }

    @Test
    public void testGetComponentType() {
        ArrayType t = new ArrayType(new Scope(), PrimitiveType.INT, 4);
        assertEquals(PrimitiveType.INT, t.getComponentType());
    }

    @Test
    public void testGetConcreteLength() {
        ArrayType t1 = new ArrayType(new Scope(), PrimitiveType.INT, 4);
        ArrayType t2 = new ArrayType(new Scope(), PrimitiveType.INT,
                                     new ParameterExpression(new Scope(), "a", PrimitiveType.INT));
        assertEquals(4, t1.getConcreteLength());
        assertTrue(t2.getConcreteLength() < 0);
    }

    @Test
    public void testGetWildcardLength() {
        Scope scope = new Scope();
        ArrayType t1 = new ArrayType(scope, PrimitiveType.INT, 4);
        ArrayType t2 = new ArrayType(scope, PrimitiveType.INT,
                                     new ParameterExpression(scope, "a", PrimitiveType.INT));

        assertNull(t1.getWildcardLength());
        assertEquals(new ParameterExpression(scope, "a", PrimitiveType.INT), t2.getWildcardLength());
    }
}
