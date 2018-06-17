package com.scheible.pocketsaw.impl;

import com.scheible.pocketsaw.impl.descriptor.ClasspathScanner;
import com.scheible.pocketsaw.impl.descriptor.FastClasspathScanner;
import com.scheible.pocketsaw.impl.descriptor.SpringClasspathScanner;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author sj
 */
public class ClasspathScanningLibrariesTest {

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
