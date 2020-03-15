package com.scheible.pocketsaw.impl.code.classgraph.testclass.bc;

import com.scheible.pocketsaw.impl.code.classgraph.testclass.a.A;

/**
 *
 * @author sj
 */
public class C {

	final A a = new A();

	String todo() {
		return a.toString();
	}
}
