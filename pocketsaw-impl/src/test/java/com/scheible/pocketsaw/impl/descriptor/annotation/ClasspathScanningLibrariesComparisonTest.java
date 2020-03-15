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
		ClasspathScanner classgraphClasspathScanner = ClassgraphClasspathScanner.create(this.getClass());

		assertThat(springClasspathScanner.getSubModuleAnnotatedClassNames())
				.containsOnlyElementsOf(fastClasspathScanner.getSubModuleAnnotatedClassNames());
		assertThat(fastClasspathScanner.getSubModuleAnnotatedClassNames())
				.containsOnlyElementsOf(classgraphClasspathScanner.getSubModuleAnnotatedClassNames());
		
		assertThat(springClasspathScanner.getExternalFunctionalityAnnotatedClassNames())
				.containsOnlyElementsOf(fastClasspathScanner.getExternalFunctionalityAnnotatedClassNames());
		assertThat(fastClasspathScanner.getExternalFunctionalityAnnotatedClassNames())
				.containsOnlyElementsOf(classgraphClasspathScanner.getExternalFunctionalityAnnotatedClassNames());
	}
}
