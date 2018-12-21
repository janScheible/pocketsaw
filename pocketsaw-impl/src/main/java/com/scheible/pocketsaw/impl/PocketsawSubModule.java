package com.scheible.pocketsaw.impl;

import com.scheible.pocketsaw.api.SubModule;
import com.scheible.pocketsaw.impl.code.CodeSubModule;
import com.scheible.pocketsaw.impl.code.jdeps.JdepsSubModule;
import com.scheible.pocketsaw.impl.dependency.DependencySubModule;
import com.scheible.pocketsaw.impl.descriptor.DescriptorSubModule;
import com.scheible.pocketsaw.impl.descriptor.annotation.AnnotationDescriptorSubModule;
import com.scheible.pocketsaw.impl.descriptor.json.JsonDescriptorSubModule;
import com.scheible.pocketsaw.impl.visualization.VisualizationSubModule;

/**
 * Provides the main entry point for Pocketsaw as a facade to allow easy usage.
 * 
 * @author sj
 */
@SubModule(includeSubPackages = false, uses = {JdepsSubModule.class, DependencySubModule.class, CodeSubModule.class,
	VisualizationSubModule.class, DescriptorSubModule.class, AnnotationDescriptorSubModule.class, 
	JsonDescriptorSubModule.class})
public class PocketsawSubModule {

}
