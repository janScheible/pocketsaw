package com.scheible.pocketsaw.impl.matching.auto;

import com.scheible.pocketsaw.impl.descriptor.ExternalFunctionalityDescriptor;
import com.scheible.pocketsaw.impl.descriptor.PackageGroupDescriptor;
import com.scheible.pocketsaw.impl.matching.PackageMatchable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author sj
 */
public class ExternalFunctionalityAutoMatcherTest {

	@Test
	public void testAutoMatchOnly() {
		final ExternalFunctionalityAutoMatcher<PackageGroupDescriptor> matcher
				= new ExternalFunctionalityAutoMatcher<>("com.base", Collections.emptySet());
		matcher.findMatching("com.base.first");
		matcher.findMatching("external");
		assertThat(matcher.getPackageGroups()).flatExtracting(PackageMatchable::getPackageMatchPatterns)
				.containsOnly("external.*");
	}

	@Test
	public void testMixed() {
		final ExternalFunctionalityAutoMatcher<PackageGroupDescriptor> matcher
				= new ExternalFunctionalityAutoMatcher<>("com.base",
						set(externalFunctionalityDescriptor("external", "External")));
		matcher.findMatching("com.base.first");
		matcher.findMatching("external");
		matcher.findMatching("library.main");
		assertThat(matcher.getPackageGroups()).flatExtracting(PackageMatchable::getPackageMatchPatterns)
				.containsOnly("external.*", "library.main.*");
	}

	private static <T> Set<T> set(final T... entries) {
		return Stream.of(entries).collect(Collectors.toSet());
	}

	private static ExternalFunctionalityDescriptor externalFunctionalityDescriptor(final String packageName,
			final String name) {
		return new ExternalFunctionalityDescriptor(packageName + ".ExternalFunctionalities." + name, name,
				new HashSet<>(Arrays.asList(packageName + ".*")));
	}
}
