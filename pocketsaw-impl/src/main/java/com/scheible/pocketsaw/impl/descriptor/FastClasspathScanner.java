package com.scheible.pocketsaw.impl.descriptor;

/**
 *
 * @author sj
 * @deprecated As of release 1.1.0, moved to
 * {@link com.scheible.pocketsaw.impl.descriptor.annotation.FastClasspathScanner}. Please switch to that class as an
 * prepartion for version 2.0.
 */
@Deprecated
public class FastClasspathScanner extends ClasspathScanner {

	private FastClasspathScanner(Class<?> basePackageClass) {
		super(basePackageClass);
	}

	public static ClasspathScanner create(Class<?> basePackageClass) {
		return new FastClasspathScanner(basePackageClass);
	}
}
