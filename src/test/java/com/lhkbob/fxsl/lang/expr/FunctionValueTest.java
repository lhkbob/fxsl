package com.lhkbob.fxsl.lang.expr;

import com.lhkbob.fxsl.lang.Scope;
import com.lhkbob.fxsl.lang.type.FunctionType;
import com.lhkbob.fxsl.lang.type.PrimitiveType;
import com.lhkbob.fxsl.lang.type.Type;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Basic tests for {@link com.lhkbob.fxsl.lang.expr.FunctionValue}.
 *
 * @author Michael Ludwig
 */
public class FunctionValueTest {
    private static final Expression DEFAULT_RETURN_VALUE = new Parameter(new Scope(), "a",
                                                                                   PrimitiveType.INT);

    public static FunctionValue makeFunctionValue(Scope scope, List<String> names,
                                                  List<? extends Type> paramTypes, Expression returnValue) {
        List<Parameter> exprs = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            exprs.add(new Parameter(scope, names.get(i), paramTypes.get(i)));
        }
        return new FunctionValue(scope, exprs, returnValue);
    }

    @Test
    public void testGetters() {
        Scope s = new Scope();
        FunctionValue value = makeFunctionValue(s, Arrays.asList("a", "b"),
                                                Arrays.asList(PrimitiveType.INT, PrimitiveType.FLOAT),
                                                DEFAULT_RETURN_VALUE);

        FunctionType expectedType = new FunctionType(s, Arrays.asList(PrimitiveType.INT, PrimitiveType.FLOAT),
                                                     PrimitiveType.INT);

        assertEquals(expectedType, value.getType());
        assertEquals(Arrays.asList(new Parameter(s, "a", PrimitiveType.INT),
                                   new Parameter(s, "b", PrimitiveType.FLOAT)),
                     value.getParameters());
        assertEquals(new Parameter(s, "a", PrimitiveType.INT), value.getParameter(0));
        assertEquals(new Parameter(s, "b", PrimitiveType.FLOAT), value.getParameter(1));
        assertEquals(DEFAULT_RETURN_VALUE, value.getReturnValue());
        assertEquals(s, value.getScope());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullParameters() {
        new FunctionValue(new Scope(), null, DEFAULT_RETURN_VALUE);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullReturnValue() {
        makeFunctionValue(new Scope(), Arrays.asList("a"), Arrays.asList(PrimitiveType.FLOAT), null);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullNameElement() {
        new FunctionValue(new Scope(), Arrays.asList((Parameter) null), DEFAULT_RETURN_VALUE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmptyParameters() {
        new FunctionValue(new Scope(), Collections.<Parameter>emptyList(), DEFAULT_RETURN_VALUE);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullScope() {
        makeFunctionValue(null, Arrays.asList("a"), Arrays.asList(PrimitiveType.FLOAT), DEFAULT_RETURN_VALUE);
    }

    @Test
    public void testConstructorClonedParameters() {
        List<Parameter> params = new ArrayList<>();
        params.add(new Parameter(new Scope(), "boo", PrimitiveType.FLOAT));

        FunctionValue value = new FunctionValue(new Scope(), params, DEFAULT_RETURN_VALUE);
        params.add(new Parameter(new Scope(), "foo", PrimitiveType.INT));

        assertEquals(1, value.getParameters().size());
        assertEquals("boo", value.getParameter(0).getParameterName());
        assertEquals(PrimitiveType.FLOAT, value.getParameter(0).getType());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnmodifiableParameterList() {
        FunctionValue value = makeFunctionValue(new Scope(), Arrays.asList("a"),
                                                Arrays.asList(PrimitiveType.FLOAT), DEFAULT_RETURN_VALUE);
        value.getParameters().add(new Parameter(new Scope(), "b", PrimitiveType.INT));
    }

    @Test
    public void testEqualsAndHashcode() {
        Scope s = new Scope();
        FunctionValue v1a = makeFunctionValue(s, Arrays.asList("a"), Arrays.asList(PrimitiveType.FLOAT),
                                              DEFAULT_RETURN_VALUE);
        FunctionValue v1b = makeFunctionValue(s, Arrays.asList("a"), Arrays.asList(PrimitiveType.FLOAT),
                                              DEFAULT_RETURN_VALUE);
        FunctionValue v2 = makeFunctionValue(s, Arrays.asList("a"), Arrays.asList(PrimitiveType.INT),
                                             DEFAULT_RETURN_VALUE);
        FunctionValue v3 = makeFunctionValue(s, Arrays.asList("b"), Arrays.asList(PrimitiveType.FLOAT),
                                             DEFAULT_RETURN_VALUE);
        FunctionValue v4 = makeFunctionValue(s, Arrays.asList("a"), Arrays.asList(PrimitiveType.FLOAT),
                                             new PrimitiveValue(3));
        FunctionValue v5 = makeFunctionValue(new Scope(), Arrays.asList("a"),
                                             Arrays.asList(PrimitiveType.FLOAT), DEFAULT_RETURN_VALUE);

        assertEquals(v1a, v1b);
        assertEquals(v1a.hashCode(), v1b.hashCode());

        assertFalse(v1a.equals(v2));
        assertFalse(v1a.hashCode() == v2.hashCode());
        assertFalse(v1a.equals(v3));
        assertFalse(v1a.hashCode() == v3.hashCode());
        assertFalse(v1a.equals(v4));
        assertFalse(v1a.hashCode() == v4.hashCode());
        assertFalse(v1a.equals(v5));
        assertFalse(v1a.hashCode() == v5.hashCode());
    }

    @Test
    public void testAccept() {
        FunctionValue v = makeFunctionValue(new Scope(), Arrays.asList("a"),
                                            Arrays.asList(PrimitiveType.FLOAT), new PrimitiveValue(3));
        FunctionValue visited = v.accept(new Expression.Visitor<FunctionValue>() {
            @Override
            public FunctionValue visitArrayAccess(ArrayAccess access) {
                return null;
            }

            @Override
            public FunctionValue visitArray(ArrayValue value) {
                return null;
            }

            @Override
            public FunctionValue visitFunctionCall(FunctionCall function) {
                return null;
            }

            @Override
            public FunctionValue visitFunction(FunctionValue function) {
                return function;
            }

            @Override
            public FunctionValue visitParameter(Parameter param) {
                return null;
            }

            @Override
            public FunctionValue visitPrimitive(PrimitiveValue primitive) {
                return null;
            }

            @Override
            public FunctionValue visitFieldAccess(StructFieldAccess access) {
                return null;
            }

            @Override
            public FunctionValue visitStruct(StructValue struct) {
                return null;
            }

            @Override
            public FunctionValue visitUnion(UnionValue union) {
                return null;
            }

            @Override
            public FunctionValue visitVariable(VariableReference var) {
                return null;
            }

            @Override
            public FunctionValue visitNativeExpression(NativeExpression expr) {
                return null;
            }
        });
        assertEquals(v, visited);
    }
}
