package com.scheible.pocketsaw.impl.code;

import static java.util.Collections.unmodifiableMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author sj
 */
public class PackageDependecies {

	private final Map<String, Set<String>> packageDependecies;

	public PackageDependecies(Map<String, Set<String>> packageDependecies) {
		this.packageDependecies = unmodifiableMap(packageDependecies);
	}

	public Set<String> keySet() {
		return packageDependecies.keySet();
	}

	public Iterable<Map.Entry<String, Set<String>>> entrySet() {
		return packageDependecies.entrySet();
	}
	
	public Set<String> get(String packageName) {
		return packageDependecies.get(packageName);
	}
}
