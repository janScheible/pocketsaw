package com.scheible.pocketsaw.impl.code.classgraph.testclass;

import com.scheible.pocketsaw.api.SubModule;
import com.scheible.pocketsaw.impl.code.classgraph.testclass.ExternalFunctionalitiesTest.Spring;
import com.scheible.pocketsaw.impl.code.classgraph.testclass.a.A;
import com.scheible.pocketsaw.impl.code.classgraph.testclass.bc.C;

/**
 *
 * @author sj
 */
public class SubModulesTest {

	@SubModule(basePackageClass = A.class, uses = {Spring.class, TestClassSubModule.class})
	public static class ASubModule {
	}

	@SubModule(basePackageClass = C.class, uses = {TestClassSubModule.class, ASubModule.class})
	public static class BCSubModule {
	}

	@SubModule(basePackageClass = MyInterface.class, includeSubPackages = false)
	public static class TestClassSubModule {
	}
}
