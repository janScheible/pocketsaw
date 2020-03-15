package com.scheible.pocketsaw.impl.code.classgraph;

import com.scheible.pocketsaw.impl.code.PackageDependencies;
import com.scheible.pocketsaw.impl.code.classgraph.testclass.MyInterface;
import com.scheible.pocketsaw.impl.code.classgraph.testclass.a.A;
import com.scheible.pocketsaw.impl.code.classgraph.testclass.bc.C;
import com.scheible.pocketsaw.impl.descriptor.annotation.ClassgraphClasspathScanner;
import com.scheible.pocketsaw.impl.descriptor.annotation.DependencyAwareClasspathScanner;
import java.io.IOException;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author sj
 */
public class ClassgraphPackageDependenciesMapperTest {

	@Test
	public void testClasses() throws IOException {
		final DependencyAwareClasspathScanner classpathScanner 
				= ClassgraphClasspathScanner.create(MyInterface.class);
		((ClassgraphClasspathScanner)classpathScanner).includeTestDependencies().enableDependencyScan();
		final PackageDependencies deps = (classpathScanner).getDependencies();

		assertThat(deps.getCodeDependencyCount(C.class.getPackage().getName(), A.class.getPackage().getName())).isEqualTo(2);
		assertThat(deps.getAllTypes(A.class.getPackage().getName()).size()).isEqualTo(1);
		assertThat(deps.getAllTypes(C.class.getPackage().getName()).size()).isEqualTo(2);
	}
}
