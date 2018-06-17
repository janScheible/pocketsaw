package com.scheible.pocketsaw.impl.descriptor;

import com.scheible.pocketsaw.impl.descriptor.SubModuleDescriptor;
import com.scheible.pocketsaw.impl.descriptor.PackageGroupNameProvider;
import com.scheible.pocketsaw.api.ExternalFunctionality;
import com.scheible.pocketsaw.api.SubModule;
import static com.scheible.pocketsaw.impl.descriptor.SubModuleDescriptorTest.SUB_MODULE_INCLUDE_SUB_PACKAGES;
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
		SubModuleDescriptor descriptor = SubModuleDescriptor.fromAnnotatedClass(TestSubModule.class);

		assertThat(descriptor.getName()).isEqualTo(PackageGroupNameProvider.getName(TestSubModule.class));
		assertThat(descriptor.getUsedExternalFunctionalities()).hasSize(1);
		assertThat(descriptor.getUsedSubModuleIds()).hasSize(1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void fromClassWithoutAnnotation() {
		SubModuleDescriptor.fromAnnotatedClass(SubModuleDescriptorTest.class);
	}

	@SubModule(uses = {SubModuleDescriptorTest.class})
	private static class TestSubModuleWithNotAnnotatedUsed {

	}

	@Test(expected = IllegalStateException.class)
	public void fromClassWithNotAnnotatedUsed() {
		SubModuleDescriptor.fromAnnotatedClass(TestSubModuleWithNotAnnotatedUsed.class);
	}
	
	@SubModule(value = {TestSubModule.class}, uses = {TestSubModule.class})
	private static class TestSubModuleWithValueAndUses {

	}

	@Test(expected = IllegalStateException.class)
	public void fromSubModuleWithValueAndUses() {
		SubModuleDescriptor.fromAnnotatedClass(TestSubModuleWithValueAndUses.class);
	}	
}
