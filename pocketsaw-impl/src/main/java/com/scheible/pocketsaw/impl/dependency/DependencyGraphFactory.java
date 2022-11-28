package com.scheible.pocketsaw.impl.dependency;

import com.scheible.pocketsaw.impl.code.PackageDependencies;
import com.scheible.pocketsaw.impl.code.TypeDependency;
import com.scheible.pocketsaw.impl.descriptor.DescriptorInfo;
import com.scheible.pocketsaw.impl.descriptor.ExternalFunctionalityDescriptor;
import com.scheible.pocketsaw.impl.matching.PackageMatcher;
import com.scheible.pocketsaw.impl.descriptor.SubModuleDescriptor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.scheible.pocketsaw.impl.descriptor.PackageGroupDescriptor;
import com.scheible.pocketsaw.impl.matching.auto.ExternalFunctionalityAutoMatcher;
import com.scheible.pocketsaw.impl.matching.auto.SubModuleAutoMatcher;
import com.scheible.pocketsaw.impl.matching.UnmatchedPackageException;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.Optional;

/**
 *
 * @author sj
 */
public class DependencyGraphFactory {

	public static DependencyGraph create(final DescriptorInfo explicitDescriptorInfo, final PackageDependencies codePackageDependencies) {
		final CodeDependencies codeDependencies = calcCodeDependencies(explicitDescriptorInfo, codePackageDependencies);
		final DescriptorInfo descriptorInfo = codeDependencies.effectiveDescriptorInfo;

		final Map<SubModuleDescriptor, Set<PackageGroupDescriptor>> descriptorDependencies
				= calcDescriptorDependencies(descriptorInfo.getSubModules());

		Set<Dependency> allDependencies = new HashSet<>();
		descriptorInfo.getSubModules().forEach(subModule -> {
			final Set<PackageGroupDescriptor> descriptorUsed = descriptorDependencies.computeIfAbsent(subModule, (key) -> new HashSet<>());
			final Map<PackageGroupDescriptor, Integer> codeUsedWithDependencyCount = codeDependencies
					.computeIfAbsent(subModule, (key) -> new HashSet<>()).stream().collect(Collectors.toMap(Entry::getKey, e -> e.getValue().size()));
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

		Set<PackageGroupDescriptor> allDescriptors = new HashSet<>(descriptorInfo.getSubModules());
		allDescriptors.addAll(descriptorInfo.getExternalFunctionalities());

		final Map<SubModuleDescriptor, Set<String>> usedSubModuleTypes = new HashMap<>();
		for (final Entry<SubModuleDescriptor, Set<Entry<PackageGroupDescriptor, Set<TypeDependency>>>> codeDependency : codeDependencies.entrySet()) {
			for (final Entry<PackageGroupDescriptor, Set<TypeDependency>> dependencyDetails : codeDependency.getValue()) {
				if (dependencyDetails.getKey() instanceof SubModuleDescriptor) {
					Set<String> usedTypes = dependencyDetails.getValue().stream().map(TypeDependency::getToFullName).collect(Collectors.toSet());
					usedSubModuleTypes.computeIfAbsent((SubModuleDescriptor) dependencyDetails.getKey(),
							key -> new HashSet<>()).addAll(usedTypes);
				}
			}
		}

		return new DependencyGraph(allDescriptors, allDependencies, usedSubModuleTypes);
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

	private static CodeDependencies calcCodeDependencies(final DescriptorInfo descriptorInfo,
			PackageDependencies packageDependencies) {
		final Map<SubModuleDescriptor, Set<Entry<PackageGroupDescriptor, Set<TypeDependency>>>> codeDependencies = new HashMap<>();

		final String basePackage = packageDependencies.getBasePackage();

		final PackageMatcher<PackageGroupDescriptor> subModuleMatcher = descriptorInfo.doAutoMatching()
				? new SubModuleAutoMatcher(basePackage, descriptorInfo.getSubModules())
				: new PackageMatcher(descriptorInfo.getSubModules());

		final PackageMatcher<ExternalFunctionalityDescriptor> externalFunctionalitiesMatcher = descriptorInfo.doAutoMatching()
				? new ExternalFunctionalityAutoMatcher(basePackage, descriptorInfo.getExternalFunctionalities())
				: new PackageMatcher(descriptorInfo.getExternalFunctionalities());

		for (final Entry<String, Set<String>> currentCodeDependencies : packageDependencies.entrySet()) {
			SubModuleDescriptor subModule = (SubModuleDescriptor) (subModuleMatcher.findMatching(currentCodeDependencies.getKey())
					.orElseThrow(() -> new UnmatchedPackageException("The package '" + currentCodeDependencies.getKey() + "' was not matched at all!")));

			for (final String usedPackageName : currentCodeDependencies.getValue()) {
				final Set<TypeDependency> typeDependencies = packageDependencies.getTypeDependencies(currentCodeDependencies.getKey(), usedPackageName);
				final PackageGroupDescriptor matchedPackage = subModuleMatcher.findMatching(usedPackageName)
						.orElseGet(() -> externalFunctionalitiesMatcher.findMatching(usedPackageName)
						.orElseThrow(() -> new UnmatchedPackageException("The package '" + usedPackageName + "' was not matched at all!")));

				final boolean isSelfDependency = subModule.equals(matchedPackage);
				if (!isSelfDependency) {
					final Set<Entry<PackageGroupDescriptor, Set<TypeDependency>>> dependencies = codeDependencies.computeIfAbsent(subModule, (key) -> new HashSet<>());
					final Optional<Entry<PackageGroupDescriptor, Set<TypeDependency>>> alreadyExistingDependency = dependencies.stream().filter(e -> e.getKey().equals(matchedPackage)).findFirst();
					if (alreadyExistingDependency.isPresent()) {
						dependencies.remove(alreadyExistingDependency.get());
					}
					dependencies.add(new SimpleImmutableEntry<>(matchedPackage, newHashSet(typeDependencies, alreadyExistingDependency.map(Entry::getValue).orElse(Collections.EMPTY_SET))));
				}
			}
		}

		return new CodeDependencies(codeDependencies, new DescriptorInfo(
				subModuleMatcher.getPackageGroups().stream().map(group -> (SubModuleDescriptor) group).collect(Collectors.toSet()),
				externalFunctionalitiesMatcher.getPackageGroups().stream().map(group -> (ExternalFunctionalityDescriptor) group).collect(Collectors.toSet()),
				descriptorInfo.doAutoMatching()));
	}

	private static <T> HashSet<T> newHashSet(Set<T> first, Set<T> second) {
		HashSet<T> result = new HashSet<>(first);
		result.addAll(second);
		return result;
	}

	private static class CodeDependencies extends HashMap<SubModuleDescriptor, Set<Entry<PackageGroupDescriptor, Set<TypeDependency>>>> {

		/** Contains the explicit descriptors as well as the auto matched ones. */
		private final DescriptorInfo effectiveDescriptorInfo;

		private CodeDependencies(final Map<SubModuleDescriptor, Set<Entry<PackageGroupDescriptor, Set<TypeDependency>>>> dependencies, DescriptorInfo effectiveDescriptorInfo) {
			super(dependencies);
			this.effectiveDescriptorInfo = effectiveDescriptorInfo;
		}
	}
}
