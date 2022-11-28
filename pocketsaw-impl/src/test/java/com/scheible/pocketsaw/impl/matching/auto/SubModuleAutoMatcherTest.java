package com.scheible.pocketsaw.impl.matching.auto;

import com.scheible.pocketsaw.impl.descriptor.PackageGroupDescriptor;
import com.scheible.pocketsaw.impl.descriptor.SubModuleDescriptor;
import com.scheible.pocketsaw.impl.matching.PackageMatchable;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author sj
 */
public class SubModuleAutoMatcherTest {

	@Test
	public void testAutoMatchOnly() {
		final SubModuleAutoMatcher<PackageGroupDescriptor> matcher
				= new SubModuleAutoMatcher<>("com.base", Collections.emptySet());
		matcher.findMatching("com.base.first");
		matcher.findMatching("external");
		assertThat(matcher.getPackageGroups()).flatExtracting(PackageMatchable::getPackageMatchPatterns)
				.containsOnly("com.base.first.*");
	}

	@Test
	public void testMixed() {
		final SubModuleAutoMatcher<PackageGroupDescriptor> matcher
				= new SubModuleAutoMatcher<>("com.base", set(subModuleDescriptor("com.base.first", "First")));
		matcher.findMatching("com.base.first");
		matcher.findMatching("com.base.second");
		matcher.findMatching("external");
		assertThat(matcher.getPackageGroups()).flatExtracting(PackageMatchable::getPackageMatchPatterns)
				.containsOnly("com.base.first.*", "com.base.second.*");
	}

	private static <T> Set<T> set(final T... entries) {
		return Stream.of(entries).collect(Collectors.toSet());
	}

	private static SubModuleDescriptor subModuleDescriptor(final String packageName, final String name) {
		return new SubModuleDescriptor(packageName + "." + name + "SubModule", name, packageName,
				false, Collections.emptySet(), Collections.emptySet());
	}
}
