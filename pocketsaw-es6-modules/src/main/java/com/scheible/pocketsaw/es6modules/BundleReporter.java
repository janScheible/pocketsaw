package com.scheible.pocketsaw.es6modules;

import com.scheible.pocketsaw.es6modules.Es6ModulesSource.ImportPath;
import static com.scheible.pocketsaw.es6modules.Es6ModulesSource.toNamespaceNotation;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import static java.util.Collections.unmodifiableMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author sj
 */
public class BundleReporter {

	public static class BundleReport {

		private final Map<String, Set<String>> moduleBundles;
		private final String errorMessage;

		public BundleReport(final Map<String, Set<String>> moduleBundles) {
			this.moduleBundles = unmodifiableMap(moduleBundles);
			this.errorMessage = null;
		}

		public BundleReport(final String errorMessage) {
			this.errorMessage = errorMessage;
			this.moduleBundles = null;
		}

		Map<String, Set<String>> getModuleBundles() {
			return moduleBundles;
		}

		@Override
		public String toString() {
			if (moduleBundles == null) {
				return errorMessage;
			} else {
				final List<String> modules = new ArrayList<>(moduleBundles.keySet());
				Collections.sort(modules);
				int maxModuleLength = modules.stream().mapToInt(String::length).max().getAsInt() + 1;

				final StringBuilder result = new StringBuilder();
				result.append("Module bundle report:\n");
				for (final String module : modules) {
					result.append(" * ").append(module).append(new String(new char[maxModuleLength - module.length()]).replace('\0', ' '))
							.append(" ").append(moduleBundles.get(module).isEmpty()
							? "*default*"
							: moduleBundles.get(module).size() == 1
							? moduleBundles.get(module).iterator().next()
							: "*default* " + moduleBundles.get(module).stream().collect(Collectors.joining(", ", "(", ")"))).append("\n");
				}

				return result.toString();
			}
		}
	}

	static class ModuleGraph {

		final Map<String, Set<String>> outEdges = new HashMap<>();
		final Map<String, Set<String>> inEdges = new HashMap<>();

		final Set<String> vertices = new HashSet<>();

		void addEdge(final String tail, final String head) {
			vertices.add(tail);
			vertices.add(head);

			outEdges.computeIfAbsent(tail, key -> new HashSet<>()).add(head);
			inEdges.computeIfAbsent(head, key -> new HashSet<>()).add(tail);
		}

		Optional<String> getStartVertex() {
			final List<String> startVertices = vertices.stream()
					.filter(v -> !inEdges.containsKey(v) || inEdges.get(v).isEmpty()).collect(Collectors.toList());
			return Optional.ofNullable(startVertices.size() == 1 ? startVertices.get(0) : null);
		}

		Set<List<String>> findAllPaths(final String startVertext, final String endVertext) {
			final Set<List<String>> paths = new HashSet<>();

			final LinkedList<String> visited = new LinkedList<>();
			visited.add(startVertext);

			findAllPathsRecursively(visited, endVertext, paths);

			return paths;
		}

		private void findAllPathsRecursively(final LinkedList<String> visited, final String endVertext,
				final Set<List<String>> paths) {
			for (final String neighbor : outEdges.getOrDefault(visited.getLast(), Collections.emptySet())) {
				if (visited.contains(neighbor)) {
				} else if (neighbor.endsWith(endVertext)) {
					visited.add(neighbor);
					paths.add(new ArrayList<>(visited));
					visited.removeLast();
					break;
				} else {
					visited.addLast(neighbor);
					findAllPathsRecursively(visited, endVertext, paths);
					visited.removeLast();
				}
			}
		}
	}

	static BundleReport create(final Map<Path, Set<ImportPath>> moduleDependencies) {
		final ModuleGraph graph = new ModuleGraph();
		final Set<String> bundleEntries = new HashSet<>();

		for (final Entry<Path, Set<ImportPath>> dependency : moduleDependencies.entrySet()) {
			for (final ImportPath importPath : dependency.getValue()) {
				graph.addEdge(toNamespaceNotation(dependency.getKey()), toNamespaceNotation(importPath.getPath()));

				if (importPath.isDynamic()) {
					bundleEntries.add(toNamespaceNotation(importPath.getPath()));
				}
			}
		}

		return graph.getStartVertex().map(startVertex -> {
			final Map<String, Set<String>> moduleBundles = new HashMap<>();

			for (final String toVertex : graph.vertices) {
				final Set<String> bundles = new HashSet<>();

				if (!toVertex.equals(startVertex)) {
					final Set<List<String>> paths = graph.findAllPaths(startVertex, toVertex);

					for (final List<String> path : paths) {
						for (final String currentPathVertex : path) {
							if (bundleEntries.contains(currentPathVertex)) {
								bundles.add(currentPathVertex.substring(currentPathVertex.lastIndexOf(".") + 1) + "-bundle");
							}
						}

					}
				}

				moduleBundles.put(toVertex, bundles);
			}
			return new BundleReport(moduleBundles);
		}).orElse(new BundleReport("No single start vertex found, there is no single vertext with only outgoing edges!"));
	}
}
