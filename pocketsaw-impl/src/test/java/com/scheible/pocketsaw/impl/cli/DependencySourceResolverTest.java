package com.scheible.pocketsaw.impl.cli;

import com.scheible.pocketsaw.impl.cli.DependencySourceResolver.ResolvedDependencySource;
import com.scheible.pocketsaw.impl.code.NopPackageDependencySource;
import com.scheible.pocketsaw.impl.code.PackageDependencySource;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author sj
 */
public class DependencySourceResolverTest {

	private static final PackageDependencySource PACKAGE_DEPENDENCY_SOURCE = new NopPackageDependencySource();

	@Test
	public void testNoDependecySourcesAvailable() {
		final Optional<ResolvedDependencySource> result = DependencySourceResolver.resolve("bla", new ArrayList<>());
		assertThat(result).isEmpty();
	}

	@Test
	public void testNonMatchingDependecySourceWithoutParamters() {
		final Optional<ResolvedDependencySource> result = DependencySourceResolver.resolve("bla", 
				Arrays.asList(PACKAGE_DEPENDENCY_SOURCE));
		assertThat(result).isEmpty();
	}

	@Test
	public void testNonMatchingDependecySourceWithSingleParamter() {
		final Optional<ResolvedDependencySource> result = DependencySourceResolver.resolve("bla:bla=blub", 
				Arrays.asList(PACKAGE_DEPENDENCY_SOURCE));
		assertThat(result).isEmpty();
	}

	@Test
	public void testMatchingDependecySourceWithoutParamters() {
		final Optional<ResolvedDependencySource> result = DependencySourceResolver.resolve(PACKAGE_DEPENDENCY_SOURCE.getIdentifier(),
				Arrays.asList(PACKAGE_DEPENDENCY_SOURCE));
		assertThat(result).isPresent().hasValueSatisfying(resolvedDependencySource -> 
				assertThat(resolvedDependencySource.getDependencySource()).isEqualTo(PACKAGE_DEPENDENCY_SOURCE));
		assertThat(result.get().getDependencySourceParameters()).hasSize(0);
	}

	@Test
	public void testMatchingDependecySourceWithSingleParamter() {
		final Optional<ResolvedDependencySource> result = DependencySourceResolver.resolve(PACKAGE_DEPENDENCY_SOURCE.getIdentifier() 
				+ ":bla=blub", Arrays.asList(PACKAGE_DEPENDENCY_SOURCE));
		assertThat(result).isPresent().hasValueSatisfying(resolvedDependencySource -> 
				assertThat(resolvedDependencySource.getDependencySource()).isEqualTo(PACKAGE_DEPENDENCY_SOURCE));
		assertThat(result.get().getDependencySourceParameters()).hasSize(1).containsOnly(
				new SimpleImmutableEntry<>("bla", "blub"));
	}

	@Test
	public void testMatchingDependecySourceWithMultipleParamters() {
		final Optional<ResolvedDependencySource> result = DependencySourceResolver.resolve(PACKAGE_DEPENDENCY_SOURCE.getIdentifier() 
				+ ":bla=blub:foo=bar",
				Arrays.asList(PACKAGE_DEPENDENCY_SOURCE));
		assertThat(result).isPresent().hasValueSatisfying(resolvedDependencySource -> 
				assertThat(resolvedDependencySource.getDependencySource()).isEqualTo(PACKAGE_DEPENDENCY_SOURCE));
		assertThat(result.get().getDependencySourceParameters()).hasSize(2).containsOnly(
				new SimpleImmutableEntry<>("bla", "blub"), new SimpleImmutableEntry<>("foo", "bar"));
	}
}
