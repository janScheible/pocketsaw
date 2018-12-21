package com.scheible.pocketsaw.impl.cli;

import com.scheible.pocketsaw.api.SubModule;
import com.scheible.pocketsaw.impl.PocketsawSubModule;
import com.scheible.pocketsaw.impl.code.CodeSubModule;
import com.scheible.pocketsaw.impl.descriptor.DescriptorSubModule;
import com.scheible.pocketsaw.impl.descriptor.json.JsonDescriptorSubModule;

/**
 * A code/package dependecy provider using the JDK command line tool jdeps.
 *
 * @author sj
 */
@SubModule({CodeSubModule.class, PocketsawSubModule.class, JsonDescriptorSubModule.class, DescriptorSubModule.class})
public class CliSubModule {

}
