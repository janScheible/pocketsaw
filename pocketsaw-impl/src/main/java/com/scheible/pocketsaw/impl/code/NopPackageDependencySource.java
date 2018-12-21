package com.scheible.pocketsaw.impl.code;

import java.io.File;
import java.util.HashMap;

/**
 *
 * @author sj
 */
public class NopPackageDependencySource implements PackageDependencySource {

	@Override
	public PackageDependencies read(File file) {
		return new PackageDependencies(new HashMap<>());
	}

	@Override
	public String getIdentifier() {
		return "internal-nop-package-dependency-source";
	}	
}
