package com.lhkbob.fxsl.lang.type;

import com.lhkbob.fxsl.lang.expr.VariableReference;
import com.lhkbob.fxsl.util.EfficientEqualityBase;
import com.lhkbob.fxsl.util.Immutable;
import com.lhkbob.fxsl.util.LogicalEquality;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import static com.lhkbob.fxsl.util.Preconditions.notNull;

/**
 * A typepath with no node elements implies that the type of the variable reference is itself the
 * type at the end of the path.
 */
@Immutable
@LogicalEquality
public final class TypePath extends EfficientEqualityBase {
  private final VariableReference source;
  private final List<Node> nodes;
  private TypePath(VariableReference source, Collection<Node> nodes) {
    this.source = source;
    this.nodes = Collections.unmodifiableList(new ArrayList<>(nodes));
  }

  public static Builder newPath(VariableReference root) {
    return new Builder(root);
  }

  public VariableReference getRoot() {
    return source;
  }

  public List<Node> getPath() {
    return nodes;
  }

  @Override
  protected int computeHashCode() {
    return source.hashCode() ^ nodes.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    TypePath t = compareHashCodes(TypePath.class, o);
    return t != null && t.source.equals(source) && t.nodes.equals(nodes);
  }

  public enum NodeType {
    ARRAY_COMPONENT(Void.class),
    FUNCTION_PARAMETER(Integer.class),
    FUNCTION_RETURN(Void.class),
    STRUCT_FIELD(String.class),
    UNION_ELEMENT(Integer.class);

    private final Class<?> metadataType;

    NodeType(Class<?> metadataType) {
      this.metadataType = metadataType;
    }

    public Class<?> getMetadataType() {
      return metadataType;
    }

    public boolean hasMetadata() {
      return !metadataType.equals(Void.class);
    }
  }

  public static class Builder {
    // The nodes without metadata can be singletons since they are immutable and all instances are equivalent.
    private static final Node ARRAY_COMPONENT = new Node(NodeType.ARRAY_COMPONENT, null);
    private static final Node FUNCION_RETURN = new Node(NodeType.FUNCTION_RETURN, null);

    private final VariableReference source;
    private final Stack<Node> nodes;

    private Builder(VariableReference source) {
      notNull("source", source);
      this.source = source;
      nodes = new Stack<>();
    }

    public Builder pushArrayComponent() {
      nodes.push(ARRAY_COMPONENT);
      return this;
    }

    public Builder pushFunctionParameter(int parameter) {
      if (parameter < 0) {
        throw new IndexOutOfBoundsException("Parameter must at least be 0: " + parameter);
      }
      nodes.push(new Node(NodeType.FUNCTION_PARAMETER, parameter));
      return this;
    }

    public Builder pushFunctionReturn() {
      nodes.push(FUNCION_RETURN);
      return this;
    }

    public Builder pushStructField(String field) {
      notNull("field", field);
      nodes.push(new Node(NodeType.STRUCT_FIELD, field));
      return this;
    }

    public Builder pushUnionElement(int element) {
      if (element < 0) {
        throw new IndexOutOfBoundsException("Element must at least be 0: " + element);
      }
      nodes.push(new Node(NodeType.UNION_ELEMENT, element));
      return this;
    }

    public Builder pop() {
      nodes.pop();
      return this;
    }

    public TypePath create() {
      return new TypePath(source, nodes);
    }
  }

  @Immutable
  @LogicalEquality(def = "Nodes are equal if their type and metadata are logically equal.")
  public static class Node {
    private final NodeType type;
    private final Object metadata;

    private Node(NodeType type, Object metadata) {
      this.type = type;
      this.metadata = metadata;
    }

    public int getIntData() {
      if (!type.metadataType.equals(Integer.class)) {
        throw new IllegalStateException("Node type does not support integer data: " + type);
      }
      return (Integer) metadata;
    }

    public String getStringData() {
      if (!type.metadataType.equals(String.class)) {
        throw new IllegalStateException("Node type does not support string data: " + type);
      }
      return (String) metadata;
    }

    @Override
    public int hashCode() {
      return type.hashCode() ^ (metadata == null ? 0 : metadata.hashCode());
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Node)) {
        return false;
      }
      Node t = (Node) o;
      return t.type.equals(type) && (metadata == null ? t.metadata == null
          : metadata.equals(t.metadata));
    }

    @Override
    public String toString() {
      if (metadata == null) {
        return type.toString();
      } else {
        return String.format("%s(%s)", type, metadata);
      }
    }
  }

  @Override
  public String toString() {
    return String.format("%s%s", source, nodes);
  }


}
