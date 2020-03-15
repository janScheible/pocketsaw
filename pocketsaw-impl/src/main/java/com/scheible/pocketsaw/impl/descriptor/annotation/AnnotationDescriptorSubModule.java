package com.scheible.pocketsaw.impl.descriptor.annotation;

import com.scheible.pocketsaw.api.SubModule;
import com.scheible.pocketsaw.impl.ExternalFunctionalities.Classgraph;
import com.scheible.pocketsaw.impl.ExternalFunctionalities.FastClasspathScanner;
import com.scheible.pocketsaw.impl.ExternalFunctionalities.Spring;
import com.scheible.pocketsaw.impl.code.CodeSubModule;
import com.scheible.pocketsaw.impl.code.classgraph.ClassgraphSubModule;
import com.scheible.pocketsaw.impl.descriptor.DescriptorSubModule;

/**
 * Creates package groups from @SubModule and @ExternalFunctionality annotated classes.
 *
 * @author sj
 */
@SubModule(uses = {DescriptorSubModule.class, CodeSubModule.class, ClassgraphSubModule.class,
	Spring.class, Classgraph.class, FastClasspathScanner.class})
public class AnnotationDescriptorSubModule {

}
