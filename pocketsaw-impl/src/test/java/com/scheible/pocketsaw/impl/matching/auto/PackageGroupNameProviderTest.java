package com.scheible.pocketsaw.impl.matching.auto;

import com.scheible.pocketsaw.impl.matching.auto.PackageGroupNameProvider;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;


/**
 *
 * @author sj
 */
public class PackageGroupNameProviderTest {
	
	@Test
	public void testPackageToName() {
		assertThat(PackageGroupNameProvider.packageToName("test")).isEqualTo("Test");
		assertThat(PackageGroupNameProvider.packageToName("com.test")).isEqualTo("ComTest");
	}
}
