package com.lhkbob.fxsl.lang.type;

import com.lhkbob.fxsl.lang.Scope;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link com.lhkbob.fxsl.lang.type.AliasType}.
 *
 * @author Michael Ludwig
 */
public class AliasTypeTest {
    @Test
    public void testEqualsAndHashCode() {
        Scope scope = new Scope();
        AliasType t1a = new AliasType(scope, "a");
        AliasType t1b = new AliasType(scope, "a");
        AliasType t2 = new AliasType(scope, "b");
        AliasType t3 = new AliasType(new Scope(), "a");

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
        AliasType t = new AliasType(scope, "a");
        assertSame(scope, t.getScope());
    }

    @Test
    public void testGetLabel() {
        Scope scope = new Scope();
        AliasType t = new AliasType(scope, "a");
        assertEquals("a", t.getLabel());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullScope() {
        new AliasType(null, "a");
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullLabel() {
        new AliasType(new Scope(), null);
    }
}
