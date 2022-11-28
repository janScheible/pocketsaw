package com.scheible.pocketsaw.impl.cli;

import com.scheible.pocketsaw.impl.cli.ValidationResult.ValidationError;
import com.scheible.pocketsaw.impl.cli.ValidationResult.ValidationSuccess;
import java.io.File;
import java.io.IOException;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 *
 * @author sj
 */
class ValidatedArguments implements ValidationResult, ValidationError, ValidationSuccess {

	enum ErrorReason {
		UNKNOWN_ARGUMENTS, MISSING_DEPENDENCY_SOURCE, WRONG_NUMBER_OF_ARGUMENTS, SUB_MODULES_JSON_FILE_DOES_NOT_EXIST,
		DEPENDENCIES_FILE_DOES_NOT_EXIST, DEPENDENCY_GRAPH_HTML_PARENT_FILE_DOES_NOT_EXIST
	}

	final Set<Entry<ErrorReason, String>> errorMessages;

	final Optional<File> subModulesJsonFile;
	final File dependenciesFile;
	final File dependencyGraphHtmlFile;

	private ValidatedArguments(final Set<Entry<ErrorReason, String>> errorMessages) {
		this.errorMessages = errorMessages;

		this.subModulesJsonFile = null;
		this.dependenciesFile = null;
		this.dependencyGraphHtmlFile = null;
	}

	private ValidatedArguments(final Optional<File> subModulesJsonFile, final File dependenciesFile,
			final File dependencyGraphHtmlFile) {
		this.errorMessages = null;

		this.subModulesJsonFile = subModulesJsonFile;
		this.dependenciesFile = dependenciesFile;
		this.dependencyGraphHtmlFile = dependencyGraphHtmlFile;
	}

	static ValidationResult validate(final ParsedArguments parsedArguments, final Function<File, Boolean> fileExists) {
		final Set<Entry<ErrorReason, String>> errorMessages = new HashSet<>();

		final boolean unknownArguments = !parsedArguments.remainingArgs.isEmpty();
		final boolean missingDependencySource = !parsedArguments.resolvedDependencySource.isPresent();
		final boolean wrongNumberOfArguments = !(parsedArguments.files.size() == 2 && parsedArguments.autoMatching)
				&& !(parsedArguments.files.size() == 3);

		if (unknownArguments) {
			errorMessages.add(error(ErrorReason.UNKNOWN_ARGUMENTS, "'" + parsedArguments.remainingArgs
					+ "' are unkown arguments!"));
		} else {
			if (missingDependencySource) {
				errorMessages.add(error(ErrorReason.MISSING_DEPENDENCY_SOURCE,
						"The dependency source is missing!"));
			} else {
				if (wrongNumberOfArguments) {
					errorMessages.add(error(ErrorReason.WRONG_NUMBER_OF_ARGUMENTS,
							"Wrong number of arguments!"));
				}
			}
		}

		if (!errorMessages.isEmpty()) {
			return new ValidatedArguments(errorMessages);
		}

		final Optional<String> subModulesJsonFileString = Optional.ofNullable(parsedArguments.files.size() == 2
				? null : parsedArguments.files.get(0));
		final Optional<File> subModulesJsonFile = subModulesJsonFileString.map(File::new).flatMap(f -> Optional.ofNullable(toCanonical(f)));

		final String dependenciesFileString = parsedArguments.files.get(parsedArguments.files.size() - 2);
		final File dependenciesFile = toCanonical(new File(dependenciesFileString));

		final String dependencyGraphHtmlFileString = parsedArguments.files.get(parsedArguments.files.size() - 1);
		final File dependencyGraphHtmlFile = toCanonical(new File(dependencyGraphHtmlFileString));

		if (subModulesJsonFileString.isPresent()) {
			if (!subModulesJsonFile.isPresent() || !subModulesJsonFile.map(fileExists).get()) {
				errorMessages.add(error(ErrorReason.SUB_MODULES_JSON_FILE_DOES_NOT_EXIST, "The sub modules JSON file '"
						+ subModulesJsonFileString.get() + "' does not exist!"));
			}
		}

		if (dependenciesFile == null || !fileExists.apply(dependenciesFile)) {
			errorMessages.add(error(ErrorReason.DEPENDENCIES_FILE_DOES_NOT_EXIST, "The dependencies file '"
					+ dependenciesFileString + "' does not exist!"));
		}

		if (dependencyGraphHtmlFile == null || !fileExists.apply(dependencyGraphHtmlFile.getAbsoluteFile().getParentFile())) {
			errorMessages.add(error(ErrorReason.DEPENDENCY_GRAPH_HTML_PARENT_FILE_DOES_NOT_EXIST,
					"At least one of the directories of the dependency graph HTML file '" + dependencyGraphHtmlFileString
					+ "' does not exist!"));
		}

		if (!errorMessages.isEmpty()) {
			return new ValidatedArguments(errorMessages);
		}

		return new ValidatedArguments(subModulesJsonFile, dependenciesFile, dependencyGraphHtmlFile);
	}

	private static Entry<ErrorReason, String> error(ErrorReason reason, String message) {
		return new SimpleImmutableEntry<>(reason, message);
	}

	private static File toCanonical(final File file) {
		try {
			return file.getCanonicalFile();
		} catch (final IOException ex) {
			return null;
		}
	}

	@Override
	public Set<Entry<ErrorReason, String>> getErrorMessages() {
		return errorMessages;
	}

	@Override
	public Optional<File> getSubModulesJsonFile() {
		return subModulesJsonFile;
	}

	@Override
	public File getDependenciesFile() {
		return dependenciesFile;
	}

	@Override
	public File getDependencyGraphHtmlFile() {
		return dependencyGraphHtmlFile;
	}

	@Override
	public boolean isError() {
		return errorMessages != null;
	}

	@Override
	public ValidationError asError() {
		return this;
	}

	@Override
	public boolean isSuccess() {
		return subModulesJsonFile != null && dependenciesFile != null && dependencyGraphHtmlFile != null;
	}

	@Override
	public ValidationSuccess asSuccess() {
		return this;
	}
}
