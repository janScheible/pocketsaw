package com.scheible.pocketsaw.impl.descriptor;

import com.scheible.pocketsaw.api.SubModule;
import com.scheible.pocketsaw.impl.matching.MatchingSubModule;

/**
 * Descriptors are used to define package groups.
 * 
 * @author sj
 */
@SubModule(includeSubPackages = false, uses = {MatchingSubModule.class})
public class DescriptorSubModule {

}
