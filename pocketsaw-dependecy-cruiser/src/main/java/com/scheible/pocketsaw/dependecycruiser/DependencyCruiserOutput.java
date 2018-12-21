package com.scheible.pocketsaw.dependecycruiser;

import com.scheible.pocketsaw.impl.code.PackageDependencies;
import com.scheible.pocketsaw.impl.code.PackageDependencySource;
import com.scheible.pocketsaw.impl.shaded.com.eclipsesource.json.Json;
import com.scheible.pocketsaw.impl.shaded.com.eclipsesource.json.JsonArray;
import com.scheible.pocketsaw.impl.shaded.com.eclipsesource.json.JsonObject;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 *
 * @author sj
 */
public class DependencyCruiserOutput implements PackageDependencySource {
	
	PackageDependencies read(final String dependencyInfo) {
		final Map<String, Set<String>> result = new HashMap<>();

		final JsonObject dependencies = Json.parse(dependencyInfo).asObject();
		final JsonArray modules = dependencies.get("modules").asArray();
		for (int i = 0; i < modules.size(); i++) {
			final JsonObject module = modules.get(i).asObject();
			final String fileName = module.get("source").asString();

			final Optional<String> packageName = fileNameToPackage(fileName);
			if (packageName.isPresent()) {
				final JsonArray moduleDependencies = module.get("dependencies").asArray();

				for (int j = 0; j < moduleDependencies.size(); j++) {
					final String dependecyFileName = moduleDependencies.get(j).asObject().get("resolved").asString();
					final Optional<String> dependencyPackageName = fileNameToPackage(dependecyFileName);

					if (dependencyPackageName.isPresent() && !packageName.equals(dependencyPackageName)) {
						result.computeIfAbsent(packageName.get(), key -> new HashSet<>())
								.add(dependencyPackageName.get());
					}
				}
			}
		}

		return new PackageDependencies(result);
	}
	
	@Override
	public PackageDependencies read(final File dependencyFile) {
		return read(readFile(dependencyFile));
	}
	
	@Override
	public String getIdentifier() {
		return "dependency-cruiser";
	}
	
	private static String readFile(File file) {
		try {
			return new String(Files.readAllBytes(file.toPath()), Charset.forName("UTF8"));
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	private static Optional<String> fileNameToPackage(final String fileName) {
		if (fileName.endsWith(".ts") && !fileName.contains(".spec")) {
			return Optional.of(fileName.substring(0, fileName.lastIndexOf("/")).replaceAll("/", "."));
		} else {
			return Optional.empty();
		}
	}
}
