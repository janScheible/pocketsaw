package com.scheible.pocketsaw.impl.descriptor.annotation;

import com.scheible.pocketsaw.api.SubModule;
import com.scheible.pocketsaw.impl.ExternalFunctionalities.SpringBeans;
import com.scheible.pocketsaw.impl.ExternalFunctionalities.SpringContext;
import com.scheible.pocketsaw.impl.ExternalFunctionalities.SpringCore;
import com.scheible.pocketsaw.impl.ExternalFunctionalities.FastClasspathScanner;
import com.scheible.pocketsaw.impl.descriptor.DescriptorSubModule;

/**
 * Creates package groups from  @SubModule and @ExternalFunctionality annotated classes.
 *
 * @author sj
 */
@SubModule(uses = {DescriptorSubModule.class, SpringCore.class, SpringContext.class, SpringBeans.class, 
	FastClasspathScanner.class})
public class AnnotationDescriptorSubModule {

}
