package com.scheible.pocketsaw.impl.descriptor.annotation;

import com.scheible.pocketsaw.api.ExternalFunctionality;
import com.scheible.pocketsaw.api.SubModule;
import com.scheible.pocketsaw.impl.descriptor.SubModuleDescriptor;
import static com.scheible.pocketsaw.impl.descriptor.annotation.SubModuleDescriptorTest.SUB_MODULE_INCLUDE_SUB_PACKAGES;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author sj
 */
public class SubModuleDescriptorTest {

	static final String SUB_MODULE_DESCRIPTION = ":-)";
	static final boolean SUB_MODULE_INCLUDE_SUB_PACKAGES = false;

	@SubModule(uses = {TestUsedExternalFunctionality.class, TestUsedSubModule.class},
			includeSubPackages = SUB_MODULE_INCLUDE_SUB_PACKAGES)
	private static class TestSubModule {

	}

	@ExternalFunctionality(packageMatchPattern = ".**")
	private static class TestUsedExternalFunctionality {

	}

	@SubModule()
	private static class TestUsedSubModule {

	}

	@Test
	public void fromAnnotatedClass() {
		SubModuleDescriptor descriptor = AnnotationDescriptorInfoFactory.fromAnnotatedSubModuleClass(TestSubModule.class);

		assertThat(descriptor.getName()).isEqualTo(PackageGroupNameProvider.getName(TestSubModule.class));
		assertThat(descriptor.getUsedExternalFunctionalities()).hasSize(1);
		assertThat(descriptor.getUsedSubModuleIds()).hasSize(1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void fromClassWithoutAnnotation() {
		AnnotationDescriptorInfoFactory.fromAnnotatedSubModuleClass(SubModuleDescriptorTest.class);
	}

	@SubModule(uses = {SubModuleDescriptorTest.class})
	private static class TestSubModuleWithNotAnnotatedUsed {

	}

	@Test(expected = IllegalStateException.class)
	public void fromClassWithNotAnnotatedUsed() {
		AnnotationDescriptorInfoFactory.fromAnnotatedSubModuleClass(TestSubModuleWithNotAnnotatedUsed.class);
	}
	
	@SubModule(value = {TestSubModule.class}, uses = {TestSubModule.class})
	private static class TestSubModuleWithValueAndUses {

	}

	@Test(expected = IllegalStateException.class)
	public void fromSubModuleWithValueAndUses() {
		AnnotationDescriptorInfoFactory.fromAnnotatedSubModuleClass(TestSubModuleWithValueAndUses.class);
	}	
}
