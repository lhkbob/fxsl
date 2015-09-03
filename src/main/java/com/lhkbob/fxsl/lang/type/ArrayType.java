package com.lhkbob.fxsl.lang.type;

import com.lhkbob.fxsl.util.EfficientEqualityBase;
import com.lhkbob.fxsl.util.Immutable;

import static com.lhkbob.fxsl.util.Preconditions.notNull;

/**
 * Array Types
 * ===========
 *
 * Arrays are custom types that can be declared in FXSL programs. They are fixed-length lists of
 * elements, where each element is of the same type. Order within an array instance is well-defined
 * based on how the elements are ordered in the constructor. Elements can be accessed by integer
 * indices, starting from `0` up to `length - 1`. The length of an array is part of its type, thus
 * `float[3]` is a different type than `float[4]`. The component type of an array refers to the type
 * of its elements. The component type can be any other valid type, including other arrays, structs,
 * and wildcards.
 *
 * The length of an array must either be an integer primitive, or an identifier. If an identifier
 * is used, the array type has a wildcard length and the specified identifier declares an implicit
 * parameter of type int that is replaced with any array instance's length when used, which exists
 * in the same scope as the defined array type. Arrays with explicit lengths must have a length
 * greater than or equal to one. Arrays with wildcard lengths can only be declared as the type of
 * function parameters: the length identifier is treated as if it were another argument to the
 * function when it's invoked.
 *
 * @author Michael Ludwig
 */
@Immutable
public final class ArrayType extends EfficientEqualityBase implements Type {

  private final Type componentType;
  private final Length length;

  /**
   * Construct a new ArrayType with the given length descriptor.
   *
   * @param componentType
   *     The component type of the array
   * @param length
   *     The length of the array (a constant or wildcard)
   * @throws java.lang.NullPointerException
   *     if `componentType` or `length` is null
   */
  public ArrayType(Type componentType, Length length) {
    notNull("componentType", componentType);
    notNull("length", length);

    this.componentType = componentType;
    this.length = length;
  }

  /**
   * @return The component type of the array
   */
  public Type getComponentType() {
    return componentType;
  }

  /**
   * Get the length of the array. If the returned length's value is at least 1 then the array has
   * a declared concrete length. If it's 0 or negative that value represents a wildcard and the
   * owning environment must be queried to determine if the wildcard has been bound to any other
   * length.
   *
   * @return The concrete or wildcard length this array was declared with
   */
  public Length getLength() {
    return length;
  }

  @Override
  public <T> T accept(Type.Visitor<T> visitor) {
    return visitor.visitArrayType(this);
  }

  @Override
  protected int computeHashCode() {
    int hash = 17;
    hash += hash * 37 + componentType.hashCode();
    hash += hash * 37 + length.hashCode();
    return hash;
  }

  @Override
  public boolean equals(Object t) {
    ArrayType a = compareHashCodes(ArrayType.class, t);
    return a != null && a.length.equals(length) && a.componentType.equals(componentType);
  }

  @Immutable
  public static final class Length {
    private final int length;

    // FIXME might be nice to specify a label if it came from actual fxsl code
    public Length(int length) {
      this.length = length;
    }

    public int get() {
      return length;
    }

    @Override
    public int hashCode() {
      return length;
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Length)) {
        return false;
      }
      Length l = (Length) o;
      return l.length == length;
    }

    @Override
    public String toString() {
      if (isWildcard()) {
        return "__" + Integer.toHexString(-length);
      } else {
        return Integer.toString(length);
      }
    }

    public boolean isWildcard() {
      return length <= 0;
    }
  }

  @Override
  public String toString() {
    return String.format("%s[%s]", componentType, length);
  }
}
