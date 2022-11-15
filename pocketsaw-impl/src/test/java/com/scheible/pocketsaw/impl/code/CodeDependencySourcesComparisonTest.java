package com.scheible.pocketsaw.impl.code;

import com.scheible.pocketsaw.api.ExternalFunctionality;
import com.scheible.pocketsaw.api.SubModule;
import com.scheible.pocketsaw.impl.Pocketsaw;
import com.scheible.pocketsaw.impl.code.jdeps.JdepsWrapper;
import com.scheible.pocketsaw.impl.descriptor.annotation.ClassgraphClasspathScanner;
import com.scheible.pocketsaw.impl.descriptor.annotation.DependencyAwareClasspathScanner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import static java.util.function.Function.identity;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author sj
 */
public class CodeDependencySourcesComparisonTest {

	@Test
	public void comparePocketsawImplDependencies() {
		final DependencyAwareClasspathScanner classpathScanner = ClassgraphClasspathScanner.create(Pocketsaw.class);
		final PackageDependencies classgraphDependencies = classpathScanner.enableDependencyScan().getDependencies();

		final DependencyFilter dependencyFilter = new DependencyFilter(
				new HashSet<>(Arrays.asList(Pocketsaw.class.getPackage().getName())), Stream.of(
						classpathScanner.getSubModuleAnnotatedClassNames().stream(),
						classpathScanner.getExternalFunctionalityAnnotatedClassNames().stream(),
						Arrays.asList(SubModule.class.getName(), ExternalFunctionality.class.getName()).stream()
				).flatMap(identity()).collect(Collectors.toSet()), true);
		final PackageDependencies jdepsDependencies = JdepsWrapper.run("./target/classes", dependencyFilter);

		assertThat(toString(jdepsDependencies)).isEqualTo(toString(classgraphDependencies));
	}

	private String toString(final PackageDependencies deps) {
		final StringBuilder result = new StringBuilder();

		final List<String> sources = new ArrayList<>(deps.keySet());
		Collections.sort(sources);

		for (final String source : sources) {
			final List<String> targets = new ArrayList<>(deps.get(source));
			Collections.sort(targets);

			for (final String target : targets) {
				result.append(source).append(" --> ").append(target).append("\n");
			}
		}

		return result.toString();
	}
}
