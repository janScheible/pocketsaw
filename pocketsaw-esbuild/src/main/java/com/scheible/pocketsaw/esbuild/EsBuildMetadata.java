package com.scheible.pocketsaw.esbuild;

import com.scheible.pocketsaw.impl.code.PackageDependencies;
import com.scheible.pocketsaw.impl.code.PackageDependencySource;
import com.scheible.pocketsaw.impl.shaded.com.eclipsesource.json.Json;
import com.scheible.pocketsaw.impl.shaded.com.eclipsesource.json.JsonArray;
import com.scheible.pocketsaw.impl.shaded.com.eclipsesource.json.JsonObject;
import com.scheible.pocketsaw.impl.shaded.com.eclipsesource.json.JsonValue;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

/**
 *
 * @author sj
 */
public class EsBuildMetadata implements PackageDependencySource {

	public static class ParameterBuilder {

		private static final String ROOT_PACKAGE_ALIAS = "root-package-alias";

		public static Set<Entry<String, String>> rootPackageAlias(Set<Entry<String, String>> parameters, String rootPackageAlias) {
			parameters.add(new SimpleImmutableEntry<>(ROOT_PACKAGE_ALIAS, rootPackageAlias));
			return parameters;
		}
	}

	PackageDependencies read(final String esBuildMetaDataString, final Set<Entry<String, String>> parameters) {
		Optional rootPackageAlias = Optional.empty();
		for (final Entry<String, String> parameter : parameters) {
			final String key = parameter.getKey().toLowerCase().trim();
			if (ParameterBuilder.ROOT_PACKAGE_ALIAS.equals(key)) {
				rootPackageAlias = Optional.of(parameter.getValue());
			} 
		}

		final JsonObject esBuildMetaData = Json.parse(esBuildMetaDataString).asObject();
		final JsonObject inputs = esBuildMetaData.get("inputs").asObject();

		final Map<Entry<String, String>, Integer> packageDependencies = new HashMap<>();

		for (final String file : inputs.names()) {

			final JsonArray imports = inputs.get(file).asObject().get("imports").asArray();

			for (final JsonValue importVal : imports) {
				final JsonObject importObj = importVal.asObject();

				final String path = importObj.get("path").asString();
				final boolean external = importObj.getBoolean("external", false);

				if (!external) {
					String fromPackage = fileNameToPackage(file, rootPackageAlias);
					String toPackage = fileNameToPackage(path, rootPackageAlias);

					if (!fromPackage.equals(toPackage)) {
						final Map.Entry<String, String> dependency = new SimpleImmutableEntry<>(
								fromPackage, toPackage);
						packageDependencies.put(dependency,
								packageDependencies.computeIfAbsent(dependency, (key) -> 0) + 1);
					}
				}
			}
		}

		return PackageDependencies.withCodeDependencyCounts(packageDependencies);
	}

	@Override
	public PackageDependencies read(final File dependencyFile) {
		return read(readFile(dependencyFile), Collections.emptySet());
	}

	@Override
	public PackageDependencies read(final File dependencyFile, final Set<Entry<String, String>> parameters) {
		return read(readFile(dependencyFile), parameters);
	}

	@Override
	public String getIdentifier() {
		return "esbuild-metadata";
	}

	private static String readFile(final File file) {
		try {
			return new String(Files.readAllBytes(file.toPath()), Charset.forName("UTF8"));
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	private static String fileNameToPackage(final String fileName, final Optional<String> rootPackageAlias) {
		String packageName = fileName.substring(0, fileName.lastIndexOf("/")).replaceAll("/", ".");

		if (rootPackageAlias.isPresent()) {
			if (!packageName.contains(".")) {
				return rootPackageAlias.get();
			} else {
				return rootPackageAlias.get() + packageName.substring(packageName.indexOf("."));
			}
		} else {
			return packageName;
		}
	}
}
