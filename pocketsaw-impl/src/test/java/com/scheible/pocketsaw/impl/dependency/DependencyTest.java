package com.scheible.pocketsaw.impl.dependency;

import com.scheible.pocketsaw.impl.dependency.Dependency;
import static com.scheible.pocketsaw.impl.dependency.Dependency.Origin.CODE;
import static com.scheible.pocketsaw.impl.dependency.Dependency.Origin.DESCRIPTOR;
import com.scheible.pocketsaw.impl.descriptor.ExternalFunctionalityDescriptor;
import com.scheible.pocketsaw.impl.descriptor.SubModuleDescriptor;
import java.util.HashSet;
import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 *
 * @author sj
 */
public class DependencyTest {

	private final SubModuleDescriptor subModule = new SubModuleDescriptor("id", "name", "package", true, new HashSet<>(), new HashSet<>());
	private final ExternalFunctionalityDescriptor externalFunctionality = new ExternalFunctionalityDescriptor("id", "name", "package");

	@Test
	public void originEnumTest() {
		Assertions.assertThat(new Dependency(subModule, externalFunctionality, true, true).getOrigins()).contains(DESCRIPTOR, CODE);
		Assertions.assertThat(new Dependency(subModule, externalFunctionality, true, false).getOrigins()).contains(DESCRIPTOR);
		Assertions.assertThat(new Dependency(subModule, externalFunctionality, false, true).getOrigins()).contains(CODE);
	}

	@Test(expected = IllegalStateException.class)
	public void testMissingOrigin() {
		new Dependency(subModule, externalFunctionality, false, false).getOrigins();
	}
}
