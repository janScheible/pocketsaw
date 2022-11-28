package com.scheible.pocketsaw.impl.matching.auto;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author sj
 */
public class PackageGroupNameProvider {

	static String packageToName(String packageName) {
		return Stream.of(packageName.split("\\.")).map(p -> Character.toUpperCase(p.charAt(0)) + p.substring(1))
				.collect(Collectors.joining());
	}
}
