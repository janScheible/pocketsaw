package com.scheible.pocketsaw.impl.descriptor;

/**
 *
 * @author sj
 * @deprecated As of release 1.1.0, moved to
 * {@link com.scheible.pocketsaw.impl.descriptor.annotation.SpringClasspathScanner}. Please switch to that class as an
 * prepartion for version 2.0.
 */
@Deprecated
public class SpringClasspathScanner extends ClasspathScanner {

	private SpringClasspathScanner(Class<?> basePackageClass) {
		super(basePackageClass);
	}

	public static ClasspathScanner create(Class<?> basePackageClass) {
		return new SpringClasspathScanner(basePackageClass);
	}
}
