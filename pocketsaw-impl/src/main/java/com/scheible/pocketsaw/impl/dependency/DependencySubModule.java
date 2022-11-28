package com.scheible.pocketsaw.impl.dependency;

import com.scheible.pocketsaw.api.SubModule;
import com.scheible.pocketsaw.impl.code.CodeSubModule;
import com.scheible.pocketsaw.impl.descriptor.DescriptorSubModule;
import com.scheible.pocketsaw.impl.matching.MatchingSubModule;
import com.scheible.pocketsaw.impl.matching.auto.AutoMatchingSubModule;

/**
 * Sub module that deals with merging the descriptor information with the package dependencies from the code to a
 * dependency graph that can be used for further analysis.
 *
 * @author sj
 */
@SubModule({MatchingSubModule.class, DescriptorSubModule.class, CodeSubModule.class, AutoMatchingSubModule.class})
public class DependencySubModule {

}
