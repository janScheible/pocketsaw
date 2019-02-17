package com.scheible.pocketsaw.impl.code;

import java.util.Objects;

/**
 *
 * @author sj
 */
public class PackageDependency {

	private final String fromPackage;
	private final String toPackage;

	public PackageDependency(String fromPackage, String toPackage) {
		this.fromPackage = fromPackage;
		this.toPackage = toPackage;
	}

	public String getFromPackage() {
		return fromPackage;
	}

	public String getToPackage() {
		return toPackage;
	}

	@Override
	public int hashCode() {
		return Objects.hash(fromPackage, toPackage);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof PackageDependency) {
			PackageDependency other = (PackageDependency) obj;
			return Objects.equals(fromPackage, other.fromPackage) && Objects.equals(toPackage, other.toPackage);
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return fromPackage + " -> " + toPackage;
	}
}
