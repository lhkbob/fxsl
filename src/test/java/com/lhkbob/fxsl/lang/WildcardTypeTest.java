package com.lhkbob.fxsl.lang;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Validate equals and hashCode contracts and that a {@link com.lhkbob.fxsl.lang.WildcardType} is not
 * concrete, is assignable to any other type, and conversion produces the other type exactly.
 *
 * @author Michael Ludwig
 */
public class WildcardTypeTest {
    @Test
    public void testEqualsAndHashCode() {
        Scope scope = new Scope();
        WildcardType t1a = new WildcardType(scope, "t1");
        WildcardType t1b = new WildcardType(scope, "t1");

        assertEquals(t1a, t1b);
        assertEquals(t1a.hashCode(), t1b.hashCode());
    }

    @Test
    public void testEqualsDifferentScope() {
        Scope scope1 = new Scope();
        Scope scope2 = new Scope(scope1);
        Scope scope3 = new Scope();
        WildcardType t1 = new WildcardType(scope1, "t1");
        WildcardType t2 = new WildcardType(scope2, "t1");
        WildcardType t3 = new WildcardType(scope3, "t1");

        assertFalse(t1.equals(t2));
        assertFalse(t2.equals(t3));
        assertFalse(t3.equals(t1));
        assertFalse(t1.hashCode() == t2.hashCode());
        assertFalse(t2.hashCode() == t3.hashCode());
        assertFalse(t3.hashCode() == t1.hashCode());
    }

    @Test
    public void testEqualsDifferentLabel() {
        Scope scope = new Scope();
        WildcardType t1 = new WildcardType(scope, "t1");
        WildcardType t2 = new WildcardType(scope, "t2");

        assertFalse(t1.equals(t2));
        assertFalse(t1.hashCode() == t2.hashCode());
    }

    @Test
    public void testTypeAssignabilityAndSharedTypes() {
        assertAssignabilityAndSharedTypes(PrimitiveType.INT);
        assertAssignabilityAndSharedTypes(new ArrayType(PrimitiveType.INT, 1));
        assertAssignabilityAndSharedTypes(new FunctionType(Arrays.asList(PrimitiveType.INT),
                                                           PrimitiveType.FLOAT));

        Map<String, Type> fields = new HashMap<>();
        fields.put("test", PrimitiveType.INT);
        assertAssignabilityAndSharedTypes(new StructType(fields));

        assertAssignabilityAndSharedTypes(new UnionType(new HashSet<>(Arrays.asList(new FunctionType(Arrays.asList(PrimitiveType.INT),
                                                                                                     PrimitiveType.INT),
                                                                                    new FunctionType(Arrays.asList(PrimitiveType.INT),
                                                                                                     PrimitiveType.FLOAT)))));
    }

    private void assertAssignabilityAndSharedTypes(Type other) {
        WildcardType t = new WildcardType(new Scope(), "t");
        assertEquals(other, t.getSharedType(other));
        assertTrue(t.isAssignableFrom(other));
    }

    @Test
    public void testIsConcrete() {
        WildcardType t = new WildcardType(new Scope(), "t1");
        assertFalse(t.isConcrete());
    }

    @Test
    public void testGetLabel() {
        WildcardType t = new WildcardType(new Scope(), "label");
        assertEquals("label", t.getLabel());
    }

    @Test
    public void testGetScope() {
        Scope scope = new Scope();
        WildcardType t = new WildcardType(scope, "t");
        assertSame(scope, t.getScope());
    }
}
