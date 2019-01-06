package com.scheible.pocketsaw.impl.dependency;

import com.scheible.pocketsaw.impl.code.PackageDependencies;
import static com.scheible.pocketsaw.impl.dependency.Dependency.Origin.CODE;
import static com.scheible.pocketsaw.impl.dependency.Dependency.Origin.DESCRIPTOR;
import com.scheible.pocketsaw.impl.descriptor.SubModuleDescriptor;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author sj
 */
public class DependencyGraphFactoryTest {

	@Test
	public void descriptorDependecyWithNoCodeDependency() {
		final SubModuleDescriptor a = new SubModuleDescriptor("a", "a", "a", false, "red", new HashSet<>(Arrays.asList("b")),
				new HashSet<>());
		final SubModuleDescriptor b = new SubModuleDescriptor("b", "b", "b", false, "red", new HashSet<>(), new HashSet<>());

		final DependencyGraph graph = DependencyGraphFactory.create(new PackageDependencies(new HashMap<>()),
				new HashSet<>(Arrays.asList(a, b)), new HashSet<>());

		assertThat(graph.getDependencies()).hasSize(1).containsExactlyInAnyOrder(new Dependency(a, b, 0, DESCRIPTOR));
	}

	@Test
	public void codeDependencyWithNoDescriptorDependecy() {
		final SubModuleDescriptor a = new SubModuleDescriptor("a", "a", "a", false, "red", new HashSet<>(), new HashSet<>());
		final SubModuleDescriptor b = new SubModuleDescriptor("b", "b", "b", false, "red", new HashSet<>(), new HashSet<>());

		final Map<String, Set<String>> codePackageDependencies = new HashMap<>();
		codePackageDependencies.put("a", new HashSet<>(Arrays.asList("b")));

		final DependencyGraph graph = DependencyGraphFactory.create(new PackageDependencies(codePackageDependencies),
				new HashSet<>(Arrays.asList(a, b)), new HashSet<>());

		assertThat(graph.getDependencies()).hasSize(1).containsExactlyInAnyOrder(new Dependency(a, b, 1, CODE));
	}

	@Test
	public void multiCodeDependencies() {
		final SubModuleDescriptor a = new SubModuleDescriptor("a", "a", "a", false, "red", new HashSet<>(), new HashSet<>());
		final SubModuleDescriptor b = new SubModuleDescriptor("b", "b", "b", false, "red", new HashSet<>(), new HashSet<>());

		final Map<Map.Entry<String, String>, Integer> weightedPackageDependencies = new HashMap<>();
		weightedPackageDependencies.put(new SimpleImmutableEntry<>("a", "b"), 2);
		final PackageDependencies packageDependencies = PackageDependencies.withCodeDependencyCounts(weightedPackageDependencies);

		final DependencyGraph graph = DependencyGraphFactory.create(packageDependencies, new HashSet<>(Arrays.asList(a, b)), new HashSet<>());

		assertThat(graph.getDependencies()).hasSize(1).containsExactlyInAnyOrder(new Dependency(a, b, 2, CODE));
	}
}
