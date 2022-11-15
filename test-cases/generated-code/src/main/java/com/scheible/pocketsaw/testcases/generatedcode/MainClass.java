package com.scheible.pocketsaw.testcases.generatedcode;

import com.scheible.pocketsaw.testcases.generatedcode.library.Library;

/**
 *
 * @author sj
 */
public class MainClass {
	
	private final Library library = new Library();
	
	public static void main(String... args) {
		new MainClass().library.doIt();
	}
}
