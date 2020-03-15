package com.scheible.pocketsaw.impl.code.classgraph.testclass;

import com.scheible.pocketsaw.api.ExternalFunctionality;

/**
 *
 * @author sj
 */
public class ExternalFunctionalitiesTest {

	@ExternalFunctionality(packageMatchPattern = {"org.springframework.context.**", "org.springframework.beans.**"})
	public static class Spring {
	}
}
