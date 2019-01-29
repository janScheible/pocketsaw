package com.scheible.pocketsaw.impl.dependency;

import com.scheible.pocketsaw.impl.code.PackageDependencies;
import com.scheible.pocketsaw.impl.matching.PackageMatcher;
import com.scheible.pocketsaw.impl.descriptor.ExternalFunctionalityDescriptor;
import com.scheible.pocketsaw.impl.descriptor.SubModuleDescriptor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.scheible.pocketsaw.impl.descriptor.PackageGroupDescriptor;
import com.scheible.pocketsaw.impl.matching.UnmatchedPackageException;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;
import java.util.Optional;

/**
 *
 * @author sj
 */
public class DependencyGraphFactory {
	
	public static DependencyGraph create(final PackageDependencies codePackageDependencies, final Set<SubModuleDescriptor> subModules,
			final Set<ExternalFunctionalityDescriptor> externalFunctionalities) {
		final Map<SubModuleDescriptor, Set<PackageGroupDescriptor>> descriptorDependencies = calcDescriptorDependencies(subModules);

		final Map<SubModuleDescriptor, Set<Entry<PackageGroupDescriptor, Integer>>> codeDependencies
				= calcCodeDependencies(subModules, externalFunctionalities, codePackageDependencies);

		Set<Dependency> allDependencies = new HashSet<>();
		subModules.forEach(subModule -> {
			final Set<PackageGroupDescriptor> descriptorUsed = descriptorDependencies.computeIfAbsent(subModule, (key) -> new HashSet<>());
			final Map<PackageGroupDescriptor, Integer> codeUsedWithDependencyCount =  codeDependencies
					.computeIfAbsent(subModule, (key) -> new HashSet<>()).stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue));
			final Set<PackageGroupDescriptor> codeUsed = codeDependencies
					.computeIfAbsent(subModule, (key) -> new HashSet<>()).stream().map(Entry::getKey).collect(Collectors.toSet());
			
			final Set<PackageGroupDescriptor> allUsed = new HashSet<>(descriptorUsed);
			allUsed.addAll(codeUsed);

			allUsed.forEach(used -> {
				final boolean isCodeDependency = codeUsed.contains(used);
				allDependencies.add(new Dependency(subModule, used, 
						descriptorUsed.contains(used), isCodeDependency, codeUsedWithDependencyCount.getOrDefault(used, isCodeDependency ? 1 : 0)));
			});
		});		
		
		Set<PackageGroupDescriptor> allDescriptors = new HashSet<>(subModules);
		allDescriptors.addAll(externalFunctionalities);				

		return new DependencyGraph(allDescriptors, allDependencies);
	}

	private static Map<SubModuleDescriptor, Set<PackageGroupDescriptor>> calcDescriptorDependencies(final Set<SubModuleDescriptor> subModules) {
		final Map<SubModuleDescriptor, Set<PackageGroupDescriptor>> descriptorDependencies = new HashMap<>();
		final Map<String, SubModuleDescriptor> subModuleIdMapping = subModules.stream()
				.collect(Collectors.toMap(SubModuleDescriptor::getId, x -> x));

		for (final SubModuleDescriptor subModule : subModules) {
			for (final PackageGroupDescriptor externalFunctionalitiesDescriptor : subModule.getUsedExternalFunctionalities()) {
				descriptorDependencies.computeIfAbsent(subModule, (key) -> new HashSet<>()).add(externalFunctionalitiesDescriptor);
			}

			for (final String subModuleId : subModule.getUsedSubModuleIds()) {
				descriptorDependencies.computeIfAbsent(subModule, (key) -> new HashSet<>()).add(subModuleIdMapping.get(subModuleId));
			}
		}

		return descriptorDependencies;
	}

	private static Map<SubModuleDescriptor, Set<Entry<PackageGroupDescriptor, Integer>>> calcCodeDependencies(final Set<SubModuleDescriptor> subModules,
			final Set<ExternalFunctionalityDescriptor> externalFunctionalities, PackageDependencies packageDependencies) {
		final Map<SubModuleDescriptor, Set<Entry<PackageGroupDescriptor, Integer>>> codeDependencies = new HashMap<>();
		
		final PackageMatcher<PackageGroupDescriptor> subModuleMatcher = new PackageMatcher(subModules);
		final PackageMatcher<PackageGroupDescriptor> externalFunctionalitiesMatcher = new PackageMatcher(externalFunctionalities);

		for (final Entry<String, Set<String>> currentCodeDependencies : packageDependencies.entrySet()) {
			SubModuleDescriptor subModule = (SubModuleDescriptor)(subModuleMatcher.findMatching(currentCodeDependencies.getKey())
					.orElseThrow(() -> new UnmatchedPackageException("The package '" + currentCodeDependencies.getKey() + "' was not matched at all!")));

			for (final String usedPackageName : currentCodeDependencies.getValue()) {
				final int codeDependencyCount = packageDependencies.getCodeDependencyCount(currentCodeDependencies.getKey(), usedPackageName);
				final PackageGroupDescriptor matchedPackage = subModuleMatcher.findMatching(usedPackageName)
						.orElseGet(() -> externalFunctionalitiesMatcher.findMatching(usedPackageName)
								.orElseThrow(() -> new UnmatchedPackageException("The package '" + usedPackageName + "' was not matched at all!")));

				final boolean isSelfDependency = subModule.equals(matchedPackage);
				if(!isSelfDependency) {
					final Set<Entry<PackageGroupDescriptor, Integer>> dependencies = codeDependencies.computeIfAbsent(subModule, (key) -> new HashSet<>());
					final Optional<Entry<PackageGroupDescriptor, Integer>> alreadyExistingDependency = dependencies.stream().filter(e -> e.getKey().equals(matchedPackage)).findFirst();
					if(alreadyExistingDependency.isPresent()) {
						dependencies.remove(alreadyExistingDependency.get());
					}
					dependencies.add(new SimpleImmutableEntry<>(matchedPackage, codeDependencyCount + alreadyExistingDependency.map(Entry::getValue).orElse(0)));
				}
			}
		}

		return codeDependencies;
	}
}
