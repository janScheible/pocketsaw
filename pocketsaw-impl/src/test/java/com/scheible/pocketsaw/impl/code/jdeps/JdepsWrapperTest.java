package com.scheible.pocketsaw.impl.code.jdeps;

import com.scheible.pocketsaw.impl.code.DependencyFilter;
import com.scheible.pocketsaw.impl.code.PackageDependecies;
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
		PackageDependecies packageDependecies = JdepsWrapper.run("./target/classes",
				new DependencyFilter(new HashSet<>(), true));
		
		Assertions.assertThat(packageDependecies.keySet()).isNotEmpty();
	}
}
