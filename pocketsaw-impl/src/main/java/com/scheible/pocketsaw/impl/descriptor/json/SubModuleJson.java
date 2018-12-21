package com.scheible.pocketsaw.impl.descriptor.json;

import com.scheible.pocketsaw.impl.descriptor.DescriptorInfo;
import com.scheible.pocketsaw.impl.descriptor.SubModuleDescriptor;
import com.scheible.pocketsaw.impl.shaded.com.eclipsesource.json.Json;
import com.scheible.pocketsaw.impl.shaded.com.eclipsesource.json.JsonArray;
import com.scheible.pocketsaw.impl.shaded.com.eclipsesource.json.JsonObject;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author sj
 */
public class SubModuleJson {

	private final Set<String> usedModules = new HashSet<>();

	private final String name;
	private final String packageName;
	private Optional<Boolean> includeSubPackages;
	private Optional<String> color;

	public static DescriptorInfo read(final File modulesJsonFile) throws IOException {
		return toDescriptorInfo(read(new String(Files.readAllBytes(modulesJsonFile.toPath()), Charset.forName("UTF8"))));
	}

	static Set<SubModuleJson> read(final String modulesJson) throws IOException {
		final Set<SubModuleJson> subModules = new HashSet<>();
		final Set<String> names = new HashSet<>();

		final JsonArray dependencies = Json.parse(modulesJson)
				.asObject().get("submodules").asArray();
		for (int i = 0; i < dependencies.size(); i++) {
			final JsonObject dependency = dependencies.get(i).asObject();
			final SubModuleJson subModule = new SubModuleJson(dependency.get("name").asString(),
					dependency.get("packageName").asString())
					.includeSubPackages(dependency.contains("includeSubPackages") ? dependency.getBoolean("includeSubPackages", true) : null)
					.color(dependency.contains("color") ? dependency.getString("color", "") : null)
					.uses(dependency.contains("uses")
							? dependency.get("uses").asArray().values().stream().map(v -> v.asString()).collect(Collectors.toSet())
							: new HashSet<>());

			if (names.contains(subModule.getName())) {
				throw new IllegalStateException("The sub module names must be unique ('" + subModule.getName() + "' was used at least twice)!");
			} else {
				names.add(subModule.getName());
			}

			subModules.add(subModule);
		}

		return subModules;
	}

	static DescriptorInfo toDescriptorInfo(final Set<SubModuleJson> subModules) {
		final DescriptorInfo descriptorInfo = new DescriptorInfo(subModules.stream().map(s
				-> new SubModuleDescriptor(s.getName(), s.getName(), s.getPackageName(), s.doIncludeSubPackages(),
						s.getColor(), s.getUsedModules(), new HashSet<>())).collect(Collectors.toSet()), new HashSet<>());
		return descriptorInfo;
	}

	private SubModuleJson(final String name, final String packageName) {
		this.name = name;
		this.packageName = packageName;
	}

	private SubModuleJson includeSubPackages(final Boolean include) {
		this.includeSubPackages = Optional.ofNullable(include);
		return this;
	}

	private SubModuleJson color(final String color) {
		this.color = Optional.ofNullable(color);
		return this;
	}

	private SubModuleJson uses(final Set<String> otherNames) {
		this.usedModules.addAll(otherNames);
		return this;
	}

	public String getName() {
		return this.name;
	}

	public String getPackageName() {
		return this.packageName;
	}

	public Optional<Boolean> doIncludeSubPackages() {
		return this.includeSubPackages;
	}

	public Optional<String> getColor() {
		return this.color;
	}

	public Set<String> getUsedModules() {
		return this.usedModules;
	}
}
