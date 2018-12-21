package com.scheible.pocketsaw.impl.descriptor.annotation;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author sj
 */
public class ClasspathScanningLibrariesComparisonTest {

	@Test
	public void compareScanningResult() {
		ClasspathScanner springClasspathScanner = SpringClasspathScanner.create(this.getClass());
		ClasspathScanner fastClasspathScanner = FastClasspathScanner.create(this.getClass());

		assertThat(springClasspathScanner.getSubModuleAnnotatedClassNames())
				.containsOnlyElementsOf(fastClasspathScanner.getSubModuleAnnotatedClassNames());

		assertThat(springClasspathScanner.getExternalFunctionalityAnnotatedClassNames())
				.containsOnlyElementsOf(fastClasspathScanner.getExternalFunctionalityAnnotatedClassNames());
	}
}
