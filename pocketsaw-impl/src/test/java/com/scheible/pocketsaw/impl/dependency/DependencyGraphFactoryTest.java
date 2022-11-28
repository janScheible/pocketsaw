package com.scheible.pocketsaw.impl.dependency;

import com.scheible.pocketsaw.impl.code.PackageDependencies;
import com.scheible.pocketsaw.impl.code.PackageDependency;
import com.scheible.pocketsaw.impl.code.TypeDependency;
import static com.scheible.pocketsaw.impl.dependency.Dependency.Origin.CODE;
import static com.scheible.pocketsaw.impl.dependency.Dependency.Origin.DESCRIPTOR;
import com.scheible.pocketsaw.impl.descriptor.DescriptorInfo;
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

		final DependencyGraph graph = DependencyGraphFactory.create(
				new DescriptorInfo(new HashSet<>(Arrays.asList(a, b)), new HashSet<>(), false),
				new PackageDependencies(new HashMap<>()));

		assertThat(graph.getDependencies()).hasSize(1).containsExactlyInAnyOrder(new Dependency(a, b, 0, DESCRIPTOR));
	}

	@Test
	public void codeDependencyWithNoDescriptorDependecy() {
		final SubModuleDescriptor a = new SubModuleDescriptor("a", "a", "a", false, "red", new HashSet<>(), new HashSet<>());
		final SubModuleDescriptor b = new SubModuleDescriptor("b", "b", "b", false, "red", new HashSet<>(), new HashSet<>());

		final Map<String, Set<String>> codePackageDependencies = new HashMap<>();
		codePackageDependencies.put("a", new HashSet<>(Arrays.asList("b")));

		final DependencyGraph graph = DependencyGraphFactory.create(
				new DescriptorInfo(new HashSet<>(Arrays.asList(a, b)), new HashSet<>(), false),
				new PackageDependencies(codePackageDependencies));

		assertThat(graph.getDependencies()).hasSize(1).containsExactlyInAnyOrder(new Dependency(a, b, 1, CODE));
	}

	@Test
	public void multiCodeDependenciesWithCodeDependencyCounts() {
		final Map<Map.Entry<String, String>, Integer> weightedPackageDependencies = new HashMap<>();
		weightedPackageDependencies.put(new SimpleImmutableEntry<>("a", "b"), 2);
		final PackageDependencies packageDependencies = PackageDependencies.withCodeDependencyCounts(weightedPackageDependencies);

		multiCodeDependencies(packageDependencies);
	}

	@Test
	public void multiCodeDependenciesWithClassLevelDependencies() {
		final Map<PackageDependency, Set<TypeDependency>> classLevelPackageDependencies = new HashMap<>();
		classLevelPackageDependencies.put(new PackageDependency("a", "b"), new HashSet<>(Arrays.asList(
				new TypeDependency("a", "a.First", "b", "b.Second"),
				new TypeDependency("a", "a.Third", "b", "b.Second"))));
		final Map<String, Set<String>> packageClasses = new HashMap<>();
		packageClasses.put("a", new HashSet<>(Arrays.asList("a.First", "a.Third")));
		packageClasses.put("b", new HashSet<>(Arrays.asList("b.Second")));

		final PackageDependencies packageDependencies = PackageDependencies.withClassLevelDependencies(
				classLevelPackageDependencies, packageClasses);

		multiCodeDependencies(packageDependencies);
	}

	private void multiCodeDependencies(final PackageDependencies packageDependencies) {
		final SubModuleDescriptor a = new SubModuleDescriptor("a", "a", "a", false, "red", new HashSet<>(), new HashSet<>());
		final SubModuleDescriptor b = new SubModuleDescriptor("b", "b", "b", false, "red", new HashSet<>(), new HashSet<>());

		final DependencyGraph graph = DependencyGraphFactory.create(
				new DescriptorInfo(new HashSet<>(Arrays.asList(a, b)), new HashSet<>(), false), packageDependencies);

		assertThat(graph.getDependencies()).hasSize(1).containsExactlyInAnyOrder(new Dependency(a, b, 2, CODE));
	}

	/**
	 * #20 "duplicate key exception in DependencyGraphFactory"
	 */
	@Test
	public void duplicatedDependenciesWithDifferentDependecyCountWithCodeDependencyCounts() {
		final Map<Map.Entry<String, String>, Integer> weightedPackageDependencies = new HashMap<>();
		weightedPackageDependencies.put(new SimpleImmutableEntry<>("a", "b.first"), 2);
		weightedPackageDependencies.put(new SimpleImmutableEntry<>("a", "b.second"), 1);
		final PackageDependencies packageDependencies = PackageDependencies.withCodeDependencyCounts(weightedPackageDependencies);

		duplicatedDependenciesWithDifferentDependecyCount(packageDependencies);
	}

	/**
	 * #20 "duplicate key exception in DependencyGraphFactory"
	 */
	@Test
	public void duplicatedDependenciesWithDifferentDependecyCountWithClassLevelDependencies() {
		final Map<PackageDependency, Set<TypeDependency>> classLevelPackageDependencies = new HashMap<>();
		classLevelPackageDependencies.put(new PackageDependency("a", "b"), new HashSet<>(Arrays.asList(
				new TypeDependency("a", "a.First", "b.first", "b.first.Second"),
				new TypeDependency("a", "a.Foo", "b.first", "b.first.Bar"),
				new TypeDependency("a", "a.Third", "b.second", "b.second.Second"))));
		final Map<String, Set<String>> packageClasses = new HashMap<>();
		packageClasses.put("a", new HashSet<>(Arrays.asList("a.First", "a.Third", "a.Foo")));
		packageClasses.put("b.first", new HashSet<>(Arrays.asList("b.first.Second", "b.first.Bar")));
		packageClasses.put("b.second", new HashSet<>(Arrays.asList("b.second.Second")));

		final PackageDependencies packageDependencies = PackageDependencies.withClassLevelDependencies(
				classLevelPackageDependencies, packageClasses);

		duplicatedDependenciesWithDifferentDependecyCount(packageDependencies);
	}

	private void duplicatedDependenciesWithDifferentDependecyCount(final PackageDependencies packageDependencies) {
		final SubModuleDescriptor a = new SubModuleDescriptor("a", "a", "a", false, "red", new HashSet<>(), new HashSet<>());
		final SubModuleDescriptor b = new SubModuleDescriptor("b", "b", "b", true, "red", new HashSet<>(), new HashSet<>());

		final DependencyGraph graph = DependencyGraphFactory.create(
				new DescriptorInfo(new HashSet<>(Arrays.asList(a, b)), new HashSet<>(), false), packageDependencies);

		assertThat(graph.getDependencies()).hasSize(1).containsExactlyInAnyOrder(new Dependency(a, b, 3, CODE));
	}
}
