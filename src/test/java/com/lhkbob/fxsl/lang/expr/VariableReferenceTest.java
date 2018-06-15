package com.lhkbob.fxsl.lang.expr;

import com.lhkbob.fxsl.lang.Scope;
import com.lhkbob.fxsl.lang.type.MetaType;
import com.lhkbob.fxsl.lang.type.PrimitiveType;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Basic test cases for {@link VariableReference}.
 *
 * @author Michael Ludwig
 */
public class VariableReferenceTest {
    @Test
    public void testEqualsAndHashcode() {
        Scope shared = new Scope();
        VariableReference t1a = new VariableReference(shared, "a");
        VariableReference t1b = new VariableReference(shared, "a");
        VariableReference t2 = new VariableReference(shared, "b");
        VariableReference t3 = new VariableReference(new Scope(), "a");

        assertEquals(t1a, t1b);
        assertEquals(t1a.hashCode(), t1b.hashCode());
        assertFalse(t1a.equals(t2));
        assertFalse(t1a.hashCode() == t2.hashCode());
        assertFalse(t1a.equals(t3));
        assertFalse(t1a.hashCode() == t3.hashCode());
    }

    @Test
    public void testGetters() {
        Scope scope = new Scope();
        VariableReference t = new VariableReference(scope, "a", PrimitiveType.INT);

        assertEquals("a", t.getVariableName());
        assertEquals(scope, t.getScope());
        assertEquals(PrimitiveType.INT, t.getType());
    }

    @Test
    public void testConstructorNullKnownType() {
        VariableReference t = new VariableReference(new Scope(), "a");
        assertTrue(t.getType() instanceof MetaType);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullScope() {
        new VariableReference(null, "a");
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullLabel() {
        new VariableReference(new Scope(), null);
    }

    @Test
    public void testAccept() {
        VariableReference t = new VariableReference(new Scope(), "a");
        VariableReference visited = t.accept(new Expression.Visitor<VariableReference>() {
            @Override
            public VariableReference visitArrayAccess(ArrayAccess access) {
                return null;
            }

            @Override
            public VariableReference visitArray(ArrayValue value) {
                return null;
            }

            @Override
            public VariableReference visitFunctionCall(FunctionCall function) {
                return null;
            }

            @Override
            public VariableReference visitFunction(FunctionValue function) {
                return null;
            }

            @Override
            public VariableReference visitParameter(Parameter param) {
                return null;
            }

            @Override
            public VariableReference visitPrimitive(PrimitiveValue primitive) {
                return null;
            }

            @Override
            public VariableReference visitFieldAccess(StructFieldAccess access) {
                return null;
            }

            @Override
            public VariableReference visitStruct(StructValue struct) {
                return null;
            }

            @Override
            public VariableReference visitUnion(UnionValue union) {
                return null;
            }

            @Override
            public VariableReference visitVariable(VariableReference var) {
                return var;
            }

            @Override
            public VariableReference visitNativeExpression(NativeExpression expr) {
                return null;
            }
        });
        assertEquals(t, visited);
    }
}
