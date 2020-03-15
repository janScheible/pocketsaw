package com.scheible.pocketsaw.impl.descriptor.annotation;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author sj
 */
public class ClassgraphClasspathScannerTest {

	@Test
	public void testNoAnnotatedClassesFound() {
		assertThat(ClassgraphClasspathScanner.create(Object.class).getSubModuleAnnotatedClassNames()).isEmpty();
		assertThat(ClassgraphClasspathScanner.create(Object.class).getExternalFunctionalityAnnotatedClassNames()).isEmpty();
	}
}
