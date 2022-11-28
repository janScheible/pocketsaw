package com.scheible.pocketsaw.impl.matching.auto;

import com.scheible.pocketsaw.impl.descriptor.ExternalFunctionalityDescriptor;
import com.scheible.pocketsaw.impl.matching.PackageMatchable;
import com.scheible.pocketsaw.impl.matching.PackageMatcher;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author sj
 */
public class ExternalFunctionalityAutoMatcher<T extends PackageMatchable> extends PackageMatcher<T> {

	private final String basePackage;
	private final Set<T> autoMatchedDescriptors = new HashSet<>();

	public ExternalFunctionalityAutoMatcher(final String basePackage, final Set<T> externalFunctionalities) {
		super(externalFunctionalities);
		this.basePackage = basePackage;
	}

	@Override
	public Optional<T> findMatching(String packageName) {
		final Optional<T> explicitExternalFunctionality = super.findMatching(packageName);

		if (explicitExternalFunctionality.isPresent()) {
			return explicitExternalFunctionality;
		} else if (!packageName.startsWith(basePackage)) {
			final String name = PackageGroupNameProvider.packageToName(packageName);
			final T descriptor = (T) new ExternalFunctionalityDescriptor(basePackage + ".ExternalFunctionalities." + name, name,
					new HashSet<>(Arrays.asList(packageName + ".*")));
			autoMatchedDescriptors.add(descriptor);
			return Optional.of(descriptor);
		} else {
			return Optional.empty();
		}
	}

	@Override
	public Set<T> getPackageGroups() {
		return Stream.concat(super.getPackageGroups().stream(), autoMatchedDescriptors.stream()).collect(Collectors.toSet());
	}
}
