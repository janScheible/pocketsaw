package com.scheible.pocketsaw.impl.descriptor;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author sj
 */
public class SpringClasspathScannerTest {

	@Test
	public void testNoAnnotatedClassesFound() {
		assertThat(SpringClasspathScanner.create(Object.class).getSubModuleAnnotatedClassNames()).isEmpty();
		assertThat(SpringClasspathScanner.create(Object.class).getExternalFunctionalityAnnotatedClassNames()).isEmpty();
	}
}
