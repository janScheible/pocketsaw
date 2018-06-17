package com.scheible.pocketsaw.impl;

import com.scheible.pocketsaw.api.ExternalFunctionality;

/**
 *
 * @author sj
 */
public class ExternalFunctionalities {

	@ExternalFunctionality(packageMatchPattern = "org.springframework.context.**")
	public static class SpringContext {
	}

	@ExternalFunctionality(packageMatchPattern = "org.springframework.core.**")
	public static class SpringCore {
	}

	@ExternalFunctionality(packageMatchPattern = "org.springframework.beans.**")
	public static class SpringBeans {
	}

	@ExternalFunctionality(packageMatchPattern = "io.github.lukehutch.fastclasspathscanner.**")
	public static class FastClasspathScanner {
	}
}
