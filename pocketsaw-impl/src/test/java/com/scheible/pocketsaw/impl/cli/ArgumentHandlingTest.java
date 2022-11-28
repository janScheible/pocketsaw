package com.scheible.pocketsaw.impl.cli;

import com.scheible.pocketsaw.impl.cli.ValidatedArguments.ErrorReason;
import static com.scheible.pocketsaw.impl.cli.ValidatedArguments.ErrorReason.DEPENDENCIES_FILE_DOES_NOT_EXIST;
import static com.scheible.pocketsaw.impl.cli.ValidatedArguments.ErrorReason.DEPENDENCY_GRAPH_HTML_PARENT_FILE_DOES_NOT_EXIST;
import static com.scheible.pocketsaw.impl.cli.ValidatedArguments.ErrorReason.MISSING_DEPENDENCY_SOURCE;
import static com.scheible.pocketsaw.impl.cli.ValidatedArguments.ErrorReason.SUB_MODULES_JSON_FILE_DOES_NOT_EXIST;
import static com.scheible.pocketsaw.impl.cli.ValidatedArguments.ErrorReason.UNKNOWN_ARGUMENTS;
import static com.scheible.pocketsaw.impl.cli.ValidatedArguments.ErrorReason.WRONG_NUMBER_OF_ARGUMENTS;
import com.scheible.pocketsaw.impl.code.PackageDependencies;
import com.scheible.pocketsaw.impl.code.PackageDependencySource;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 * Integration test that tests argument parsing as well as validation.
 * 
 * @author sj
 */
public class ArgumentHandlingTest {

	@Test
	public void testArgumentValidation() {
		assertThat(validateArguments(true))
				.containsOnly(MISSING_DEPENDENCY_SOURCE);
		assertThat(validateArguments(true, "dependency-source", "sub-module.json", "dependencies.file", "graph.html"))
				.containsOnly(); // not planned but the position of the dependency source does not matter at all
		assertThat(validateArguments(true, "dependencies.file", "dependency-source", "graph.html"))
				.containsOnly(WRONG_NUMBER_OF_ARGUMENTS);
		assertThat(validateArguments(true, "--haha-not-a-parameter"))
				.containsOnly(UNKNOWN_ARGUMENTS);
		assertThat(validateArguments(true, "sub-module.json", "dependencies.file", "dependency-source", "graph.html"))
				.containsOnly();
	}

	@Test
	public void testArgumentValidationExistingFiles() {
		assertThat(validateArguments(true, "dependencies.file", "dependency-source", "graph.html", "--auto-matching"))
				.containsOnly();
		assertThat(validateArguments(true,
				"sub-module.json", "dependencies.file", "dependency-source", "graph.html", "--auto-matching"))
				.containsOnly();
	}

	@Test
	public void testArgumentValidationNotExistingFiles() {
		assertThat(validateArguments(false, "dependencies.file", "dependency-source", "graph.html", "--auto-matching"))
				.containsOnly(DEPENDENCY_GRAPH_HTML_PARENT_FILE_DOES_NOT_EXIST, DEPENDENCIES_FILE_DOES_NOT_EXIST);
		assertThat(validateArguments(false,
				"sub-module.json", "dependencies.file", "dependency-source", "graph.html", "--auto-matching"))
				.containsOnly(SUB_MODULES_JSON_FILE_DOES_NOT_EXIST, DEPENDENCY_GRAPH_HTML_PARENT_FILE_DOES_NOT_EXIST,
						DEPENDENCIES_FILE_DOES_NOT_EXIST);
	}

	private static Set<ErrorReason> validateArguments(final boolean filesExist, final String... args) {
		final ParsedArguments parsedArgs = ParsedArguments.parse(args, Arrays.asList(new TestPackageDependencySource()));
		final ValidationResult validatedArgs = ValidatedArguments.validate(parsedArgs, file -> filesExist);
		
		return validatedArgs.isError() 
				? validatedArgs.asError().getErrorMessages().stream().map(Entry::getKey).collect(Collectors.toSet())
				: Collections.emptySet();
	}

	private static class TestPackageDependencySource implements PackageDependencySource {

		@Override
		public PackageDependencies read(final File file) {
			return new PackageDependencies(Collections.emptyMap());
		}

		@Override
		public String getIdentifier() {
			return "dependency-source";
		}
	}
}
