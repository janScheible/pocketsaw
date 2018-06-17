package com.scheible.pocketsaw.impl.descriptor;

import com.scheible.pocketsaw.api.ExternalFunctionality;
import java.util.Objects;

/**
 *
 * @author sj
 */
public class ExternalFunctionalityDescriptor implements PackageGroupDescriptor {

	private final String id;

	private final String name;
	private final String packageMatchPattern;
	private final String color;
	
	public static ExternalFunctionalityDescriptor fromAnnotatedClass(Class<?> externalFunctionalityAnnotatedClass) {
		ExternalFunctionality annotation = getAnnotation(externalFunctionalityAnnotatedClass);
		
		return new ExternalFunctionalityDescriptor(externalFunctionalityAnnotatedClass.getName(), 
				PackageGroupNameProvider.getName(externalFunctionalityAnnotatedClass), 
				annotation.packageMatchPattern(), annotation.color());
	}
	
	static ExternalFunctionality getAnnotation(Class<?> externalFunctionalityAnnotatedClass) {
		ExternalFunctionality annotation = externalFunctionalityAnnotatedClass.getDeclaredAnnotation(ExternalFunctionality.class);
		
		if (annotation == null) {
			throw new IllegalArgumentException("No @" + ExternalFunctionality.class.getSimpleName()
					+ " was found on class '" + externalFunctionalityAnnotatedClass.getName() + "'!");
		} else {
			return annotation;
		}
	}

	public ExternalFunctionalityDescriptor(String id, String name, String packageMatchPattern, String color) {
		this.id = id;

		this.name = name;
		this.packageMatchPattern = packageMatchPattern;
		this.color = color;
	}
	
	public ExternalFunctionalityDescriptor(String id, String name, String packageMatchPattern) {
		this(id, name, packageMatchPattern, PackageGroupColorProvider.getExternalFunctionalityDefaultColor());
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
				"packageMatchPattern = " + this.packageMatchPattern,
				"color = " + this.color
		) + "]";
	}
}
