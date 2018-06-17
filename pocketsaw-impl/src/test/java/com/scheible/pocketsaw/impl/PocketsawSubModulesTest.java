package com.scheible.pocketsaw.impl;

import com.scheible.pocketsaw.impl.descriptor.SpringClasspathScanner;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author sj
 */
public class PocketsawSubModulesTest {

	private static Pocketsaw.AnalysisResult result;

	@BeforeClass
	public static void beforeClass() {
		result = Pocketsaw.analizeCurrentProject(SpringClasspathScanner.create(Pocketsaw.class));
	}

	@Test
	public void testNoDescriptorCycle() {
		assertThat(result.getAnyDescriptorCycle()).isEmpty();
	}

	@Test
	public void testNoCodeCycle() {
		assertThat(result.getAnyCodeCycle()).isEmpty();
	}

	@Test
	public void testNoIllegalCodeDependencies() {
		assertThat(result.getIllegalCodeDependencies()).isEmpty();
	}
}
