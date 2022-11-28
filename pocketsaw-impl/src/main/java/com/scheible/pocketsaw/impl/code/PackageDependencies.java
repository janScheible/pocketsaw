package com.scheible.pocketsaw.impl.code;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import static java.util.Collections.unmodifiableMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

/**
 *
 * @author sj
 */
public class PackageDependencies {

	private final Map<String, Set<String>> packageDependencies;
	private final Map<PackageDependency, Set<TypeDependency>> classLevelPackageDependencies;
	private final Map<String, Set<String>> packageClasses;

	public PackageDependencies(Map<String, Set<String>> packageDependencies) {
		this.packageDependencies = unmodifiableMap(packageDependencies);
		classLevelPackageDependencies = Collections.EMPTY_MAP;
		packageClasses = Collections.EMPTY_MAP;
	}

	private PackageDependencies(final Map<String, Set<String>> packageDependencies, 
			final Map<PackageDependency, Set<TypeDependency>> classLevelPackageDependencies,
			final Map<String, Set<String>> packageClasses) {
		this.packageDependencies = unmodifiableMap(packageDependencies);
		this.classLevelPackageDependencies = unmodifiableMap(classLevelPackageDependencies);
		this.packageClasses = unmodifiableMap(packageClasses);
	}

	public static PackageDependencies withCodeDependencyCounts(Map<Entry<String, String>, Integer> weightedPackageDependencies) {
		final Map<String, Set<String>> packageDependencies = weightedPackageDependencies
				.entrySet().stream()
				.map(Entry::getKey)
				.collect(groupingBy(Entry::getKey))
				.entrySet().stream()
				.collect(toMap(Entry::getKey,
						e -> e.getValue().stream().map(Entry::getValue).collect(toSet())));

		final Map<PackageDependency, Set<TypeDependency>> classLevelPackageDependencies = weightedPackageDependencies
				.entrySet().stream()
				.collect(toMap(e -> new PackageDependency(e.getKey().getKey(), e.getKey().getValue()),
						e -> {
							final Set<TypeDependency> dummyDependencies = new HashSet<>();
							final String fromPackage = e.getKey().getKey();
							final String toPackage = e.getKey().getValue();
							for(int i = 0; i < e.getValue(); i++) {
								// NOTE We don't have class level information --> add dummy dependencies with random
								//      UUID class names to fake the number of dependencies.
								dummyDependencies.add(new TypeDependency(fromPackage, UUID.randomUUID().toString(),
										toPackage,  UUID.randomUUID().toString()));
							}
							return dummyDependencies;
						}));

		return new PackageDependencies(packageDependencies, classLevelPackageDependencies, new HashMap<>());
	}
	
	public static PackageDependencies withClassLevelDependencies(
			final Map<PackageDependency, Set<TypeDependency>> classLevelPackageDependencies,
			final Map<String, Set<String>> packageClasses) {
		final Map<String, Set<String>> packageDependencies = classLevelPackageDependencies
				.entrySet().stream()
				.map(Entry::getKey)
				.collect(groupingBy(PackageDependency::getFromPackage))
				.entrySet().stream()
				.collect(toMap(Entry::getKey,
						e -> e.getValue().stream().map(PackageDependency::getToPackage).collect(toSet())));

		return new PackageDependencies(packageDependencies, classLevelPackageDependencies, packageClasses);
	}
	
	public int getCodeDependencyCount(String fromPackageName, String toPackageName) {
		return getTypeDependencies(fromPackageName, toPackageName).size();
	}
	
	public Set<TypeDependency> getTypeDependencies(String fromPackageName, String toPackageName) {
		final PackageDependency packageDependency = new PackageDependency(fromPackageName, toPackageName);
		
		if(classLevelPackageDependencies.containsKey(packageDependency)) {
			return classLevelPackageDependencies.get(packageDependency);
		} else {
			final boolean isExistingDependendy = packageDependencies.get(fromPackageName).contains(toPackageName);
			if(isExistingDependendy) {
				return new HashSet<>(Arrays.asList(new TypeDependency(fromPackageName, UUID.randomUUID().toString(),
						toPackageName, UUID.randomUUID().toString())));
			} else {
				throw new IllegalStateException("No dependendy between package '" + fromPackageName + "' and '" 
						+ toPackageName + "' exists!");
			}
		}
	}

	public String getBasePackage() {
		return getBasePackage(new ArrayList<>(packageDependencies.keySet()));
	}

	static String getBasePackage(final List<String> packages) {
		final StringBuilder basePackage = new StringBuilder();

		for(int pos = 0; pos < Integer.MAX_VALUE; pos++) {
			boolean same = true;
			char currentChar = ' ';

			for(int i = 0; i < packages.size(); i++) {
				if(pos < packages.get(i).length()) {
					if(i == 0) {
						currentChar = packages.get(i).charAt(pos);
					} else if(packages.get(i).charAt(pos) != currentChar) {
						same = false;
						break;
					}					
				} else {
					same = false;
					break;
				}
			}
			
			if(same && packages.size() > 0) {
				basePackage.append(currentChar);
			} else {
				break;
			}
		}
		
		// remove trailing dot if there is any
		if(basePackage.length() > 0 && basePackage.charAt(basePackage.length() - 1) == '.') {
			basePackage.deleteCharAt(basePackage.length() - 1);
		}
		
		return basePackage.toString();		
	}

	public Set<String> getAllTypes(final String packageName) {
		return packageClasses.get(packageName);
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
