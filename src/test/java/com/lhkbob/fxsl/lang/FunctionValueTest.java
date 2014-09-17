package com.lhkbob.fxsl.lang;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Basic tests for {@link com.lhkbob.fxsl.lang.FunctionValue}.
 *
 * @author Michael Ludwig
 */
public class FunctionValueTest {
    private static final Expression DEFAULT_RETURN_VALUE = new ParameterExpression(new Scope(), "a",
                                                                                   PrimitiveType.INT);

    @Test
    public void testGetters() {
        FunctionValue value = new FunctionValue(Arrays.asList("a", "b"),
                                                Arrays.asList(PrimitiveType.INT, PrimitiveType.FLOAT),
                                                DEFAULT_RETURN_VALUE);

        FunctionType expectedType = new FunctionType(Arrays.asList(PrimitiveType.INT, PrimitiveType.FLOAT),
                                                     PrimitiveType.INT);

        assertEquals(expectedType, value.getType());
        assertEquals(Arrays.asList("a", "b"), value.getParameterNames());
        assertEquals("a", value.getParameterName(0));
        assertEquals("b", value.getParameterName(1));
        assertEquals(DEFAULT_RETURN_VALUE, value.getReturnValue());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullNames() {
        new FunctionValue(null, Arrays.asList(PrimitiveType.FLOAT), DEFAULT_RETURN_VALUE);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullTypes() {
        new FunctionValue(Arrays.asList("a"), null, DEFAULT_RETURN_VALUE);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullReturnValue() {
        new FunctionValue(Arrays.asList("a"), Arrays.asList(PrimitiveType.FLOAT), null);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullNameElement() {
        new FunctionValue(Arrays.asList("a", null), Arrays.asList(PrimitiveType.FLOAT, PrimitiveType.INT),
                          DEFAULT_RETURN_VALUE);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullTypeElement() {
        new FunctionValue(Arrays.asList("a", "b"), Arrays.asList(PrimitiveType.FLOAT, null),
                          DEFAULT_RETURN_VALUE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmptyNames() {
        new FunctionValue(Collections.<String>emptyList(), Arrays.asList(PrimitiveType.FLOAT),
                          DEFAULT_RETURN_VALUE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmptyTypes() {
        new FunctionValue(Arrays.asList("a"), Collections.<Type>emptyList(), DEFAULT_RETURN_VALUE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorDifferentNameTypeSizes() {
        new FunctionValue(Arrays.asList("a", "b"), Arrays.asList(PrimitiveType.INT), DEFAULT_RETURN_VALUE);
    }

    @Test
    public void testClonedParameterNamesAndTypes() {
        List<String> names = new ArrayList<>();
        names.add("boo");
        List<Type> types = new ArrayList<>();
        types.add(PrimitiveType.FLOAT);

        FunctionValue value = new FunctionValue(names, types, DEFAULT_RETURN_VALUE);
        names.add("foo");
        types.add(PrimitiveType.INT);

        assertEquals(Arrays.asList("boo"), value.getParameterNames());
        assertEquals(Arrays.<Type>asList(PrimitiveType.FLOAT), value.getType().getParameterTypes());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnmodifiableParameterList() {
        FunctionValue value = new FunctionValue(Arrays.asList("a"), Arrays.asList(PrimitiveType.FLOAT),
                                                DEFAULT_RETURN_VALUE);
        value.getParameterNames().add("boo");
    }

    @Test
    public void testEqualsAndHashcode() {
        FunctionValue v1a = new FunctionValue(Arrays.asList("a"), Arrays.asList(PrimitiveType.FLOAT),
                                              DEFAULT_RETURN_VALUE);
        FunctionValue v1b = new FunctionValue(Arrays.asList("a"), Arrays.asList(PrimitiveType.FLOAT),
                                              DEFAULT_RETURN_VALUE);
        FunctionValue v2 = new FunctionValue(Arrays.asList("a"), Arrays.asList(PrimitiveType.INT),
                                             DEFAULT_RETURN_VALUE);
        FunctionValue v3 = new FunctionValue(Arrays.asList("b"), Arrays.asList(PrimitiveType.FLOAT),
                                             DEFAULT_RETURN_VALUE);
        FunctionValue v4 = new FunctionValue(Arrays.asList("a"), Arrays.asList(PrimitiveType.FLOAT),
                                             new PrimitiveValue(3));

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
    public void testIsConcrete() {
        FunctionValue concrete = new FunctionValue(Arrays.asList("a"), Arrays.asList(PrimitiveType.FLOAT),
                                                   new PrimitiveValue(3));
        FunctionValue notConcrete1 = new FunctionValue(Arrays.asList("a"),
                                                       Arrays.asList(new WildcardType(new Scope(), "a")),
                                                       new PrimitiveValue(3));
        FunctionValue notConcrete2 = new FunctionValue(Arrays.asList("a"), Arrays.asList(PrimitiveType.INT),
                                                       new ParameterExpression(new Scope(), "b",
                                                                               new WildcardType(new Scope(),
                                                                                                "a")));

        assertTrue(concrete.isConcrete());
        assertFalse(notConcrete1.isConcrete());
        assertFalse(notConcrete2.isConcrete());
    }

    @Test
    public void testAccept() {
        FunctionValue v = new FunctionValue(Arrays.asList("a"), Arrays.asList(PrimitiveType.FLOAT),
                                            new PrimitiveValue(3));
        assertTrue(v.accept(new FunctionValueTestVisitor()));
    }

    private static class FunctionValueTestVisitor implements Expression.Visitor<Boolean> {
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
            return true;
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
