package com.scheible.pocketsaw.impl.descriptor.json;

import com.scheible.pocketsaw.impl.descriptor.DescriptorInfo;
import com.scheible.pocketsaw.impl.descriptor.ExternalFunctionalityDescriptor;
import com.scheible.pocketsaw.impl.descriptor.SubModuleDescriptor;
import com.scheible.pocketsaw.impl.shaded.com.eclipsesource.json.Json;
import com.scheible.pocketsaw.impl.shaded.com.eclipsesource.json.JsonArray;
import com.scheible.pocketsaw.impl.shaded.com.eclipsesource.json.JsonObject;
import com.scheible.pocketsaw.impl.shaded.com.eclipsesource.json.JsonValue;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 *
 * @author sj
 */
public class JsonDescriptorReader {

	static class DescriptorJson {

		private final List<SubModuleJson> subModules;
		private final List<ExternalFunctionalityJson> externalFunctionalities;

		public DescriptorJson(List<SubModuleJson> subModules, List<ExternalFunctionalityJson> externalFunctionalities) {
			this.subModules = subModules;
			this.externalFunctionalities = externalFunctionalities;
		}

		List<ExternalFunctionalityJson> getExternalFunctionalities() {
			return externalFunctionalities;
		}

		List<SubModuleJson> getSubModules() {
			return subModules;
		}
	}

	public static DescriptorInfo read(final File modulesJsonFile) throws IOException {
		return toDescriptorInfo(read(new String(Files.readAllBytes(modulesJsonFile.toPath()), Charset.forName("UTF8"))));
	}

	static DescriptorJson read(final String modulesJson) throws IOException {
		final List<SubModuleJson> subModuleDescriptors = new ArrayList<>();
		final List<ExternalFunctionalityJson> externalFunctionalityDescriptors = new ArrayList<>();
		final Set<String> names = new HashSet<>();

		final JsonObject descriptors = Json.parse(modulesJson).asObject();
		final JsonArray subModules = (descriptors.contains("submodules") ? descriptors.get("submodules")
				: descriptors.get("subModules")).asArray();
		for (int i = 0; i < subModules.size(); i++) {
			final JsonObject subModule = subModules.get(i).asObject();
			final SubModuleJson subModuleDescriptor = new SubModuleJson(subModule.get("name").asString(),
					subModule.get("packageName").asString())
					.includeSubPackages(subModule.contains("includeSubPackages") ? subModule.getBoolean("includeSubPackages", true) : null)
					.color(subModule.contains("color") ? subModule.getString("color", "") : null)
					.uses(subModule.contains("uses")
							? subModule.get("uses").asArray().values().stream().map(JsonValue::asString).collect(Collectors.toSet())
							: new HashSet<>());

			if (names.contains(subModuleDescriptor.getName())) {
				throw new IllegalStateException("The sub module names must be unique ('" + subModuleDescriptor.getName() + "' was used at least twice)!");
			} else {
				names.add(subModuleDescriptor.getName());
			}

			subModuleDescriptors.add(subModuleDescriptor);
		}

		names.clear();

		final JsonValue externalFunctionalities = descriptors.get("externalFunctionalities");
		if (externalFunctionalities != null) {
			for (int i = 0; i < externalFunctionalities.asArray().size(); i++) {
				final JsonObject externalFunctionality = externalFunctionalities.asArray().get(i).asObject();

				final String name = Objects.requireNonNull(externalFunctionality.getString("name", null));

				final JsonValue packageMatchPatternValue = Objects.requireNonNull(externalFunctionality.get("packageMatchPattern"));
				final String packageMatchPattern = packageMatchPatternValue.isString() ? packageMatchPatternValue.asString() : null;
				final Set<String> packageMatchPatterns = StreamSupport.stream(
						(packageMatchPatternValue.isArray() ? packageMatchPatternValue.asArray() : new JsonArray()).spliterator(), false)
						.map(v -> v.asString()).collect(Collectors.toSet());
				final boolean packageMatchPatternIsDefinedExactlyOnce = ((packageMatchPattern != null && !packageMatchPattern.isEmpty() && packageMatchPatterns.isEmpty())
						|| ((packageMatchPattern == null || packageMatchPattern.isEmpty()) && !packageMatchPatterns.isEmpty()));
				if (!packageMatchPatternIsDefinedExactlyOnce) {
					throw new IllegalStateException("'packageMatchPattern' must either be a string or an array has to be defined (name = '" + name + "')!");
				}

				final ExternalFunctionalityJson externalFunctionalityDescriptor = new ExternalFunctionalityJson(name,
						packageMatchPatterns.isEmpty() ? new HashSet<>(Arrays.asList(packageMatchPattern)) : packageMatchPatterns);

				if (names.contains(externalFunctionalityDescriptor.getName())) {
					throw new IllegalStateException("The external functionality names must be unique ('" + externalFunctionalityDescriptor.getName() + "' was used at least twice)!");
				} else {
					names.add(externalFunctionalityDescriptor.getName());
				}

				externalFunctionalityDescriptors.add(externalFunctionalityDescriptor);
			}
		}

		return new DescriptorJson(subModuleDescriptors, externalFunctionalityDescriptors);
	}

	static DescriptorInfo toDescriptorInfo(final DescriptorJson descriptorJson) {
		final DescriptorInfo descriptorInfo = new DescriptorInfo(descriptorJson.subModules.stream().map(s
				-> new SubModuleDescriptor(s.getName(), s.getName(), s.getPackageName(), s.doIncludeSubPackages(),
						s.getColor(), s.getUsedModules(), new HashSet<>()))
				.collect(Collectors.toSet()),
				descriptorJson.externalFunctionalities.stream().map(e
						-> new ExternalFunctionalityDescriptor(e.getName(), e.getName(), e.getPackageMatchPatterns()))
						.collect(Collectors.toSet()));
		return descriptorInfo;
	}
}
