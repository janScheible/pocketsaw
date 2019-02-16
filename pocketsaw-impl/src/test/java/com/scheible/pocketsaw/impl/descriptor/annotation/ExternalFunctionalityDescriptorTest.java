package com.scheible.pocketsaw.impl.descriptor.annotation;

import com.scheible.pocketsaw.api.ExternalFunctionality;
import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 *
 * @author sj
 */
public class ExternalFunctionalityDescriptorTest {

	@ExternalFunctionality(packageMatchPattern = {})
	static class TestExternalFunctionality {

	}

	@Test
	public void fromClassWithoutAnnotation() {
		Assertions.assertThatThrownBy(()
				-> AnnotationDescriptorInfoFactory.fromAnnotatedExternalFunctionalityClass(ExternalFunctionalityDescriptorTest.class))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining(ExternalFunctionalityDescriptorTest.class.getName())
				.hasMessageContaining("was found on class")
				.hasMessageContaining(ExternalFunctionality.class.getSimpleName());
	}

	@Test
	public void fromClassWithoutPackageMatchPattern() {
		Assertions.assertThatThrownBy(()
				-> AnnotationDescriptorInfoFactory.fromAnnotatedExternalFunctionalityClass(TestExternalFunctionality.class))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining(TestExternalFunctionality.class.getName())
				.hasMessageContaining("has no packageMatchPatterns defined")
				.hasMessageContaining(ExternalFunctionality.class.getSimpleName());
	}
}
