package com.scheible.pocketsaw.impl.code.jdeps;

import com.scheible.pocketsaw.impl.code.DependencyFilter;
import com.scheible.pocketsaw.impl.code.PackageDependencies;
import com.scheible.pocketsaw.impl.code.PackageDependency;
import com.scheible.pocketsaw.impl.code.TypeDependency;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 *
 * @author sj
 */
public class JdepsWrapper {
	
	private static PackageDependencies parseOutput(final List<String> lines, DependencyFilter dependencyFilter) {
		final Map<PackageDependency, Set<TypeDependency>> packageDependencies = new HashMap<>();
		final Map<String, Set<String>> packageClasses = new HashMap<>();

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
			
			if (!packageName.equals(dependentPackageName) && !dependencyFilter.testDependency(className, dependentClass)) {
				final PackageDependency packageDependency = new PackageDependency(packageName, dependentPackageName);
				final TypeDependency typeDependency = new TypeDependency(packageName, className, dependentPackageName, dependentClass);
				
				packageDependencies.computeIfAbsent(packageDependency, key -> new HashSet<>()).add(typeDependency);
			}
			
			if(!dependencyFilter.testSingle(className)) {
				packageClasses.computeIfAbsent(packageName, key -> new HashSet<>()).add(className);
			}
		}
		
		return PackageDependencies.withClassLevelDependencies(packageDependencies, packageClasses);
	}
	
	public static PackageDependencies run(final String relativeClassesDirectory, DependencyFilter dependencyFilter) {
		return run(relativeClassesDirectory, Optional.empty(), Optional.empty(), dependencyFilter);
	}

	public static PackageDependencies run(final String relativeClassesDirectory, final Optional<File> workingDirectory,
			final Optional<String> classpath, final DependencyFilter dependencyFilter) {
		try {
			String jdepsDirectory = JdepsLocator.locate(System.getenv("JAVA_HOME"), System.getProperty("java.home"), 
					file -> file.exists()).orElse("");

			final List<String> command = new ArrayList<>(Arrays.asList(jdepsDirectory + "jdeps", "-v"));
			if(classpath.isPresent()) {
				command.add("-cp");
				command.add(classpath.get());
			}
			command.add(relativeClassesDirectory);
			
			final Process process = new ProcessBuilder(command)
					.redirectErrorStream(true).directory(workingDirectory.orElse(null)).start();
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
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		} catch (InterruptedException ex) {
			throw new IllegalStateException(ex);
		}
	}
}
