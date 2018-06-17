package com.scheible.pocketsaw.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SubModule {

	/**
	 * Alias for uses();
	 */
	Class<?>[] value() default {};

	/**
	 * Alias for value().
	 */
	Class<?>[] uses() default {};

	boolean includeSubPackages() default true;
	
	String color() default "orange";
}
