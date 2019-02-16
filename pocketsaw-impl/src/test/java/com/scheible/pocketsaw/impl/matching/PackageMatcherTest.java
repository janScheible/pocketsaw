package com.scheible.pocketsaw.impl.matching;

import com.scheible.pocketsaw.impl.descriptor.ExternalFunctionalityDescriptor;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class PackageMatcherTest {

	@Test
	public void testMatchPrecedenceSorting() {
		List<String> packageMatchPatterns = new PackageMatcher<>(new HashSet<>(Arrays.asList(
				new ExternalFunctionalityDescriptor("1", "adapter", new HashSet<>(Arrays.asList("adapter.**"))),
				new ExternalFunctionalityDescriptor("2", "application-service", new HashSet<>(Arrays.asList("applicationservice.*", "infrastructure.*"))),
				new ExternalFunctionalityDescriptor("3", "domain", new HashSet<>(Arrays.asList("store.first.**"))),
				new ExternalFunctionalityDescriptor("4", "domain", new HashSet<>(Arrays.asList("store.second.*")))))).getPackageMatchPatterns();
		
		assertThat(packageMatchPatterns)
				.containsExactly("store.second.*", "store.first.**", "applicationservice.*", "infrastructure.*", "adapter.**");
	}
}
