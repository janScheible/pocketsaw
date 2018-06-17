package com.scheible.pocketsaw.impl.matching;

import com.scheible.pocketsaw.impl.matching.PackageMatcher;
import com.scheible.pocketsaw.impl.descriptor.ExternalFunctionalityDescriptor;
import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class PackageMatcherTest {

	@Test
	public void testMatchPrecedenceSortiung() {
		assertThat(new PackageMatcher<>(new HashSet<>(Arrays.asList(new ExternalFunctionalityDescriptor("1", "adapter", "adapter.**"),
				new ExternalFunctionalityDescriptor("2", "application-service", "applicationservice.*"),
				new ExternalFunctionalityDescriptor("3", "domain", "store.first.**"),
				new ExternalFunctionalityDescriptor("4", "domain", "store.second.*")))).getPackageGroups()
				.stream().map(def -> def.getPackageMatchPattern()).collect(Collectors.toList()))
				.containsExactly("store.second.*", "store.first.**", "applicationservice.*", "adapter.**");
	}
}
