package com.scheible.pocketsaw.impl.cli;

import com.scheible.pocketsaw.impl.cli.DependencySourceResolver.ResolvedDependencySource;
import com.scheible.pocketsaw.impl.code.NopPackageDependencySource;
import com.scheible.pocketsaw.impl.code.PackageDependencySource;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Arrays;
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
		final ResolvedDependencySource result = DependencySourceResolver.resolve("bla", new ArrayList<>());
		assertThat(result.getDependencySource()).isEmpty();
	}

	@Test
	public void testNonMatchingDependecySourceWithoutParamters() {
		final ResolvedDependencySource result = DependencySourceResolver.resolve("bla", 
				Arrays.asList(PACKAGE_DEPENDENCY_SOURCE));
		assertThat(result.getDependencySource()).isEmpty();
	}

	@Test
	public void testNonMatchingDependecySourceWithSingleParamter() {
		final ResolvedDependencySource result = DependencySourceResolver.resolve("bla:bla=blub", 
				Arrays.asList(PACKAGE_DEPENDENCY_SOURCE));
		assertThat(result.getDependencySource()).isEmpty();
	}

	@Test
	public void testMatchingDependecySourceWithoutParamters() {
		final ResolvedDependencySource result = DependencySourceResolver.resolve(PACKAGE_DEPENDENCY_SOURCE.getIdentifier(),
				Arrays.asList(PACKAGE_DEPENDENCY_SOURCE));
		assertThat(result.getDependencySource()).isPresent().hasValue(PACKAGE_DEPENDENCY_SOURCE);
		assertThat(result.getDependencySourceParameters()).hasSize(0);
	}
	
	@Test
	public void testMatchingDependecySourceWithSingleParamter() {
		final ResolvedDependencySource result = DependencySourceResolver.resolve(PACKAGE_DEPENDENCY_SOURCE.getIdentifier() 
				+ ":bla=blub", Arrays.asList(PACKAGE_DEPENDENCY_SOURCE));
		assertThat(result.getDependencySource()).isPresent().hasValue(PACKAGE_DEPENDENCY_SOURCE);
		assertThat(result.getDependencySourceParameters()).hasSize(1).containsOnly(
				new SimpleImmutableEntry<>("bla", "blub"));
	}
	

	@Test
	public void testMatchingDependecySourceWithMultipleParamters() {
		final ResolvedDependencySource result = DependencySourceResolver.resolve(PACKAGE_DEPENDENCY_SOURCE.getIdentifier() 
				+ ":bla=blub:foo=bar",
				Arrays.asList(PACKAGE_DEPENDENCY_SOURCE));
		assertThat(result.getDependencySource()).isPresent().hasValue(PACKAGE_DEPENDENCY_SOURCE);
		assertThat(result.getDependencySourceParameters()).hasSize(2).containsOnly(
				new SimpleImmutableEntry<>("bla", "blub"), new SimpleImmutableEntry<>("foo", "bar"));
	}
}
