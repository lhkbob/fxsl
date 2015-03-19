package com.lhkbob.fxsl.lang.type;

import com.lhkbob.fxsl.lang.Scope;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

/**
 * Unit tests for {@link com.lhkbob.fxsl.lang.type.MetaType}.
 *
 * @author Michael Ludwig
 */
public class MetaTypeTest {
    @Test
    public void testEqualsAndHashCode() {
        Scope scope = new Scope();
        MetaType t1a = new MetaType(scope);
        MetaType t1b = new MetaType(scope);

        // even with the same scope they should not be equal
        assertFalse(t1a.equals(t1b));
        assertFalse(t1a.hashCode() == t1b.hashCode());
    }

    @Test
    public void testGetScope() {
        Scope scope = new Scope();
        MetaType t = new MetaType(scope);
        assertSame(scope, t.getScope());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullScope() {
        new MetaType(null);
    }
}
