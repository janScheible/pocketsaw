package com.scheible.pocketsaw.impl.code;

import java.util.AbstractMap.SimpleImmutableEntry;
import static java.util.Collections.unmodifiableMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

/**
 *
 * @author sj
 */
public class PackageDependencies {

	private final Map<String, Set<String>> packageDependencies;
	private final Map<Entry<String, String>, Integer> codeDependencyCounts;

	public PackageDependencies(Map<String, Set<String>> packageDependencies) {
		this.packageDependencies = unmodifiableMap(packageDependencies);
		codeDependencyCounts = new HashMap<>();
	}

	private PackageDependencies(Map<String, Set<String>> packageDependencies, Map<Entry<String, String>, Integer> codeDependencyCounts) {
		this.packageDependencies = unmodifiableMap(packageDependencies);
		this.codeDependencyCounts = unmodifiableMap(codeDependencyCounts);
	}

	public static PackageDependencies withCodeDependencyCounts(Map<Entry<String, String>, Integer> weightedPackageDependencies) {
		final Map<String, Set<String>> packageDependencies = weightedPackageDependencies
				.entrySet().stream()
				.map(Entry::getKey)
				.collect(groupingBy(Entry::getKey))
				.entrySet().stream()
				.collect(toMap(Entry::getKey,
						e -> e.getValue().stream().map(Entry::getValue).collect(toSet())));

		return new PackageDependencies(packageDependencies, weightedPackageDependencies);
	}

	public int getCodeDependencyCount(String fromPackageName, String toPackageName) {
		final Integer count = codeDependencyCounts.get(new SimpleImmutableEntry<>(fromPackageName, toPackageName));
		if(count != null) {
			return count;
		} else {
			final boolean isExistingDependendy = packageDependencies.get(fromPackageName).contains(toPackageName);
			if(isExistingDependendy) {
				return 1;
			} else {
				throw new IllegalStateException("No dependendy between package '" + fromPackageName + "' and '" 
						+ toPackageName + "' exists!");
			}
		}
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
