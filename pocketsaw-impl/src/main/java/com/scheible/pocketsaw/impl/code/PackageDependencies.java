package com.scheible.pocketsaw.impl.code;

import static java.util.Collections.unmodifiableMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author sj
 */
public class PackageDependencies {

	private final Map<String, Set<String>> packageDependencies;

	public PackageDependencies(Map<String, Set<String>> packageDependencies) {
		this.packageDependencies = unmodifiableMap(packageDependencies);
	}

	public Set<String> keySet() {
		return packageDependencies.keySet();
	}

	public Iterable<Map.Entry<String, Set<String>>> entrySet() {
		return packageDependencies.entrySet();
	}
	
	public Set<String> get(String packageName) {
		return packageDependencies.get(packageName);
	}
}
