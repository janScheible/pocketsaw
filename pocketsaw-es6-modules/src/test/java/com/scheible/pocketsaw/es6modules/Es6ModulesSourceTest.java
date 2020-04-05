package com.scheible.pocketsaw.es6modules;

import com.scheible.pocketsaw.es6modules.Es6ModulesSource.JavaScriptFile;
import com.scheible.pocketsaw.impl.code.PackageDependencies;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author sj
 */
public class Es6ModulesSourceTest {

	@Test
	public void testDependencies() {
		final String basePath = Es6ModulesSourceTest.class.getPackage().getName().replaceAll(Pattern.quote("."), "/");

		try (ScanResult scanResult = new ClassGraph().whitelistPaths(basePath).scan()) {
			final Function<Path, Stream<JavaScriptFile>> javaScriptFileWalker
					= p -> scanResult.getResourcesWithExtension("js").stream()
							.map(r -> {
								try {
									final String[] pathParts = r.getPath().split(Pattern.quote("/"));
									return new JavaScriptFile(Paths.get("c:\\", pathParts), r.getContentAsString());
								} catch (IOException ex) {
									throw new UncheckedIOException(ex);
								}
							});
			
			final Es6ModulesSource es6ModulesSource = new Es6ModulesSource(javaScriptFileWalker);
			final PackageDependencies dependencies = es6ModulesSource.read(new File("c:\\", basePath));

			assertThat(dependencies.keySet()).hasSize(3);
			assertThat(dependencies.get("es6modules")).containsOnly("es6modules.first");
			assertThat(dependencies.get("es6modules.first")).containsOnly("es6modules.second");
			assertThat(dependencies.get("es6modules.second")).containsOnly("es6modules.first");
		}
	}
}
