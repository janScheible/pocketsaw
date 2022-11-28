package com.scheible.pocketsaw.impl.matching;

import com.scheible.pocketsaw.api.SubModule;
import com.scheible.pocketsaw.impl.shaded.org.springframework.util.SpringUtilSubModule;

/**
 * Helps to map package name to groups.
 *
 * @author sj
 */
@SubModule(includeSubPackages = false, uses = {SpringUtilSubModule.class})
public class MatchingSubModule {

}
