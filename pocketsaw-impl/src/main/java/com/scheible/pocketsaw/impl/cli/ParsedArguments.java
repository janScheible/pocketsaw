package com.scheible.pocketsaw.impl.cli;

import com.scheible.pocketsaw.impl.cli.DependencySourceResolver.ResolvedDependencySource;
import com.scheible.pocketsaw.impl.code.PackageDependencySource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author sj
 */
class ParsedArguments {

	final boolean ignoreIllegalCodeDependencies;
	final boolean autoMatching;
	final boolean verbose;

	final Optional<ResolvedDependencySource> resolvedDependencySource;

	final List<String> files;

	final List<String> remainingArgs;

	private ParsedArguments(final boolean ignoreIllegalCodeDependencies, final boolean autoMatching,
			final boolean verbose, final Optional<ResolvedDependencySource> resolvedDependencySource, 
			final List<String> files, final List<String> remainingArgs) {
		this.ignoreIllegalCodeDependencies = ignoreIllegalCodeDependencies;
		this.autoMatching = autoMatching;
		this.verbose = verbose;

		this.resolvedDependencySource = resolvedDependencySource;

		this.files = files;

		this.remainingArgs = remainingArgs;
	}

	static ParsedArguments parse(final String[] argsArray, final List<PackageDependencySource> packageDependencySources) {
		final List<String> args = new ArrayList<>(Arrays.asList(argsArray));

		boolean ignoreIllegalCodeDependencies = false;
		boolean autoMatching = false;
		boolean verbose = false;

		Optional<ResolvedDependencySource> dependencySource = Optional.empty();

		final List<String> files = new ArrayList<>();

		for (int i = args.size() - 1; i >= 0; i--) {
			final String arg = args.get(i);

			boolean foundParameter = false;
			if ("--ignore-illegal-code-dependencies".equals(arg.trim().toLowerCase())) {
				ignoreIllegalCodeDependencies = true;
				foundParameter = true;
			} else if ("--verbose".equals(arg.trim().toLowerCase())) {
				verbose = true;
				foundParameter = true;
			} else if ("--auto-matching".equals(arg.trim().toLowerCase())) {
				autoMatching = true;
				foundParameter = true;
			}

			if (foundParameter) {
				args.remove(i);
				continue;
			}

			final Optional<ResolvedDependencySource> argAsDependencySource 
					= DependencySourceResolver.resolve(arg, packageDependencySources);
			if (argAsDependencySource.isPresent()) {
				args.remove(i);
				dependencySource = argAsDependencySource;
				continue;
			}

			if (!arg.startsWith("--")) {
				files.add(0, arg);
				args.remove(i);
			}
		}

		return new ParsedArguments(ignoreIllegalCodeDependencies, autoMatching, verbose, dependencySource, files, args);
	}
}
