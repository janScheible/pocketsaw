package com.scheible.pocketsaw.impl.code.jdeps;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;

/**
 *
 * @author sj
 */
public class ApiAnnotationDependencyFilter implements BiFunction<String, String, Boolean> {

	private final Set<String> ignoredClassNames;

	public ApiAnnotationDependencyFilter(Set<String> ignoredClassNames) {
		this.ignoredClassNames = enrichWithTopLevelClasses(ignoredClassNames);
	}

	/**
	 * Returns the set of class names enriched by all top level classes (if any).
	 */
	private final Set<String> enrichWithTopLevelClasses(Set<String> classNames) {
		Set<String> result = new HashSet<>();
		for (String className : classNames) {
			result.add(className);

			if (className.contains("$")) {
				result.add(className.substring(0, className.indexOf("$")));
			}
		}
		return result;
	}

	@Override
	public Boolean apply(String className, String dependentClass) {
		if (ignoredClassNames.contains(className) || ignoredClassNames.contains(dependentClass)) {
			return false;
		} else if (dependentClass.startsWith("java.") || dependentClass.startsWith("javax.")) {
			return false;
		}

		return true;
	}
}
