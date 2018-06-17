package com.scheible.pocketsaw.impl;

import com.scheible.pocketsaw.api.ExternalFunctionality;
import com.scheible.pocketsaw.api.SubModule;
import com.scheible.pocketsaw.impl.code.jdeps.ApiAnnotationDependencyFilter;
import com.scheible.pocketsaw.impl.code.PackageDependecies;
import com.scheible.pocketsaw.impl.code.jdeps.JdepsWrapper;
import com.scheible.pocketsaw.impl.dependency.DependencyGraph;
import com.scheible.pocketsaw.impl.dependency.DependencyGraphFactory;
import com.scheible.pocketsaw.impl.descriptor.ClasspathScanner;
import com.scheible.pocketsaw.impl.descriptor.DescriptorInfo;
import com.scheible.pocketsaw.impl.descriptor.PackageGroupDescriptor;
import com.scheible.pocketsaw.impl.visualization.VisJsRenderer;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import static java.util.function.Function.identity;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author sj
 */
public class Pocketsaw {

	/**
	 * Convenience methods to check the rules and get readable information in case of vialotions.
	 */
	public static class AnalysisResult {

		private final DependencyGraph dependencyGraph;

		AnalysisResult(DependencyGraph dependencyGraph) {
			this.dependencyGraph = dependencyGraph;
		}

		public Optional<String> getAnyDescriptorCycle() {
			return dependencyGraph.getAnyDescriptorCycle()
					.map(cycle -> cycle.stream().map(PackageGroupDescriptor::getName).collect(Collectors.joining(" -> ")));
		}

		public Optional<String> getAnyCodeCycle() {
			return dependencyGraph.getAnyCodeCycle()
					.map(cycle -> cycle.stream().map(PackageGroupDescriptor::getName).collect(Collectors.joining(" -> ")));
		}

		public Set<String> getIllegalCodeDependencies() {
			return dependencyGraph.getIllegalCodeDependencies().stream()
					.map(dep -> dep.getSource().getName() + " -> " + dep.getTarget().getName()).collect(Collectors.toSet());
		}
	}

	private static final String CLASS_DIRECTORY = "./target/classes";
	private static final String GRAPH_HTML_OUTPUT_FILE = "./target/pocketsaw-dependency-graph.html";

	public static AnalysisResult analizeCurrentProject(ClasspathScanner classpathScanner) {
		final DescriptorInfo descriptorInfo = DescriptorInfo.createFromClasspath(classpathScanner);

		final ApiAnnotationDependencyFilter dependencyFilter = new ApiAnnotationDependencyFilter(Stream.of(
				classpathScanner.getSubModuleAnnotatedClassNames().stream(),
				classpathScanner.getExternalFunctionalityAnnotatedClassNames().stream(),
				Arrays.asList(SubModule.class.getName(), ExternalFunctionality.class.getName()).stream()
		).flatMap(identity()).collect(Collectors.toSet()));
		final PackageDependecies packageDependecies = JdepsWrapper.run(CLASS_DIRECTORY, dependencyFilter);

		final DependencyGraph dependencyGraph = DependencyGraphFactory.create(packageDependecies,
				descriptorInfo.getSubModules(), descriptorInfo.getExternalFunctionalities());

		VisJsRenderer.render(dependencyGraph, getGraphHtmlOutputFile());
		System.out.println("Pocketsaw dependency graph: " + getGraphHtmlOutputFile());

		return new AnalysisResult(dependencyGraph);
	}

	private static File getGraphHtmlOutputFile() {
		File graphHTmlOutputFile = Paths.get(GRAPH_HTML_OUTPUT_FILE).toFile();

		try {
			return graphHTmlOutputFile.getCanonicalFile();
		} catch (IOException ex1) {
		}

		return graphHTmlOutputFile;
	}
}
