package com.scheible.pocketsaw.impl.descriptor.annotation;

import com.scheible.pocketsaw.api.ExternalFunctionality;
import com.scheible.pocketsaw.api.SubModule;
import com.scheible.pocketsaw.impl.code.PackageDependencies;
import com.scheible.pocketsaw.impl.code.classgraph.ClassgraphPackageDependenciesMapper;
import static com.scheible.pocketsaw.impl.descriptor.annotation.ClasspathScanner.TEST_CLASS_FILTER;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 *
 * @author sj
 */
public class ClassgraphClasspathScanner extends DependencyAwareClasspathScanner {

	private Set<String> lazySubModuleAnnotatedClassNames = null;
	private Set<String> lazyExternalFunctionalityAnnotatedClassNames = null;
	private PackageDependencies lazyDependencies = null;
	
	private boolean includeTestDependencies = false;

	private ClassgraphClasspathScanner(final String basePackage) {
		super(basePackage, new HashSet<>(), new HashSet<>());
	}

	public static DependencyAwareClasspathScanner create(Class<?> basePackageClass) {
		return new ClassgraphClasspathScanner(basePackageClass.getPackage().getName());
	}

	public static DependencyAwareClasspathScanner create(String basePackage) {
		return new ClassgraphClasspathScanner(basePackage);
	}

	private ClassgraphClasspathScanner scan() {
		final ClassGraph classGraph = new ClassGraph().whitelistPackages(getBasePackage())
				.enableAnnotationInfo().ignoreClassVisibility().disableRuntimeInvisibleAnnotations();
		if (doDependencyScan()) {
			classGraph.enableInterClassDependencies().enableExternalClasses();
		}

		// NOTE It is needed to filter for base package because ClassGraph extends the scan to other related classes. 
		BiFunction<ScanResult, Class<?>, Set<String>> toClassNames = (sr, ac) -> sr.getClassesWithAnnotation(ac.getName())
				.stream().map(ClassInfo::getName)
				.filter(cn -> cn.startsWith(getBasePackage() + "."))
				.filter(TEST_CLASS_FILTER).collect(Collectors.toSet());

		try (final ScanResult scanResult = classGraph.scan()) {
			lazySubModuleAnnotatedClassNames = toClassNames.apply(scanResult, SubModule.class);
			lazyExternalFunctionalityAnnotatedClassNames = toClassNames.apply(scanResult, ExternalFunctionality.class);

			if (doDependencyScan()) {
				lazyDependencies = ClassgraphPackageDependenciesMapper.map(getBasePackage(), scanResult.getClassDependencyMap(), includeTestDependencies);
			}
		}

		return this;
	}

	@Override
	public Set<String> getExternalFunctionalityAnnotatedClassNames() {
		return lazyExternalFunctionalityAnnotatedClassNames != null
				? lazyExternalFunctionalityAnnotatedClassNames : scan().lazyExternalFunctionalityAnnotatedClassNames;
	}

	@Override
	public Set<String> getSubModuleAnnotatedClassNames() {
		return lazySubModuleAnnotatedClassNames != null
				? lazySubModuleAnnotatedClassNames : scan().lazySubModuleAnnotatedClassNames;
	}

	@Override
	public PackageDependencies getDependencies() {
		if(!this.doDependencyScan()) {
			throw new IllegalStateException("Scanning of dependency information is not enabled!");
		}
		
		return lazyDependencies != null ? lazyDependencies : scan().lazyDependencies;
	}
	
	public ClassgraphClasspathScanner includeTestDependencies() {
		includeTestDependencies = true;
		return this;
	}
	
	public boolean doIncludeTestClasses() {
		return includeTestDependencies;
	}
}
