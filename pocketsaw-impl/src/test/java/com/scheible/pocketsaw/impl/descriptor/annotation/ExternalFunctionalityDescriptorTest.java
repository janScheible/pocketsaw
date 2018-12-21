package com.scheible.pocketsaw.impl.descriptor.annotation;

import org.junit.Test;

/**
 *
 * @author sj
 */
public class ExternalFunctionalityDescriptorTest {

	@Test(expected = IllegalArgumentException.class)
	public void fromClassWithoutAnnotation() {
		AnnotationDescriptorInfoFactory.fromAnnotatedExternalFunctionalityClass(ExternalFunctionalityDescriptorTest.class);
	}
}
