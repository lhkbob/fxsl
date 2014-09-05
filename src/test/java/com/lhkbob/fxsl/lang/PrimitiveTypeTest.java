package com.lhkbob.fxsl.lang;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Verify the behavior of {@link com.lhkbob.fxsl.lang.PrimitiveType}, namely ints can be converted and
 * assigned to floats (but not vice versa), types are assignable to themselves but no other unless it's a
 * wildcard.
 *
 * @author Michael Ludwig
 */
public class PrimitiveTypeTest {
    @Test
    public void testIntToFloatAssignabilityAndSharedTypes() {
        assertTrue(PrimitiveType.FLOAT.isAssignableFrom(PrimitiveType.INT));
        assertFalse(PrimitiveType.INT.isAssignableFrom(PrimitiveType.FLOAT));

        assertEquals(PrimitiveType.FLOAT, PrimitiveType.INT.getSharedType(PrimitiveType.FLOAT));
        assertEquals(PrimitiveType.FLOAT, PrimitiveType.FLOAT.getSharedType(PrimitiveType.INT));
    }

    @Test
    public void testWildcardAssignabilityAndSharedTypes() {
        WildcardType wild = new WildcardType(new Scope(), "T");
        for (PrimitiveType type : PrimitiveType.values()) {
            assertEquals(type, type.getSharedType(wild));
            assertTrue(type.isAssignableFrom(wild));
        }
    }

    @Test
    public void testSelfAssignabilityAndSharedTypes() {
        for (PrimitiveType type : PrimitiveType.values()) {
            assertEquals(type, type.getSharedType(type));
            assertTrue(type.isAssignableFrom(type));
        }
    }

    @Test
    public void testCrossTypeAssignabilityAndSharedTypes() {
        // within primitive type failures to convert
        for (PrimitiveType type : PrimitiveType.values()) {
            if (type == PrimitiveType.FLOAT || type == PrimitiveType.INT) {
                continue; // tested elsewhere
            }

            for (PrimitiveType other : PrimitiveType.values()) {
                if (other != type) {
                    assertNull(type.getSharedType(other));
                    assertFalse(type.isAssignableFrom(other));
                }
            }
        }

        // other types
        for (PrimitiveType type : PrimitiveType.values()) {
            assertAssignabilityAndSharedTypes(type, new ArrayType(PrimitiveType.INT, 1));
            assertAssignabilityAndSharedTypes(type, new FunctionType(Arrays.asList(PrimitiveType.INT),
                                                                     PrimitiveType.FLOAT));

            Map<String, Type> fields = new HashMap<>();
            fields.put("test", PrimitiveType.INT);
            assertAssignabilityAndSharedTypes(type, new StructType(fields));

            assertAssignabilityAndSharedTypes(type,
                                              new UnionType(new HashSet<>(Arrays.asList(new FunctionType(Arrays.asList(PrimitiveType.INT),
                                                                                                         PrimitiveType.INT),
                                                                                        new FunctionType(Arrays.asList(PrimitiveType.INT),
                                                                                                         PrimitiveType.FLOAT)))));
        }
    }

    private void assertAssignabilityAndSharedTypes(PrimitiveType t, Type other) {
        assertNull(t.getSharedType(other));
        assertFalse(t.isAssignableFrom(other));
    }

    @Test
    public void testIsConcrete() {
        for (PrimitiveType type : PrimitiveType.values()) {
            assertTrue(type.isConcrete());
        }
    }
}
