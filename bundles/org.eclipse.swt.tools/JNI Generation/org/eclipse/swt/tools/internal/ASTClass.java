/*******************************************************************************
 * Copyright (c) 2004, 2021 IBM Corporation and others.
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

import java.io.*;
import java.util.*;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.swt.tools.internal.ASTType.*;

public class ASTClass extends ASTItem implements JNIClass {
	String sourcePath;
	MetaData metaData;

	ASTClass superclass;
	ASTField[] fields;
	ASTMethod[] methods;
	String name, simpleName, superclassName, packageName;
	String[] imports;
	String data;
	int start;
	
	TypeResolver resolver = new TypeResolver() {
		@Override
		public String findPath(String simpleName) {
			if (simpleName.equals(ASTClass.this.simpleName)) return sourcePath;
			String basePath = sourcePath.substring(0, sourcePath.length() - name.length() - ".java".length());
			File file = new File(basePath + packageName.replace('.', '/') + "/" + simpleName + ".java");
			if (file.exists()) {
				return file.getAbsolutePath();
			}
			for (String imp : imports) {
				file = new File(basePath + imp.replace('.', '/') + "/" + simpleName + ".java");
				if (file.exists()) {
					return file.getAbsolutePath();				
				}
			}
			return "";
		}
		@Override
		public String resolve(String simpleName) {
			if (simpleName.equals(ASTClass.this.simpleName)) return packageName + "." + simpleName;
			String basePath = sourcePath.substring(0, sourcePath.length() - name.length() - ".java".length());
			File file = new File(basePath + packageName.replace('.', '/') + "/" + simpleName + ".java");
			if (file.exists()) {
				return packageName + "." + simpleName;				
			}
			for (String imp : imports) {
				file = new File(basePath + imp.replace('.', '/') + "/" + simpleName + ".java");
				if (file.exists()) {
					return imp + "." + simpleName;				
				}
			}
			return simpleName;
		}
	};

public ASTClass(String sourcePath, MetaData metaData) {
	this.sourcePath = sourcePath;
	this.metaData = metaData;
	
	String source = JNIGenerator.loadFile(sourcePath);
	ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
	parser.setSource(source.toCharArray());
	CompilationUnit unit = (CompilationUnit)parser.createAST(null);
	TypeDeclaration type = (TypeDeclaration)unit.types().get(0);
	simpleName = type.getName().getIdentifier();
	packageName = unit.getPackage().getName().getFullyQualifiedName();
	name = packageName + "." + simpleName;
	superclassName = type.getSuperclassType() != null ? type.getSuperclassType().toString() : null;
	List<ImportDeclaration> imports = unit.imports();
	this.imports = new String[imports.size()];
	int count = 0;
	for (ImportDeclaration imp : imports) {
		this.imports[count++] = imp.getName().getFullyQualifiedName();
	}
	start = type.getStartPosition();
	
	Javadoc doc = type.getJavadoc();
	List<TagElement> tags = null;
	if (doc != null) {
		tags = doc.tags();
		for (TagElement tag : tags) {
			if ("@jniclass".equals(tag.getTagName())) {
				String data = tag.fragments().get(0).toString();
				setMetaData(data);
				break;
			}
		}
	}

	List<ASTField> fid = new ArrayList<>();
	for (FieldDeclaration field : type.getFields()) {
		List<VariableDeclarationFragment> fragments = field.fragments();
		for (VariableDeclarationFragment fragment : fragments) {
			fid.add(new ASTField(this, field, fragment));
		}
	}
	this.fields = fid.toArray(new ASTField[fid.size()]);
	List<ASTMethod> mid = new ArrayList<>();
	for (MethodDeclaration method : type.getMethods()) {
		if (method.getReturnType2() == null) continue;
		mid.add(new ASTMethod(this, method));
	}
	this.methods = mid.toArray(new ASTMethod[mid.size()]);
}

@Override
public int hashCode() {
	return getName().hashCode();
}

@Override
public boolean equals(Object obj) {
	if (this == obj) return true;
	if (!(obj instanceof ASTClass)) return false;
	return ((ASTClass)obj).getName().equals(getName());
}

@Override
public JNIField[] getDeclaredFields() {
	JNIField[] result = new JNIField[fields.length];
	System.arraycopy(fields, 0, result, 0, result.length);
	return result;
}

@Override
public JNIMethod[] getDeclaredMethods() {
	JNIMethod[] result = new JNIMethod[methods.length];
	System.arraycopy(methods, 0, result, 0, result.length);
	return result;
}

@Override
public String getName() {
	return name;
}

@Override
public JNIClass getSuperclass() {
	if (superclassName == null) return new ReflectClass(Object.class);
	if (superclass != null) return superclass;
	String sourcePath = resolver.findPath(superclassName);
	return superclass = new ASTClass(sourcePath, metaData);
}

@Override
public String getSimpleName() {
	return simpleName;
}

@Override
public String getExclude() {
	return (String)getParam("exclude");
}

@Override
public String getMetaData() {
	if (data != null) return data;
	String key = JNIGenerator.toC(getName());
	return metaData.getMetaData(key, "");
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
