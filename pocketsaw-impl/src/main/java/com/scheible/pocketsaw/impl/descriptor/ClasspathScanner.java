package com.scheible.pocketsaw.impl.descriptor;

/**
 *
 * @author sj
 * @deprecated As of release 1.1.0, moved to
 * {@link com.scheible.pocketsaw.impl.descriptor.annotation.ClasspathScanner}. Please switch to that class as an
 * prepartion for version 2.0.
 */
@Deprecated
public abstract class ClasspathScanner {
	
	private final Class<?> basePackageClass;

	protected ClasspathScanner(Class<?> basePackageClass) {
		this.basePackageClass = basePackageClass;
	}

	public Class<?> getBasePackageClass() {
		return basePackageClass;
	}
}
