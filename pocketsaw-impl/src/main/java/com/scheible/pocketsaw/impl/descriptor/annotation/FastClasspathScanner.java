package com.scheible.pocketsaw.impl.descriptor.annotation;

import com.scheible.pocketsaw.api.ExternalFunctionality;
import com.scheible.pocketsaw.api.SubModule;
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult;
import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Requires the Fast Classpath Scanner to be on the classpath.
 *
 * @author sj
 */
public class FastClasspathScanner extends ClasspathScanner {

	private FastClasspathScanner(Set<String> subModuleAnnotatedClassNames, Set<String> externalFunctionalityAnnotatedClassNames) {
		super(subModuleAnnotatedClassNames, externalFunctionalityAnnotatedClassNames);
	}

	public static ClasspathScanner create(Class<?> basePackageClass) {
		ScanResult scanResult = new io.github.lukehutch.fastclasspathscanner.FastClasspathScanner(basePackageClass.getPackage().getName()).scan();

		return new FastClasspathScanner(findClasses(scanResult, SubModule.class),
				findClasses(scanResult, ExternalFunctionality.class));

	}

	private static <A extends Annotation> Set<String> findClasses(ScanResult scanResult,
			Class<A> annotationClass) {
		return scanResult.getNamesOfClassesWithAnnotation(annotationClass).stream()
				.filter(TEST_CLASS_FILTER).collect(Collectors.toSet());
	}

}
