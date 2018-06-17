package com.scheible.pocketsaw.impl.visualization;

import com.scheible.pocketsaw.api.SubModule;
import com.scheible.pocketsaw.impl.dependency.DependencySubModule;
import com.scheible.pocketsaw.impl.descriptor.DescriptorSubModule;
import com.scheible.pocketsaw.impl.shaded.com.eclipsesource.json.MinimalJsonSubModule;

/**
 * Sub module for visualizing the dependency graph.
 * 
 * @author sj
 */
@SubModule(uses = {DescriptorSubModule.class, DependencySubModule.class, 
	MinimalJsonSubModule.class})
public class VisualizationSubModule {
	
}
