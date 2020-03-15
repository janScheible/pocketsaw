package com.scheible.pocketsaw.impl;

import com.scheible.pocketsaw.api.ExternalFunctionality;

/**
 *
 * @author sj
 */
public class ExternalFunctionalities {

	@ExternalFunctionality(packageMatchPattern = {"org.springframework.context.**", "org.springframework.core.**", 
		"org.springframework.beans.**"})
	public static class Spring {
	}

	@ExternalFunctionality(packageMatchPattern = "io.github.lukehutch.fastclasspathscanner.**")
	public static class FastClasspathScanner {
	}
	
	@ExternalFunctionality(packageMatchPattern = "io.github.classgraph.**")
	public static class Classgraph {
	}
}
