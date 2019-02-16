package com.scheible.pocketsaw.impl.descriptor.annotation;

import com.scheible.pocketsaw.api.SubModule;
import com.scheible.pocketsaw.impl.ExternalFunctionalities.FastClasspathScanner;
import com.scheible.pocketsaw.impl.ExternalFunctionalities.Spring;
import com.scheible.pocketsaw.impl.descriptor.DescriptorSubModule;

/**
 * Creates package groups from  @SubModule and @ExternalFunctionality annotated classes.
 *
 * @author sj
 */
@SubModule(uses = {DescriptorSubModule.class, Spring.class, 
	FastClasspathScanner.class})
public class AnnotationDescriptorSubModule {

}
