package com.scheible.pocketsaw.impl.descriptor;

import java.util.Objects;
import java.util.Set;

/**
 *
 * @author sj
 */
public class ExternalFunctionalityDescriptor implements PackageGroupDescriptor {

	private final String id;

	private final String name;
	private final Set<String> packageMatchPatterns;
	private final String color;
	

	public ExternalFunctionalityDescriptor(String id, String name, Set<String> packageMatchPatterns, String color) {
		this.id = id;

		this.name = name;
		this.packageMatchPatterns = packageMatchPatterns;
		this.color = color;
	}
	
	public ExternalFunctionalityDescriptor(String id, String name, Set<String> packageMatchPatterns) {
		this(id, name, packageMatchPatterns, PackageGroupColorProvider.getExternalFunctionalityDefaultColor());
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

	@Override
	public int hashCode() {
		return Objects.hash(this.id);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		} else if (obj != null && obj.getClass().equals(this.getClass())) {
			ExternalFunctionalityDescriptor other = (ExternalFunctionalityDescriptor) obj;
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
				"color = " + this.color
		) + "]";
	}
}
