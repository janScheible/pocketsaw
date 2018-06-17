package com.scheible.pocketsaw.impl.descriptor;

import com.scheible.pocketsaw.impl.matching.PackageMatchable;

/**
 * A descriptor either describes a sub-module or an external functionality.
 * 
 * @author sj
 */
public interface PackageGroupDescriptor extends PackageMatchable {
	
	String getId();
	
	String getName();
	
	String getColor();
	
	default boolean isExternalFunctionality() {
		return this instanceof ExternalFunctionalityDescriptor;
	}
	
	default boolean isSubModule() {
		return this instanceof SubModuleDescriptor;
	}
	
	default ExternalFunctionalityDescriptor asExternalFunctionality() {
		return (ExternalFunctionalityDescriptor) this;
	}
	
	default SubModuleDescriptor asSubModule() {
		return (SubModuleDescriptor) this;
	}
}
