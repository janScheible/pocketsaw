package com.scheible.pocketsaw.impl.code.jdeps;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 *
 * @author sj
 */
public class JdepsLocator {

	static Optional<String> locate(final String envJavaHome, final String propJavaHome, final Predicate<File> existencePredicate) {
		final BiFunction<String, String, Optional<String>> check = (baseDir, subDir) -> {
			if (baseDir != null && !baseDir.trim().isEmpty()) {
				for (final String extension : Arrays.asList("", ".exe")) {
					final File jdepsExecutable = new File(baseDir + subDir, "jdeps" + extension);
					if (existencePredicate.test(jdepsExecutable)) {
						return Optional.of(baseDir + subDir);
					}
				}
			}

			return Optional.empty();
		};

		return or(check.apply(envJavaHome, File.separator + "bin" + File.separator),
				() -> or(check.apply(propJavaHome, File.separator + ".." + File.separator + "bin" + File.separator), // JDK 8
						() -> check.apply(propJavaHome, File.separator + "bin" + File.separator))); // JDK 11
	}

	private static <T> Optional<T> or(Optional<T> a, Supplier<Optional<T>> b) {
		if (a.isPresent()) {
			return a;
		} else {
			return b.get();
		}
	}
}
