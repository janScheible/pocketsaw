package com.scheible.pocketsaw.impl.code.jdeps;

import com.scheible.pocketsaw.impl.code.DependencyFilter;
import com.scheible.pocketsaw.impl.code.PackageDependencies;
import java.util.Arrays;
import java.util.HashSet;
import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 *
 * @author sj
 */
public class JdepsWrapperTest {

	@Test
	public void testRun() {
		PackageDependencies packageDependencies = JdepsWrapper.run("./target/classes",
				new DependencyFilter(new HashSet<>(Arrays.asList(getClass().getPackage().getName())), new HashSet<>(), true));
		
		Assertions.assertThat(packageDependencies.keySet()).isNotEmpty();
	}
}
