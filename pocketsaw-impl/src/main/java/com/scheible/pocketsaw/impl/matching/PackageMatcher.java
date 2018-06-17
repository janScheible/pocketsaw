package com.scheible.pocketsaw.impl.matching;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import com.scheible.pocketsaw.impl.shaded.org.springframework.util.AntPathMatcher;

/**
 *
 * @author sj
 */
public class PackageMatcher<T extends PackageMatchable> {

	private static final AntPathMatcher PACKAGE_MATCHER = new AntPathMatcher(".");

	private final List<T> packageGroups;

	public PackageMatcher(Set<T> packageGroups) {
		this.packageGroups = this.sortByHigherMatchPrecedence(new ArrayList<>(packageGroups));
	}

	public Optional<T> findMatching(final String packageName) {
		T result = null;
		
		for (T packageGroup : packageGroups) {
			if (PACKAGE_MATCHER.match(packageGroup.getPackageMatchPattern(), packageName + ".")) {
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
	 * 1. More specific packages are considered first (e.g. 'com.bla.test' before 'com.bla'). 1.1. Packages with less
	 * '**' are considered first.
	 *
	 * @return A sorted copy of the input list.
	 */
	private List<T> sortByHigherMatchPrecedence(List<T> definitions) {
		Collections.sort(definitions, new Comparator<T>() {

			private final String MULTI_WHILDCARD_PATTERN = Pattern.quote("**");

			private int getPackageCount(String pattern) {
				return (int) pattern.chars().filter(ch -> ch == '.').count();
			}

			private int getMultiWhildcardCount(String pattern) {
				return (pattern.length() - pattern.replaceAll(MULTI_WHILDCARD_PATTERN, "").length()) / 2;
			}

			@Override
			public int compare(final T first, final T second) {
				int firstPackageCount = getPackageCount(first.getPackageMatchPattern());
				int secondPackageCount = getPackageCount(second.getPackageMatchPattern());

				if (firstPackageCount != secondPackageCount) {
					return -1 * (firstPackageCount - secondPackageCount);
				} else {
					return getMultiWhildcardCount(first.getPackageMatchPattern())
							- getMultiWhildcardCount(second.getPackageMatchPattern());
				}
			}
		});
		
		return definitions;
	}

	List<T> getPackageGroups() {
		return packageGroups;
	}
}
