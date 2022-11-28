package com.scheible.pocketsaw.impl.descriptor.annotation;

import static java.util.Collections.unmodifiableSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 *
 * @author sj
 */
public abstract class ClasspathScanner {
	
	protected static final Predicate<String> TEST_CLASS_FILTER = className -> !(className.endsWith("Test") || className.contains("Test$"));
	
	private final String basePackage;
	private final Set<String> subModuleAnnotatedClassNames;
	private final Set<String> externalFunctionalityAnnotatedClassNames;
	
	private boolean autoMatching = false;

	protected ClasspathScanner(final String basePackage, final Set<String> subModuleAnnotatedClassNames, 
			final Set<String> externalFunctionalityAnnotatedClassNames) {
		this.basePackage = basePackage;
		this.subModuleAnnotatedClassNames = unmodifiableSet(subModuleAnnotatedClassNames);
		this.externalFunctionalityAnnotatedClassNames = unmodifiableSet(externalFunctionalityAnnotatedClassNames);
	}

	/**
	 * Enables automatic generation of sub-module and external functionality descriptors if they are not
	 * explicitly defined. For sub-modules includeSubPackages is always {@code false}. If this is not the desired
	 * behavior a corresponding {@code @SubModule} has to be defined explicitly.
	 */
	public ClasspathScanner enableAutoMatching() {
		autoMatching = true;
		return this;
	}

	public boolean doAutoMatching() {
		return autoMatching;
	}

	public Set<String> getExternalFunctionalityAnnotatedClassNames() {
		return externalFunctionalityAnnotatedClassNames;
	}

	public Set<String> getSubModuleAnnotatedClassNames() {
		return subModuleAnnotatedClassNames;
	}
	
	public String getBasePackage() {
		return basePackage;
	}
}
