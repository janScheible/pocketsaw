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

/**
 *
 * @author sj
 */
public class DependencyGraphFactory {
	
	public static DependencyGraph create(PackageDependencies codePackageDependencies, Set<SubModuleDescriptor> subModules,
			Set<ExternalFunctionalityDescriptor> externalFunctionalities) {
		Map<SubModuleDescriptor, Set<PackageGroupDescriptor>> descriptorDependencies = calcDescriptorDependencies(subModules);

		Map<SubModuleDescriptor, Set<PackageGroupDescriptor>> codeDependencies
				= calcCodeDependencies(subModules, externalFunctionalities, codePackageDependencies);

		Set<Dependency> allDependencies = new HashSet<>();
		subModules.forEach(subModule -> {
			Set<PackageGroupDescriptor> descriptorUsed = descriptorDependencies.computeIfAbsent(subModule, (key) -> new HashSet<>());
			Set<PackageGroupDescriptor> codeUsed =  codeDependencies.computeIfAbsent(subModule, (key) -> new HashSet<>());
			
			Set<PackageGroupDescriptor> allUsed = new HashSet<>(descriptorUsed);
			allUsed.addAll(codeUsed);
			
			allUsed.forEach(used -> {
				allDependencies.add(new Dependency(subModule, used, descriptorUsed.contains(used), codeUsed.contains(used)));
			});
		});		
		
		Set<PackageGroupDescriptor> allDescriptors = new HashSet<>(subModules);
		allDescriptors.addAll(externalFunctionalities);				

		return new DependencyGraph(allDescriptors, allDependencies);
	}

	private static Map<SubModuleDescriptor, Set<PackageGroupDescriptor>> calcDescriptorDependencies(Set<SubModuleDescriptor> subModules) {
		final Map<SubModuleDescriptor, Set<PackageGroupDescriptor>> descriptorDependencies = new HashMap<>();
		final Map<String, SubModuleDescriptor> subModuleIdMapping = subModules.stream().collect(Collectors.toMap(x -> x.getId(), x -> x));

		for (SubModuleDescriptor subModule : subModules) {
			for (PackageGroupDescriptor externalFunctionalitiesDescriptor : subModule.getUsedExternalFunctionalities()) {
				descriptorDependencies.computeIfAbsent(subModule, (key) -> new HashSet<>()).add(externalFunctionalitiesDescriptor);
			}

			for (String subModuleId : subModule.getUsedSubModuleIds()) {
				descriptorDependencies.computeIfAbsent(subModule, (key) -> new HashSet<>()).add(subModuleIdMapping.get(subModuleId));
			}
		}

		return descriptorDependencies;
	}

	private static Map<SubModuleDescriptor, Set<PackageGroupDescriptor>> calcCodeDependencies(Set<SubModuleDescriptor> subModules,
			final Set<ExternalFunctionalityDescriptor> externalFunctionalities, PackageDependencies packageDependencies) {
		final Map<SubModuleDescriptor, Set<PackageGroupDescriptor>> codeDependencies = new HashMap<>();
		
		final PackageMatcher<PackageGroupDescriptor> subModuleMatcher = new PackageMatcher(subModules);
		final PackageMatcher<PackageGroupDescriptor> externalFunctionalitiesMatcher = new PackageMatcher(externalFunctionalities);

		for (Map.Entry<String, Set<String>> currentCodeDependencies : packageDependencies.entrySet()) {
			SubModuleDescriptor subModule = (SubModuleDescriptor)(subModuleMatcher.findMatching(currentCodeDependencies.getKey())
					.orElseThrow(() -> new UnmatchedPackageException("The package '" + currentCodeDependencies.getKey() + "' was not matched at all!")));

			for (String usedPackageName : currentCodeDependencies.getValue()) {
				PackageGroupDescriptor matchedPackage = subModuleMatcher.findMatching(usedPackageName)
						.orElseGet(() -> externalFunctionalitiesMatcher.findMatching(usedPackageName)
								.orElseThrow(() -> new UnmatchedPackageException("The package '" + usedPackageName + "' was not matched at all!")));

				boolean isSelfDependency = subModule.equals(matchedPackage);
				if(!isSelfDependency) {
					codeDependencies.computeIfAbsent(subModule, (key) -> new HashSet<>()).add(matchedPackage);
				}
			}
		}

		return codeDependencies;
	}
}
