package com.scheible.pocketsaw.impl.cli;

import com.scheible.pocketsaw.impl.Pocketsaw;
import com.scheible.pocketsaw.impl.Pocketsaw.AnalysisResult;
import com.scheible.pocketsaw.impl.code.NopPackageDependencySource;
import com.scheible.pocketsaw.impl.code.PackageDependencies;
import com.scheible.pocketsaw.impl.code.PackageDependencySource;
import com.scheible.pocketsaw.impl.descriptor.DescriptorInfo;
import com.scheible.pocketsaw.impl.descriptor.SubModuleDescriptor;
import com.scheible.pocketsaw.impl.descriptor.json.SubModuleJson;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author sj
 */
public class Main {

	public static void main(String... args) {
		final List<PackageDependencySource> packageDependencySources = new ArrayList<>();
		ServiceLoader.load(PackageDependencySource.class).iterator().forEachRemaining(packageDependencySources::add);
		if (packageDependencySources.size() == 1) { // NOTE NopPackageDependencySource is always found.
			System.err.println("No package dependency source was found on classpath (e.g. add "
					+ "'com.scheible.pocketsaw.dependencycruiser:pocketsaw-dependency-cruiser').");
			System.exit(1);
		}

		if (args.length < 4) {
			printUsageInfo(packageDependencySources);
			System.err.println("Not enough arguments!");
			System.exit(2);
		}
		final String subModulesJsonFile = args[0];
		final String dependenciesFile = args[1];

		final String dependencySource = args[2];

		final String dependencyGraphHtmlFile = args[3];

		final ResolvedArguments resolvedArguments = resolveArguments(subModulesJsonFile, dependenciesFile,
				dependencyGraphHtmlFile, dependencySource, packageDependencySources);
		if (!resolvedArguments.isValid()) {
			printUsageInfo(packageDependencySources);
			if (!resolvedArguments.subModulesJsonFile.exists()) {
				System.err.println("The sub modules JSON file '" + resolvedArguments.subModulesJsonFile.getAbsolutePath()
						+ "' does not exist!");
			}
			if (!resolvedArguments.dependenciesFile.exists()) {
				System.err.println("The dependencies file '" + resolvedArguments.dependenciesFile.getAbsolutePath()
						+ "' does not exist!");
			}
			if (!resolvedArguments.dependencyGraphHtmlFile.getParentFile().exists()) {
				System.err.println("At least one of the directories of the dependency graph HTML file '"
						+ resolvedArguments.dependencyGraphHtmlFile.getAbsolutePath() + "' does not exist!");
			}
			if (!resolvedArguments.dependencySource.isPresent()) {
				System.err.println("The dependency source '" + dependencySource + "' is unknown!");
			}

			System.exit(3);
		}

		boolean ignoreIllegalCodeDependencies = false;
		boolean verbose = false;
		for (final String arg : args) {
			if ("--ignore-illegal-code-dependencies".equals(arg.trim().toLowerCase())) {
				ignoreIllegalCodeDependencies = true;
			} else if ("--verbose".equals(arg.trim().toLowerCase())) {
				verbose = true;
			}
		}

		System.out.println("sub modules JSON file: '" + resolvedArguments.subModulesJsonFile.getAbsolutePath()
				+ "', dependencies file: '" + resolvedArguments.dependenciesFile.getAbsolutePath()
				+ "', ignore illegal code dependencies: " + ignoreIllegalCodeDependencies
				+ ", verbose: " + verbose);
		if (resolvedArguments.dependencySource.get() instanceof NopPackageDependencySource) {
			System.out.println("The dependency source '" + resolvedArguments.dependencySource.get().getIdentifier()
					+ "' is only intended for testing purposes! It simply does... well nothing...");
		}

		try {
			final DescriptorInfo descriptorInfo = SubModuleJson.read(resolvedArguments.subModulesJsonFile);
			final PackageDependencies packageDependencies = resolvedArguments.dependencySource.get().read(new File(dependenciesFile));
			if (verbose) {
				System.out.println("sub modules:");
				for (final SubModuleDescriptor subModule : descriptorInfo.getSubModules()) {
					System.out.print("  - ");
					System.out.println(subModule.getName() + ": " + subModule.getPackageMatchPattern() + " using " + subModule.getUsedSubModuleIds());
				}
				System.out.println("package dependencies:");
				final List<Map.Entry<String, Set<String>>> entryList = new ArrayList<>();
				packageDependencies.entrySet().forEach(entryList::add);
				Collections.sort(entryList, (Map.Entry<String, Set<String>> first, Map.Entry<String, Set<String>> second)
						-> first.getKey().compareTo(second.getKey()));
				for (final Map.Entry<String, Set<String>> dependency : entryList) {
					System.out.print("  - ");
					System.out.println(dependency.getKey() + " --> " + dependency.getValue());
				}
			}
			final AnalysisResult result = Pocketsaw.analize(resolvedArguments.subModulesJsonFile,
					resolvedArguments.dependenciesFile, resolvedArguments.dependencySource.get().getClass(),
					Optional.of(resolvedArguments.dependencyGraphHtmlFile));

			if (result.getAnyDescriptorCycle().isPresent()) {
				System.err.println("found a descriptor cycle: " + result.getAnyDescriptorCycle().get());
				System.exit(4);
			} else if (result.getAnyCodeCycle().isPresent()) {
				System.err.println("found a code cycle: " + result.getAnyCodeCycle().get());
				System.exit(5);
			} else if (!ignoreIllegalCodeDependencies && !result.getIllegalCodeDependencies().isEmpty()) {
				System.err.println("found illegal code dependencies: " + result.getIllegalCodeDependencies());
				System.exit(6);
			}
		} catch (Exception ex) {
			System.err.print("An unexpected error occured: ");
			ex.printStackTrace(System.err);
			System.exit(-1);
		}

		System.exit(0);
	}

	private static void printUsageInfo(final List<PackageDependencySource> packageDependencySources) {
		System.out.println("usage: pocketsaw <sub-module.json> <dependencies.file> {"
				+ packageDependencySources.stream()
						.filter(pds -> !(pds instanceof NopPackageDependencySource))
						.map(PackageDependencySource::getIdentifier)
						.collect(Collectors.joining("|")) + "} <pocketsaw-dependency-graph.html> [--ignore-illegal-code-dependencies] [--verbose]");
	}

	private static class ResolvedArguments {

		private final File subModulesJsonFile;
		private final File dependenciesFile;
		private final Optional<PackageDependencySource> dependencySource;
		private final File dependencyGraphHtmlFile;

		private ResolvedArguments(File subModulesJsonFile, File dependencyFile, File dependencyGraphHtmlFile,
				Optional<PackageDependencySource> dependencySource) {
			this.subModulesJsonFile = subModulesJsonFile;
			this.dependenciesFile = dependencyFile;
			this.dependencySource = dependencySource;
			this.dependencyGraphHtmlFile = dependencyGraphHtmlFile;
		}

		private boolean isValid() {
			return subModulesJsonFile.exists() && dependenciesFile.exists()
					&& dependencyGraphHtmlFile.getParentFile().exists() && dependencySource.isPresent();
		}
	}

	private static ResolvedArguments resolveArguments(String subModuleFile, String dependencyFile,
			String dependencyGraphHtmlFile, String dependencySource,
			List<PackageDependencySource> packageDependencySources) {
		return new ResolvedArguments(toCanonical(subModuleFile), toCanonical(dependencyFile),
				toCanonical(dependencyGraphHtmlFile), packageDependencySources.stream()
				.filter(pds -> pds.getIdentifier().equals(dependencySource)).findFirst());
	}

	private static File toCanonical(String relativeFile) {
		try {
			return new File(relativeFile).getCanonicalFile();
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}
}
