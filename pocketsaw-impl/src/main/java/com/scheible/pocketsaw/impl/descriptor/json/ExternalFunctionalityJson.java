package com.scheible.pocketsaw.impl.descriptor.json;

/**
 *
 * @author sj
 */
class ExternalFunctionalityJson {
	
	private final String name;
	private final String packageMatchPattern;

	ExternalFunctionalityJson(String name, String packageMatchPattern) {
		this.name = name;
		this.packageMatchPattern = packageMatchPattern;
	}

	String getName() {
		return name;
	}

	String getPackageMatchPattern() {
		return packageMatchPattern;
	}
}
