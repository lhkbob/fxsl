package com.lhkbob.fxsl.util;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Immutable
 * =========
 *
 * Class-level annotation that declares the class is an immutable value type that is not
 * modifiable after creation. This is purely informational and it is up to the implementations to
 * ensure this fact. Usually classes which have this annotation should be marked as `final` to
 * prevent subclasses from contradicting the base implementation.
 *
 * @author Michael Ludwig
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface Immutable {
}
