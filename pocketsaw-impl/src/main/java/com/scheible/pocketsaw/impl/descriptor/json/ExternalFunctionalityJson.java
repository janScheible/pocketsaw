package com.scheible.pocketsaw.impl.descriptor.json;

import java.util.Set;

/**
 *
 * @author sj
 */
class ExternalFunctionalityJson {
	
	private final String name;
	private final Set<String> packageMatchPatterns;

	ExternalFunctionalityJson(String name, Set<String> packageMatchPatterns) {
		this.name = name;
		this.packageMatchPatterns = packageMatchPatterns;
	}

	String getName() {
		return name;
	}

	Set<String> getPackageMatchPatterns() {
		return packageMatchPatterns;
	}
}
