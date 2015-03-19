package com.lhkbob.fxsl.lang.type;

import com.lhkbob.fxsl.lang.Scope;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Unit tests for {@link com.lhkbob.fxsl.lang.type.FunctionType}.
 *
 * @author Michael Ludwig
 */
public class FunctionTypeTest {
    public static FunctionType makeType(Scope scope, Type returnType, Type... parameters) {
        return new FunctionType(scope, Arrays.asList(parameters), returnType);
    }

    @Test
    public void testEqualsAndHashcode() {
        Scope scope = new Scope();
        FunctionType t1a = makeType(scope, PrimitiveType.INT, PrimitiveType.INT);
        FunctionType t1b = makeType(scope, PrimitiveType.INT, PrimitiveType.INT);
        FunctionType t2 = makeType(scope, PrimitiveType.INT, PrimitiveType.FLOAT);
        FunctionType t3 = makeType(scope, PrimitiveType.INT, PrimitiveType.INT, PrimitiveType.FLOAT);
        FunctionType t4 = makeType(scope, PrimitiveType.FLOAT, PrimitiveType.INT);
        FunctionType t5 = makeType(new Scope(), PrimitiveType.INT, PrimitiveType.INT);

        assertEquals(t1a, t1b);
        assertEquals(t1a.hashCode(), t1b.hashCode());

        assertFalse(t1a.equals(t2));
        assertFalse(t1a.hashCode() == t2.hashCode());
        assertFalse(t1a.equals(t3));
        assertFalse(t1a.hashCode() == t3.hashCode());
        assertFalse(t1a.equals(t4));
        assertFalse(t1a.hashCode() == t4.hashCode());
        assertFalse(t1a.equals(t5));
        assertFalse(t1a.hashCode() == t5.hashCode());
    }

    @Test
    public void testGetScope() {
        Scope s = new Scope();
        FunctionType t = makeType(s, PrimitiveType.INT, PrimitiveType.INT);
        assertEquals(s, t.getScope());
    }

    @Test
    public void testGetReturnType() {
        FunctionType f = makeType(new Scope(), PrimitiveType.INT, PrimitiveType.INT);
        assertEquals(PrimitiveType.INT, f.getReturnType());
    }

    @Test
    public void testGetParameterList() {
        FunctionType f = makeType(new Scope(), PrimitiveType.INT, PrimitiveType.FLOAT, PrimitiveType.BOOL);
        assertEquals(Arrays.<Type>asList(PrimitiveType.FLOAT, PrimitiveType.BOOL), f.getParameterTypes());
    }

    @Test
    public void testGetParameter() {
        FunctionType f = makeType(new Scope(), PrimitiveType.INT, PrimitiveType.FLOAT, PrimitiveType.BOOL);
        assertEquals(PrimitiveType.FLOAT, f.getParameterType(0));
        assertEquals(PrimitiveType.BOOL, f.getParameterType(1));
    }

    @Test
    public void testGetParameterCount() {
        FunctionType f = makeType(new Scope(), PrimitiveType.INT, PrimitiveType.FLOAT, PrimitiveType.BOOL);
        assertEquals(2, f.getParameterCount());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullParameterList() {
        new FunctionType(new Scope(), null, PrimitiveType.INT);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullParameter() {
        makeType(new Scope(), PrimitiveType.INT, PrimitiveType.INT, null);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullReturnType() {
        makeType(new Scope(), null, PrimitiveType.INT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNoParameters() {
        makeType(new Scope(), PrimitiveType.INT);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullScope() {
        makeType(null, PrimitiveType.INT, PrimitiveType.INT);
    }

    @Test
    public void testAccept() {
        FunctionType t1 = makeType(new Scope(), PrimitiveType.BOOL, PrimitiveType.INT);
        FunctionType t2 = t1.accept(new Type.Visitor<FunctionType>() {
            @Override
            public FunctionType visitArrayType(ArrayType t) {
                return null;
            }

            @Override
            public FunctionType visitFunctionType(FunctionType t) {
                return t;
            }

            @Override
            public FunctionType visitMetaType(MetaType t) {
                return null;
            }

            @Override
            public FunctionType visitParametricType(ParametricType t) {
                return null;
            }

            @Override
            public FunctionType visitAliasType(AliasType t) {
                return null;
            }

            @Override
            public FunctionType visitPrimitiveType(PrimitiveType t) {
                return null;
            }

            @Override
            public FunctionType visitStructType(StructType t) {
                return null;
            }

            @Override
            public FunctionType visitUnionType(UnionType t) {
                return null;
            }
        });
        assertEquals(t1, t2);
    }
}
