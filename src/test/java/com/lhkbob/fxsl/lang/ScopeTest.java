package com.lhkbob.fxsl.lang;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Basic unit tests for {@link Scope}.
 *
 * @author Michael Ludwig
 */
public class ScopeTest {
    @Test
    public void testEqualsAndHashcode() {
        Scope s1 = new Scope();
        Scope s2 = new Scope();
        Scope s3 = new Scope(s1);
        Scope s4 = new Scope(s1);

        assertFalse(s1.equals(s2));
        assertFalse(s1.hashCode() == s2.hashCode());
        assertFalse(s3.equals(s4));
        assertFalse(s3.hashCode() == s4.hashCode());

        assertEquals(s1, s1);
        assertEquals(s2, s2);
        assertEquals(s3, s3);
        assertEquals(s4, s4);
    }

    @Test
    public void testConstructorAndGetter() {
        Scope s1 = new Scope();
        Scope s2 = new Scope(s1);

        assertNull(s1.getParent());
        assertEquals(s1, s2.getParent());
    }
}
