package com.scheible.pocketsaw.impl.descriptor.json;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
public class SubModuleJsonTest {
	
	@Test
	public void testBasicRead() throws IOException {
		List<SubModuleJson> modules = new ArrayList<>(SubModuleJson.read(readSubModuleJson("basic-sub-modules.json")));
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
	public void testNonUniquePackageName() throws IOException {
		assertThatThrownBy(() -> SubModuleJson.read(readSubModuleJson("non-unique-pacakge-name-sub-modules.json")))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("The sub module names must be unique");
	}		
	
	private static String readSubModuleJson(String fileName) throws IOException {
		try (BufferedReader buffer = new BufferedReader(new InputStreamReader(
				SubModuleJsonTest.class.getResource(fileName).openStream(), "UTF8"))) {
			return buffer.lines().collect(Collectors.joining("\n"));			
		}
	}
}
