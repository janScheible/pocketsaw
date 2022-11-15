package com.scheible.pocketsaw.impl;

import com.scheible.pocketsaw.api.ExternalFunctionality;
import com.scheible.pocketsaw.api.SubModule;
import com.scheible.pocketsaw.impl.code.DependencyFilter;
import com.scheible.pocketsaw.impl.code.PackageDependencies;
import com.scheible.pocketsaw.impl.code.PackageDependencySource;
import com.scheible.pocketsaw.impl.code.jdeps.JdepsWrapper;
import com.scheible.pocketsaw.impl.dependency.DependencyGraph;
import com.scheible.pocketsaw.impl.dependency.DependencyGraphFactory;
import com.scheible.pocketsaw.impl.descriptor.annotation.ClasspathScanner;
import com.scheible.pocketsaw.impl.descriptor.DescriptorInfo;
import com.scheible.pocketsaw.impl.descriptor.PackageGroupDescriptor;
import com.scheible.pocketsaw.impl.descriptor.annotation.AnnotationDescriptorInfoFactory;
import com.scheible.pocketsaw.impl.descriptor.annotation.DependencyAwareClasspathScanner;
import com.scheible.pocketsaw.impl.descriptor.annotation.FastClasspathScanner;
import com.scheible.pocketsaw.impl.descriptor.annotation.SpringClasspathScanner;
import com.scheible.pocketsaw.impl.descriptor.json.JsonDescriptorReader;
import com.scheible.pocketsaw.impl.visualization.VisJsRenderer;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
		final DependencyFilter dependencyFilter = new DependencyFilter(
				new HashSet<>(Arrays.asList(classpathScanner.getBasePackage())), Stream.of(
						classpathScanner.getSubModuleAnnotatedClassNames().stream(),
						classpathScanner.getExternalFunctionalityAnnotatedClassNames().stream(),
						Arrays.asList(SubModule.class.getName(), ExternalFunctionality.class.getName()).stream()
				).flatMap(identity()).collect(Collectors.toSet()), true);
		boolean dependenciesFromClasspathScanner = classpathScanner instanceof DependencyAwareClasspathScanner 
				&& ((DependencyAwareClasspathScanner)classpathScanner).doDependencyScan();
		
		final PackageDependencies packageDependencies = dependenciesFromClasspathScanner
				?((DependencyAwareClasspathScanner)classpathScanner).getDependencies()
				: JdepsWrapper.run(CLASS_DIRECTORY, dependencyFilter);

		final DescriptorInfo descriptorInfo = AnnotationDescriptorInfoFactory.createFromClasspath(classpathScanner);

		return analize(descriptorInfo, packageDependencies, Optional.empty());
	}
	
	public static AnalysisResult analizeClasspath(DependencyAwareClasspathScanner classpathScanner) {
		return analizeCurrentProject(classpathScanner.enableDependencyScan());
	}

	/**
	 *
	 * @param classpathScanner
	 * @return
	 * @deprecated Introduced in release 1.1.0 to be backwards compatible after
	 * {@link com.scheible.pocketsaw.impl.descriptor.annotation.ClasspathScanner} was moved to the annotation package.
	 * Please switch to that the moved class as an prepartion for version 2.0.
	 */
	@Deprecated
	public static AnalysisResult analizeCurrentProject(com.scheible.pocketsaw.impl.descriptor.ClasspathScanner classpathScanner) {
		if (classpathScanner instanceof com.scheible.pocketsaw.impl.descriptor.SpringClasspathScanner) {
			return analizeCurrentProject(SpringClasspathScanner.create(classpathScanner.getBasePackageClass()));
		} else if (classpathScanner instanceof com.scheible.pocketsaw.impl.descriptor.FastClasspathScanner) {
			return analizeCurrentProject(FastClasspathScanner.create(classpathScanner.getBasePackageClass()));
		} else {
			throw new IllegalStateException("Unknown sub class of ClasspathScanner!");
		}
	}

	public static AnalysisResult analize(final File subModulesJsonFile, final File dependenciesFile,
			final Class<? extends PackageDependencySource> packageDependencySourceClass,
			Optional<File> dependencyGraphHtmlFile) {
		final PackageDependencies packageDependencies = instantiate(packageDependencySourceClass).read(dependenciesFile);
		return analize(loadSubModuleJson(subModulesJsonFile), packageDependencies, dependencyGraphHtmlFile);
	}

	public static AnalysisResult analize(final File subModulesJsonFile, final PackageDependencies packageDependencies,
			final Optional<File> dependencyGraphHtmlFile) {
		return analize(loadSubModuleJson(subModulesJsonFile), packageDependencies, dependencyGraphHtmlFile);
	}

	private static PackageDependencySource instantiate(final Class<? extends PackageDependencySource> packageDependencySourceClass) {
		try {
			return packageDependencySourceClass.getConstructor().newInstance();
		} catch (IllegalAccessException | InstantiationException | IllegalArgumentException 
				| NoSuchMethodException | InvocationTargetException ex) {
			throw new IllegalStateException(ex);
		}
	}

	static AnalysisResult analize(final DescriptorInfo descriptorInfo, final PackageDependencies packageDependencies,
			Optional<File> dependencyGraphHtmlFile) {
		final DependencyGraph dependencyGraph = DependencyGraphFactory.create(packageDependencies,
				descriptorInfo.getSubModules(), descriptorInfo.getExternalFunctionalities());

		final File resolvedDependencyGraphHtmlFile = toCanonical(dependencyGraphHtmlFile.orElseGet(() -> Paths.get(GRAPH_HTML_OUTPUT_FILE).toFile()));
		VisJsRenderer.render(dependencyGraph, resolvedDependencyGraphHtmlFile);
		System.out.println("Pocketsaw dependency graph: " + resolvedDependencyGraphHtmlFile.getAbsolutePath());

		return new AnalysisResult(dependencyGraph);
	}

	static DescriptorInfo loadSubModuleJson(final File subModulesJsonFile) {
		final File canonicalSubModulesJsonFile = toCanonical(subModulesJsonFile);
		try {
			return JsonDescriptorReader.read(canonicalSubModulesJsonFile);
		} catch (IOException ex) {
			throw new UncheckedIOException("An error occured while reading the sub modules from the JSON file '"
					+ canonicalSubModulesJsonFile.getAbsolutePath() + "'!", ex);
		}
	}

	private static File toCanonical(File file) {
		try {
			return file.getCanonicalFile();
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}
}
