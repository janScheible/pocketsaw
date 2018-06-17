package com.scheible.pocketsaw.impl.code.jdeps;

import com.scheible.pocketsaw.impl.code.jdeps.ApiAnnotationDependencyFilter;
import com.scheible.pocketsaw.impl.code.jdeps.JdepsWrapper;
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
				new ApiAnnotationDependencyFilter(new HashSet<>()));
		
		Assertions.assertThat(packageDependecies.keySet()).isNotEmpty();
	}
}
