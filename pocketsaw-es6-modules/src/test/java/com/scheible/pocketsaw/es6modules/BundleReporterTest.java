package com.scheible.pocketsaw.es6modules;

import com.scheible.pocketsaw.es6modules.BundleReporter.BundleReport;
import com.scheible.pocketsaw.es6modules.BundleReporter.ModuleGraph;
import com.scheible.pocketsaw.es6modules.Es6ModulesSource.ImportPath;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import static java.util.Arrays.asList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 * See bundleReporterTest.png for the graph images.
 * 
 * @author sj
 */
public class BundleReporterTest {

	@Test
	public void testModuleGraphFindAllPaths() {
		final ModuleGraph graph = new ModuleGraph();
		graph.addEdge("a", "b");
		graph.addEdge("b", "c");
		graph.addEdge("b", "d");
		graph.addEdge("b", "e");
		graph.addEdge("c", "e");
		graph.addEdge("d", "e");
		graph.addEdge("e", "f");
		graph.addEdge("g", "d");

		assertThat(graph.findAllPaths("a", "f")).containsOnly(asList("a", "b", "e", "f"),
				asList("a", "b", "c", "e", "f"), asList("a", "b", "d", "e", "f"));
	}

	@Test
	public void testBundleReport() {
		final Map<Path, Set<ImportPath>> moduleDependencies = new HashMap<>();
		moduleDependencies.put(Paths.get("app"), importPaths(
				importPath(Paths.get("router"), false), importPath(Paths.get("util"), false)));
		moduleDependencies.put(Paths.get("router"), importPaths(
				importPath(Paths.get("first-page"), true), importPath(Paths.get("second-page"), true)));
		moduleDependencies.put(Paths.get("first-page"), importPaths(
				importPath(Paths.get("first-page-component"), false)));
		moduleDependencies.put(Paths.get("second-page"), importPaths(
				importPath(Paths.get("second-page-component"), false)));
		moduleDependencies.put(Paths.get("first-page-component"), importPaths(
				importPath(Paths.get("util"), false), importPath(Paths.get("label"), false), importPath(Paths.get("button"), false)));
		moduleDependencies.put(Paths.get("second-page-component"), importPaths(
				importPath(Paths.get("util"), false), importPath(Paths.get("label"), false)));

		final BundleReport report = BundleReporter.create(moduleDependencies, Optional.empty());

		assertThat(report.getModuleBundles()).hasSize(9);
		assertThat(report.getModuleBundles().get("app")).containsOnly();
		assertThat(report.getModuleBundles().get("router")).containsOnly();
		assertThat(report.getModuleBundles().get("first-page")).containsOnly("first-page-bundle");
		assertThat(report.getModuleBundles().get("second-page")).containsOnly("second-page-bundle");
		assertThat(report.getModuleBundles().get("first-page-component")).containsOnly("first-page-bundle");
		assertThat(report.getModuleBundles().get("second-page-component")).containsOnly("second-page-bundle");
		assertThat(report.getModuleBundles().get("util")).containsOnly("second-page-bundle", "first-page-bundle");
		assertThat(report.getModuleBundles().get("label")).containsOnly("second-page-bundle", "first-page-bundle");
		assertThat(report.getModuleBundles().get("button")).containsOnly("first-page-bundle");
	}

	private static Set<ImportPath> importPaths(final ImportPath... importPaths) {
		return new HashSet<>(Arrays.asList(importPaths));
	}

	private static ImportPath importPath(Path path, boolean dynamic) {
		return new ImportPath(path, dynamic);
	}
}
