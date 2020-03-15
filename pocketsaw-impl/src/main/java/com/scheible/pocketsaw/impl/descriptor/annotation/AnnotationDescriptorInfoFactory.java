package com.scheible.pocketsaw.impl.descriptor.annotation;

import com.scheible.pocketsaw.api.ExternalFunctionality;
import com.scheible.pocketsaw.api.SubModule;
import com.scheible.pocketsaw.impl.descriptor.DescriptorInfo;
import com.scheible.pocketsaw.impl.descriptor.ExternalFunctionalityDescriptor;
import com.scheible.pocketsaw.impl.descriptor.SubModuleDescriptor;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author sj
 */
public class AnnotationDescriptorInfoFactory {
	
	public static DescriptorInfo createFromClasspath(ClasspathScanner classpathScanner) {
		Set<SubModuleDescriptor> subModules = classpathScanner.getSubModuleAnnotatedClassNames().stream()
				.map(className -> fromAnnotatedSubModuleClass(forName(className))).collect(Collectors.toSet());
		
		Set<Class<?>> externalFunctionalitiesClasses = classpathScanner.getExternalFunctionalityAnnotatedClassNames().stream()
				.map(className -> forName(className)).collect(Collectors.toSet());
		Set<ExternalFunctionalityDescriptor> externalFunctionalities = externalFunctionalitiesClasses.stream()
				.map(AnnotationDescriptorInfoFactory::fromAnnotatedExternalFunctionalityClass).collect(Collectors.toSet());
		
		return new DescriptorInfo(subModules, externalFunctionalities);
	}
	
	private static Class<?> forName(String className) {
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException ex) {
			throw new IllegalStateException(ex);
		}
	}	

	static ExternalFunctionalityDescriptor fromAnnotatedExternalFunctionalityClass(Class<?> externalFunctionalityAnnotatedClass) {
		ExternalFunctionality annotation = getAnnotation(externalFunctionalityAnnotatedClass);

		if(annotation.packageMatchPattern().length == 0) {
			throw new IllegalArgumentException(String.format("The class '%s' annotated with @%s has no packageMatchPatterns defined!", 
					externalFunctionalityAnnotatedClass.getName(), ExternalFunctionality.class.getSimpleName()));
		}
		return new ExternalFunctionalityDescriptor(externalFunctionalityAnnotatedClass.getName(),
				PackageGroupNameProvider.getName(externalFunctionalityAnnotatedClass),
				new HashSet<>(Arrays.asList(annotation.packageMatchPattern())), annotation.color());
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

	static SubModuleDescriptor fromAnnotatedSubModuleClass(Class<?> subModuleAnnotatedClass) {
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
				usedExternalFunctionalities.add(fromAnnotatedExternalFunctionalityClass(usedClass));
			} else {
				throw new IllegalStateException("The used class '" + usedClass.getName() + "' of '"
						+ subModuleAnnotatedClass.getName() + "' is neither annotated with @"
						+ SubModule.class.getSimpleName() + " nor with @" + ExternalFunctionality.class.getSimpleName() + "!");
			}
		}

		final String packageName = annotation.basePackageClass().equals(Void.class)
				? subModuleAnnotatedClass.getPackage().getName() : annotation.basePackageClass().getPackage().getName();

		return new SubModuleDescriptor(subModuleAnnotatedClass.getName(), PackageGroupNameProvider.getName(subModuleAnnotatedClass),
				packageName, annotation.includeSubPackages(), annotation.color(),
				usedSubModuleIds, usedExternalFunctionalities);
	}

	private static Class<?>[] resolveUsedAlias(Class<?> subModuleAnnotatedClass, SubModule annotation) {
		if (annotation.value().length > 0 && annotation.uses().length > 0) {
			throw new IllegalStateException("@" + SubModule.class.getSimpleName() + " on "
					+ subModuleAnnotatedClass.getName() + " has value() and uses() defined. Only one is allowed!");
		}

		return annotation.uses().length > 0 ? annotation.uses() : annotation.value();
	}
}
