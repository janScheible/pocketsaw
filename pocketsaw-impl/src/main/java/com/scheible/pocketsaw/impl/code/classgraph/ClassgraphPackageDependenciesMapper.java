package com.scheible.pocketsaw.impl.code.classgraph;

import com.scheible.pocketsaw.api.ExternalFunctionality;
import com.scheible.pocketsaw.api.SubModule;
import com.scheible.pocketsaw.impl.code.PackageDependencies;
import com.scheible.pocketsaw.impl.code.PackageDependency;
import com.scheible.pocketsaw.impl.code.TypeDependency;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author sj
 */
public class ClassgraphPackageDependenciesMapper {
	
	private static final String TEST_CLASS_MAVEN_DIRECTORY = "/target/test-classes".replaceAll(Pattern.quote("/"), Matcher.quoteReplacement(File.separator));

	public static PackageDependencies map(final String basePackage, final Map<ClassInfo, ClassInfoList> classDependencyMap, boolean includeTestClasses) {
		final Map<PackageDependency, Set<TypeDependency>> classLevelPackageDependencies = new HashMap<>();
		final Map<String, Set<String>> packageClasses = new HashMap<>();

		final Predicate<ClassInfo> isJdk = ci -> ci.getPackageName().startsWith("java.") || ci.getPackageName().startsWith("javax.");
		final Predicate<ClassInfo> isUnderRootPackage = ci -> ci.getName().startsWith(basePackage + ".");
		final Predicate<ClassInfo> isTestClassToIgnore = ci -> {
			try {
				if(includeTestClasses) {
					return false;
				}
				
				final File file = ci.getClasspathElementFile();
				return file != null && file.getAbsolutePath().endsWith(TEST_CLASS_MAVEN_DIRECTORY);
			} catch (final IllegalArgumentException ex) {
				return false;
			}
		};
		final Predicate<ClassInfo> hasOrIsPocketsawAnnotation = ci -> ci.getName().equals(SubModule.class.getName())
				||  ci.getName().equals(ExternalFunctionality.class.getName())
				|| ci.getAnnotationInfo(SubModule.class.getName()) != null
				|| ci.getAnnotationInfo(ExternalFunctionality.class.getName()) != null;

		for (final Map.Entry<ClassInfo, ClassInfoList> classgraphDependency : classDependencyMap.entrySet()) {
			final ClassInfo source = classgraphDependency.getKey();
			
			if (isJdk.test(source) || !isUnderRootPackage.test(source) || isTestClassToIgnore.test(source) 
					|| hasOrIsPocketsawAnnotation.test(source)) {
				continue;
			}

			packageClasses.computeIfAbsent(source.getPackageName(), key -> new HashSet<>()).add(source.getName());

			for (final ClassInfo target : source.getClassDependencies()) {
				if (isJdk.test(target) || isTestClassToIgnore.test(target) || hasOrIsPocketsawAnnotation.test(target)) {
					continue;
				}

				if (isUnderRootPackage.test(target)) {
					packageClasses.computeIfAbsent(target.getPackageName(), key -> new HashSet<>()).add(target.getName());
				}

				if (!source.getPackageName().equals(target.getPackageName())) {
					final PackageDependency packageDependency = new PackageDependency(
							source.getPackageName(), target.getPackageName());

					classLevelPackageDependencies.computeIfAbsent(packageDependency, key -> new HashSet<>())
							.add(new TypeDependency(source.getPackageName(), source.getName(), target.getPackageName(), target.getName()));
				}
			}
		}

		return PackageDependencies.withClassLevelDependencies(classLevelPackageDependencies, packageClasses);
	}
}
