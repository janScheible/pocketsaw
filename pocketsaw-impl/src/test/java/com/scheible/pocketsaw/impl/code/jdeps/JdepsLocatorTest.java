package com.scheible.pocketsaw.impl.code.jdeps;

import java.io.File;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author sj
 */
public class JdepsLocatorTest {

	@Test
	public void testJavaHomeEnvVariable() {
		assertThat(JdepsLocator.locate("/opt/java", null, file -> {
			final String dir = File.separator + "opt" + File.separator + "java" + File.separator + "bin" + File.separator + "jdeps";
			return dir.equals(file.toString());
		})).isPresent();
	}

	@Test
	public void testJavaHomePropertyOnly() {
		assertThat(JdepsLocator.locate(null, "/opt/java", file -> {
			final String dir = File.separator + "opt" + File.separator + "java" + File.separator + "bin" + File.separator + "jdeps";
			return dir.equals(file.toString());
		})).isPresent();

		assertThat(JdepsLocator.locate(null, "/opt/java/jre", file -> {
			final String dir = File.separator + "opt" + File.separator + "java" + File.separator + "jre" + File.separator + ".." + File.separator + "bin" + File.separator + "jdeps";
			return dir.equals(file.toString());
		})).isPresent();
	}
}
