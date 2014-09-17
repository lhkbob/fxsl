package com.lhkbob.fxsl.lang;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link com.lhkbob.fxsl.lang.StructType} that verifies logical equality, and the contract
 * for type conversions and assignability described in its documentation.
 *
 * @author Michael Ludwig
 */
public class StructTypeTest {
    public static StructType makeType(List<String> fieldNames, List<? extends Type> fieldTypes) {
        Map<String, Type> fields = new HashMap<>();
        for (int i = 0; i < fieldNames.size(); i++) {
            fields.put(fieldNames.get(i), fieldTypes.get(i));
        }
        return new StructType(fields);
    }

    @Test
    public void testEqualsAndHashcode() {
        StructType t1a = makeType(Arrays.asList("a", "b"),
                                  Arrays.asList(PrimitiveType.INT, PrimitiveType.FLOAT));
        StructType t1b = makeType(Arrays.asList("a", "b"),
                                  Arrays.asList(PrimitiveType.INT, PrimitiveType.FLOAT));
        StructType t2 = makeType(Arrays.asList("a", "b", "c"),
                                 Arrays.asList(PrimitiveType.INT, PrimitiveType.FLOAT, PrimitiveType.INT));

        assertEquals(t1a, t1b);
        assertFalse(t1a.equals(t2));
        assertEquals(t1a.hashCode(), t1b.hashCode());
        assertFalse(t1a.hashCode() == t2.hashCode());
    }

    @Test
    public void testIsConcrete() {
        StructType t1 = makeType(Arrays.asList("a", "b"),
                                 Arrays.asList(PrimitiveType.INT, PrimitiveType.FLOAT));
        StructType t2 = makeType(Arrays.asList("a", "b"),
                                 Arrays.asList(PrimitiveType.INT, new WildcardType(new Scope(), "c")));

        assertTrue(t1.isConcrete());
        assertFalse(t2.isConcrete());
    }

    @Test
    public void testWildcardAssignabilityAndSharedTypes() {
        StructType t1 = makeType(Arrays.asList("a", "b"),
                                 Arrays.asList(PrimitiveType.FLOAT, PrimitiveType.BOOL));
        WildcardType t2 = new WildcardType(new Scope(), "t");

        assertTrue(t1.isAssignableFrom(t2));
        assertEquals(t1, t1.getSharedType(t2));
    }

    @Test
    public void testOtherTypeAssignabilityAndSharedTypes() {
        assertNotAssignable(PrimitiveType.INT);
        assertNotAssignable(new ArrayType(PrimitiveType.INT, 1));
        assertNotAssignable(new FunctionType(Arrays.asList(PrimitiveType.INT), PrimitiveType.FLOAT));
        assertNotAssignable(new UnionType(new HashSet<>(Arrays.asList(new FunctionType(Arrays.asList(PrimitiveType.INT),
                                                                                       PrimitiveType.INT),
                                                                      new FunctionType(Arrays.asList(PrimitiveType.INT),
                                                                                       PrimitiveType.FLOAT)))));
    }

    private void assertNotAssignable(Type other) {
        StructType t = makeType(Arrays.asList("a", "b"),
                                Arrays.asList(PrimitiveType.FLOAT, PrimitiveType.BOOL));
        assertNull(t.getSharedType(other));
        assertFalse(t.isAssignableFrom(other));
    }

    @Test
    public void testSelfAssignabilityAndSharedTypes() {
        StructType t = makeType(Arrays.asList("a", "b"),
                                Arrays.asList(PrimitiveType.FLOAT, PrimitiveType.BOOL));
        assertTrue(t.isAssignableFrom(t));
        assertEquals(t, t.getSharedType(t));
    }

    @Test
    public void testSubsetAssignabilityAndSharedTypes() {
        StructType t1 = makeType(Arrays.asList("a", "b"),
                                 Arrays.asList(PrimitiveType.FLOAT, PrimitiveType.BOOL));
        StructType t2 = makeType(Arrays.asList("a", "b", "c"),
                                 Arrays.asList(PrimitiveType.FLOAT, PrimitiveType.BOOL, PrimitiveType.INT));

        assertTrue(t1.isAssignableFrom(t2));
        assertFalse(t2.isAssignableFrom(t1));

        assertEquals(t1, t1.getSharedType(t2));
        assertEquals(t1, t2.getSharedType(t1));
    }

    @Test
    public void testOverlappingAssignabilityAndSharedTypes() {
        StructType t1 = makeType(Arrays.asList("a", "b", "d"),
                                 Arrays.asList(PrimitiveType.FLOAT, PrimitiveType.BOOL, PrimitiveType.INT));
        StructType t2 = makeType(Arrays.asList("a", "b", "c"),
                                 Arrays.asList(PrimitiveType.FLOAT, PrimitiveType.BOOL, PrimitiveType.INT));

        StructType conversion = makeType(Arrays.asList("a", "b"),
                                         Arrays.asList(PrimitiveType.FLOAT, PrimitiveType.BOOL));

        assertFalse(t1.isAssignableFrom(t2));
        assertFalse(t2.isAssignableFrom(t1));

        assertEquals(conversion, t1.getSharedType(t2));
        assertEquals(conversion, t2.getSharedType(t1));
    }

    @Test
    public void testDisjointAssignabilityAndSharedTypes() {
        StructType t1 = makeType(Arrays.asList("a1", "b1"),
                                 Arrays.asList(PrimitiveType.FLOAT, PrimitiveType.BOOL));
        StructType t2 = makeType(Arrays.asList("a2", "b2"),
                                 Arrays.asList(PrimitiveType.FLOAT, PrimitiveType.BOOL));

        assertFalse(t1.isAssignableFrom(t2));
        assertFalse(t2.isAssignableFrom(t1));

        assertNull(t1.getSharedType(t2));
        assertNull(t2.getSharedType(t1));
    }

    @Test
    public void testFieldTypeAssignabilityAndSharedTypes() {
        StructType t1 = makeType(Arrays.asList("a", "b"),
                                 Arrays.asList(PrimitiveType.FLOAT, PrimitiveType.BOOL));
        StructType t2 = makeType(Arrays.asList("a", "b"),
                                 Arrays.asList(PrimitiveType.INT, PrimitiveType.BOOL));

        assertTrue(t1.isAssignableFrom(t2));
        assertFalse(t2.isAssignableFrom(t1));

        assertEquals(t1, t1.getSharedType(t2));
        assertEquals(t1, t2.getSharedType(t1));
    }

    @Test
    public void testWrongFieldTypesAssignabilityAndSharedTypes() {
        StructType t1 = makeType(Arrays.asList("a", "b"),
                                 Arrays.asList(PrimitiveType.BOOL, PrimitiveType.FLOAT));
        StructType t2 = makeType(Arrays.asList("a", "b"),
                                 Arrays.asList(PrimitiveType.FLOAT, PrimitiveType.BOOL));

        assertFalse(t1.isAssignableFrom(t2));
        assertFalse(t2.isAssignableFrom(t1));

        assertNull(t1.getSharedType(t2));
        assertNull(t2.getSharedType(t1));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetFieldsImmutable() {
        StructType t = makeType(Arrays.asList("a", "b"),
                                Arrays.asList(PrimitiveType.FLOAT, PrimitiveType.BOOL));
        t.getFieldTypes().put("a", PrimitiveType.INT);
    }

    @Test
    public void testGetField() {
        StructType t = makeType(Arrays.asList("a", "b"),
                                Arrays.asList(PrimitiveType.FLOAT, PrimitiveType.BOOL));
        assertEquals(PrimitiveType.FLOAT, t.getFieldType("a"));
        assertEquals(PrimitiveType.BOOL, t.getFieldType("b"));
    }

    @Test
    public void testGetFields() {
        StructType t = makeType(Arrays.asList("a", "b"),
                                Arrays.asList(PrimitiveType.FLOAT, PrimitiveType.BOOL));
        Map<String, Type> fields = t.getFieldTypes();
        assertEquals(2, fields.size());
        assertEquals(PrimitiveType.FLOAT, fields.get("a"));
        assertEquals(PrimitiveType.BOOL, fields.get("b"));
    }

    @Test
    public void testConstructorClone() {
        Map<String, Type> fields = new HashMap<>();
        fields.put("a", PrimitiveType.INT);
        StructType type = new StructType(fields);

        fields.put("b", PrimitiveType.INT);
        assertNull(type.getFieldType("b"));
        assertEquals(1, type.getFieldTypes().size());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullMap() {
        new StructType(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmptyMap() {
        new StructType(Collections.<String, Type>emptyMap());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullFieldInMap() {
        makeType(Arrays.asList("a", "b"), Arrays.asList(PrimitiveType.FLOAT, null));
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullFieldNameInMap() {
        makeType(Arrays.asList("a", null), Arrays.asList(PrimitiveType.FLOAT, PrimitiveType.INT));
    }
}
