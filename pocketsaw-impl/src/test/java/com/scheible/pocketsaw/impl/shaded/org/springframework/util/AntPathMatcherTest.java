package com.scheible.pocketsaw.impl.shaded.org.springframework.util;

import com.scheible.pocketsaw.impl.shaded.org.springframework.util.AntPathMatcher;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class AntPathMatcherTest {
	
	@Test
	public void test() {
		final AntPathMatcher matcher = new AntPathMatcher(".");
		
		
		assertThat(matcher.match("com.test.*", "com.test.")).isTrue();
		assertThat(matcher.match("com.test.*", "com.test")).isFalse();
		
		assertThat(matcher.match("com.test.*", "com.test.Bla")).isTrue();
		assertThat(matcher.match("com.test.*", "com.test.bla.Blub")).isFalse();
		
		assertThat(matcher.match("com.test.**", "com.test.Bla")).isTrue();
		assertThat(matcher.match("com.test.**", "com.test.bla.Blub")).isTrue();
		
		assertThat(matcher.match("**", "com.test.bla.")).isTrue();
		assertThat(matcher.match("**", "com.test.bla")).isTrue();
	}
}
