package com.lhkbob.fxsl.lang.type;

import com.lhkbob.fxsl.lang.Scope;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link ParametricType}.
 *
 * @author Michael Ludwig
 */
public class ParametricTypeTest {
    @Test
    public void testEqualsAndHashCode() {
        Scope scope = new Scope();
        ParametricType t1a = new ParametricType(scope, "a");
        ParametricType t1b = new ParametricType(scope, "a");
        ParametricType t2 = new ParametricType(scope, "b");
        ParametricType t3 = new ParametricType(new Scope(), "a");

        assertEquals(t1a, t1b);
        assertEquals(t1a.hashCode(), t1b.hashCode());
        assertFalse(t1a.equals(t2));
        assertFalse(t1a.hashCode() == t2.hashCode());
        assertFalse(t1a.equals(t3));
        assertFalse(t1a.hashCode() == t3.hashCode());
    }

    @Test
    public void testGetScope() {
        Scope scope = new Scope();
        ParametricType t = new ParametricType(scope, "a");
        assertSame(scope, t.getScope());
    }

    @Test
    public void testGetLabel() {
        Scope scope = new Scope();
        ParametricType t = new ParametricType(scope, "a");
        assertEquals("a", t.getLabel());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullScope() {
        new ParametricType(null, "a");
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullLabel() {
        new ParametricType(new Scope(), null);
    }
}
