package com.scheible.pocketsaw.esbuild;

import com.scheible.pocketsaw.esbuild.EsBuildMetadata.ParameterBuilder;
import com.scheible.pocketsaw.impl.code.PackageDependencies;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author sj
 */
public class EsBuildMetadataTest {

	@Test
	public void testDependencies() throws URISyntaxException, IOException {
		final PackageDependencies dependencies = new EsBuildMetadata().read(getMetaDataJson(), Collections.emptySet());

		assertThat(dependencies.keySet()).containsOnly("src");
		assertThat(dependencies.getCodeDependencyCount("src", "src.level")).isEqualTo(1);
	}

	@Test
	public void testDependenciesWothRootPackageAlias() throws URISyntaxException, IOException {
		final PackageDependencies dependencies = new EsBuildMetadata().read(getMetaDataJson(),
				ParameterBuilder.rootPackageAlias(new HashSet<>(), "frontend"));

		assertThat(dependencies.keySet()).containsOnly("frontend");
		assertThat(dependencies.getCodeDependencyCount("frontend", "frontend.level")).isEqualTo(1);
	}

	private static String getMetaDataJson() throws URISyntaxException, IOException {
		return new String(Files.readAllBytes(Paths.get(Thread.currentThread().getContextClassLoader()
				.getResource(EsBuildMetadataTest.class.getPackage().getName().replace(".", "/")
						+ "/esbuild-metadata.json").toURI())), StandardCharsets.UTF_8);
	}
}
