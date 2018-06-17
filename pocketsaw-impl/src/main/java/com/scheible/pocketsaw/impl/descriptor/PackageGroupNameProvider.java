package com.scheible.pocketsaw.impl.descriptor;

/**
 *
 * @author sj
 */
class PackageGroupNameProvider {

	static String getName(Class<?> annotatedClass) {
		final String name = annotatedClass.getSimpleName();
		
		if (name.endsWith("SubModule")) {
			return name.substring(0, name.length() - 9);
		} else {
			return name;
		}
	}
}
