package com.scheible.pocketsaw.dependecycruiser;

import com.scheible.pocketsaw.impl.code.PackageDependencies;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author sj
 */
public class DependencyCruiserOutputTest {

	@Test
	public void testEmptyAngular4AppDependencies() throws IOException {
		PackageDependencies packageDependencies = new DependencyCruiserOutput().read(readDependencies());

		assertThat(packageDependencies.keySet()).hasSize(1);
		assertThat(packageDependencies.get(packageDependencies.keySet().iterator().next())).containsExactly("src.app");
	}

	private static String readDependencies() throws IOException {
		try (BufferedReader buffer = new BufferedReader(new InputStreamReader(
				DependencyCruiserOutputTest.class.getResource("empty-angular4-app-dependencies.json").openStream(), "UTF8"))) {
			return buffer.lines().collect(Collectors.joining("\n"));
		}
	}
}
