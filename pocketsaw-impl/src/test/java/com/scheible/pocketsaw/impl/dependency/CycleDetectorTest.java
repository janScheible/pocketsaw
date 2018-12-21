package com.scheible.pocketsaw.impl.dependency;

import com.scheible.pocketsaw.impl.dependency.Dependency;
import com.scheible.pocketsaw.impl.dependency.CycleDetector;
import com.scheible.pocketsaw.impl.dependency.DependencyGraph;
import com.scheible.pocketsaw.impl.descriptor.PackageGroupDescriptor;
import com.scheible.pocketsaw.impl.descriptor.SubModuleDescriptor;
import java.util.Arrays;
import java.util.HashSet;
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
				new Dependency(a, b, Dependency.Origin.DESCRIPTOR),
				new Dependency(a, c, Dependency.Origin.DESCRIPTOR),
				new Dependency(b, d, Dependency.Origin.DESCRIPTOR),
				new Dependency(c, d, Dependency.Origin.DESCRIPTOR),
				new Dependency(c, e, Dependency.Origin.DESCRIPTOR),
				new Dependency(d, e, Dependency.Origin.DESCRIPTOR),
				new Dependency(e, c, Dependency.Origin.DESCRIPTOR),
				new Dependency(c, a, Dependency.Origin.DESCRIPTOR)
		)));

		// NOTE It is non determenistric!
		// assertThat(CycleDetector.findAny(graph, (dependency) -> true)).hasValue(Arrays.asList(a, b, d, e, c));
		
		assertThat(CycleDetector.findAny(graph, (dependency) -> true)).isPresent();
	}
}
