package com.scheible.pocketsaw.impl.dependency;

import com.scheible.pocketsaw.impl.descriptor.PackageGroupDescriptor;
import com.scheible.pocketsaw.impl.descriptor.SubModuleDescriptor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author sj
 */
public class CycleDetectorTest {

	@Test
	public void testBasicCycleDetection() {
		SubModuleDescriptor a = new SubModuleDescriptor("a", "a", "a", true, new HashSet<>(), new HashSet<>());
		SubModuleDescriptor b = new SubModuleDescriptor("b", "b", "b", true, new HashSet<>(), new HashSet<>());
		SubModuleDescriptor c = new SubModuleDescriptor("c", "c", "c", true, new HashSet<>(), new HashSet<>());
		SubModuleDescriptor d = new SubModuleDescriptor("d", "d", "d", true, new HashSet<>(), new HashSet<>());
		SubModuleDescriptor e = new SubModuleDescriptor("e", "e", "e", true, new HashSet<>(), new HashSet<>());

		DependencyGraph graph = new DependencyGraph(new HashSet<>(Arrays.<PackageGroupDescriptor>asList(a, b, c, d, e)), new HashSet<>(Arrays.asList(
				new Dependency(a, b, 0, Dependency.Origin.DESCRIPTOR),
				new Dependency(a, c, 0, Dependency.Origin.DESCRIPTOR),
				new Dependency(b, d, 0, Dependency.Origin.DESCRIPTOR),
				new Dependency(c, d, 0, Dependency.Origin.DESCRIPTOR),
				new Dependency(c, e, 0, Dependency.Origin.DESCRIPTOR),
				new Dependency(d, e, 0, Dependency.Origin.DESCRIPTOR),
				new Dependency(e, c, 0, Dependency.Origin.DESCRIPTOR),
				new Dependency(c, a, 0, Dependency.Origin.DESCRIPTOR)
		)), new HashMap<>());

		// NOTE It is non determenistric!
		// assertThat(CycleDetector.findAny(graph, (dependency) -> true)).hasValue(Arrays.asList(a, b, d, e, c));
		
		assertThat(CycleDetector.findAny(graph, (dependency) -> true)).isPresent();
	}
	
	/**
	 * Before the fix the incorrect cycle a -> b -> c -> a was returned. Reason was that instead of a the first vertex
	 * that is part of the cycle should be used as starting point (b in that case).
	 */
	@Test
	public void testAccessRouteBug() {
		SubModuleDescriptor a = new SubModuleDescriptor("a", "a", "a", true, new HashSet<>(), new HashSet<>());
		SubModuleDescriptor b = new SubModuleDescriptor("b", "b", "b", true, new HashSet<>(), new HashSet<>());
		SubModuleDescriptor c = new SubModuleDescriptor("c", "c", "c", true, new HashSet<>(), new HashSet<>());

		DependencyGraph graph = new DependencyGraph(new HashSet<>(Arrays.<PackageGroupDescriptor>asList(a, b, c)), new HashSet<>(Arrays.asList(
				new Dependency(a, b, 0, Dependency.Origin.DESCRIPTOR),
				new Dependency(b, c, 0, Dependency.Origin.DESCRIPTOR),
				new Dependency(c, b, 0, Dependency.Origin.DESCRIPTOR)
		)), new HashMap<>());

		Optional<List<PackageGroupDescriptor>> cycle = CycleDetector.findAny(graph, (dependency) -> true);
		assertThat(cycle).isPresent();
		assertThat(cycle.get()).containsExactly(b, c, b);
	}
}
