package com.scheible.pocketsaw.impl.descriptor;

import com.scheible.pocketsaw.api.SubModule;
import com.scheible.pocketsaw.impl.ExternalFunctionalities.SpringBeans;
import com.scheible.pocketsaw.impl.ExternalFunctionalities.SpringContext;
import com.scheible.pocketsaw.impl.ExternalFunctionalities.SpringCore;
import com.scheible.pocketsaw.impl.ExternalFunctionalities.FastClasspathScanner;
import com.scheible.pocketsaw.impl.matching.MatchingSubModule;

/**
 * Descriptors are used to define package groups. The default source are the @SubModule and @ExternalFunctionality
 * annotated classes.
 * 
 * @author sj
 */
@SubModule({MatchingSubModule.class, SpringCore.class, SpringContext.class, SpringBeans.class, FastClasspathScanner.class})
public class DescriptorSubModule {

}
