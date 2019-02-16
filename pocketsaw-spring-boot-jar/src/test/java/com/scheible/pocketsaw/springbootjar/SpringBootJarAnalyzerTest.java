package com.scheible.pocketsaw.springbootjar;

import com.scheible.pocketsaw.impl.code.PackageDependencies;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author sj
 */
public class SpringBootJarAnalyzerTest {

	@Test
	public void testSampleSpringBootAppWithOneLibrary() throws IOException {
		File springBootJarFile = new File(SpringBootJarAnalyzerTest.class
				.getResource("spring-boot-test-application.jar").getFile());
		PackageDependencies packageDependencies = new SpringBootJarAnalyzer().readInternal(springBootJarFile, 
				new HashSet<>(Arrays.asList("com.scheible.springbootgettingstarted.multimodule")), false, Optional.empty());

		assertThat(packageDependencies.keySet()).hasSize(1);
		assertThat(packageDependencies.get(packageDependencies.keySet().iterator().next())).containsOnly(
				"org.springframework.boot", "com.scheible.springbootgettingstarted.multimodule.library",
				"org.springframework.boot.autoconfigure", "org.springframework.context");
		assertThat(packageDependencies.getCodeDependencyCount("com.scheible.springbootgettingstarted.multimodule.application",
				"com.scheible.springbootgettingstarted.multimodule.library")).isEqualTo(1);
	}
}
