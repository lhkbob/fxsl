package com.lhkbob.fxsl.lang.type;

import com.lhkbob.fxsl.lang.Scope;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Verify the behavior of {@link com.lhkbob.fxsl.lang.type.PrimitiveType}.
 *
 * @author Michael Ludwig
 */
public class PrimitiveTypeTest {
    @Test
    public void testEqualsAndHashcode() {
        assertEquals(PrimitiveType.INT, PrimitiveType.INT);
        assertFalse(PrimitiveType.BOOL.equals(PrimitiveType.INT));
        assertFalse(PrimitiveType.FLOAT.equals(PrimitiveType.INT));
        assertFalse(PrimitiveType.SAMPLER1D.equals(PrimitiveType.INT));
        assertFalse(PrimitiveType.SAMPLER2D.equals(PrimitiveType.INT));
        assertFalse(PrimitiveType.SAMPLER3D.equals(PrimitiveType.INT));
        assertFalse(PrimitiveType.SAMPLERCUBE.equals(PrimitiveType.INT));

        assertEquals(PrimitiveType.INT.hashCode(), PrimitiveType.INT.hashCode());
        assertFalse(PrimitiveType.INT.hashCode() == PrimitiveType.BOOL.hashCode());
        assertFalse(PrimitiveType.INT.hashCode() == PrimitiveType.FLOAT.hashCode());
        assertFalse(PrimitiveType.INT.hashCode() == PrimitiveType.SAMPLER1D.hashCode());
        assertFalse(PrimitiveType.INT.hashCode() == PrimitiveType.SAMPLER2D.hashCode());
        assertFalse(PrimitiveType.INT.hashCode() == PrimitiveType.SAMPLER3D.hashCode());
        assertFalse(PrimitiveType.INT.hashCode() == PrimitiveType.SAMPLERCUBE.hashCode());
    }

    @Test
    public void testGetScope() {
        assertEquals(Scope.NATIVE_SCOPE, PrimitiveType.INT.getScope());
        assertEquals(Scope.NATIVE_SCOPE, PrimitiveType.FLOAT.getScope());
        assertEquals(Scope.NATIVE_SCOPE, PrimitiveType.BOOL.getScope());
        assertEquals(Scope.NATIVE_SCOPE, PrimitiveType.SAMPLER1D.getScope());
        assertEquals(Scope.NATIVE_SCOPE, PrimitiveType.SAMPLER2D.getScope());
        assertEquals(Scope.NATIVE_SCOPE, PrimitiveType.SAMPLER3D.getScope());
        assertEquals(Scope.NATIVE_SCOPE, PrimitiveType.SAMPLERCUBE.getScope());
    }

    @Test
    public void testAccept() {
        for (PrimitiveType t: PrimitiveType.values()) {
            doAcceptTest(t);
        }
    }

    private void doAcceptTest(PrimitiveType type) {
        PrimitiveType t = type.accept(new Type.Visitor<PrimitiveType>() {
            @Override
            public PrimitiveType visitArrayType(ArrayType t) {
                return null;
            }

            @Override
            public PrimitiveType visitFunctionType(FunctionType t) {
                return null;
            }

            @Override
            public PrimitiveType visitMetaType(MetaType t) {
                return null;
            }

            @Override
            public PrimitiveType visitParametricType(ParametricType t) {
                return null;
            }

            @Override
            public PrimitiveType visitAliasType(AliasType t) {
                return null;
            }

            @Override
            public PrimitiveType visitPrimitiveType(PrimitiveType t) {
                return t;
            }

            @Override
            public PrimitiveType visitStructType(StructType t) {
                return null;
            }

            @Override
            public PrimitiveType visitUnionType(UnionType t) {
                return null;
            }
        });
        assertEquals(t, type);
    }
}
