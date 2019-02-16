package com.scheible.pocketsaw.impl.descriptor.json;

import com.scheible.pocketsaw.impl.descriptor.json.JsonDescriptorReader.DescriptorJson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;

/**
 *
 * @author sj
 */
public class JsonDescriptorReaderTest {
	
	@Test
	public void testBasicRead() throws IOException {
		final DescriptorJson descriptors  = JsonDescriptorReader.read(readSubModuleJson("basic-sub-modules.json"));
		final List<SubModuleJson> modules = descriptors.getSubModules();
		Collections.sort(modules, (SubModuleJson first, SubModuleJson second) -> first.getName().compareTo(second.getName()));
		
		assertThat(modules).hasSize(2);
		
		assertThat(modules.get(0).getName()).isEqualTo("First");
		assertThat(modules.get(0).getPackageName()).isEqualTo("project.first");
		assertThat(modules.get(0).getColor()).get().isEqualTo("red");
		assertThat(modules.get(0).doIncludeSubPackages()).get().isEqualTo(false);
		assertThat(modules.get(0).getUsedModules()).isEmpty();
		
		assertThat(modules.get(1).getName()).isEqualTo("FirstChild");
		assertThat(modules.get(1).getPackageName()).isEqualTo("project.first.child");
		assertThat(modules.get(1).getColor()).isEmpty();
		assertThat(modules.get(1).doIncludeSubPackages()).isEmpty();
		assertThat(modules.get(1).getUsedModules()).containsExactly("First");
	}
	
	@Test
	public void testNonUniqueSubModuleName() throws IOException {
		assertThatThrownBy(() -> JsonDescriptorReader.read(readSubModuleJson("non-unique-sub-module-names.json")))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("The sub module names must be unique");
	}		
	
	@Test
	public void testNonUniqueExternalFunctionalityNames() throws IOException {
		assertThatThrownBy(() -> JsonDescriptorReader.read(readSubModuleJson("non-unique-external-functionality-names.json.json")))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("The external functionality names must be unique");
	}	
	
	@Test
	public void testWithExternalFunctionalities() throws IOException {
		final DescriptorJson descriptors  = JsonDescriptorReader.read(readSubModuleJson("basic-external-functionalities.json"));
		final List<ExternalFunctionalityJson> externalFunctionalities = descriptors.getExternalFunctionalities();
		Collections.sort(externalFunctionalities, (ExternalFunctionalityJson first, ExternalFunctionalityJson second) 
				-> first.getName().compareTo(second.getName()));
		Collections.sort(externalFunctionalities, (first, second) -> first.getName().compareTo(second.getName()));
		
		assertThat(externalFunctionalities).hasSize(2);

		assertThat(externalFunctionalities.get(0).getName()).isEqualTo("Guava");
		assertThat(externalFunctionalities.get(0).getPackageMatchPatterns()).containsOnly("com.google.common.**");
		
		assertThat(externalFunctionalities.get(1).getName()).isEqualTo("Spring");
		assertThat(externalFunctionalities.get(1).getPackageMatchPatterns()).containsOnly("org.springframework.beans.**", "org.springframework.context.**");
	}		
	
	private static String readSubModuleJson(String fileName) throws IOException {
		try (BufferedReader buffer = new BufferedReader(new InputStreamReader(
				JsonDescriptorReaderTest.class.getResource(fileName).openStream(), "UTF8"))) {
			return buffer.lines().collect(Collectors.joining("\n"));			
		}
	}
}
