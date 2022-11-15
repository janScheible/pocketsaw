package com.scheible.pocketsaw.impl.descriptor.annotation;

import com.scheible.pocketsaw.api.ExternalFunctionality;
import com.scheible.pocketsaw.api.SubModule;
import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

/**
 * Requires the Spring Framework to be on the classpath.
 * 
 * @author sj
 */
public class SpringClasspathScanner extends ClasspathScanner {
	
	private final String basePackage;

	private SpringClasspathScanner(Class<?> basePackageClass, Set<String> subModuleAnnotatedClassNames,
			Set<String> externalFunctionalityAnnotatedClassNames) {
		super(subModuleAnnotatedClassNames, externalFunctionalityAnnotatedClassNames);

		this.basePackage = basePackageClass.getPackage().getName();
	}

	public static ClasspathScanner create(Class<?> basePackageClass) {
		return new SpringClasspathScanner(basePackageClass, findClasses(basePackageClass, SubModule.class),
				findClasses(basePackageClass, ExternalFunctionality.class));
	}

	private static <A extends Annotation> Set<String> findClasses(Class<?> basePackageClass, Class<A> annotationClass) {
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(new AnnotationTypeFilter(annotationClass));
		return scanner.findCandidateComponents(basePackageClass.getPackage().getName()).stream()
				.map(BeanDefinition::getBeanClassName).filter(TEST_CLASS_FILTER).collect(Collectors.toSet());
	}

	@Override
	public String getBasePackage() {
		return basePackage;
	}
}
