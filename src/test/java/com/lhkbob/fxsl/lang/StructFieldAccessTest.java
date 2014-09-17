package com.lhkbob.fxsl.lang;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Basic tests for {@link com.lhkbob.fxsl.lang.StructFieldAccess}.
 *
 * @author Michael Ludwig
 */
public class StructFieldAccessTest {
    @Test
    public void testDirectStructAccess() {
        // test construction with valid field on known struct type and all getters and concreteness
        Expression struct = new ParameterExpression(new Scope(), "struct", StructTypeTest
                                                                                   .makeType(Arrays.asList("field1",
                                                                                                           "field2"),
                                                                                             Arrays.asList(PrimitiveType.INT,
                                                                                                           PrimitiveType.BOOL)));
        StructFieldAccess access = new StructFieldAccess(struct, "field1");

        assertEquals(struct, access.getStruct());
        assertEquals("field1", access.getField());
        assertEquals(PrimitiveType.INT, access.getType());
        assertTrue(access.isConcrete());
    }

    @Test
    public void testWildcardStructAccess() {
        // test construction with valid field on wildcard type and all getters and concreteness
        Expression struct = new ParameterExpression(new Scope(), "struct",
                                                    new WildcardType(new Scope(), "struct"));
        StructFieldAccess access = new StructFieldAccess(struct, "field1");

        assertEquals(struct, access.getStruct());
        assertEquals("field1", access.getField());

        assertTrue(access.getType() instanceof WildcardType);
        String depLabel = ((WildcardType) access.getType()).getLabel();
        assertEquals("struct:field1", depLabel);

        assertFalse(access.isConcrete());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNoSuchFieldAccess() {
        Expression struct = new ParameterExpression(new Scope(), "struct", StructTypeTest
                                                                                   .makeType(Arrays.asList("field1",
                                                                                                           "field2"),
                                                                                             Arrays.asList(PrimitiveType.INT,
                                                                                                           PrimitiveType.BOOL)));
        new StructFieldAccess(struct, "field3");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorIncorrectExpressionType() {
        Expression struct = new ParameterExpression(new Scope(), "struct", PrimitiveType.INT);
        new StructFieldAccess(struct, "field");
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullStruct() {
        new StructFieldAccess(null, "field");
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullField() {
        Expression struct = new ParameterExpression(new Scope(), "struct", StructTypeTest
                                                                                   .makeType(Arrays.asList("field1",
                                                                                                           "field2"),
                                                                                             Arrays.asList(PrimitiveType.INT,
                                                                                                           PrimitiveType.BOOL)));
        new StructFieldAccess(struct, null);
    }

    @Test
    public void testEqualsAndHashcode() {
        Expression struct1 = new ParameterExpression(new Scope(), "struct", StructTypeTest
                                                                                    .makeType(Arrays.asList("field1",
                                                                                                            "field2"),
                                                                                              Arrays.asList(PrimitiveType.INT,
                                                                                                            PrimitiveType.BOOL)));
        Expression struct2 = new ParameterExpression(new Scope(), "struct2", StructTypeTest
                                                                                     .makeType(Arrays.asList("field1",
                                                                                                             "field3"),
                                                                                               Arrays.asList(PrimitiveType.INT,
                                                                                                             PrimitiveType.FLOAT)));
        StructFieldAccess a1a = new StructFieldAccess(struct1, "field1");
        StructFieldAccess a1b = new StructFieldAccess(struct1, "field1");
        StructFieldAccess a2 = new StructFieldAccess(struct1, "field2");
        StructFieldAccess a3 = new StructFieldAccess(struct2, "field1");

        assertEquals(a1a, a1b);
        assertEquals(a1a.hashCode(), a1b.hashCode());
        assertFalse(a1a.equals(a2));
        assertFalse(a1a.hashCode() == a2.hashCode());
        assertFalse(a1a.equals(a3));
        assertFalse(a1a.hashCode() == a3.hashCode());
    }

    @Test
    public void testAccept() {
        Expression struct = new ParameterExpression(new Scope(), "struct",
                                                    new WildcardType(new Scope(), "struct"));
        StructFieldAccess access = new StructFieldAccess(struct, "field1");

        assertTrue(access.accept(new StructFieldAccessTestVisitor()));
    }

    private static class StructFieldAccessTestVisitor implements Expression.Visitor<Boolean> {
        @Override
        public Boolean visitArrayAccess(ArrayAccess access) {
            return false;
        }

        @Override
        public Boolean visitArray(ArrayValue value) {
            return false;
        }

        @Override
        public Boolean visitFunctionCall(FunctionCall function) {
            return false;
        }

        @Override
        public Boolean visitFunction(FunctionValue function) {
            return false;
        }

        @Override
        public Boolean visitParameter(ParameterExpression param) {
            return false;
        }

        @Override
        public Boolean visitPrimitive(PrimitiveValue primitive) {
            return false;
        }

        @Override
        public Boolean visitFieldAccess(StructFieldAccess access) {
            return true;
        }

        @Override
        public Boolean visitStruct(StructValue struct) {
            return false;
        }

        @Override
        public Boolean visitUnion(UnionValue union) {
            return false;
        }

        @Override
        public Boolean visitVariable(VariableExpression var) {
            return false;
        }

        @Override
        public Boolean visitNativeExpression(NativeExpression expr) {
            return false;
        }
    }
}
