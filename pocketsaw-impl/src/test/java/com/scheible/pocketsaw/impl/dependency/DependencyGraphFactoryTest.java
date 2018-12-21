package com.scheible.pocketsaw.impl.dependency;

import com.scheible.pocketsaw.impl.code.PackageDependencies;
import static com.scheible.pocketsaw.impl.dependency.Dependency.Origin.CODE;
import static com.scheible.pocketsaw.impl.dependency.Dependency.Origin.DESCRIPTOR;
import com.scheible.pocketsaw.impl.descriptor.SubModuleDescriptor;
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

	public DependencyGraphFactoryTest() {
	}

	@Test
	public void descriptorDependecyWithNoCodeDependency() {
		SubModuleDescriptor a = new SubModuleDescriptor("a", "a", "a", false, "red", new HashSet<>(Arrays.asList("b")), new HashSet<>());
		SubModuleDescriptor b = new SubModuleDescriptor("b", "b", "b", false, "red", new HashSet<>(), new HashSet<>());

		DependencyGraph graph = DependencyGraphFactory.create(new PackageDependencies(new HashMap<>()), new HashSet<>(Arrays.asList(a, b)), new HashSet<>());

		assertThat(graph.getDependencies()).hasSize(1).containsExactlyInAnyOrder(new Dependency(a, b, DESCRIPTOR));
	}

	@Test
	public void codeDependencyWithNoDescriptorDependecy() {
		SubModuleDescriptor a = new SubModuleDescriptor("a", "a", "a", false, "red", new HashSet<>(), new HashSet<>());
		SubModuleDescriptor b = new SubModuleDescriptor("b", "b", "b", false, "red", new HashSet<>(), new HashSet<>());

		Map<String, Set<String>> codePackageDependencies = new HashMap<>();
		codePackageDependencies.put("a", new HashSet<>(Arrays.asList("b")));

		DependencyGraph graph = DependencyGraphFactory.create(new PackageDependencies(codePackageDependencies), new HashSet<>(Arrays.asList(a, b)), new HashSet<>());

		assertThat(graph.getDependencies()).hasSize(1).containsExactlyInAnyOrder(new Dependency(a, b, CODE));
	}
}
