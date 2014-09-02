package com.lhkbob.fxsl.lang;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
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
    public void testIntToFloatConversionAndAssignability() {
        assertTrue(PrimitiveType.FLOAT.isAssignableFrom(PrimitiveType.INT));
        assertFalse(PrimitiveType.INT.isAssignableFrom(PrimitiveType.FLOAT));

        assertEquals(PrimitiveType.FLOAT, PrimitiveType.INT.getValidConversion(PrimitiveType.FLOAT));
        assertEquals(PrimitiveType.FLOAT, PrimitiveType.FLOAT.getValidConversion(PrimitiveType.INT));
    }

    @Test
    public void testWildcardConversionAndAssignability() {
        WildcardType wild = new WildcardType(new Scope(), "T");
        for (PrimitiveType type : PrimitiveType.values()) {
            assertEquals(type, type.getValidConversion(wild));
            assertTrue(type.isAssignableFrom(wild));
        }
    }

    @Test
    public void testSelfConversionAndAssignability() {
        for (PrimitiveType type : PrimitiveType.values()) {
            assertEquals(type, type.getValidConversion(type));
            assertTrue(type.isAssignableFrom(type));
        }
    }

    @Test
    public void testCrossTypeConversionAndAssignability() {
        // within primitive type failures to convert
        for (PrimitiveType type : PrimitiveType.values()) {
            if (type == PrimitiveType.FLOAT || type == PrimitiveType.INT) {
                continue; // tested elsewhere
            }

            for (PrimitiveType other : PrimitiveType.values()) {
                if (other != type) {
                    assertNull(type.getValidConversion(other));
                    assertFalse(type.isAssignableFrom(other));
                }
            }
        }

        // other types
        for (PrimitiveType type : PrimitiveType.values()) {
            assertConversionAndAssignability(type, new ArrayType(PrimitiveType.INT, 1));
            assertConversionAndAssignability(type, new FunctionType(Arrays.<Type>asList(PrimitiveType.INT),
                                                                    PrimitiveType.FLOAT));

            Map<String, Type> fields = new HashMap<>();
            fields.put("test", PrimitiveType.INT);
            assertConversionAndAssignability(type, new StructType(fields));

            assertConversionAndAssignability(type,
                                             new UnionType(Arrays.<Type>asList(new FunctionType(Arrays.<Type>asList(PrimitiveType.INT),
                                                                                                PrimitiveType.INT),
                                                                               new FunctionType(Arrays.<Type>asList(PrimitiveType.INT),
                                                                                                PrimitiveType.FLOAT))));
        }
    }

    private void assertConversionAndAssignability(PrimitiveType t, Type other) {
        assertNull(t.getValidConversion(other));
        assertFalse(t.isAssignableFrom(other));
    }

    @Test
    public void testIsConcrete() {
        for (PrimitiveType type : PrimitiveType.values()) {
            assertTrue(type.isConcrete());
        }
    }
}
