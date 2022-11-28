package com.scheible.pocketsaw.impl.code;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;

/**
 *
 * @author sj
 */
public class PackageDependenciesTest {

	@Test
	public void testPackageDependencies() {
		final PackageDependencies packageDependencies = new PackageDependencies(getTestPackageDependencies());
		
		assertThat(packageDependencies.getCodeDependencyCount("a", "b")).isEqualTo(1);
		assertThatThrownBy(() -> packageDependencies.getCodeDependencyCount("a", "c"))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("No dependendy between package").hasMessageEndingWith(" exists!");
	}

	private Map<String, Set<String>> getTestPackageDependencies() {
		final Map<String, Set<String>> result = new HashMap<>();
		result.put("a", new HashSet<>(Arrays.asList("b")));
		return result;
	}

	@Test
	public void testGetBasePackage() {
		assertThat(PackageDependencies.getBasePackage(Collections.emptyList())).isEqualTo("");
		assertThat(PackageDependencies.getBasePackage(Arrays.asList(""))).isEqualTo("");
		assertThat(PackageDependencies.getBasePackage(Arrays.asList("com.test", ""))).isEqualTo("");
		assertThat(PackageDependencies.getBasePackage(Arrays.asList("com.test"))).isEqualTo("com.test");
		assertThat(PackageDependencies.getBasePackage(Arrays.asList("com.test", "com.foo"))).isEqualTo("com");
	}
}
