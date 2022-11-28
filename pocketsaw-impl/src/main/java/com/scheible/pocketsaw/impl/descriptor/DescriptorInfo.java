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
	private final boolean autoMatching;

	public DescriptorInfo(Set<SubModuleDescriptor> subModules, Set<ExternalFunctionalityDescriptor> externalFunctionalities,
			final boolean autoMatching) {
		this.subModules = unmodifiableSet(subModules);
		this.externalFunctionalities = unmodifiableSet(externalFunctionalities);
		this.autoMatching = autoMatching;
	}

	public DescriptorInfo withAutoMatching(final boolean autoMatching) {
		if(this.autoMatching == autoMatching) {
			return this;
		} else {
			return new DescriptorInfo(subModules, externalFunctionalities, autoMatching);
		}
	}

	public Set<ExternalFunctionalityDescriptor> getExternalFunctionalities() {
		return externalFunctionalities;
	}

	public Set<SubModuleDescriptor> getSubModules() {
		return subModules;
	}

	public boolean doAutoMatching() {
		return autoMatching;
	}
}
