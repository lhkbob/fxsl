package com.lhkbob.fxsl.lang.type;

import com.lhkbob.fxsl.util.Immutable;

/**
 * Primitive Types
 * ===============
 *
 * FXSL has several primitive types that are fundamental and cannot be decomposed further. Because
 * of their simplicity they are represented as an enumeration instead of a full class. The sampler
 * types could technically be represented as arrays of the other primitive types except that their
 * dimensions and operating semantics are so specialized it makes sense to treat them as unique
 * types. Sampler arrays, supported in newer versions of OpenGL, are represented as arrays of the
 * base samplers.
 *
 * @author Michael Ludwig
 */
@Immutable
public enum PrimitiveType implements Type {
  /**
   * `BOOL` represents a boolean true or false value. By default the `bool` type name refers to
   * this primitive type. FXSL supports all of the common boolean operators. Although all operators
   * are represented as functions, the FXSL compiler adds short-circuit evaluation for the generally
   * assumed behavior. Boolean values are declared in FXSL using the `true` or `false` keywords.
   */
  BOOL,
  /**
   * `INT` represents a 32-bit signed integer. By default the `int` type name in FXSL refers to
   * this primitive type. Values of this type are automatically converted to the `FLOAT` type if
   * necessary. The syntax to write a integer primitive is equivalent to that in Java or C.
   */
  INT,
  /**
   * `FLOAT` represents a 32-bit floating point number. By default the `float` type name in FXSL
   * refers to this primitive type. Most graphics cards follow the IEEE standard. The syntax to
   * write a float is equivalent to that in Java or C for their `double` types (i.e. no trailing
   * 'f'). Integer values are automatically converted to floats, making it unnecessary to add a
   * decimal for simple values.
   */
  FLOAT,
  /**
   * `SAMPLER1D` refers to the `sampler1d` FXSL data type, which represents a one dimensional
   * texture in OpenGL. A sampler value cannot be instantiated directly in FXSL code but must be
   * constructed through the usual OpenGL process and then linked using uniforms.
   */
  SAMPLER1D,
  /**
   * `SAMPLER2D` refers to the `sampler2d` FXSL data type, which represents a two dimensional
   * texture in OpenGL. A sampler value cannot be instantiated directly in FXSL code but must be
   * constructed through the usual OpenGL process and then linked using uniforms.
   */
  SAMPLER2D,
  /**
   * `SAMPLER3D` refers to the `sampler3d` FXSL data type, which represents a three dimensional
   * texture in OpenGL. A sampler value cannot be instantiated directly in FXSL code but must be
   * constructed through the usual OpenGL process and then linked using uniforms.
   */
  SAMPLER3D,
  /**
   * `SAMPLERCUBE` refers to the `samplerCube` FXSL data type, which represents a cubemap texture
   * in OpenGL. A sampler value cannot be instantiated directly in FXSL code but must be constructed
   * through the usual OpenGL process and then linked using uniforms.
   */
  SAMPLERCUBE;

  @Override
  public <T> T accept(Type.Visitor<T> visitor) {
    return visitor.visitPrimitiveType(this);
  }

  @Override
  public String toString() {
    return name().toLowerCase();
  }
}
