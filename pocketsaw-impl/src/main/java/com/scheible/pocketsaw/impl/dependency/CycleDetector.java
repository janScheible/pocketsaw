package com.scheible.pocketsaw.impl.dependency;

import com.scheible.pocketsaw.impl.descriptor.PackageGroupDescriptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 *
 * @author sj
 */
public class CycleDetector {

	static Optional<List<PackageGroupDescriptor>> findAny(DependencyGraph graph, Predicate<Dependency> dependencyFilter) {
		// NOTE Create color sets and put initially all in the white set.
		Set<PackageGroupDescriptor> whiteSet = new HashSet<>(graph.getPackageGroups());
		Map<PackageGroupDescriptor, PackageGroupDescriptor> greyPreviousMapping = new HashMap<>();
		Set<PackageGroupDescriptor> blackSet = new HashSet<>();

		for (PackageGroupDescriptor vertex : graph.getPackageGroups()) {
			if (whiteSet.contains(vertex)) {
				Optional<PackageGroupDescriptor> firstCycleVertex = findAny(graph, dependencyFilter, vertex, null, whiteSet, greyPreviousMapping, blackSet);
				if (firstCycleVertex.isPresent()) {
					return Optional.of(toCycleList(firstCycleVertex.get(), greyPreviousMapping));
				}
			}
		}

		return Optional.empty();
	}

	private static List<PackageGroupDescriptor> toCycleList(PackageGroupDescriptor firstCycleVertex, Map<PackageGroupDescriptor, PackageGroupDescriptor> greyPreviousMapping) {
		Map<PackageGroupDescriptor, PackageGroupDescriptor> greyNextMapping = greyPreviousMapping.entrySet().stream()
				.collect(Collectors.toMap(Entry::getValue, Entry::getKey));

		List<PackageGroupDescriptor> cycle = new ArrayList<>();
		PackageGroupDescriptor current = firstCycleVertex;
		while (current != null) {
			cycle.add(current);
			current = greyNextMapping.get(current);
		}
		cycle.add(firstCycleVertex);

		return Collections.unmodifiableList(cycle);
	}

	/**
	 * @return The first vertex of the found cycle that was already visited.
	 */
	private static Optional<PackageGroupDescriptor> findAny(DependencyGraph graph, Predicate<Dependency> dependencyFilter,
			PackageGroupDescriptor vertex, PackageGroupDescriptor previous, Set<PackageGroupDescriptor> whiteSet,
			Map<PackageGroupDescriptor, PackageGroupDescriptor> greyPreviousMapping,
			Set<PackageGroupDescriptor> blackSet) {
		// NOTE Swtich color from white to gray.
		whiteSet.remove(vertex);
		greyPreviousMapping.put(vertex, previous);

		for (Dependency dependency : graph.getNeighbors(vertex)) {
			if (!dependencyFilter.test(dependency)) {
				continue;
			}

			// NOTE Check if this vertex is present in gray set, means cycle is found.
			if (greyPreviousMapping.keySet().contains(dependency.getTarget())) {
				return Optional.of(dependency.getTarget());
			}

			// NOTE Check if this vertex is present in black set, means this vertex is already done.
			if (blackSet.contains(dependency.getTarget())) {
				continue;
			}

			// NOTE Do traversal from this vertex.
			Optional<PackageGroupDescriptor> firstCycleVertex = findAny(graph, dependencyFilter, dependency.getTarget(), vertex, whiteSet, greyPreviousMapping, blackSet);
			if (firstCycleVertex.isPresent()) {
				return firstCycleVertex;
			}
		}

		// NOTE If here means cycle is not found from this vertex, make if black from gray.
		greyPreviousMapping.remove(vertex);
		blackSet.add(vertex);

		return Optional.empty();
	}
}
