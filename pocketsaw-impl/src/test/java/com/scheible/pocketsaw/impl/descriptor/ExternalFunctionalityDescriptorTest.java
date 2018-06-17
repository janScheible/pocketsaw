package com.scheible.pocketsaw.impl.descriptor;

import com.scheible.pocketsaw.impl.descriptor.ExternalFunctionalityDescriptor;
import org.junit.Test;

/**
 *
 * @author sj
 */
public class ExternalFunctionalityDescriptorTest {

	@Test(expected = IllegalArgumentException.class)
	public void fromClassWithoutAnnotation() {
		ExternalFunctionalityDescriptor.fromAnnotatedClass(ExternalFunctionalityDescriptorTest.class);
	}
}
