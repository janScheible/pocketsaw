package com.scheible.pocketsaw.impl.code.jdeps;

import com.scheible.pocketsaw.impl.code.DependencyFilter;
import com.scheible.pocketsaw.impl.code.PackageDependecies;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 *
 * @author sj
 */
public class JdepsWrapper {

	private static PackageDependecies parseOutput(final List<String> lines, DependencyFilter dependencyFilter) {
		Map<String, Set<String>> packageDependecies = new HashMap<>();

		final Function<String, String> packageNameExtractor = className -> {
			return className.substring(0, className.lastIndexOf('.'));
		};

		for (final String line : lines) {
			if (!line.startsWith(" ")) { // ignore
				continue;
			}

			final String[] lineParts = line.trim().split("->");
			final String className = lineParts[0].trim();
			final String packageName = packageNameExtractor.apply(className);
			final String dependentClass = lineParts[1].trim().split(" ")[0];
			final String dependentPackageName = packageNameExtractor.apply(dependentClass);

			Set<String> dependentClasses = packageDependecies.computeIfAbsent(packageName, key -> new HashSet<>());
			if (!packageName.equals(dependentPackageName) && !dependencyFilter.apply(className, dependentClass)) {
				dependentClasses.add(dependentPackageName);
			}
		}

		return new PackageDependecies(packageDependecies);
	}

	public static PackageDependecies run(final String relativeClassesDirectory, DependencyFilter dependencyFilter) {
		try {
			final Predicate<String> isNotEmpty = str -> str != null && !str.trim().isEmpty();

			final String envJavaHome = System.getenv("JAVA_HOME");
			final String propJavaHome = System.getProperty("java.home");

			final String jdepsDirectory = isNotEmpty.test(envJavaHome) ? envJavaHome + File.separator + "bin" + File.separator
					: isNotEmpty.test(propJavaHome) ? propJavaHome + File.separator + ".." + File.separator + "bin" + File.separator
					: "";

			final Process process = new ProcessBuilder(jdepsDirectory + "jdeps", "-v", relativeClassesDirectory)
					.redirectErrorStream(true).directory(null).start();
			List<String> lines = new ArrayList<>();

			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF8"))) {
				String line;
				while ((line = reader.readLine()) != null) {
					if (line.contains("Invalid classname or pathname not exist")) {
						throw new IllegalStateException(line);
					} else {
						lines.add(line);
					}
				}
			} catch (Exception ex) {
				throw new IllegalStateException(ex);
			}

			int jdepsResult = process.waitFor();
			if (jdepsResult != 0) {
				throw new IllegalStateException("jdeps exit code was " + jdepsResult);
			}

			return parseOutput(lines, dependencyFilter);
		} catch (IOException | InterruptedException ex) {
			throw new IllegalStateException(ex);
		}
	}
}
