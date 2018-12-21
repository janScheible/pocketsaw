package com.scheible.pocketsaw.impl.descriptor;

import com.scheible.pocketsaw.api.ExternalFunctionality;
import com.scheible.pocketsaw.api.SubModule;
import java.lang.reflect.Method;

/**
 *
 * @author sj
 */
class PackageGroupColorProvider {

	static String getSubModuleDefaultColor() {
		try {
			Method method = SubModule.class.getMethod("color");
			return (String) method.getDefaultValue();
		} catch (NoSuchMethodException | SecurityException ex) {
			throw new IllegalStateException(ex);
		}
	}

	static String getExternalFunctionalityDefaultColor() {
		try {
			Method method = ExternalFunctionality.class.getMethod("color");
			return (String) method.getDefaultValue();
		} catch (NoSuchMethodException | SecurityException ex) {
			throw new IllegalStateException(ex);
		}
	}
}
