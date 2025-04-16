/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.swt.tools.internal;

public interface JNIMethod extends JNIItem {

	public static final String[] FLAGS = {FLAG_NO_GEN, FLAG_ADDRESS, FLAG_CONST, FLAG_DYNAMIC, FLAG_JNI, FLAG_CAST, FLAG_CPP, FLAG_NEW, FLAG_DELETE, FLAG_GCNEW, FLAG_OBJECT, FLAG_SETTER, FLAG_GETTER, FLAG_ADDER, FLAG_IGNORE_DEPRECATIONS};
	
public String getName();

public int getModifiers();

public boolean isNativeUnique();

public JNIParameter[] getParameters();

public JNIType getReturnType();

public JNIType[] getParameterTypes();

public JNIClass getDeclaringClass();

public String getAccessor();

public String getExclude();

public void setAccessor(String str);

public void setExclude(String str);
}
