package com.scheible.pocketsaw.impl.descriptor;

import com.scheible.pocketsaw.api.ExternalFunctionality;
import com.scheible.pocketsaw.api.SubModule;
import static java.util.Collections.unmodifiableSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author sj
 */
public class SubModuleDescriptor implements PackageGroupDescriptor {

	private final String id;

	private final String name;
	private final String packageMatchPattern;
	private final String color;

	private final Set<String> usedSubModuleIds;
	private final Set<ExternalFunctionalityDescriptor> usedExternalFunctionalities;

	public static SubModuleDescriptor fromAnnotatedClass(Class<?> subModuleAnnotatedClass) {
		SubModule annotation = subModuleAnnotatedClass.getDeclaredAnnotation(SubModule.class);

		if (annotation == null) {
			throw new IllegalArgumentException("No @" + SubModule.class.getSimpleName()
					+ " was found on class '" + subModuleAnnotatedClass.getName() + "'!");
		}

		Set<String> usedSubModuleIds = new HashSet<>();
		Set<ExternalFunctionalityDescriptor> usedExternalFunctionalities = new HashSet<>();

		for (Class<?> usedClass : resolveUsedAlias(subModuleAnnotatedClass, annotation)) {
			if (usedClass.getDeclaredAnnotation(SubModule.class) != null) {
				usedSubModuleIds.add(usedClass.getName());
			} else if (usedClass.getDeclaredAnnotation(ExternalFunctionality.class) != null) {
				usedExternalFunctionalities.add(ExternalFunctionalityDescriptor.fromAnnotatedClass(usedClass));
			} else {
				throw new IllegalStateException("The used class '" + usedClass.getName() + "' of '"
						+ subModuleAnnotatedClass.getName() + "' is neither annotated with @"
						+ SubModule.class.getSimpleName() + " nor with @" + ExternalFunctionality.class.getSimpleName() + "!");
			}
		}

		return new SubModuleDescriptor(subModuleAnnotatedClass.getName(), PackageGroupNameProvider.getName(subModuleAnnotatedClass),
				subModuleAnnotatedClass.getPackage().getName(), annotation.includeSubPackages(), annotation.color(),
				usedSubModuleIds, usedExternalFunctionalities);
	}

	private static Class<?>[] resolveUsedAlias(Class<?> subModuleAnnotatedClass, SubModule annotation) {
		if (annotation.value().length > 0 && annotation.uses().length > 0) {
			throw new IllegalStateException("@" + SubModule.class.getSimpleName() + " on "
					+ subModuleAnnotatedClass.getName() + " has value() and uses() defined. Only one is allowed!");
		}

		return annotation.uses().length > 0 ? annotation.uses() : annotation.value();
	}

	public SubModuleDescriptor(String id, String name, String packageName, boolean includeSubPackages, String color,
			Set<String> usedSubModuleIds, Set<ExternalFunctionalityDescriptor> usedExternalFunctionalities) {
		this.id = id;

		this.name = name;
		this.packageMatchPattern = packageName + (includeSubPackages ? ".**" : ".*");
		this.color = color;

		this.usedSubModuleIds = unmodifiableSet(usedSubModuleIds);
		this.usedExternalFunctionalities = unmodifiableSet(usedExternalFunctionalities);
	}
	
	public SubModuleDescriptor(String id, String name, String packageName, boolean includeSubPackages, 
			Set<String> usedSubModuleIds, Set<ExternalFunctionalityDescriptor> usedExternalFunctionalities) {
		this(id, name, packageName, includeSubPackages, PackageGroupColorProvider.getSubModuleDefaultColor(), usedSubModuleIds, usedExternalFunctionalities);
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
	public String getPackageMatchPattern() {
		return packageMatchPattern;
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
				"packageMatchPattern = " + this.packageMatchPattern,
				"color = " + this.color,
				"usedSubModuleIds = " + this.usedSubModuleIds,
				"usedExternalFunctionalities = " + this.usedExternalFunctionalities
		) + "]";
	}
}
