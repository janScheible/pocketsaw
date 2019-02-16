package com.scheible.pocketsaw.impl.cli;

import com.scheible.pocketsaw.impl.code.PackageDependencySource;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *
 * @author sj
 */
class DependencySourceResolver {

	static class ResolvedDependencySource {

		private final Optional<PackageDependencySource> dependencySource;
		private final Set<Map.Entry<String, String>> dependencySourceParameters;

		private ResolvedDependencySource(Optional<PackageDependencySource> dependencySource,
				Set<Map.Entry<String, String>> dependencySourceParameters) {
			this.dependencySource = dependencySource;
			this.dependencySourceParameters = dependencySourceParameters;
		}

		Optional<PackageDependencySource> getDependencySource() {
			return dependencySource;
		}

		Set<Map.Entry<String, String>> getDependencySourceParameters() {
			return dependencySourceParameters;
		}
	}

	static ResolvedDependencySource resolve(String dependencySourceParameter,
			List<PackageDependencySource> packageDependencySources) {
		final String[] dependencySourceAndParameters = dependencySourceParameter.split(Pattern.quote(":"));
		final Optional<PackageDependencySource> dependencySource = packageDependencySources.stream()
				.filter(pds -> pds.getIdentifier().equals(dependencySourceAndParameters[0])).findFirst();
		final Set<Map.Entry<String, String>> dependencySourceParameters = Arrays.stream(dependencySourceAndParameters)
				.filter(p -> p.contains("="))
				.map(p -> {
					final String[] paramParts = p.split(Pattern.quote("="));
					return new AbstractMap.SimpleImmutableEntry<>(paramParts[0], paramParts[1]);
				})
				.collect(Collectors.toSet());

		return new ResolvedDependencySource(dependencySource, dependencySourceParameters);
	}
}
