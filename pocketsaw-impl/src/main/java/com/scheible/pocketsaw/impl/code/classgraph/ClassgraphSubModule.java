package com.scheible.pocketsaw.impl.code.classgraph;

import com.scheible.pocketsaw.api.SubModule;
import com.scheible.pocketsaw.impl.ExternalFunctionalities.Classgraph;
import com.scheible.pocketsaw.impl.code.CodeSubModule;

/**
 * A code/package dependecy provider using the Classgraph library.
 * 
 * @author sj
 */
@SubModule(uses = {CodeSubModule.class, Classgraph.class})
public class ClassgraphSubModule {
	
}
