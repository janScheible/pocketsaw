package com.scheible.pocketsaw.impl.matching;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import com.scheible.pocketsaw.impl.shaded.org.springframework.util.AntPathMatcher;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author sj
 */
public class PackageMatcher<T extends PackageMatchable> {

	private static final AntPathMatcher PACKAGE_MATCHER = new AntPathMatcher(".");

	private final List<String> packageMatchPatterns;
	private final Map<String, T> patternMapping = new HashMap<>();

	public PackageMatcher(Set<T> packageGroups) {
		for(final T packageGroup : packageGroups) {
			for(final String packageMatchPattern : packageGroup.getPackageMatchPatterns()) {
				if(patternMapping.containsKey(packageMatchPattern)) {
					throw new IllegalStateException(String.format("The %s is matched by %s as well ny %s!",
							packageMatchPattern, packageGroup, patternMapping.get(packageMatchPattern)));
				} else {
					patternMapping.put(packageMatchPattern, packageGroup);
				}
			}
		}
		
		this.packageMatchPatterns = sortByHigherMatchPrecedence(patternMapping.keySet());
	}

	public Optional<T> findMatching(final String packageName) {
		T result = null;

		for (final String packageMatchPattern : packageMatchPatterns) {
			final T packageGroup = patternMapping.get(packageMatchPattern);
			if (PACKAGE_MATCHER.match(packageMatchPattern, packageName + ".")) {
				if (result != null) {
					throw new IllegalStateException("The package '" + packageName + "' is matched by "
							+ result.toString() + "" + " as well by " + packageGroup.toString() + "!");
				} else {
					result = packageGroup;
				}
			}
		}

		return Optional.ofNullable(result);
	}

	/**
	 * 1. More specific packages are considered first (e.g. 'com.bla.test' before 'com.bla'). 
	 * 1.1. Packages with less '**' are considered first.
	 *
	 * @return A sorted copy of the input list.
	 */
	private static List<String> sortByHigherMatchPrecedence(Collection<String> packageMatchPatterns) {
		List<String> result = new ArrayList<>(packageMatchPatterns);
		Collections.sort(result, new Comparator<String>() {

			private final String MULTI_WHILDCARD_PATTERN = Pattern.quote("**");

			private int getPackageCount(String pattern) {
				return (int) pattern.chars().filter(ch -> ch == '.').count();
			}

			private int getMultiWhildcardCount(String pattern) {
				return (pattern.length() - pattern.replaceAll(MULTI_WHILDCARD_PATTERN, "").length()) / 2;
			}

			@Override
			public int compare(final String first, final String second) {
				final int firstPackageCount = getPackageCount(first);
				final int secondPackageCount = getPackageCount(second);

				if (firstPackageCount != secondPackageCount) {
					return -1 * (firstPackageCount - secondPackageCount);
				} else {
					final int firstPackageMultiWhildcardCount = getMultiWhildcardCount(first);
					final int secondPackageMultiWhildcardCount = getMultiWhildcardCount(second);
					
					if(firstPackageMultiWhildcardCount != secondPackageMultiWhildcardCount) {
						return firstPackageMultiWhildcardCount - secondPackageMultiWhildcardCount;
					} else {
						return first.compareTo(second);
					}					
				}
			}
		});
		
		return result;
	}
	
	List<String> getPackageMatchPatterns() {
		return packageMatchPatterns;
	}
}
