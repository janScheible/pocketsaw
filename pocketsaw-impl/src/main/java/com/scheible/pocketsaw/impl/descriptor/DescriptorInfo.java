package com.scheible.pocketsaw.impl.descriptor;

import static java.util.Collections.unmodifiableSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author sj
 */
public class DescriptorInfo {

	private final Set<SubModuleDescriptor> subModules;
	private final Set<ExternalFunctionalityDescriptor> externalFunctionalities;

	public DescriptorInfo(Set<SubModuleDescriptor> subModules, Set<ExternalFunctionalityDescriptor> externalFunctionalities) {
		this.subModules = unmodifiableSet(subModules);
		this.externalFunctionalities = unmodifiableSet(externalFunctionalities);
	}
	
	public static DescriptorInfo createFromClasspath(ClasspathScanner classpathScanner) {
		Set<SubModuleDescriptor> subModules = classpathScanner.getSubModuleAnnotatedClassNames().stream()
				.map(className -> SubModuleDescriptor.fromAnnotatedClass(forName(className))).collect(Collectors.toSet());
		
		Set<Class<?>> externalFunctionalitiesClasses = classpathScanner.getExternalFunctionalityAnnotatedClassNames().stream()
				.map(className -> forName(className)).collect(Collectors.toSet());
		Set<ExternalFunctionalityDescriptor> externalFunctionalities = externalFunctionalitiesClasses.stream()
				.map(ExternalFunctionalityDescriptor::fromAnnotatedClass).collect(Collectors.toSet());
		
		return new DescriptorInfo(subModules, externalFunctionalities);
	}
	
	private static Class<?> forName(String className) {
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException ex) {
			throw new IllegalStateException(ex);
		}
	}

	public Set<ExternalFunctionalityDescriptor> getExternalFunctionalities() {
		return externalFunctionalities;
	}

	public Set<SubModuleDescriptor> getSubModules() {
		return subModules;
	}
}
