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
	
	private final Set<String> subModuleAnnotatedClassNames;
	private final Set<String> externalFunctionalityAnnotatedClassNames;

	protected ClasspathScanner(final Set<String> subModuleAnnotatedClassNames, final Set<String> externalFunctionalityAnnotatedClassNames) {
		this.subModuleAnnotatedClassNames = unmodifiableSet(subModuleAnnotatedClassNames);
		this.externalFunctionalityAnnotatedClassNames = unmodifiableSet(externalFunctionalityAnnotatedClassNames);
	}

	public Set<String> getExternalFunctionalityAnnotatedClassNames() {
		return externalFunctionalityAnnotatedClassNames;
	}

	public Set<String> getSubModuleAnnotatedClassNames() {
		return subModuleAnnotatedClassNames;
	}
	
	public abstract String getBasePackage();
}
