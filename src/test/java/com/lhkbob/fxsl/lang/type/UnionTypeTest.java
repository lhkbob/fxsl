package com.lhkbob.fxsl.lang.type;

import com.lhkbob.fxsl.lang.Scope;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Unit tests for {@link com.lhkbob.fxsl.lang.type.UnionType}.
 *
 * @author Michael Ludwig
 */
public class UnionTypeTest {
    public static UnionType makeType(Scope scope, Type... types) {
        return new UnionType(scope, new HashSet<>(Arrays.asList(types)));
    }

    @Test
    public void testEqualsAndHashcode() {
        Scope s = new Scope();
        UnionType t1a = makeType(s, FunctionTypeTest.makeType(s, PrimitiveType.INT, PrimitiveType.INT),
                                 FunctionTypeTest.makeType(s, PrimitiveType.FLOAT, PrimitiveType.FLOAT));
        UnionType t1b = makeType(s, FunctionTypeTest.makeType(s, PrimitiveType.FLOAT, PrimitiveType.FLOAT),
                                 FunctionTypeTest.makeType(s, PrimitiveType.INT, PrimitiveType.INT));
        UnionType t2 = makeType(s, FunctionTypeTest.makeType(s, PrimitiveType.INT, PrimitiveType.INT),
                                new MetaType(new Scope()));
        UnionType t3 = makeType(new Scope(),
                                FunctionTypeTest.makeType(s, PrimitiveType.INT, PrimitiveType.INT),
                                FunctionTypeTest.makeType(s, PrimitiveType.FLOAT, PrimitiveType.FLOAT));

        assertEquals(t1a, t1b);
        assertEquals(t1a.hashCode(), t1b.hashCode());
        assertFalse(t1a.equals(t2));
        assertFalse(t1a.hashCode() == t2.hashCode());
        assertFalse(t1a.equals(t3));
        assertFalse(t1a.hashCode() == t3.hashCode());
    }

    @Test
    public void testGetOptions() {
        // also tests the flattening out of the constructor
        Scope s = new Scope();
        UnionType t1 = makeType(s, FunctionTypeTest.makeType(s, PrimitiveType.INT, PrimitiveType.INT),
                                FunctionTypeTest.makeType(s, PrimitiveType.FLOAT, PrimitiveType.BOOL),
                                FunctionTypeTest
                                        .makeType(s, PrimitiveType.SAMPLER1D, PrimitiveType.SAMPLER1D));
        UnionType t2 = makeType(s, FunctionTypeTest.makeType(s, PrimitiveType.INT, PrimitiveType.INT),
                                FunctionTypeTest.makeType(s, PrimitiveType.FLOAT, PrimitiveType.BOOL),
                                FunctionTypeTest.makeType(s, PrimitiveType.BOOL, PrimitiveType.BOOL));

        UnionType shared = makeType(s, t1, t2);
        Set<? extends Type> expectedOptions = new HashSet<>(Arrays.asList(FunctionTypeTest.makeType(s,
                                                                                                    PrimitiveType.INT,
                                                                                                    PrimitiveType.INT),
                                                                          FunctionTypeTest.makeType(s,
                                                                                                    PrimitiveType.FLOAT,
                                                                                                    PrimitiveType.BOOL),
                                                                          FunctionTypeTest.makeType(s,
                                                                                                    PrimitiveType.BOOL,
                                                                                                    PrimitiveType.BOOL),
                                                                          FunctionTypeTest.makeType(s,
                                                                                                    PrimitiveType.SAMPLER1D,
                                                                                                    PrimitiveType.SAMPLER1D)));
        assertEquals(expectedOptions, shared.getOptions());
    }

    @Test
    public void testGetScope() {
        Scope s = new Scope();
        UnionType t = makeType(s, FunctionTypeTest.makeType(s, PrimitiveType.INT, PrimitiveType.INT),
                               FunctionTypeTest.makeType(s, PrimitiveType.FLOAT, PrimitiveType.BOOL));
        assertEquals(s, t.getScope());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorTypeRestrictions() {
        makeType(new Scope(), new ArrayType(new Scope(), PrimitiveType.INT, 2), PrimitiveType.INT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorSizeRequirement() {
        makeType(new Scope(), FunctionTypeTest.makeType(new Scope(), PrimitiveType.INT, PrimitiveType.INT));
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullSet() {
        new UnionType(new Scope(), null);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullElements() {
        new UnionType(new Scope(), new HashSet<>(Arrays.asList(null, PrimitiveType.INT)));
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullScope() {
        makeType(null, FunctionTypeTest.makeType(new Scope(), PrimitiveType.INT, PrimitiveType.INT),
                 FunctionTypeTest.makeType(new Scope(), PrimitiveType.FLOAT, PrimitiveType.BOOL));
    }

    @Test
    public void testAccept() {
        Scope s = new Scope();
        UnionType t1 = makeType(s, FunctionTypeTest.makeType(s, PrimitiveType.INT, PrimitiveType.INT),
                                FunctionTypeTest.makeType(s, PrimitiveType.FLOAT, PrimitiveType.BOOL),
                                FunctionTypeTest
                                        .makeType(s, PrimitiveType.SAMPLER1D, PrimitiveType.SAMPLER1D));

        UnionType t2 = t1.accept(new Type.Visitor<UnionType>() {
            @Override
            public UnionType visitArrayType(ArrayType t) {
                return null;
            }

            @Override
            public UnionType visitFunctionType(FunctionType t) {
                return null;
            }

            @Override
            public UnionType visitMetaType(MetaType t) {
                return null;
            }

            @Override
            public UnionType visitParametricType(ParametricType t) {
                return null;
            }

            @Override
            public UnionType visitAliasType(AliasType t) {
                return null;
            }

            @Override
            public UnionType visitPrimitiveType(PrimitiveType t) {
                return null;
            }

            @Override
            public UnionType visitStructType(StructType t) {
                return null;
            }

            @Override
            public UnionType visitUnionType(UnionType t) {
                return t;
            }
        });
        assertEquals(t1, t2);
    }
}
