package com.lhkbob.fxsl.lang.type;

import com.lhkbob.fxsl.lang.Scope;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link com.lhkbob.fxsl.lang.type.StructType}.
 *
 * @author Michael Ludwig
 */
public class StructTypeTest {
    public static StructType makeType(Scope scope, List<String> fieldNames, List<? extends Type> fieldTypes) {
        Map<String, Type> fields = new HashMap<>();
        for (int i = 0; i < fieldNames.size(); i++) {
            fields.put(fieldNames.get(i), fieldTypes.get(i));
        }
        return new StructType(scope, fields);
    }

    @Test
    public void testEqualsAndHashcode() {
        Scope scope = new Scope();
        StructType t1a = makeType(scope, Arrays.asList("a", "b"),
                                  Arrays.asList(PrimitiveType.INT, PrimitiveType.FLOAT));
        StructType t1b = makeType(scope, Arrays.asList("a", "b"),
                                  Arrays.asList(PrimitiveType.INT, PrimitiveType.FLOAT));
        StructType t2 = makeType(scope, Arrays.asList("a", "b", "c"),
                                 Arrays.asList(PrimitiveType.INT, PrimitiveType.FLOAT, PrimitiveType.INT));
        StructType t3 = makeType(new Scope(), Arrays.asList("a", "b"),
                                 Arrays.asList(PrimitiveType.INT, PrimitiveType.FLOAT));

        assertEquals(t1a, t1b);
        assertFalse(t1a.equals(t2));
        assertFalse(t1a.equals(t3));
        assertEquals(t1a.hashCode(), t1b.hashCode());
        assertFalse(t1a.hashCode() == t2.hashCode());
        assertFalse(t1a.hashCode() == t3.hashCode());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetFieldsImmutable() {
        StructType t = makeType(new Scope(), Arrays.asList("a", "b"),
                                Arrays.asList(PrimitiveType.FLOAT, PrimitiveType.BOOL));
        t.getFieldTypes().put("a", PrimitiveType.INT);
    }

    @Test
    public void testGetField() {
        StructType t = makeType(new Scope(), Arrays.asList("a", "b"),
                                Arrays.asList(PrimitiveType.FLOAT, PrimitiveType.BOOL));
        assertEquals(PrimitiveType.FLOAT, t.getFieldType("a"));
        assertEquals(PrimitiveType.BOOL, t.getFieldType("b"));
    }

    @Test
    public void testGetFields() {
        StructType t = makeType(new Scope(), Arrays.asList("a", "b"),
                                Arrays.asList(PrimitiveType.FLOAT, PrimitiveType.BOOL));
        Map<String, Type> fields = t.getFieldTypes();
        assertEquals(2, fields.size());
        assertEquals(PrimitiveType.FLOAT, fields.get("a"));
        assertEquals(PrimitiveType.BOOL, fields.get("b"));
    }

    @Test
    public void testGetScope() {
        Scope scope = new Scope();
        StructType t = makeType(scope, Arrays.asList("a", "b"),
                                Arrays.asList(PrimitiveType.FLOAT, PrimitiveType.BOOL));
        assertEquals(scope, t.getScope());
    }

    @Test
    public void testConstructorClone() {
        Map<String, Type> fields = new HashMap<>();
        fields.put("a", PrimitiveType.INT);
        StructType type = new StructType(new Scope(), fields);

        fields.put("b", PrimitiveType.INT);
        assertNull(type.getFieldType("b"));
        assertEquals(1, type.getFieldTypes().size());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullMap() {
        new StructType(new Scope(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmptyMap() {
        new StructType(new Scope(), Collections.<String, Type>emptyMap());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullFieldInMap() {
        makeType(new Scope(), Arrays.asList("a", "b"), Arrays.asList(PrimitiveType.FLOAT, null));
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullFieldNameInMap() {
        makeType(new Scope(), Arrays.asList("a", null),
                 Arrays.asList(PrimitiveType.FLOAT, PrimitiveType.INT));
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorScopeNull() {
        makeType(null, Arrays.asList("f"), Arrays.asList(PrimitiveType.INT));
    }
}
