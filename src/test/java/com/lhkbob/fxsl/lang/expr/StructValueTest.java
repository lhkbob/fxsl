package com.lhkbob.fxsl.lang.expr;

import com.lhkbob.fxsl.lang.Scope;
import com.lhkbob.fxsl.lang.type.PrimitiveType;
import com.lhkbob.fxsl.lang.type.StructTypeTest;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Basic test cases for {@link com.lhkbob.fxsl.lang.expr.StructValue}.
 *
 * @author Michael Ludwig
 */
public class StructValueTest {
    public static StructValue makeValue(Scope s, List<String> fields, List<? extends Expression> values) {
        Map<String, Expression> map = new HashMap<>();
        for (int i = 0; i < fields.size(); i++) {
            map.put(fields.get(i), values.get(i));
        }
        return new StructValue(s, map);
    }

    @Test
    public void testGetType() {
        Scope s = new Scope();
        StructValue value = makeValue(s, Arrays.asList("f1", "f2"),
                                      Arrays.asList(new PrimitiveValue(1), new PrimitiveValue(2.0f)));

        assertEquals(StructTypeTest.makeType(s, Arrays.asList("f1", "f2"),
                                             Arrays.asList(PrimitiveType.INT, PrimitiveType.FLOAT)),
                     value.getType());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullMap() {
        new StructValue(new Scope(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmptyMap() {
        new StructValue(new Scope(), Collections.<String, Expression>emptyMap());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullFieldName() {
        makeValue(new Scope(), Arrays.asList("f1", null),
                  Arrays.asList(new PrimitiveValue(1), new PrimitiveValue(2.0f)));
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullFieldValue() {
        makeValue(new Scope(), Arrays.asList("f1", "f2"), Arrays.asList(new PrimitiveValue(1), null));
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullScope() {
        makeValue(null, Arrays.asList("f1", "f2"),
                  Arrays.asList(new PrimitiveValue(1), new PrimitiveValue(2)));
    }

    @Test
    public void testConstructorClonedFields() {
        Map<String, Expression> fields = new HashMap<>();
        fields.put("field", new PrimitiveValue(1));
        StructValue value = new StructValue(new Scope(), fields);

        fields.put("field1", new PrimitiveValue(2.0f));
        fields.put("field", new PrimitiveValue(2));

        assertNull(value.getField("field1"));
        assertEquals(new PrimitiveValue(1), value.getField("field"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnmodifiableFieldMap() {
        StructValue value = makeValue(new Scope(), Arrays.asList("f1", "f2"),
                                      Arrays.asList(new PrimitiveValue(1), new PrimitiveValue(2.0f)));
        value.getFields().put("f3", new PrimitiveValue(false));
    }

    @Test
    public void testGetFields() {
        StructValue value = makeValue(new Scope(), Arrays.asList("f1", "f2"),
                                      Arrays.asList(new PrimitiveValue(1), new PrimitiveValue(2.0f)));
        Map<String, Expression> fields = value.getFields();
        assertEquals(2, fields.size());
        assertEquals(new PrimitiveValue(1), fields.get("f1"));
        assertEquals(new PrimitiveValue(2.0f), fields.get("f2"));
    }

    @Test
    public void testGetField() {
        StructValue value = makeValue(new Scope(), Arrays.asList("f1", "f2"),
                                      Arrays.asList(new PrimitiveValue(1), new PrimitiveValue(2.0f)));
        assertEquals(new PrimitiveValue(1), value.getField("f1"));
        assertEquals(new PrimitiveValue(2.0f), value.getField("f2"));
    }

    @Test
    public void testGetScope() {
        Scope s = new Scope();
        StructValue value = makeValue(s, Arrays.asList("f1", "f2"),
                                      Arrays.asList(new PrimitiveValue(1), new PrimitiveValue(2.0f)));
        assertEquals(s, value.getScope());
    }

    @Test
    public void testEqualsAndHashcode() {
        Scope s = new Scope();
        StructValue v1a = makeValue(s, Arrays.asList("f1", "f2"),
                                    Arrays.asList(new PrimitiveValue(1), new PrimitiveValue(2.0f)));
        StructValue v1b = makeValue(s, Arrays.asList("f1", "f2"),
                                    Arrays.asList(new PrimitiveValue(1), new PrimitiveValue(2.0f)));
        StructValue v2 = makeValue(s, Arrays.asList("f1", "f2", "f3"),
                                   Arrays.asList(new PrimitiveValue(1), new PrimitiveValue(2.0f),
                                                 new PrimitiveValue(false)));
        StructValue v3 = makeValue(s, Arrays.asList("f1", "f2"),
                                   Arrays.asList(new PrimitiveValue(1), new PrimitiveValue(2)));
        StructValue v4 = makeValue(new Scope(), Arrays.asList("f1", "f2"),
                                   Arrays.asList(new PrimitiveValue(1), new PrimitiveValue(2.0f)));

        assertEquals(v1a, v1b);
        assertEquals(v1a.hashCode(), v1b.hashCode());
        assertFalse(v1a.equals(v2));
        assertFalse(v1a.hashCode() == v2.hashCode());
        assertFalse(v1a.equals(v3));
        assertFalse(v1a.hashCode() == v3.hashCode());
        assertFalse(v1a.equals(v4));
        assertFalse(v1a.hashCode() == v4.hashCode());
    }

    @Test
    public void testAccept() {
        StructValue v = makeValue(new Scope(), Arrays.asList("f1", "f2"),
                                  Arrays.asList(new PrimitiveValue(1), new PrimitiveValue(2.0f)));
        StructValue visited = v.accept(new Expression.Visitor<StructValue>() {
            @Override
            public StructValue visitArrayAccess(ArrayAccess access) {
                return null;
            }

            @Override
            public StructValue visitArray(ArrayValue value) {
                return null;
            }

            @Override
            public StructValue visitFunctionCall(FunctionCall function) {
                return null;
            }

            @Override
            public StructValue visitFunction(FunctionValue function) {
                return null;
            }

            @Override
            public StructValue visitParameter(ParameterExpression param) {
                return null;
            }

            @Override
            public StructValue visitPrimitive(PrimitiveValue primitive) {
                return null;
            }

            @Override
            public StructValue visitFieldAccess(StructFieldAccess access) {
                return null;
            }

            @Override
            public StructValue visitStruct(StructValue struct) {
                return struct;
            }

            @Override
            public StructValue visitUnion(UnionValue union) {
                return null;
            }

            @Override
            public StructValue visitVariable(VariableExpression var) {
                return null;
            }

            @Override
            public StructValue visitNativeExpression(NativeExpression expr) {
                return null;
            }
        });
        assertEquals(v, visited);
    }
}
