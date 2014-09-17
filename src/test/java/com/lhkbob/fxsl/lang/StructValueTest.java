package com.lhkbob.fxsl.lang;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Basic test cases for {@link com.lhkbob.fxsl.lang.StructValue}.
 *
 * @author Michael Ludwig
 */
public class StructValueTest {
    public static StructValue makeValue(List<String> fields, List<? extends Expression> values) {
        Map<String, Expression> map = new HashMap<>();
        for (int i = 0; i < fields.size(); i++) {
            map.put(fields.get(i), values.get(i));
        }
        return new StructValue(map);
    }

    @Test
    public void testGetType() {
        StructValue value = makeValue(Arrays.asList("f1", "f2"),
                                      Arrays.asList(new PrimitiveValue(1), new PrimitiveValue(2.0f)));

        assertEquals(StructTypeTest.makeType(Arrays.asList("f1", "f2"),
                                             Arrays.asList(PrimitiveType.INT, PrimitiveType.FLOAT)),
                     value.getType());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullMap() {
        new StructValue(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmptyMap() {
        new StructValue(Collections.<String, Expression>emptyMap());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullFieldName() {
        makeValue(Arrays.asList("f1", null), Arrays.asList(new PrimitiveValue(1), new PrimitiveValue(2.0f)));
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullFieldValue() {
        makeValue(Arrays.asList("f1", "f2"), Arrays.asList(new PrimitiveValue(1), null));
    }

    @Test
    public void testConstructorClonedFields() {
        Map<String, Expression> fields = new HashMap<>();
        fields.put("field", new PrimitiveValue(1));
        StructValue value = new StructValue(fields);

        fields.put("field1", new PrimitiveValue(2.0f));
        fields.put("field", new PrimitiveValue(2));

        assertNull(value.getField("field1"));
        assertEquals(new PrimitiveValue(1), value.getField("field"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnmodifiableFieldMap() {
        StructValue value = makeValue(Arrays.asList("f1", "f2"),
                                      Arrays.asList(new PrimitiveValue(1), new PrimitiveValue(2.0f)));
        value.getFields().put("f3", new PrimitiveValue(false));
    }

    @Test
    public void testGetFields() {
        StructValue value = makeValue(Arrays.asList("f1", "f2"),
                                      Arrays.asList(new PrimitiveValue(1), new PrimitiveValue(2.0f)));
        Map<String, Expression> fields = value.getFields();
        assertEquals(2, fields.size());
        assertEquals(new PrimitiveValue(1), fields.get("f1"));
        assertEquals(new PrimitiveValue(2.0f), fields.get("f2"));
    }

    @Test
    public void testGetField() {
        StructValue value = makeValue(Arrays.asList("f1", "f2"),
                                      Arrays.asList(new PrimitiveValue(1), new PrimitiveValue(2.0f)));
        assertEquals(new PrimitiveValue(1), value.getField("f1"));
        assertEquals(new PrimitiveValue(2.0f), value.getField("f2"));
    }

    @Test
    public void testEqualsAndHashcode() {
        StructValue v1a = makeValue(Arrays.asList("f1", "f2"),
                                    Arrays.asList(new PrimitiveValue(1), new PrimitiveValue(2.0f)));
        StructValue v1b = makeValue(Arrays.asList("f1", "f2"),
                                    Arrays.asList(new PrimitiveValue(1), new PrimitiveValue(2.0f)));
        StructValue v2 = makeValue(Arrays.asList("f1", "f2", "f3"),
                                   Arrays.asList(new PrimitiveValue(1), new PrimitiveValue(2.0f),
                                                 new PrimitiveValue(false)));
        StructValue v3 = makeValue(Arrays.asList("f1", "f2"),
                                   Arrays.asList(new PrimitiveValue(1), new PrimitiveValue(2)));

        assertEquals(v1a, v1b);
        assertEquals(v1a.hashCode(), v1b.hashCode());
        assertFalse(v1a.equals(v2));
        assertFalse(v1a.hashCode() == v2.hashCode());
        assertFalse(v1a.equals(v3));
        assertFalse(v1a.hashCode() == v3.hashCode());
    }

    @Test
    public void testIsConcrete() {
        StructValue concrete = makeValue(Arrays.asList("f1", "f2"),
                                         Arrays.asList(new PrimitiveValue(1), new PrimitiveValue(2.0f)));
        StructValue notConcrete = makeValue(Arrays.asList("f1", "f2"), Arrays.asList(new PrimitiveValue(1),
                                                                                     new ParameterExpression(new Scope(),
                                                                                                             "a",
                                                                                                             new WildcardType(new Scope(),
                                                                                                                              "aType"))));

        assertTrue(concrete.isConcrete());
        assertFalse(notConcrete.isConcrete());
    }

    @Test
    public void testAccept() {
        StructValue v = makeValue(Arrays.asList("f1", "f2"),
                                  Arrays.asList(new PrimitiveValue(1), new PrimitiveValue(2.0f)));
        assertTrue(v.accept(new StructValueTestVisitor()));
    }

    private static class StructValueTestVisitor implements Expression.Visitor<Boolean> {
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
            return false;
        }

        @Override
        public Boolean visitStruct(StructValue struct) {
            return true;
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
