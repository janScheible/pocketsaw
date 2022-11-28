package com.scheible.pocketsaw.impl;

import com.scheible.pocketsaw.impl.Pocketsaw.AnalysisResult;
import com.scheible.pocketsaw.impl.code.PackageDependencies;
import com.scheible.pocketsaw.impl.descriptor.DescriptorInfo;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author sj
 */
public class PocketsawTest {

	@Test
	public void testThirdPartyDependencyInformationMode() {
		final DescriptorInfo descriptorInfo = new DescriptorInfo(new HashSet<>(), new HashSet<>(), false);
		final PackageDependencies packageDependencies = new PackageDependencies(new HashMap<>());

		final AnalysisResult result = Pocketsaw.analize(descriptorInfo, packageDependencies, 
				Optional.of(new File("./target/third-party-pocketsaw-dependency-graph.html")));

		assertThat(result).isNotNull();
	}
}
