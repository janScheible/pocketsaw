package com.scheible.pocketsaw.impl.code;

import static java.util.Arrays.asList;
import java.util.HashSet;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author sj
 */
public class DependencyFilterTest {

	public DependencyFilterTest() {
	}

	@Test
	public void testIgnoreCoreJavaClasses() {
		assertThat(new DependencyFilter(new HashSet<>(), true).apply("com.hey.Ho", "java.test.Bl")).isTrue();
		assertThat(new DependencyFilter(new HashSet<>(), true).apply("com.hey.Ho", "javax.test.Bla")).isTrue();

		assertThat(new DependencyFilter(new HashSet<>(), true).apply("com.hey.Ho", "org.Test")).isFalse();
	}

	@Test
	public void testNotIgnoreCoreJavaClasses() {
		assertThat(new DependencyFilter(new HashSet<>(), false).apply("com.hey.Ho", "java.test.Bl")).isFalse();
		assertThat(new DependencyFilter(new HashSet<>(), false).apply("com.hey.Ho", "javax.test.Bla")).isFalse();

		assertThat(new DependencyFilter(new HashSet<>(), false).apply("com.hey.Ho", "org.Test")).isFalse();
	}

	@Test
	public void testIgnoredClasses() {
		assertThat(new DependencyFilter(new HashSet<>(asList("com.hey.Ho")), false).apply("com.hey.Ho", "java.test.Bl")).isTrue();
		assertThat(new DependencyFilter(new HashSet<>(asList("com.hey.Ho")), false).apply("java.test.Bl", "com.hey.Ho")).isTrue();

		assertThat(new DependencyFilter(new HashSet<>(asList("com.hey.Ho")), false).apply("java.test.Bl", "info.do.That")).isFalse();
	}
}
