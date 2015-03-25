package com.lhkbob.fxsl.lang.expr;

import com.lhkbob.fxsl.lang.Scope;
import com.lhkbob.fxsl.lang.type.*;
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
    public static UnionValue makeValue(Scope s, Expression... functions) {
        return new UnionValue(s, new HashSet<>(Arrays.asList(functions)));
    }

    @Test
    public void testSimpleUnion() {
        Scope s = new Scope();
        FunctionValue func1 = FunctionValueTest.makeFunctionValue(s, Arrays.asList("a"),
                                                                  Arrays.asList(PrimitiveType.INT),
                                                                  new PrimitiveValue(1));
        FunctionValue func2 = FunctionValueTest.makeFunctionValue(s, Arrays.asList("a"),
                                                                  Arrays.asList(PrimitiveType.FLOAT),
                                                                  new PrimitiveValue(1.0f));

        UnionValue value = makeValue(s, func1, func2);

        UnionType expectedType = UnionTypeTest.makeType(s, FunctionTypeTest.makeType(s, PrimitiveType.INT,
                                                                                     PrimitiveType.INT),
                                                        FunctionTypeTest.makeType(s, PrimitiveType.FLOAT,
                                                                                  PrimitiveType.FLOAT));

        assertEquals(expectedType, value.getType());
        Set<Expression> expectedOptions = new HashSet<>();
        expectedOptions.add(func1);
        expectedOptions.add(func2);
        assertEquals(expectedOptions, value.getOptions());
        assertEquals(s, value.getScope());
    }

    @Test
    public void testWildcardUnion() {
        Scope s = new Scope();
        FunctionValue func1 = FunctionValueTest.makeFunctionValue(s, Arrays.asList("a"),
                                                                  Arrays.asList(PrimitiveType.INT),
                                                                  new PrimitiveValue(1));
        Expression func2 = new ParameterExpression(new Scope(), "b", new MetaType(new Scope()));

        UnionValue value = makeValue(s, func1, func2);

        UnionType expectedType = UnionTypeTest.makeType(s, FunctionTypeTest.makeType(s, PrimitiveType.INT,
                                                                                     PrimitiveType.INT),
                                                        func2.getType());

        assertEquals(expectedType, value.getType());

        Set<Expression> expectedOptions = new HashSet<>();
        expectedOptions.add(func1);
        expectedOptions.add(func2);
        assertEquals(expectedOptions, value.getOptions());
        assertEquals(s, value.getScope());
    }

    @Test
    public void testFlattenedUnionOfUnions() {
        // test the options set result when unions can be flattened
        Scope s = new Scope();
        FunctionValue func1 = FunctionValueTest.makeFunctionValue(s, Arrays.asList("a"),
                                                                  Arrays.asList(PrimitiveType.INT),
                                                                  new PrimitiveValue(1));
        Expression func2 = new ParameterExpression(new Scope(), "b", new MetaType(new Scope()));
        UnionValue value1 = makeValue(s, func1, func2);

        FunctionValue func3 = FunctionValueTest.makeFunctionValue(s, Arrays.asList("a"),
                                                                  Arrays.asList(PrimitiveType.FLOAT),
                                                                  new PrimitiveValue(1));
        FunctionValue func4 = FunctionValueTest.makeFunctionValue(s, Arrays.asList("a"),
                                                                  Arrays.asList(PrimitiveType.BOOL),
                                                                  new PrimitiveValue(1.0f));
        UnionValue value2 = makeValue(s, func3, func4);

        UnionValue union = makeValue(s, value1, value2);
        UnionType expectedType = UnionTypeTest.makeType(s, func1.getType(), func2.getType(), func3.getType(),
                                                        func4.getType());

        assertEquals(expectedType, union.getType());

        Set<Expression> expectedOptions = new HashSet<>();
        expectedOptions.add(func1);
        expectedOptions.add(func2);
        expectedOptions.add(func3);
        expectedOptions.add(func4);
        assertEquals(expectedOptions, union.getOptions());
        assertEquals(s, union.getScope());
    }

    @Test
    public void testUnflattenedUnionOfUnions() {
        // test the options set result if there is a union that cannot be flattened
        Scope s = new Scope();
        FunctionValue func1 = FunctionValueTest.makeFunctionValue(s, Arrays.asList("a"),
                                                                  Arrays.asList(PrimitiveType.INT),
                                                                  new PrimitiveValue(1));
        Expression func2 = new ParameterExpression(new Scope(), "b", new MetaType(new Scope()));
        UnionValue value1 = makeValue(s, func1, func2);

        FunctionValue func3 = FunctionValueTest.makeFunctionValue(s, Arrays.asList("a"),
                                                                  Arrays.asList(PrimitiveType.FLOAT),
                                                                  new PrimitiveValue(1));
        FunctionValue func4 = FunctionValueTest.makeFunctionValue(s, Arrays.asList("a"),
                                                                  Arrays.asList(PrimitiveType.BOOL),
                                                                  new PrimitiveValue(1.0f));

        Expression value2 = new ParameterExpression(new Scope(), "a", UnionTypeTest
                                                                              .makeType(s, func3.getType(),
                                                                                        func4.getType()));

        UnionValue union = makeValue(s, value1, value2);
        UnionType expectedType = UnionTypeTest.makeType(s, func1.getType(), func2.getType(), func3.getType(),
                                                        func4.getType());

        assertEquals(expectedType, union.getType());
        assertEquals(s, union.getScope());

        Set<Expression> expectedOptions = new HashSet<>();
        expectedOptions.add(func1);
        expectedOptions.add(func2);
        expectedOptions.add(value2);
        assertEquals(expectedOptions, union.getOptions());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorInvalidUnionType() {
        // test creation with two equivalent signature functions
        Scope s = new Scope();
        FunctionValue func1 = FunctionValueTest.makeFunctionValue(s, Arrays.asList("a"),
                                                                  Arrays.asList(PrimitiveType.INT),
                                                                  new PrimitiveValue(1));
        FunctionValue func2 = FunctionValueTest.makeFunctionValue(s, Arrays.asList("b"),
                                                                  Arrays.asList(PrimitiveType.INT),
                                                                  new PrimitiveValue(2));
        makeValue(s, func1, func2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorInvalidOptionType() {
        // test if an expression does not have a union, wildcard, or function
        makeValue(new Scope(), new PrimitiveValue(1), new PrimitiveValue(2));
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullOptions() {
        new UnionValue(new Scope(), null);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullOptionElement() {
        FunctionValue func1 = FunctionValueTest.makeFunctionValue(new Scope(), Arrays.asList("a"),
                                                                  Arrays.asList(PrimitiveType.INT),
                                                                  new PrimitiveValue(1));
        makeValue(new Scope(), func1, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorSingletonOptionSet() {
        FunctionValue func1 = FunctionValueTest.makeFunctionValue(new Scope(), Arrays.asList("a"),
                                                                  Arrays.asList(PrimitiveType.INT),
                                                                  new PrimitiveValue(1));
        makeValue(new Scope(), func1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmptyOptions() {
        new UnionValue(new Scope(), Collections.<Expression>emptySet());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullScope() {
        Scope s = new Scope();
        FunctionValue func1 = FunctionValueTest.makeFunctionValue(s, Arrays.asList("a"),
                                                                  Arrays.asList(PrimitiveType.INT),
                                                                  new PrimitiveValue(1));
        FunctionValue func2 = FunctionValueTest.makeFunctionValue(s, Arrays.asList("b"),
                                                                  Arrays.asList(PrimitiveType.INT),
                                                                  new PrimitiveValue(2));
        makeValue(null, func1, func2);
    }

    @Test
    public void testConstructorClonedOptions() {
        Scope s = new Scope();
        FunctionValue func1 = FunctionValueTest.makeFunctionValue(s, Arrays.asList("a"),
                                                                  Arrays.asList(PrimitiveType.INT),
                                                                  new PrimitiveValue(1));
        FunctionValue func2 = FunctionValueTest.makeFunctionValue(s, Arrays.asList("a"),
                                                                  Arrays.asList(PrimitiveType.FLOAT),
                                                                  new PrimitiveValue(1.0f));

        Set<Expression> options = new HashSet<>();
        options.add(func1);
        options.add(func2);

        UnionValue value = new UnionValue(s, options);

        options.remove(func1);
        assertTrue(value.getOptions().contains(func1));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnmodifiableOptionsSet() {
        Scope s = new Scope();
        FunctionValue func1 = FunctionValueTest.makeFunctionValue(s, Arrays.asList("a"),
                                                                  Arrays.asList(PrimitiveType.INT),
                                                                  new PrimitiveValue(1));
        FunctionValue func2 = FunctionValueTest.makeFunctionValue(s, Arrays.asList("a"),
                                                                  Arrays.asList(PrimitiveType.FLOAT),
                                                                  new PrimitiveValue(1.0f));
        UnionValue value = makeValue(s, func1, func2);

        value.getOptions().remove(func1);
    }

    @Test
    public void testEqualsAndHashcode() {
        Scope s = new Scope();
        FunctionValue func1 = FunctionValueTest.makeFunctionValue(s, Arrays.asList("a"),
                                                                  Arrays.asList(PrimitiveType.INT),
                                                                  new PrimitiveValue(1));
        FunctionValue func2 = FunctionValueTest.makeFunctionValue(s, Arrays.asList("a"),
                                                                  Arrays.asList(PrimitiveType.FLOAT),
                                                                  new PrimitiveValue(1.0f));
        UnionValue v1a = makeValue(s, func1, func2);
        UnionValue v1b = makeValue(s, func1, func2);

        FunctionValue func3 = FunctionValueTest.makeFunctionValue(s, Arrays.asList("a"),
                                                                  Arrays.asList(PrimitiveType.INT),
                                                                  new PrimitiveValue(1));
        Expression func4 = new ParameterExpression(new Scope(), "b", new MetaType(new Scope()));

        UnionValue v2 = makeValue(s, func3, func4);
        UnionValue v3a = makeValue(s, func1, func2, func3, func4);
        UnionValue v3b = makeValue(s, v1a, v2);
        UnionValue v4 = makeValue(new Scope(), func1, func2);

        assertEquals(v1a, v1b);
        assertEquals(v1a.hashCode(), v1b.hashCode());
        assertEquals(v3a, v3b);
        assertEquals(v3a.hashCode(), v3b.hashCode());

        assertFalse(v1a.equals(v2));
        assertFalse(v1a.hashCode() == v2.hashCode());
        assertFalse(v1a.equals(v3a));
        assertFalse(v1a.hashCode() == v3a.hashCode());
        assertFalse(v1a.equals(v4));
        assertFalse(v1a.hashCode() == v4.hashCode());
    }

    @Test
    public void testAccept() {
        Scope s = new Scope();
        FunctionValue func1 = FunctionValueTest.makeFunctionValue(s, Arrays.asList("a"),
                                                                  Arrays.asList(PrimitiveType.INT),
                                                                  new PrimitiveValue(1));
        FunctionValue func2 = FunctionValueTest.makeFunctionValue(s, Arrays.asList("a"),
                                                                  Arrays.asList(PrimitiveType.FLOAT),
                                                                  new PrimitiveValue(1.0f));
        UnionValue v = makeValue(s, func1, func2);
        UnionValue visited = v.accept(new Expression.Visitor<UnionValue>() {
            @Override
            public UnionValue visitArrayAccess(ArrayAccess access) {
                return null;
            }

            @Override
            public UnionValue visitArray(ArrayValue value) {
                return null;
            }

            @Override
            public UnionValue visitFunctionCall(FunctionCall function) {
                return null;
            }

            @Override
            public UnionValue visitFunction(FunctionValue function) {
                return null;
            }

            @Override
            public UnionValue visitParameter(ParameterExpression param) {
                return null;
            }

            @Override
            public UnionValue visitPrimitive(PrimitiveValue primitive) {
                return null;
            }

            @Override
            public UnionValue visitFieldAccess(StructFieldAccess access) {
                return null;
            }

            @Override
            public UnionValue visitStruct(StructValue struct) {
                return null;
            }

            @Override
            public UnionValue visitUnion(UnionValue union) {
                return union;
            }

            @Override
            public UnionValue visitVariable(VariableExpression var) {
                return null;
            }

            @Override
            public UnionValue visitNativeExpression(NativeExpression expr) {
                return null;
            }
        });
        assertEquals(v, visited);
    }
}
