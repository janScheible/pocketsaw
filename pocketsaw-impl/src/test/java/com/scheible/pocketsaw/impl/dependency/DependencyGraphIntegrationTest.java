package com.scheible.pocketsaw.impl.dependency;

import com.scheible.pocketsaw.impl.code.PackageDependencies;
import com.scheible.pocketsaw.impl.descriptor.DescriptorInfo;
import com.scheible.pocketsaw.impl.descriptor.ExternalFunctionalityDescriptor;
import com.scheible.pocketsaw.impl.descriptor.SubModuleDescriptor;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class DependencyGraphIntegrationTest {
	
	private PackageDependencies getPackageDependencies() {
		Map<String, Set<String>> deps = new HashMap<>();

		deps.put("com.scheible.javasubmodules.sample.adapter.first.FirstAdapter",
				new HashSet<>(Arrays.asList(
						"com.scheible.javasubmodules.sample.applicationservice.FirstApplicationService")));
		deps.put("com.scheible.javasubmodules.sample.adapter.second.SecondAdapter",
				new HashSet<>(Arrays.asList(
						"com.scheible.javasubmodules.sample.applicationservice.SecondApplicationService",
						"org.springframework.jms.annotation.JmsListener")));
		deps.put("com.scheible.javasubmodules.sample.applicationservice.FirstApplicationService",
				new HashSet<>(Arrays.asList(
						"com.scheible.javasubmodules.sample.domain.Domain",
						"com.scheible.javasubmodules.sample.store.second.FirstStore",
						"org.springframework.transaction.annotation.Transactional")));
		deps.put("com.scheible.javasubmodules.sample.applicationservice.SecondApplicationService",
				new HashSet<>(Arrays.asList(
						"com.scheible.javasubmodules.sample.domain.Domain",
						"com.scheible.javasubmodules.sample.store.second.SecondStore",
						"org.springframework.transaction.annotation.Transactional")));
		deps.put("com.scheible.javasubmodules.sample.domain.Domain",
				new HashSet<>(Arrays.asList(
						"com.scheible.javasubmodules.sample.store.first.FirstStore")));
		deps.put("com.scheible.javasubmodules.sample.store.first.FirstStore",
				new HashSet<>());
		deps.put("com.scheible.javasubmodules.sample.store.second.SecondStore",
				new HashSet<>());

		return new PackageDependencies(deps);
	}

	private DescriptorInfo getPackageGroups() {
		final ExternalFunctionalityDescriptor springTx = new ExternalFunctionalityDescriptor("1", "spring-tx", "org.springframework.transaction.**");
		final ExternalFunctionalityDescriptor springJms = new ExternalFunctionalityDescriptor("2", "spring-jms", "org.springframework.jms.**");
		final ExternalFunctionalityDescriptor googleGuava = new ExternalFunctionalityDescriptor("3", "google-guava", "com.google.common.**");

		final SubModuleDescriptor store = new SubModuleDescriptor("4", "store", "com.scheible.javasubmodules.sample.store", true,
				new HashSet<>(), new HashSet<>());
		final SubModuleDescriptor domain = new SubModuleDescriptor("5", "domain", "com.scheible.javasubmodules.sample.domain", true,
				new HashSet<>(), newHashSet(googleGuava));
		final SubModuleDescriptor applicationService = new SubModuleDescriptor("6", "application-service", "com.scheible.javasubmodules.sample.applicationservice", true,
				newHashSet("4", "5"), newHashSet(springTx));
		final SubModuleDescriptor adapter = new SubModuleDescriptor("7", "adapter", "com.scheible.javasubmodules.sample.adapter", true,
				newHashSet("6"), newHashSet(springJms));

		final HashSet<SubModuleDescriptor> subModules = new HashSet<>(Arrays.asList(domain, store, applicationService, adapter,
				new SubModuleDescriptor("8", "sample", "com.scheible.javasubmodules.sample", false, new HashSet<>(), new HashSet<>())));

		return new DescriptorInfo(subModules, new HashSet<>(Arrays.asList(springTx, springJms, googleGuava)));
	}

	@Test
	public void testDependencyGraph() throws IOException {
		final PackageDependencies packageDependencies = getPackageDependencies();
		final DescriptorInfo descriptorInfo = getPackageGroups();
		
		final DependencyGraph dependencyGraph = DependencyGraphFactory.create(packageDependencies,
				descriptorInfo.getSubModules(), descriptorInfo.getExternalFunctionalities());		
		
		assertThat(dependencyGraph.getAnyDescriptorCycle()).isEmpty();
		assertThat(dependencyGraph.getAnyCodeCycle()).isEmpty();
		assertThat(dependencyGraph.getIllegalCodeDependencies()).hasSize(1)
				.allMatch((dependency) -> dependency.getSource().getId().equals("5")
				&& dependency.getTarget().getId().equals("4"));
		
		// VisJsRenderer.render(dependencyGraph, Paths.getPackageDependencies("./target/java-project-sub-modules.html").toFile());
	}

	private Set<ExternalFunctionalityDescriptor> newHashSet(ExternalFunctionalityDescriptor... externalFunctionalities) {
		Set<ExternalFunctionalityDescriptor> result = new HashSet<>();
		for (ExternalFunctionalityDescriptor descriptor : externalFunctionalities) {
			result.add(descriptor);
		}
		return result;
	}

	private Set<String> newHashSet(String... subModuleIds) {
		Set<String> result = new HashSet<>();
		for (String subModuleId : subModuleIds) {
			result.add(subModuleId);
		}
		return result;
	}
}
