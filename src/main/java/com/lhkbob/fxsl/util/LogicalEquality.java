package com.lhkbob.fxsl.util;

import java.lang.annotation.*;

/**
 * LogicalEquality
 * ===============
 *
 * Class-level annotation that declares the class (and any subclasses) must implement `equals()` and
 * `hashCode()` to define a logical equality based on its member values instead of the default reference
 * equality and system hash code.
 *
 * @author Michael Ludwig
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface LogicalEquality {
    /**
     * @return Developer consumable explanation of what defines logical equality for the type.
     */
    String def() default "";
}
