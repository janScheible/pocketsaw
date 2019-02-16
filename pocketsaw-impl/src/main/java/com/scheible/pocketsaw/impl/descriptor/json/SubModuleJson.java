package com.scheible.pocketsaw.impl.descriptor.json;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 *
 * @author sj
 */
class SubModuleJson {
	
	private final Set<String> usedModules = new HashSet<>();

	private final String name;
	private final String packageName;
	private Optional<Boolean> includeSubPackages;
	private Optional<String> color;
	
	SubModuleJson(final String name, final String packageName) {
		this.name = name;
		this.packageName = packageName;
	}

	SubModuleJson includeSubPackages(final Boolean include) {
		this.includeSubPackages = Optional.ofNullable(include);
		return this;
	}

	SubModuleJson color(final String color) {
		this.color = Optional.ofNullable(color);
		return this;
	}

	SubModuleJson uses(final Set<String> otherNames) {
		this.usedModules.addAll(otherNames);
		return this;
	}

	String getName() {
		return this.name;
	}

	String getPackageName() {
		return this.packageName;
	}

	Optional<Boolean> doIncludeSubPackages() {
		return this.includeSubPackages;
	}

	Optional<String> getColor() {
		return this.color;
	}

	Set<String> getUsedModules() {
		return this.usedModules;
	}
}
