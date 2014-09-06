package com.lhkbob.fxsl.lang;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link com.lhkbob.fxsl.lang.UnionType}. Note that although the UnionType documentation
 * describes the function selection process, it does not implement any of it so tests are not included here.
 *
 * @author Michael Ludwig
 */
public class UnionTypeTest {
    public static UnionType makeType(Type... types) {
        return new UnionType(new HashSet<>(Arrays.asList(types)));
    }

    @Test
    public void testEqualsAndHashcode() {
        UnionType t1a = makeType(FunctionTypeTest.makeType(PrimitiveType.INT, PrimitiveType.INT),
                                 FunctionTypeTest.makeType(PrimitiveType.FLOAT, PrimitiveType.FLOAT));
        UnionType t1b = makeType(FunctionTypeTest.makeType(PrimitiveType.FLOAT, PrimitiveType.FLOAT),
                                 FunctionTypeTest.makeType(PrimitiveType.INT, PrimitiveType.INT));
        UnionType t2 = makeType(FunctionTypeTest.makeType(PrimitiveType.INT, PrimitiveType.INT),
                                new WildcardType(new Scope(), "t"));

        assertEquals(t1a, t1b);
        assertEquals(t1a.hashCode(), t1b.hashCode());
        assertFalse(t1a.equals(t2));
        assertFalse(t1a.hashCode() == t2.hashCode());
    }

    @Test
    public void testIsConcrete() {
        UnionType concrete = makeType(FunctionTypeTest.makeType(PrimitiveType.INT, PrimitiveType.INT),
                                      FunctionTypeTest.makeType(PrimitiveType.FLOAT, PrimitiveType.INT));
        UnionType notConcrete = makeType(FunctionTypeTest.makeType(PrimitiveType.INT,
                                                                   new WildcardType(new Scope(), "A")),
                                         FunctionTypeTest.makeType(PrimitiveType.INT, PrimitiveType.INT));

        assertTrue(concrete.isConcrete());
        assertFalse(notConcrete.isConcrete());
    }

    @Test
    public void testWildcardAssignabilityAndSharedTypes() {
        UnionType ut = makeType(FunctionTypeTest.makeType(PrimitiveType.INT, PrimitiveType.INT),
                                FunctionTypeTest.makeType(PrimitiveType.FLOAT, PrimitiveType.INT));
        WildcardType w = new WildcardType(new Scope(), "t");

        assertTrue(ut.isAssignableFrom(w));
        assertEquals(ut, ut.getSharedType(w));
        assertEquals(ut, w.getSharedType(ut));
    }

    @Test
    public void testSelfAssignabilityAndSharedTypes() {
        UnionType t = makeType(FunctionTypeTest.makeType(PrimitiveType.INT, PrimitiveType.INT),
                               FunctionTypeTest.makeType(PrimitiveType.FLOAT, PrimitiveType.INT));
        assertTrue(t.isAssignableFrom(t));
        assertEquals(t, t.getSharedType(t));
    }

    @Test
    public void testSubsetAssignabilityAndSharedTypes() {
        UnionType subset = makeType(FunctionTypeTest.makeType(PrimitiveType.INT, PrimitiveType.INT),
                                    FunctionTypeTest.makeType(PrimitiveType.FLOAT, PrimitiveType.BOOL));
        UnionType superset = makeType(FunctionTypeTest.makeType(PrimitiveType.INT, PrimitiveType.INT),
                                      FunctionTypeTest.makeType(PrimitiveType.FLOAT, PrimitiveType.BOOL),
                                      FunctionTypeTest.makeType(PrimitiveType.BOOL, PrimitiveType.BOOL));

        assertTrue(subset.isAssignableFrom(superset));
        assertFalse(superset.isAssignableFrom(subset));
        assertEquals(subset, subset.getSharedType(superset));
        assertEquals(subset, superset.getSharedType(subset));
    }

    @Test
    public void testOverlapAssignabilityAndSharedTypes() {
        UnionType t1 = makeType(FunctionTypeTest.makeType(PrimitiveType.INT, PrimitiveType.INT),
                                FunctionTypeTest.makeType(PrimitiveType.FLOAT, PrimitiveType.BOOL),
                                FunctionTypeTest.makeType(PrimitiveType.SAMPLER1D, PrimitiveType.SAMPLER1D));
        UnionType t2 = makeType(FunctionTypeTest.makeType(PrimitiveType.INT, PrimitiveType.INT),
                                FunctionTypeTest.makeType(PrimitiveType.FLOAT, PrimitiveType.BOOL),
                                FunctionTypeTest.makeType(PrimitiveType.BOOL, PrimitiveType.BOOL));

        UnionType shared = makeType(FunctionTypeTest.makeType(PrimitiveType.INT, PrimitiveType.INT),
                                    FunctionTypeTest.makeType(PrimitiveType.FLOAT, PrimitiveType.BOOL));

        assertFalse(t1.isAssignableFrom(t2));
        assertFalse(t2.isAssignableFrom(t1));
        assertEquals(shared, t1.getSharedType(t2));
        assertEquals(shared, t2.getSharedType(t1));
    }

    @Test
    public void testDisjointAssignabilityAndSharedTypes() {
        UnionType t1 = makeType(FunctionTypeTest.makeType(PrimitiveType.INT, PrimitiveType.INT),
                                FunctionTypeTest.makeType(PrimitiveType.FLOAT, PrimitiveType.BOOL));
        UnionType t2 = makeType(FunctionTypeTest.makeType(PrimitiveType.INT, PrimitiveType.SAMPLER1D),
                                FunctionTypeTest.makeType(PrimitiveType.BOOL, PrimitiveType.BOOL));

        assertFalse(t1.isAssignableFrom(t2));
        assertFalse(t2.isAssignableFrom(t1));
        assertNull(t1.getSharedType(t2));
        assertNull(t2.getSharedType(t1));
    }

    @Test
    public void testGetOptions() {
        // also tests the flattening out of the constructor
        UnionType t1 = makeType(FunctionTypeTest.makeType(PrimitiveType.INT, PrimitiveType.INT),
                                FunctionTypeTest.makeType(PrimitiveType.FLOAT, PrimitiveType.BOOL),
                                FunctionTypeTest.makeType(PrimitiveType.SAMPLER1D, PrimitiveType.SAMPLER1D));
        UnionType t2 = makeType(FunctionTypeTest.makeType(PrimitiveType.INT, PrimitiveType.INT),
                                FunctionTypeTest.makeType(PrimitiveType.FLOAT, PrimitiveType.BOOL),
                                FunctionTypeTest.makeType(PrimitiveType.BOOL, PrimitiveType.BOOL));

        UnionType shared = makeType(t1, t2);
        Set<Type> expectedOptions = new HashSet<>(Arrays.<Type>asList(FunctionTypeTest
                                                                              .makeType(PrimitiveType.INT,
                                                                                        PrimitiveType.INT),
                                                                      FunctionTypeTest
                                                                              .makeType(PrimitiveType.FLOAT,
                                                                                        PrimitiveType.BOOL),
                                                                      FunctionTypeTest
                                                                              .makeType(PrimitiveType.BOOL,
                                                                                        PrimitiveType.BOOL),
                                                                      FunctionTypeTest
                                                                              .makeType(PrimitiveType.SAMPLER1D,
                                                                                        PrimitiveType.SAMPLER1D)));
        assertEquals(expectedOptions, shared.getOptions());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorTypeRestrictions() {
        makeType(new ArrayType(PrimitiveType.INT, 2), PrimitiveType.INT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorSizeRequirement() {
        makeType(FunctionTypeTest.makeType(PrimitiveType.INT, PrimitiveType.INT));
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullSet() {
        new UnionType(null);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullElements() {
        new UnionType(new HashSet<>(Arrays.asList(null, PrimitiveType.INT)));
    }
}
