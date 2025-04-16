/*******************************************************************************
 * Copyright (c) 2004, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Red Hat Inc. - generification
 *******************************************************************************/
package org.eclipse.swt.tools.internal;

import java.lang.reflect.*;

public class CleanupNatives extends CleanupClass {
	
public CleanupNatives() {
}

@Override
public void generate(JNIClass clazz) {
	unusedCount = usedCount = 0;
	super.generate(clazz);
	JNIMethod[] methods = clazz.getDeclaredMethods();
	generate(methods);
	output("used=" + usedCount + " unused=" + unusedCount + " total=" + (unusedCount + usedCount));
}

public void generate(JNIMethod[] methods) {
	sort(methods);	
	for (JNIMethod method : methods) {
		if ((method.getModifiers() & Modifier.NATIVE) == 0) continue;
		generate(method);
	}
}

public void generate(JNIMethod method) {
	String name = method.getName();
	for (String str : files.values()) {
		if (str.contains(name)) {
//			int modifiers = method.getModifiers();
//			Class clazz = method.getDeclaringClass();
//			String modifiersStr = Modifier.toString(modifiers);
//			output(modifiersStr);
//			if (modifiersStr.length() > 0) output(" ");
//			output(getTypeSignature3(method.getReturnType()));
//			output(" " );
//			output(method.getName());
//			output("(");
//			Class[] paramTypes = method.getParameterTypes();
//			String[] paramNames = getArgNames(method);
//			for (int i = 0; i < paramTypes.length; i++) {
//				Class paramType = paramTypes[i];
//				if (i != 0) output(", ");
//				String sig = getTypeSignature3(paramType);
//				if (clazz.getPackage().equals(paramType.getPackage())) sig = getClassName(paramType);
//				output(sig);
//				output(" ");
//				output(paramNames[i]);
//			}
//			outputln(");");
			usedCount++;
			return;
		}
	}
	unusedCount++;
	output("NOT USED=" + method.toString() + "\n");
}

public static void main(String[] args) {
	if (args.length < 2) {
		System.out.println("Usage: java CleanupNatives <OS className> <OS class source> <src path0> <src path1>");
		return;
	}
	try {
		CleanupNatives gen = new CleanupNatives();
		String clazzName = args[0];
		String classSource = args[1]; 
		String[] sourcePath = new String[args.length - 2];
		System.arraycopy(args, 2, sourcePath, 0, sourcePath.length);
		Class<?> clazz = Class.forName(clazzName);
		gen.setSourcePath(sourcePath);
		gen.setClassSourcePath(classSource);
		gen.generate(new ReflectClass(clazz));
	} catch (Exception e) {
		System.out.println("Problem");
		e.printStackTrace(System.out);
	}
}

}
