package com.scheible.pocketsaw.impl.descriptor;

import static java.util.Collections.unmodifiableSet;
import java.util.Set;

/**
 *
 * @author sj
 */
public class DescriptorInfo {

	private final Set<SubModuleDescriptor> subModules;
	private final Set<ExternalFunctionalityDescriptor> externalFunctionalities;

	public DescriptorInfo(Set<SubModuleDescriptor> subModules, Set<ExternalFunctionalityDescriptor> externalFunctionalities) {
		this.subModules = unmodifiableSet(subModules);
		this.externalFunctionalities = unmodifiableSet(externalFunctionalities);
	}

	public Set<ExternalFunctionalityDescriptor> getExternalFunctionalities() {
		return externalFunctionalities;
	}

	public Set<SubModuleDescriptor> getSubModules() {
		return subModules;
	}
}
