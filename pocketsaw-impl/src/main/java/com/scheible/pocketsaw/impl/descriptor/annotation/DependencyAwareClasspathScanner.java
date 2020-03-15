package com.scheible.pocketsaw.impl.descriptor.annotation;

import com.scheible.pocketsaw.impl.code.PackageDependencies;
import java.util.Set;

/**
 * Base class for classpth scanners that also provide dependency information.

 * @author sj
 */
public abstract class DependencyAwareClasspathScanner extends ClasspathScanner {
	
	private boolean dependencyScan = false;
	
	protected DependencyAwareClasspathScanner(final Set<String> subModuleAnnotatedClassNames, 
			final Set<String> externalFunctionalityAnnotatedClassNames) {
		super(subModuleAnnotatedClassNames, externalFunctionalityAnnotatedClassNames);
	}
	
	public DependencyAwareClasspathScanner enableDependencyScan() {
		dependencyScan = true;
		return this;
	}
	
	public boolean doDependencyScan() {
		return dependencyScan;
	}
	
	public abstract PackageDependencies getDependencies();
}
