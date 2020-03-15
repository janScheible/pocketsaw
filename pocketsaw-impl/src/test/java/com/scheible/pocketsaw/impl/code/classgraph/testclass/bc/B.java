package com.scheible.pocketsaw.impl.code.classgraph.testclass.bc;

import com.scheible.pocketsaw.impl.code.classgraph.testclass.MySuperClass;
import com.scheible.pocketsaw.impl.code.classgraph.testclass.a.A;

/**
 *
 * @author sj
 */
class B extends MySuperClass {
	
	final A a = new A();
	
	String todo() {
		return a.toString();
	}
}
