package com.scheible.pocketsaw.impl.descriptor.json;

import com.scheible.pocketsaw.api.SubModule;
import com.scheible.pocketsaw.impl.descriptor.DescriptorSubModule;
import com.scheible.pocketsaw.impl.shaded.com.eclipsesource.json.MinimalJsonSubModule;

/**
* Creates sub modules from an JSON file.
 *
 * @author sj
 */
@SubModule(uses = {DescriptorSubModule.class, MinimalJsonSubModule.class})
public class JsonDescriptorSubModule {

}
