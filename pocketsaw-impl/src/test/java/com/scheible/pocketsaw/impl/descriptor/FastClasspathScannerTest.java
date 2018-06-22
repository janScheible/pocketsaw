package com.scheible.pocketsaw.impl.descriptor;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author sj
 */
public class FastClasspathScannerTest {

	@Test
	public void testNoAnnotatedClassesFound() {
		assertThat(FastClasspathScanner.create(Object.class).getSubModuleAnnotatedClassNames()).isEmpty();
		assertThat(FastClasspathScanner.create(Object.class).getExternalFunctionalityAnnotatedClassNames()).isEmpty();
	}
}
