package com.lhkbob.fxsl.lang.expr;

import com.lhkbob.fxsl.lang.Scope;
import com.lhkbob.fxsl.lang.type.MetaType;
import com.lhkbob.fxsl.lang.type.PrimitiveType;
import com.lhkbob.fxsl.lang.type.StructTypeTest;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Basic tests for {@link com.lhkbob.fxsl.lang.expr.StructFieldAccess}.
 *
 * @author Michael Ludwig
 */
public class StructFieldAccessTest {
    @Test
    public void testDirectStructAccess() {
        // test construction with valid field on known struct type and all getters and concreteness
        Scope s = new Scope();
        Expression struct = new Parameter(s, "struct", StructTypeTest.makeType(s,
                                                                                         Arrays.asList("field1",
                                                                                                       "field2"),
                                                                                         Arrays.asList(PrimitiveType.INT,
                                                                                                       PrimitiveType.BOOL)));
        StructFieldAccess access = new StructFieldAccess(s, struct, "field1");

        assertEquals(struct, access.getStruct());
        assertEquals("field1", access.getField());
        assertEquals(PrimitiveType.INT, access.getType());
        assertEquals(s, access.getScope());
    }

    @Test
    public void testWildcardStructAccess() {
        // test construction with valid field on wildcard type and all getters and concreteness
        Scope s = new Scope();
        Expression struct = new Parameter(s, "struct", new MetaType(s));
        StructFieldAccess access = new StructFieldAccess(s, struct, "field1");

        assertEquals(struct, access.getStruct());
        assertEquals("field1", access.getField());

        assertTrue(access.getType() instanceof MetaType);
        assertEquals(s, access.getScope());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNoSuchFieldAccess() {
        Scope s = new Scope();
        Expression struct = new Parameter(s, "struct", StructTypeTest.makeType(s,
                                                                                         Arrays.asList("field1",
                                                                                                       "field2"),
                                                                                         Arrays.asList(PrimitiveType.INT,
                                                                                                       PrimitiveType.BOOL)));
        new StructFieldAccess(s, struct, "field3");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorIncorrectExpressionType() {
        Scope s = new Scope();
        Expression struct = new Parameter(s, "struct", PrimitiveType.INT);
        new StructFieldAccess(s, struct, "field");
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullStruct() {
        new StructFieldAccess(new Scope(), null, "field");
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullField() {
        Scope s = new Scope();
        Expression struct = new Parameter(s, "struct", StructTypeTest.makeType(s,
                                                                                         Arrays.asList("field1",
                                                                                                       "field2"),
                                                                                         Arrays.asList(PrimitiveType.INT,
                                                                                                       PrimitiveType.BOOL)));
        new StructFieldAccess(s, struct, null);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullScope() {
        Scope s = new Scope();
        Expression struct = new Parameter(s, "struct", StructTypeTest.makeType(s,
                                                                                         Arrays.asList("field1",
                                                                                                       "field2"),
                                                                                         Arrays.asList(PrimitiveType.INT,
                                                                                                       PrimitiveType.BOOL)));
        new StructFieldAccess(null, struct, "field1");
    }

    @Test
    public void testEqualsAndHashcode() {
        Scope s = new Scope();
        Expression struct1 = new Parameter(s, "struct", StructTypeTest.makeType(s,
                                                                                          Arrays.asList("field1",
                                                                                                        "field2"),
                                                                                          Arrays.asList(PrimitiveType.INT,
                                                                                                        PrimitiveType.BOOL)));
        Expression struct2 = new Parameter(s, "struct2", StructTypeTest.makeType(s,
                                                                                           Arrays.asList("field1",
                                                                                                         "field3"),
                                                                                           Arrays.asList(PrimitiveType.INT,
                                                                                                         PrimitiveType.FLOAT)));
        StructFieldAccess a1a = new StructFieldAccess(s, struct1, "field1");
        StructFieldAccess a1b = new StructFieldAccess(s, struct1, "field1");
        StructFieldAccess a2 = new StructFieldAccess(s, struct1, "field2");
        StructFieldAccess a3 = new StructFieldAccess(s, struct2, "field1");
        StructFieldAccess a4 = new StructFieldAccess(new Scope(), struct1, "field1");

        assertEquals(a1a, a1b);
        assertEquals(a1a.hashCode(), a1b.hashCode());
        assertFalse(a1a.equals(a2));
        assertFalse(a1a.hashCode() == a2.hashCode());
        assertFalse(a1a.equals(a3));
        assertFalse(a1a.hashCode() == a3.hashCode());
        assertFalse(a1a.equals(a4));
        assertFalse(a1a.hashCode() == a4.hashCode());
    }

    @Test
    public void testAccept() {
        Expression struct = new Parameter(new Scope(), "struct", new MetaType(new Scope()));
        StructFieldAccess access = new StructFieldAccess(new Scope(), struct, "field1");
        StructFieldAccess visited = access.accept(new Expression.Visitor<StructFieldAccess>() {
            @Override
            public StructFieldAccess visitArrayAccess(ArrayAccess access) {
                return null;
            }

            @Override
            public StructFieldAccess visitArray(ArrayValue value) {
                return null;
            }

            @Override
            public StructFieldAccess visitFunctionCall(FunctionCall function) {
                return null;
            }

            @Override
            public StructFieldAccess visitFunction(FunctionValue function) {
                return null;
            }

            @Override
            public StructFieldAccess visitParameter(Parameter param) {
                return null;
            }

            @Override
            public StructFieldAccess visitPrimitive(PrimitiveValue primitive) {
                return null;
            }

            @Override
            public StructFieldAccess visitFieldAccess(StructFieldAccess access) {
                return access;
            }

            @Override
            public StructFieldAccess visitStruct(StructValue struct) {
                return null;
            }

            @Override
            public StructFieldAccess visitUnion(UnionValue union) {
                return null;
            }

            @Override
            public StructFieldAccess visitVariable(VariableReference var) {
                return null;
            }

            @Override
            public StructFieldAccess visitNativeExpression(NativeExpression expr) {
                return null;
            }
        });
        assertEquals(access, visited);
    }
}
