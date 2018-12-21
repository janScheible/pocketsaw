package com.scheible.pocketsaw.impl.code;

import java.io.File;

/**
 * This interface has to be implemented by classes providing third-party dependency information. A no-args constructor
 * is mandatory because {@link java.util.ServiceLoader} is used to find and instantiate the implementing classes.
 * 
 * @author sj
 */
public interface PackageDependencySource {

	PackageDependencies read(File file);

	String getIdentifier();
}
