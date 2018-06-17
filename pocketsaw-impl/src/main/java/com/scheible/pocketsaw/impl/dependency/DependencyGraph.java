package com.scheible.pocketsaw.impl.dependency;

import java.util.Set;
import com.scheible.pocketsaw.impl.descriptor.PackageGroupDescriptor;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 * @author sj
 */
public class DependencyGraph {

	private final Set<PackageGroupDescriptor> packageGroups;
	private final Set<Dependency> dependencies;

	private final Map<PackageGroupDescriptor, Set<Dependency>> neighbors;

	public DependencyGraph(Set<PackageGroupDescriptor> packageGroups, Set<Dependency> dependencies) {
		this.packageGroups = unmodifiableSet(packageGroups);
		this.dependencies = unmodifiableSet(dependencies);

		Map<PackageGroupDescriptor, Set<Dependency>> neighbors = new HashMap<>();
		for (Dependency dependency : dependencies) {
			neighbors.computeIfAbsent(dependency.getSource(), (key) -> new HashSet<>()).add(dependency);
		}
		for (Map.Entry<PackageGroupDescriptor, Set<Dependency>> packageGroupEntry : neighbors.entrySet()) {
			neighbors.put(packageGroupEntry.getKey(), unmodifiableSet(packageGroupEntry.getValue()));
		}
		this.neighbors = unmodifiableMap(neighbors);
	}

	public Set<PackageGroupDescriptor> getPackageGroups() {
		return packageGroups;
	}

	public Set<Dependency> getDependencies() {
		return dependencies;
	}

	Set<Dependency> getNeighbors(PackageGroupDescriptor packageGroup) {
		if (!neighbors.containsKey(packageGroup)) {
			return new HashSet<>();
		} else if (!packageGroups.contains(packageGroup)) {
			throw new IllegalStateException("'" + packageGroup + "' is not part of that dependecy graph!");
		} else {
			return neighbors.get(packageGroup);
		}
	}

	public Optional<List<PackageGroupDescriptor>> getAnyDescriptorCycle() {
		return CycleDetector.findAny(this, (dependency) -> dependency.hasDescriptorOrigin());
	}

	public Optional<List<PackageGroupDescriptor>> getAnyCodeCycle() {
		return CycleDetector.findAny(this, (dependency) -> dependency.hasCodeOrigin());
	}

	public Set<Dependency> getIllegalCodeDependencies() {
		return dependencies.stream()
				.filter(dependency -> dependency.hasCodeOrigin() && !dependency.hasDescriptorOrigin())
				.collect(Collectors.toSet());
	}

	public Set<Dependency> getUnusedDescriptorDependencies() {
		return dependencies.stream()
				.filter(dependency -> dependency.hasDescriptorOrigin() && !dependency.hasCodeOrigin())
				.collect(Collectors.toSet());
	}
}
