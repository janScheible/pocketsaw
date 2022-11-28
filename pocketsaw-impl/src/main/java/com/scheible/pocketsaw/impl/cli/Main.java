package com.scheible.pocketsaw.impl.cli;

import com.scheible.pocketsaw.impl.Pocketsaw;
import com.scheible.pocketsaw.impl.Pocketsaw.AnalysisResult;
import com.scheible.pocketsaw.impl.cli.DependencySourceResolver.ResolvedDependencySource;
import com.scheible.pocketsaw.impl.cli.ValidatedArguments.ErrorReason;
import com.scheible.pocketsaw.impl.cli.ValidationResult.ValidationSuccess;
import com.scheible.pocketsaw.impl.code.NopPackageDependencySource;
import com.scheible.pocketsaw.impl.code.PackageDependencies;
import com.scheible.pocketsaw.impl.code.PackageDependencySource;
import com.scheible.pocketsaw.impl.descriptor.DescriptorInfo;
import com.scheible.pocketsaw.impl.descriptor.ExternalFunctionalityDescriptor;
import com.scheible.pocketsaw.impl.descriptor.SubModuleDescriptor;
import com.scheible.pocketsaw.impl.descriptor.json.JsonDescriptorReader;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
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

		final ParsedArguments parsedArgs = ParsedArguments.parse(args, packageDependencySources);
		final ValidationResult valdiationResult = ValidatedArguments.validate(parsedArgs, file -> file.exists());

		if (valdiationResult.isError()) {
			final Set<Entry<ErrorReason, String>> errorMessages = valdiationResult.asError().getErrorMessages();
			
			printUsageInfo(packageDependencySources);
			errorMessages.stream().map(Entry::getValue).forEach(System.err::println);

			System.exit(2);
		}

		final ResolvedDependencySource resolvedDependencySource = parsedArgs.resolvedDependencySource.get();
		if (resolvedDependencySource.getDependencySource() instanceof NopPackageDependencySource) {
			System.out.println("The dependency source '" + resolvedDependencySource.getDependencySource().getIdentifier()
					+ "' is only intended for testing purposes! It simply does... well nothing...");
		}

		final ValidationSuccess validatedArgs = valdiationResult.asSuccess();
		
		final Optional<File> subModulesJsonFile = validatedArgs.getSubModulesJsonFile();
		final File dependenciesFile = validatedArgs.getDependenciesFile();
		final File dependencyGraphHtmlFile = validatedArgs.getDependencyGraphHtmlFile();

		final String subModulesJsonFileDesc = subModulesJsonFile.isPresent()
				? ("sub modules JSON file: '" + subModulesJsonFile.get().getAbsolutePath() + "', ") : "";
		System.out.println(subModulesJsonFileDesc
				+ "dependency source: " + resolvedDependencySource.getDependencySource().getIdentifier()
				+ ", dependency source specific parameters: " + resolvedDependencySource.getDependencySourceParameters()
				+ ", dependencies file/directory: '" + dependenciesFile.getAbsolutePath()
				+ "', ignore illegal code dependencies: " + parsedArgs.ignoreIllegalCodeDependencies
				+ ", auto matching: " + parsedArgs.autoMatching + ", verbose: " + parsedArgs.verbose);

		try {
			final DescriptorInfo descriptorInfo = subModulesJsonFile.isPresent()
					? JsonDescriptorReader.read(subModulesJsonFile.get()).withAutoMatching(parsedArgs.autoMatching)
					: new DescriptorInfo(Collections.emptySet(), Collections.emptySet(), true);
			final PackageDependencies packageDependencies = resolvedDependencySource.getDependencySource().read(
					dependenciesFile, resolvedDependencySource.getDependencySourceParameters());
			if (parsedArgs.verbose) {
				System.out.println("sub modules:");
				for (final SubModuleDescriptor subModule : descriptorInfo.getSubModules()) {
					System.out.print("  - ");
					System.out.println(subModule.getName() + ": "
							+ subModule.getPackageMatchPatterns().stream().collect(Collectors.joining(", "))
							+ " using " + subModule.getUsedSubModuleIds());
				}

				System.out.println("external functionalities:");
				for (final ExternalFunctionalityDescriptor externalFunctionality : descriptorInfo.getExternalFunctionalities()) {
					System.out.print("  - ");
					System.out.println(externalFunctionality.getName() + ": "
							+ externalFunctionality.getPackageMatchPatterns().stream().collect(Collectors.joining(", ")));
				}

				final boolean autoMatchingOverrideByCli = parsedArgs.autoMatching && parsedArgs.autoMatching != descriptorInfo.doAutoMatching();
				System.out.println("auto matching: " + parsedArgs.autoMatching
						+ (autoMatchingOverrideByCli ? " (overriden by cli)" : ""));

				System.out.println("package dependencies:");
				final List<Entry<String, Set<String>>> entryList = new ArrayList<>();
				packageDependencies.entrySet().forEach(entryList::add);
				Collections.sort(entryList, (Entry<String, Set<String>> first, Entry<String, Set<String>> second)
						-> first.getKey().compareTo(second.getKey()));
				for (final Entry<String, Set<String>> dependency : entryList) {
					System.out.print("  - ");
					System.out.println(dependency.getKey() + " --> " + dependency.getValue());
				}
			}

			final AnalysisResult result = Pocketsaw.analize(descriptorInfo, packageDependencies,
					Optional.of(dependencyGraphHtmlFile));

			if (result.getAnyDescriptorCycle().isPresent()) {
				System.err.println("found a descriptor cycle: " + result.getAnyDescriptorCycle().get());
				System.exit(4);
			} else if (result.getAnyCodeCycle().isPresent()) {
				System.err.println("found a code cycle: " + result.getAnyCodeCycle().get());
				System.exit(5);
			} else if (!parsedArgs.ignoreIllegalCodeDependencies && !result.getIllegalCodeDependencies().isEmpty()) {
				System.err.println("found illegal code dependencies: " + result.getIllegalCodeDependencies());
				System.exit(6);
			}
		} catch (final Exception ex) {
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
						.collect(Collectors.joining("|")) + "} <pocketsaw-dependency-graph.html> [--ignore-illegal-code-dependencies] [--auto-matching] [--verbose]");
		System.out.println("");
		System.out.println("options:");
		System.out.println("  --ignore-illegal-code-dependencies   Does not fail in case of illegal code dependencies.");
		System.out.println("  --auto-matching                      Enables auto matching (<sub-module.json> is optional then).");
		System.out.println("  --verbose                            Print detailed information.");
	}
}
