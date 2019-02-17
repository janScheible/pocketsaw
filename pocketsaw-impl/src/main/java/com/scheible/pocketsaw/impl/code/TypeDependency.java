package com.scheible.pocketsaw.impl.code;

import java.util.Objects;

/**
 *
 * @author sj
 */
public class TypeDependency {

	private final String fromPackage;
	private final String fromFullName;

	private final String toPackage;
	private final String toFullName;

	public TypeDependency(String fromPackage, String fromFullName, String toPackage, String toFullName) {
		this.fromPackage = fromPackage;
		this.fromFullName = fromFullName;
		this.toPackage = toPackage;
		this.toFullName = toFullName;
	}

	public String getFromFullName() {
		return fromFullName;
	}

	public String getFromPackage() {
		return fromPackage;
	}

	public String getToFullName() {
		return toFullName;
	}

	public String getToPackage() {
		return toPackage;
	}

	@Override
	public int hashCode() {
		return Objects.hash(fromFullName, fromPackage, toFullName, toPackage);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof TypeDependency) {
			TypeDependency other = (TypeDependency) obj;
			return Objects.equals(fromFullName, other.fromFullName) && Objects.equals(fromPackage, other.fromPackage)
					&& Objects.equals(toFullName, other.toFullName) && Objects.equals(toPackage, other.toPackage);
		} else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		return fromFullName + " -> " + toFullName;
	}
}
