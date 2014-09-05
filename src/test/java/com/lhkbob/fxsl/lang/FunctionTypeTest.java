package com.lhkbob.fxsl.lang;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link com.lhkbob.fxsl.lang.FunctionType}, targeted particularly at how it handles
 * assignment and sharing with union types.
 *
 * @author Michael Ludwig
 */
public class FunctionTypeTest {
    public static FunctionType makeType(Type returnType, Type... parameters) {
        return new FunctionType(Arrays.asList(parameters), returnType);
    }

    @Test
    public void testEqualsAndHashcode() {
        FunctionType t1a = makeType(PrimitiveType.INT, PrimitiveType.INT);
        FunctionType t1b = makeType(PrimitiveType.INT, PrimitiveType.INT);
        FunctionType t2 = makeType(PrimitiveType.INT, PrimitiveType.FLOAT);
        FunctionType t3 = makeType(PrimitiveType.INT, PrimitiveType.INT, PrimitiveType.FLOAT);
        FunctionType t4 = makeType(PrimitiveType.FLOAT, PrimitiveType.INT);

        assertEquals(t1a, t1b);
        assertEquals(t1a.hashCode(), t1b.hashCode());

        assertFalse(t1a.equals(t2));
        assertFalse(t1a.hashCode() == t2.hashCode());
        assertFalse(t1a.equals(t3));
        assertFalse(t1a.hashCode() == t3.hashCode());
        assertFalse(t1a.equals(t4));
        assertFalse(t1a.hashCode() == t4.hashCode());
    }

    @Test
    public void testIsConcrete() {
        FunctionType concrete = makeType(PrimitiveType.FLOAT, PrimitiveType.BOOL);
        FunctionType notConcrete = makeType(PrimitiveType.FLOAT, new WildcardType(new Scope(), "t"));

        assertTrue(concrete.isConcrete());
        assertFalse(notConcrete.isConcrete());
    }

    @Test
    public void testWildcardAssignabilityAndSharing() {
        FunctionType ft = makeType(PrimitiveType.INT, PrimitiveType.INT);
        WildcardType w = new WildcardType(new Scope(), "t");

        assertTrue(ft.isAssignableFrom(w));
        assertEquals(ft, ft.getSharedType(w));
        assertEquals(ft, w.getSharedType(ft));
    }

    @Test
    public void testSelfAssignabilityAndSharing() {
        FunctionType ft = makeType(PrimitiveType.INT, PrimitiveType.FLOAT);
        assertTrue(ft.isAssignableFrom(ft));
        assertEquals(ft, ft.getSharedType(ft));
    }

    @Test
    public void testNarrowReturnTypeWidParametersAssignabilityAndSharing() {
        FunctionType t1 = makeType(PrimitiveType.FLOAT, PrimitiveType.INT, StructTypeTest
                                                                                   .makeType(Arrays.asList("a",
                                                                                                           "b",
                                                                                                           "c"),
                                                                                             Arrays.asList(PrimitiveType.INT,
                                                                                                           PrimitiveType.FLOAT,
                                                                                                           PrimitiveType.BOOL)));
        FunctionType t2 = makeType(PrimitiveType.INT, PrimitiveType.FLOAT, StructTypeTest
                                                                                   .makeType(Arrays.asList("a",
                                                                                                           "b"),
                                                                                             Arrays.asList(PrimitiveType.INT,
                                                                                                           PrimitiveType.FLOAT)));

        assertTrue(t1.isAssignableFrom(t2));
        assertFalse(t2.isAssignableFrom(t1));

        assertEquals(t1, t1.getSharedType(t2));
        assertEquals(t1, t2.getSharedType(t1));
    }

    @Test
    public void testNotAssignableButValidSharing() {
        FunctionType t1 = makeType(PrimitiveType.INT, PrimitiveType.INT, StructTypeTest
                                                                                 .makeType(Arrays.asList("a",
                                                                                                         "b",
                                                                                                         "c"),
                                                                                           Arrays.asList(PrimitiveType.INT,
                                                                                                         PrimitiveType.FLOAT,
                                                                                                         PrimitiveType.BOOL)));
        FunctionType t2 = makeType(PrimitiveType.FLOAT, PrimitiveType.FLOAT, StructTypeTest
                                                                                     .makeType(Arrays.asList("a",
                                                                                                             "b"),
                                                                                               Arrays.asList(PrimitiveType.INT,
                                                                                                             PrimitiveType.FLOAT)));

        FunctionType shared = new FunctionType(t1.getParameterTypes(), t2.getReturnType());

        assertFalse(t1.isAssignableFrom(t2));
        assertFalse(t2.isAssignableFrom(t1));
        assertEquals(shared, t1.getSharedType(t2));
        assertEquals(shared, t2.getSharedType(t1));
    }

    @Test
    public void testUnionMultipleValidAssignabilityAndSharing() {
        FunctionType t1 = makeType(PrimitiveType.INT, PrimitiveType.INT, StructTypeTest
                                                                                 .makeType(Arrays.asList("a",
                                                                                                         "b",
                                                                                                         "c"),
                                                                                           Arrays.asList(PrimitiveType.INT,
                                                                                                         PrimitiveType.FLOAT,
                                                                                                         PrimitiveType.BOOL)));
        FunctionType t2 = makeType(PrimitiveType.INT, PrimitiveType.INT, StructTypeTest
                                                                                 .makeType(Arrays.asList("a",
                                                                                                         "b"),
                                                                                           Arrays.asList(PrimitiveType.INT,
                                                                                                         PrimitiveType.FLOAT)));
        FunctionType t3 = makeType(PrimitiveType.FLOAT, PrimitiveType.FLOAT, StructTypeTest
                                                                                     .makeType(Arrays.asList("a",
                                                                                                             "b"),
                                                                                               Arrays.asList(PrimitiveType.INT,
                                                                                                             PrimitiveType.FLOAT)));
        UnionType union = new UnionType(new HashSet<>(Arrays.asList(t2, t3)));

        UnionType shared = new UnionType(new HashSet<>(Arrays.asList(t1,
                                                                     new FunctionType(t1.getParameterTypes(),
                                                                                      PrimitiveType.FLOAT))));

        assertTrue(t1.isAssignableFrom(union));
        assertEquals(shared, t1.getSharedType(union));
        assertEquals(shared, union.getSharedType(t1));
    }

    @Test
    public void testUnionSingleValidAssignabilityAndSharing() {
        FunctionType t1 = makeType(PrimitiveType.FLOAT, PrimitiveType.FLOAT, StructTypeTest
                                                                                     .makeType(Arrays.asList("a",
                                                                                                             "b"),
                                                                                               Arrays.asList(PrimitiveType.INT,
                                                                                                             PrimitiveType.FLOAT)));
        FunctionType t2 = makeType(PrimitiveType.INT, PrimitiveType.BOOL, StructTypeTest
                                                                                  .makeType(Arrays.asList("a",
                                                                                                          "b"),
                                                                                            Arrays.asList(PrimitiveType.INT,
                                                                                                          PrimitiveType.FLOAT)));
        FunctionType t3 = makeType(PrimitiveType.INT, PrimitiveType.INT, StructTypeTest
                                                                                 .makeType(Arrays.asList("a",
                                                                                                         "b",
                                                                                                         "c"),
                                                                                           Arrays.asList(PrimitiveType.INT,
                                                                                                         PrimitiveType.FLOAT,
                                                                                                         PrimitiveType.BOOL)));
        UnionType union = new UnionType(new HashSet<>(Arrays.asList(t2, t3)));

        FunctionType shared = makeType(PrimitiveType.FLOAT, PrimitiveType.INT, StructTypeTest
                                                                                       .makeType(Arrays.asList("a",
                                                                                                               "b",
                                                                                                               "c"),
                                                                                                 Arrays.asList(PrimitiveType.INT,
                                                                                                               PrimitiveType.FLOAT,
                                                                                                               PrimitiveType.BOOL)));

        assertFalse(t1.isAssignableFrom(union));
        assertEquals(shared, t1.getSharedType(union));
        assertEquals(shared, union.getSharedType(t1));
    }

    @Test
    public void testUnionInvalidAssignabilityAndSharing() {
        FunctionType t1 = makeType(PrimitiveType.FLOAT, PrimitiveType.FLOAT, StructTypeTest
                                                                                     .makeType(Arrays.asList("a",
                                                                                                             "b"),
                                                                                               Arrays.asList(PrimitiveType.INT,
                                                                                                             PrimitiveType.FLOAT)));
        FunctionType t2 = makeType(PrimitiveType.INT, PrimitiveType.BOOL, StructTypeTest
                                                                                  .makeType(Arrays.asList("a",
                                                                                                          "b"),
                                                                                            Arrays.asList(PrimitiveType.INT,
                                                                                                          PrimitiveType.FLOAT)));
        FunctionType t3 = makeType(PrimitiveType.BOOL, PrimitiveType.INT, StructTypeTest
                                                                                  .makeType(Arrays.asList("a",
                                                                                                          "b",
                                                                                                          "c"),
                                                                                            Arrays.asList(PrimitiveType.INT,
                                                                                                          PrimitiveType.FLOAT,
                                                                                                          PrimitiveType.BOOL)));
        UnionType union = new UnionType(new HashSet<>(Arrays.asList(t2, t3)));

        assertFalse(t1.isAssignableFrom(union));
        assertNull(t1.getSharedType(union));
        assertNull(union.getSharedType(t1));
    }

    @Test
    public void testOtherTypeAssignabilityAndSharedTypes() {
        assertNotAssignable(PrimitiveType.INT);
        assertNotAssignable(new ArrayType(PrimitiveType.INT, 4));

        Map<String, Type> fields = new HashMap<>();
        fields.put("test", PrimitiveType.INT);
        assertNotAssignable(new StructType(fields));
    }

    private void assertNotAssignable(Type other) {
        FunctionType t = makeType(PrimitiveType.INT, PrimitiveType.INT);
        assertNull(t.getSharedType(other));
        assertFalse(t.isAssignableFrom(other));
    }

    @Test
    public void testGetReturnType() {
        FunctionType f = makeType(PrimitiveType.INT, PrimitiveType.INT);
        assertEquals(PrimitiveType.INT, f.getReturnType());
    }

    @Test
    public void testGetParameterList() {
        FunctionType f = makeType(PrimitiveType.INT, PrimitiveType.FLOAT, PrimitiveType.BOOL);
        assertEquals(Arrays.<Type>asList(PrimitiveType.FLOAT, PrimitiveType.BOOL), f.getParameterTypes());
    }

    @Test
    public void testGetParameter() {
        FunctionType f = makeType(PrimitiveType.INT, PrimitiveType.FLOAT, PrimitiveType.BOOL);
        assertEquals(PrimitiveType.FLOAT, f.getParameterType(0));
        assertEquals(PrimitiveType.BOOL, f.getParameterType(1));
    }

    @Test
    public void testGetParameterCount() {
        FunctionType f = makeType(PrimitiveType.INT, PrimitiveType.FLOAT, PrimitiveType.BOOL);
        assertEquals(2, f.getParameterCount());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullParameterList() {
        new FunctionType(null, PrimitiveType.INT);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullParameter() {
        makeType(PrimitiveType.INT, PrimitiveType.INT, null);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullReturnType() {
        makeType(null, PrimitiveType.INT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNoParameters() {
        makeType(PrimitiveType.INT);
    }
}
