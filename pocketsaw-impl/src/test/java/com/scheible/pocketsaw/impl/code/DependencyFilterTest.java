package com.scheible.pocketsaw.impl.code;

import static java.util.Arrays.asList;
import java.util.HashSet;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author sj
 */
public class DependencyFilterTest {

	private static final Set<String> ALLOW_ALL_EMPTY_BASE_PACKAGE = new HashSet<>(asList(""));

	@Test
	public void testIgnoreCoreJavaClasses() {
		assertThat(new DependencyFilter(ALLOW_ALL_EMPTY_BASE_PACKAGE, new HashSet<>(), true)
				.testDependency("com.hey.Ho", "java.test.Bl")).isTrue();
		assertThat(new DependencyFilter(ALLOW_ALL_EMPTY_BASE_PACKAGE, new HashSet<>(), true)
				.testDependency("com.hey.Ho", "javax.test.Bla")).isTrue();

		assertThat(new DependencyFilter(ALLOW_ALL_EMPTY_BASE_PACKAGE, new HashSet<>(), true)
				.testDependency("com.hey.Ho", "org.Test")).isFalse();
	}

	@Test
	public void testNotIgnoreCoreJavaClasses() {
		assertThat(new DependencyFilter(ALLOW_ALL_EMPTY_BASE_PACKAGE, new HashSet<>(), false)
				.testDependency("com.hey.Ho", "java.test.Bl")).isFalse();
		assertThat(new DependencyFilter(ALLOW_ALL_EMPTY_BASE_PACKAGE, new HashSet<>(), false)
				.testDependency("com.hey.Ho", "javax.test.Bla")).isFalse();

		assertThat(new DependencyFilter(ALLOW_ALL_EMPTY_BASE_PACKAGE, new HashSet<>(), false)
				.testDependency("com.hey.Ho", "org.Test")).isFalse();
	}

	@Test
	public void testIgnoredClasses() {
		assertThat(new DependencyFilter(ALLOW_ALL_EMPTY_BASE_PACKAGE, new HashSet<>(asList("com.hey.Ho")), false)
				.testDependency("com.hey.Ho", "java.test.Bl")).isTrue();
		assertThat(new DependencyFilter(ALLOW_ALL_EMPTY_BASE_PACKAGE, new HashSet<>(asList("com.hey.Ho")), false)
				.testDependency("java.test.Bl", "com.hey.Ho")).isTrue();

		assertThat(new DependencyFilter(ALLOW_ALL_EMPTY_BASE_PACKAGE, new HashSet<>(asList("com.hey.Ho")), false)
				.testDependency("java.test.Bl", "info.do.That")).isFalse();
	}

	@Test
	public void testFilterSingleClass() {
		assertThat(new DependencyFilter(ALLOW_ALL_EMPTY_BASE_PACKAGE, new HashSet<>(asList("com.hey.Ho")), false)
				.testSingle("com.hey.Ho")).isTrue();
		assertThat(new DependencyFilter(ALLOW_ALL_EMPTY_BASE_PACKAGE, new HashSet<>(asList("com.hey.Ho")), false)
				.testSingle("com.hey.Hi")).isFalse();

		assertThat(new DependencyFilter(ALLOW_ALL_EMPTY_BASE_PACKAGE, new HashSet<>(asList("com.hey.Ho")), true)
				.testSingle("java.test.Bl")).isTrue();
		assertThat(new DependencyFilter(ALLOW_ALL_EMPTY_BASE_PACKAGE, new HashSet<>(asList("com.hey.Ho")), true)
				.testSingle("com.hey.Hi")).isFalse();
	}

	@Test
	public void testBasePackageFilter() {
		DependencyFilter filter = new DependencyFilter(new HashSet<>(asList("com.hey", "foo.bar")), new HashSet<>(), false);

		assertThat(filter.testSingle("com.hey.Ho")).isFalse();
		assertThat(filter.testSingle("com.foo.Ho")).isFalse();

		assertThat(filter.testDependency("com.hey.Hi", "com.hey.Ho")).isFalse();
		assertThat(filter.testDependency("foo.bar.Hi", "com.hey.Ho")).isFalse();
		assertThat(filter.testDependency("com.foo.Hi", "com.hey.Ho")).isTrue();
		assertThat(filter.testDependency("com.foo.Hi", "foo.bar.Ho")).isTrue();
		assertThat(filter.testDependency("com.hey.Hi", "com.foo.Ho")).isFalse();
		assertThat(filter.testDependency("foo.bar.Hi", "com.foo.Ho")).isFalse();
	}
}
