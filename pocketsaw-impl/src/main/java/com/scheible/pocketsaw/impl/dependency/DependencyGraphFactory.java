package com.scheible.pocketsaw.impl.dependency;

import com.scheible.pocketsaw.impl.code.PackageDependecies;
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

	public static DependencyGraph create(PackageDependecies codePackageDependencies, Set<SubModuleDescriptor> subModules,
			Set<ExternalFunctionalityDescriptor> externalFunctionalities) {
		Map<SubModuleDescriptor, Set<PackageGroupDescriptor>> descriptorDependecies = calcDescriptorDependencies(subModules);

		Map<SubModuleDescriptor, Set<PackageGroupDescriptor>> codeDependecies
				= calcCodeDependencies(subModules, externalFunctionalities, codePackageDependencies);

		Set<Dependency> allDependencies = new HashSet<>();
		subModules.forEach(subModule -> {
			Set<PackageGroupDescriptor> descriptorUsed = descriptorDependecies.containsKey(subModule) 
					? descriptorDependecies.get(subModule) : new HashSet<>();
			Set<PackageGroupDescriptor> codeUsed = codeDependecies.get(subModule);
			
			Set<PackageGroupDescriptor> allUsed = descriptorUsed != null ? new HashSet<>(descriptorUsed) : new HashSet<>();
			if (codeUsed != null) {
				allUsed.addAll(codeUsed);
			}
			
			allUsed.forEach(used -> {
				allDependencies.add(new Dependency(subModule, used, descriptorUsed.contains(used), codeUsed.contains(used)));
			});
		});		
		
		Set<PackageGroupDescriptor> allDescriptors = new HashSet<>(subModules);
		allDescriptors.addAll(externalFunctionalities);				

		return new DependencyGraph(allDescriptors, allDependencies);
	}

	private static Map<SubModuleDescriptor, Set<PackageGroupDescriptor>> calcDescriptorDependencies(Set<SubModuleDescriptor> subModules) {
		final Map<SubModuleDescriptor, Set<PackageGroupDescriptor>> descriptorDependecies = new HashMap<>();
		final Map<String, SubModuleDescriptor> subModuleIdMapping = subModules.stream().collect(Collectors.toMap(x -> x.getId(), x -> x));

		for (SubModuleDescriptor subModule : subModules) {
			for (PackageGroupDescriptor externalFunctionalitiesDescriptor : subModule.getUsedExternalFunctionalities()) {
				descriptorDependecies.computeIfAbsent(subModule, (key) -> new HashSet<>()).add(externalFunctionalitiesDescriptor);
			}

			for (String subModuleId : subModule.getUsedSubModuleIds()) {
				descriptorDependecies.computeIfAbsent(subModule, (key) -> new HashSet<>()).add(subModuleIdMapping.get(subModuleId));
			}
		}

		return descriptorDependecies;
	}

	private static Map<SubModuleDescriptor, Set<PackageGroupDescriptor>> calcCodeDependencies(Set<SubModuleDescriptor> subModules, final Set<ExternalFunctionalityDescriptor> externalFunctionalities, PackageDependecies packageDependencies) {
		final Map<SubModuleDescriptor, Set<PackageGroupDescriptor>> codeDependecies = new HashMap<>();
		
		final PackageMatcher<PackageGroupDescriptor> subModuleMatcher = new PackageMatcher(subModules);
		final PackageMatcher<PackageGroupDescriptor> externalFunctionalitiesMatcher = new PackageMatcher(externalFunctionalities);

		for (Map.Entry<String, Set<String>> codeDependencies : packageDependencies.entrySet()) {
			SubModuleDescriptor subModule = (SubModuleDescriptor)(subModuleMatcher.findMatching(codeDependencies.getKey())
					.orElseThrow(() -> new UnmatchedPackageException("The package '" + codeDependencies.getKey() + "' was not matched at all!")));

			for (String usedPackageName : codeDependencies.getValue()) {
				PackageGroupDescriptor matchedPackage = subModuleMatcher.findMatching(usedPackageName)
						.orElseGet(() -> externalFunctionalitiesMatcher.findMatching(usedPackageName)
								.orElseThrow(() -> new UnmatchedPackageException("The package '" + usedPackageName + "' was not matched at all!")));

				codeDependecies.computeIfAbsent(subModule, (key) -> new HashSet<>()).add(matchedPackage);
			}
		}

		return codeDependecies;
	}
}
