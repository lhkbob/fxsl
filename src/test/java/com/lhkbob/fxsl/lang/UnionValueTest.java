package com.lhkbob.fxsl.lang;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Basic unit tests for {@link UnionValue}.
 *
 * @author Michael Ludwig
 */
public class UnionValueTest {
    public static UnionValue makeValue(Expression... functions) {
        return new UnionValue(new HashSet<>(Arrays.asList(functions)));
    }

    @Test
    public void testSimpleUnion() {
        FunctionValue func1 = new FunctionValue(Arrays.asList("a"), Arrays.asList(PrimitiveType.INT),
                                                new PrimitiveValue(1));
        FunctionValue func2 = new FunctionValue(Arrays.asList("a"), Arrays.asList(PrimitiveType.FLOAT),
                                                new PrimitiveValue(1.0f));

        UnionValue value = makeValue(func1, func2);

        UnionType expectedType = UnionTypeTest.makeType(FunctionTypeTest.makeType(PrimitiveType.INT,
                                                                                  PrimitiveType.INT),
                                                        FunctionTypeTest.makeType(PrimitiveType.FLOAT,
                                                                                  PrimitiveType.FLOAT));

        assertEquals(expectedType, value.getType());
        Set<Expression> expectedOptions = new HashSet<>();
        expectedOptions.add(func1);
        expectedOptions.add(func2);
        assertEquals(expectedOptions, value.getOptions());
        assertFalse(value.isConcrete());
    }

    @Test
    public void testWildcardUnion() {
        FunctionValue func1 = new FunctionValue(Arrays.asList("a"), Arrays.asList(PrimitiveType.INT),
                                                new PrimitiveValue(1));
        Expression func2 = new ParameterExpression(new Scope(), "b", new WildcardType(new Scope(), "b"));

        UnionValue value = makeValue(func1, func2);

        UnionType expectedType = UnionTypeTest.makeType(FunctionTypeTest.makeType(PrimitiveType.INT,
                                                                                  PrimitiveType.INT),
                                                        func2.getType());

        assertEquals(expectedType, value.getType());

        Set<Expression> expectedOptions = new HashSet<>();
        expectedOptions.add(func1);
        expectedOptions.add(func2);
        assertEquals(expectedOptions, value.getOptions());

        assertFalse(value.isConcrete());
    }

    @Test
    public void testFlattenedUnionOfUnions() {
        // test the options set result when unions can be flattened
        FunctionValue func1 = new FunctionValue(Arrays.asList("a"), Arrays.asList(PrimitiveType.INT),
                                                new PrimitiveValue(1));
        Expression func2 = new ParameterExpression(new Scope(), "b", new WildcardType(new Scope(), "b"));
        UnionValue value1 = makeValue(func1, func2);

        FunctionValue func3 = new FunctionValue(Arrays.asList("a"), Arrays.asList(PrimitiveType.FLOAT),
                                                new PrimitiveValue(1));
        FunctionValue func4 = new FunctionValue(Arrays.asList("a"), Arrays.asList(PrimitiveType.BOOL),
                                                new PrimitiveValue(1.0f));
        UnionValue value2 = makeValue(func3, func4);

        UnionValue union = makeValue(value1, value2);
        UnionType expectedType = UnionTypeTest.makeType(func1.getType(), func2.getType(), func3.getType(),
                                                        func4.getType());

        assertEquals(expectedType, union.getType());

        Set<Expression> expectedOptions = new HashSet<>();
        expectedOptions.add(func1);
        expectedOptions.add(func2);
        expectedOptions.add(func3);
        expectedOptions.add(func4);
        assertEquals(expectedOptions, union.getOptions());

        assertFalse(union.isConcrete());
    }

    @Test
    public void testUnflattenedUnionOfUnions() {
        // test the options set result if there is a union that cannot be flattened
        FunctionValue func1 = new FunctionValue(Arrays.asList("a"), Arrays.asList(PrimitiveType.INT),
                                                new PrimitiveValue(1));
        Expression func2 = new ParameterExpression(new Scope(), "b", new WildcardType(new Scope(), "b"));
        UnionValue value1 = makeValue(func1, func2);

        FunctionValue func3 = new FunctionValue(Arrays.asList("a"), Arrays.asList(PrimitiveType.FLOAT),
                                                new PrimitiveValue(1));
        FunctionValue func4 = new FunctionValue(Arrays.asList("a"), Arrays.asList(PrimitiveType.BOOL),
                                                new PrimitiveValue(1.0f));

        Expression value2 = new ParameterExpression(new Scope(), "a",
                                                    UnionTypeTest.makeType(func3.getType(), func4.getType()));

        UnionValue union = makeValue(value1, value2);
        UnionType expectedType = UnionTypeTest.makeType(func1.getType(), func2.getType(), func3.getType(),
                                                        func4.getType());

        assertEquals(expectedType, union.getType());

        Set<Expression> expectedOptions = new HashSet<>();
        expectedOptions.add(func1);
        expectedOptions.add(func2);
        expectedOptions.add(value2);
        assertEquals(expectedOptions, union.getOptions());

        assertFalse(union.isConcrete());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorInvalidUnionType() {
        // test creation with two equivalent signature functions
        FunctionValue func1 = new FunctionValue(Arrays.asList("a"), Arrays.asList(PrimitiveType.INT),
                                                new PrimitiveValue(1));
        FunctionValue func2 = new FunctionValue(Arrays.asList("b"), Arrays.asList(PrimitiveType.INT),
                                                new PrimitiveValue(2));
        makeValue(func1, func2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorInvalidOptionType() {
        // test if an expression does not have a union, wildcard, or function
        makeValue(new PrimitiveValue(1), new PrimitiveValue(2));
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullOptions() {
        new UnionValue(null);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullOptionElement() {
        FunctionValue func1 = new FunctionValue(Arrays.asList("a"), Arrays.asList(PrimitiveType.INT),
                                                new PrimitiveValue(1));
        makeValue(func1, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorSingletonOptionSet() {
        FunctionValue func1 = new FunctionValue(Arrays.asList("a"), Arrays.asList(PrimitiveType.INT),
                                                new PrimitiveValue(1));
        makeValue(func1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmptyOptions() {
        new UnionValue(Collections.<Expression>emptySet());
    }

    @Test
    public void testConstructorClonedOptions() {
        FunctionValue func1 = new FunctionValue(Arrays.asList("a"), Arrays.asList(PrimitiveType.INT),
                                                new PrimitiveValue(1));
        FunctionValue func2 = new FunctionValue(Arrays.asList("a"), Arrays.asList(PrimitiveType.FLOAT),
                                                new PrimitiveValue(1.0f));

        Set<Expression> options = new HashSet<>();
        options.add(func1);
        options.add(func2);

        UnionValue value = new UnionValue(options);

        options.remove(func1);
        assertTrue(value.getOptions().contains(func1));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnmodifiableOptionsSet() {
        FunctionValue func1 = new FunctionValue(Arrays.asList("a"), Arrays.asList(PrimitiveType.INT),
                                                new PrimitiveValue(1));
        FunctionValue func2 = new FunctionValue(Arrays.asList("a"), Arrays.asList(PrimitiveType.FLOAT),
                                                new PrimitiveValue(1.0f));
        UnionValue value = makeValue(func1, func2);

        value.getOptions().remove(func1);
    }

    @Test
    public void testEqualsAndHashcode() {
        FunctionValue func1 = new FunctionValue(Arrays.asList("a"), Arrays.asList(PrimitiveType.INT),
                                                new PrimitiveValue(1));
        FunctionValue func2 = new FunctionValue(Arrays.asList("a"), Arrays.asList(PrimitiveType.FLOAT),
                                                new PrimitiveValue(1.0f));
        UnionValue v1a = makeValue(func1, func2);
        UnionValue v1b = makeValue(func1, func2);

        FunctionValue func3 = new FunctionValue(Arrays.asList("a"), Arrays.asList(PrimitiveType.INT),
                                                new PrimitiveValue(1));
        Expression func4 = new ParameterExpression(new Scope(), "b", new WildcardType(new Scope(), "b"));

        UnionValue v2 = makeValue(func3, func4);
        UnionValue v3a = makeValue(func1, func2, func3, func4);
        UnionValue v3b = makeValue(v1a, v2);

        assertEquals(v1a, v1b);
        assertEquals(v1a.hashCode(), v1b.hashCode());
        assertEquals(v3a, v3b);
        assertEquals(v3a.hashCode(), v3b.hashCode());

        assertFalse(v1a.equals(v2));
        assertFalse(v1a.hashCode() == v2.hashCode());
        assertFalse(v1a.equals(v3a));
        assertFalse(v1a.hashCode() == v3a.hashCode());
    }

    @Test
    public void testAccept() {
        FunctionValue func1 = new FunctionValue(Arrays.asList("a"), Arrays.asList(PrimitiveType.INT),
                                                new PrimitiveValue(1));
        FunctionValue func2 = new FunctionValue(Arrays.asList("a"), Arrays.asList(PrimitiveType.FLOAT),
                                                new PrimitiveValue(1.0f));
        UnionValue v = makeValue(func1, func2);
        assertTrue(v.accept(new UnionValueTestVisitor()));
    }

    private static class UnionValueTestVisitor implements Expression.Visitor<Boolean> {
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
            return false;
        }

        @Override
        public Boolean visitUnion(UnionValue union) {
            return true;
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
