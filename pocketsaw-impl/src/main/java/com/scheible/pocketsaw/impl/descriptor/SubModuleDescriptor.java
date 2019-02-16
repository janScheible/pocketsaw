package com.scheible.pocketsaw.impl.descriptor;

import com.scheible.pocketsaw.api.SubModule;
import java.lang.reflect.Method;
import java.util.Arrays;
import static java.util.Collections.unmodifiableSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 *
 * @author sj
 */
public class SubModuleDescriptor implements PackageGroupDescriptor {

	private final String id;

	private final String name;
	private final Set<String> packageMatchPatterns;
	private final String color;

	private final Set<String> usedSubModuleIds;
	private final Set<ExternalFunctionalityDescriptor> usedExternalFunctionalities;

	public SubModuleDescriptor(String id, String name, String packageName, boolean includeSubPackages, String color,
			Set<String> usedSubModuleIds, Set<ExternalFunctionalityDescriptor> usedExternalFunctionalities) {
		this.id = id;

		this.name = name;
		this.packageMatchPatterns = new HashSet<>(Arrays.asList(packageName + (includeSubPackages ? ".**" : ".*")));
		this.color = color;

		this.usedSubModuleIds = unmodifiableSet(usedSubModuleIds);
		this.usedExternalFunctionalities = unmodifiableSet(usedExternalFunctionalities);
	}
	
	public SubModuleDescriptor(String id, String name, String packageName, boolean includeSubPackages,
			Set<String> usedSubModuleIds, Set<ExternalFunctionalityDescriptor> usedExternalFunctionalities) {	
		this(id, name, packageName, includeSubPackages, PackageGroupColorProvider.getSubModuleDefaultColor(),
				usedSubModuleIds, usedExternalFunctionalities);
	}
	
	public SubModuleDescriptor(String id, String name, String packageName, Optional<Boolean> includeSubPackages,
			Optional<String> color, Set<String> usedSubModuleIds,
			Set<ExternalFunctionalityDescriptor> usedExternalFunctionalities) {
		this(id, name, packageName,
				includeSubPackages.orElseGet(SubModuleDescriptor::getDefaultIncludeSubPackages),
				color.orElseGet(PackageGroupColorProvider::getSubModuleDefaultColor),
				usedSubModuleIds, usedExternalFunctionalities);
	}
	
	static boolean getDefaultIncludeSubPackages() {
		try {
			Method method = SubModule.class.getMethod("includeSubPackages");
			return (boolean) method.getDefaultValue();
		} catch (NoSuchMethodException | SecurityException ex) {
			throw new IllegalStateException(ex);
		}
	}	

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Set<String> getPackageMatchPatterns() {
		return packageMatchPatterns;
	}

	@Override
	public String getColor() {
		return color;
	}

	public Set<String> getUsedSubModuleIds() {
		return usedSubModuleIds;
	}

	public Set<ExternalFunctionalityDescriptor> getUsedExternalFunctionalities() {
		return usedExternalFunctionalities;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.id);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		} else if (obj != null && obj.getClass().equals(this.getClass())) {
			SubModuleDescriptor other = (SubModuleDescriptor) obj;
			return this.id.equals(other.id);
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "[" + String.join(", ",
				"id = " + this.id,
				"name = " + this.name,
				"packageMatchPattern = " + this.packageMatchPatterns,
				"color = " + this.color,
				"usedSubModuleIds = " + this.usedSubModuleIds,
				"usedExternalFunctionalities = " + this.usedExternalFunctionalities
		) + "]";
	}
}
