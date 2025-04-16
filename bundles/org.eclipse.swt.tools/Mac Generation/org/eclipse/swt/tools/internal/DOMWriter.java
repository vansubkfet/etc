/*******************************************************************************
 * Copyright (c) 2008, 2020 IBM Corporation and others.
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

import org.w3c.dom.*;

public class DOMWriter {

	static String ENCONDING = "UTF8";
	PrintStream out;
	String[] attributeFilter;
	String[] idAttributes;
	String nodeFilter;

	public DOMWriter(PrintStream out) {
		this.out = new PrintStream(out);
	}

	String nodeName(Node node) {
		// TODO use getLocalName()?
		return node.getNodeName();
	}
	
	boolean filter(Attr attr) {
		if (attributeFilter == null) return false;
		String name = attr.getNodeName();
		for (String filteredName : attributeFilter) {
			if (name.matches(filteredName)) return false;
		}
		return true;
	}
	
	Node getIDAttribute(Node node) {
		NamedNodeMap attributes = node.getAttributes();
		if (attributes == null) return null;
		for (String name : idAttributes) {
			Node nameAttrib = attributes.getNamedItem(name);
			if (nameAttrib != null) return nameAttrib;
		}
		return null;
	}
	
	void print(String str) {
		out.print(str);
	}
	void println() {
		out.println();
	}

	public void print(Node node) {
		print(node, 0);
	}
	
	public void print(Node node, int level) {
		if (node == null)
			return;
		int type = node.getNodeType();
		switch (type) {
			case Node.DOCUMENT_NODE: {
				print("<?xml version=\"1.0\" encoding=\"");
				print(ENCONDING);
				print("\"?>");
				println();
				print(((Document) node).getDocumentElement());
				break;
			}
			case Node.ELEMENT_NODE: {
				Attr attrs[] = sort(node.getAttributes());
				String name = nodeName(node);
				boolean isArg = name.equals("arg");
				boolean gen = isArg || name.equals("retval");
				for (int i = 0; i < attrs.length && !gen; i++) {
					Attr attr = attrs[i];
					if (nodeName(attr).startsWith(nodeFilter)) gen = true;
				}
				if (!gen) break;
				for (int i = 0; i < level; i++) print("\t");
				print("<");
				print(name);
				for (Attr attr : attrs) {
					if (isArg && "name".equals(attr.getNodeName())) continue;
					if (filter(attr)) continue;
					print(" ");
					print(nodeName(attr));
					print("=\"");
					print(normalize(attr.getNodeValue()));
					print("\"");
				}
				print(">");
				NodeList children = node.getChildNodes();
				if (children != null) {
					int len = children.getLength();
					List<Node> nodes = new ArrayList<>();
					for (int i = 0; i < len; i++) {
						Node child = children.item(i);
						if (child.getNodeType() == Node.ELEMENT_NODE) nodes.add(child);
					}
					int count = nodes.size();
					nodes.sort((a, b) -> {
						String nameA = a.getNodeName();
						String nameB = b.getNodeName();
						if ("arg".equals(nameA)) {
							return 0;
						}
						int result = nameA.compareTo(nameB);
						if (result == 0) {
							Node idA = getIDAttribute(a);
							Node idB = getIDAttribute(b);
							if (idA == null || idB == null)
								return 0;
							return idA.getNodeValue().compareTo(idB.getNodeValue());
						}
						return result;
					});
					if (count > 0) println();
					for (int i = 0; i < count; i++) {
						print(nodes.get(i), level + 1);
					}
					if (count > 0) {
						for (int i = 0; i < level; i++) print("\t");
					}
				}
				print("</");
				print(nodeName(node));
				print(">");
				println();
				break;
			}
		}
		out.flush();
	}

	Attr[] sort(NamedNodeMap attrs) {
		if (attrs == null)
			return new Attr[0];
		Attr result[] = new Attr[attrs.getLength()];
		for (int i = 0; i < result.length; i++) {
			result[i] = (Attr) attrs.item(i);
		}
		Arrays.sort(result, (arg0, arg1) -> nodeName(arg0).compareTo(nodeName(arg1)));
		return result;
	}

	String normalize(String s) {
		if (s == null) return "";
		StringBuilder str = new StringBuilder();
		for (int i = 0, length = s.length(); i < length; i++) {
			char ch = s.charAt(i);
			switch (ch) {
				case '"': str.append("\""); break;
				case '\r':
				case '\n':
					// FALL THROUGH
				default: str.append(ch);
			}
		}
		return str.toString();
	}
	
	public void setNodeFilter(String filter) {
		
		nodeFilter = filter;
	}
	
	public void setAttributeFilter(String[] filter) {
		attributeFilter = filter;
	}
	
	public void setIDAttributes(String[] ids) {
		idAttributes = ids;
	}
}