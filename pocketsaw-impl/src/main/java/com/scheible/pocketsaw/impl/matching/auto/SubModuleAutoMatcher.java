package com.scheible.pocketsaw.impl.matching.auto;

import com.scheible.pocketsaw.impl.descriptor.SubModuleDescriptor;
import com.scheible.pocketsaw.impl.matching.PackageMatchable;
import com.scheible.pocketsaw.impl.matching.PackageMatcher;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author sj
 */
public class SubModuleAutoMatcher<T extends PackageMatchable> extends PackageMatcher<T> {

	private final String basePackage;
	private final Set<T> autoMatchedDescriptors = new HashSet<>();

	public SubModuleAutoMatcher(final String basePackage, final Set<T> subModules) {
		super(subModules);
		this.basePackage = basePackage;
	}

	@Override
	public Optional<T> findMatching(String packageName) {
		final Optional<T> explicitSubModule = super.findMatching(packageName);

		if (explicitSubModule.isPresent()) {
			return explicitSubModule;
		} else if (packageName.startsWith(basePackage)) {
			final String name = PackageGroupNameProvider.packageToName(basePackage.equals(packageName) ? basePackage
					: packageName.substring(basePackage.length() + 1));

			final T descriptor = (T) new SubModuleDescriptor(packageName + "." + name + "SubModule", name, packageName, false,
					Collections.emptySet(), Collections.emptySet());
			autoMatchedDescriptors.add(descriptor);
			return Optional.of((descriptor));
		} else {
			return Optional.empty();
		}
	}

	@Override
	public Set<T> getPackageGroups() {
		return Stream.concat(super.getPackageGroups().stream(), autoMatchedDescriptors.stream()).collect(Collectors.toSet());
	}
}
