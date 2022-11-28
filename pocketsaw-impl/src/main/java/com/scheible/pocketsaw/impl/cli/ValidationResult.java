package com.scheible.pocketsaw.impl.cli;

import java.io.File;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

/**
 *
 * @author sj
 */
interface ValidationResult {

	interface ValidationError extends ValidationResult {

		Set<Entry<ValidatedArguments.ErrorReason, String>> getErrorMessages();
	}

	interface ValidationSuccess extends ValidationResult {

		Optional<File> getSubModulesJsonFile();
		File getDependenciesFile();
		File getDependencyGraphHtmlFile();
	}

	boolean isError();
	ValidationError asError();

	boolean isSuccess();
	ValidationSuccess asSuccess();
}
