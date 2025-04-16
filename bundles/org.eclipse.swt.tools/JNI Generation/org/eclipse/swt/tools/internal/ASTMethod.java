/*******************************************************************************
 * Copyright (c) 2004, 2020 IBM Corporation and others.
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

import java.lang.reflect.Modifier;
import java.util.*;

import org.eclipse.jdt.core.dom.*;

public class ASTMethod extends ASTItem implements JNIMethod {
	String name, qualifiedName;
	int modifiers;
	ASTClass declaringClass;
	ASTType[] paramTypes;
	ASTType returnType;
	ASTParameter[] parameters;
	Boolean unique;
	String data;
	int start;
	
public ASTMethod(ASTClass declaringClass, MethodDeclaration method) {
	this.declaringClass = declaringClass;
	
	name = method.getName().getIdentifier();
	modifiers = method.getModifiers();
	start = method.getStartPosition();
	
	Javadoc doc = method.getJavadoc();
	List<TagElement> tags = null;
	if (doc != null) {
		tags = doc.tags();
		for (TagElement tag : tags) {
			if ("@method".equals(tag.getTagName())) {
				String data = tag.fragments().get(0).toString();
				setMetaData(data);
				break;
			}
		}
	}
	returnType = new ASTType(declaringClass.resolver, method.getReturnType2(), method.getExtraDimensions());
	
	List<SingleVariableDeclaration> parameters = method.parameters();
	paramTypes = new ASTType[parameters.size()];
	this.parameters = new ASTParameter[paramTypes.length];
	int i = 0;
	for (Iterator<SingleVariableDeclaration> iterator = parameters.iterator(); iterator.hasNext(); i++) {
		SingleVariableDeclaration param = iterator.next();
		paramTypes[i] = new ASTType(declaringClass.resolver, param.getType(), param.getExtraDimensions());
		this.parameters[i] = new ASTParameter(this, i, param.getName().getIdentifier());
	
		if (tags != null) {
			String name = param.getName().getIdentifier();
			for (TagElement tag : tags) {
				if ("@param".equals(tag.getTagName())) {
					List<?> fragments = tag.fragments();
					if (fragments.size() >= 2 && name.equals(fragments.get(0).toString())) {
						String data = fragments.get(1).toString();
						this.parameters[i].setMetaData(data);
					}
				}
			}
		}
	}
}

@Override
public JNIClass getDeclaringClass() {
	return declaringClass;
}

@Override
public int getModifiers() {
	return modifiers;
}

@Override
public String getName() {
	return name;
}

@Override
public boolean isNativeUnique() {
	if (unique != null) return unique.booleanValue();
	boolean result = true;
	String name = getName();
	for (JNIMethod mth : declaringClass.getDeclaredMethods()) {
		if ((mth.getModifiers() & Modifier.NATIVE) != 0 &&
			this != mth && !this.equals(mth) &&
			name.equals(mth.getName()))
			{
				result = false;
				break;
			}
	}
	unique = Boolean.valueOf(result);
	return result;
}

@Override
public JNIType[] getParameterTypes() {
	return paramTypes;
}

@Override
public JNIParameter[] getParameters() {
	return this.parameters;
}

@Override
public JNIType getReturnType() {
	return returnType;
}

@Override
public String getAccessor() {
	return (String)getParam("accessor");
}

@Override
public String getExclude() {
	return (String)getParam("exclude");
}

@Override
public String getMetaData() {
	if (data != null) return data;
	String className = getDeclaringClass().getSimpleName();
	String key = className + "_" + JNIGenerator.getFunctionName(this);
	MetaData metaData = declaringClass.metaData;
	String value = metaData.getMetaData(key, null);
	if (value == null) {
		key = className + "_" + getName();
		value = metaData.getMetaData(key, null);
	}
	/*
	* Support for lock.
	*/
	if (value == null && getName().startsWith("_")) {
		key = className + "_" + JNIGenerator.getFunctionName(this).substring(2);
		value = metaData.getMetaData(key, null);
		if (value == null) {
			key = className + "_" + getName().substring(1);
			value = metaData.getMetaData(key, null);
		}
	}
	if (value == null) value = "";	
	return value;
}

@Override
public void setAccessor(String str) { 
	setParam("accessor", str);
}

@Override
public void setExclude(String str) { 
	setParam("exclude", str);
}

@Override
public void setMetaData(String value) {
	data = value;
}

@Override
public String toString() {
	return getName();
}

}
