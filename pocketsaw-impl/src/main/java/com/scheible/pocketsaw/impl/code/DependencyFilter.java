package com.scheible.pocketsaw.impl.code;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author sj
 */
public class DependencyFilter {

	private final Set<String> ignoredClassNames;
	private final boolean ignoreCoreJavaClasses;

	public DependencyFilter(Set<String> ignoredClassNames, boolean ignoreCoreJavaClasses) {
		this.ignoredClassNames = Collections.unmodifiableSet(enrichWithTopLevelClasses(ignoredClassNames));
		this.ignoreCoreJavaClasses = ignoreCoreJavaClasses;
	}

	/**
	 * Returns the set of class names enriched by all top level classes (if any).
	 */
	private Set<String> enrichWithTopLevelClasses(Set<String> classNames) {
		Set<String> result = new HashSet<>();
		for (String className : classNames) {
			result.add(className);

			if (className.contains("$")) {
				result.add(className.substring(0, className.indexOf("$")));
			}
		}
		return result;
	}

	public boolean apply(String className, String dependentClass) {
		if (ignoredClassNames.contains(className) || ignoredClassNames.contains(dependentClass)) {
			return true;
		} else if (ignoreCoreJavaClasses && (dependentClass.startsWith("java.") || dependentClass.startsWith("javax."))) {
			return true;
		}

		return false;
	}
}
