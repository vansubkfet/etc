/*******************************************************************************
 * Copyright (c) 2008, 2016 IBM Corporation and others.
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

import java.util.*;

import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Text;
import org.w3c.dom.*;

public class MacGeneratorUI {
	MacGenerator gen;
	boolean actions = true;
	public static boolean SHOW_SWT_PREFIX = true;

	Tree nodesTree;
	Table attribTable;

	public MacGeneratorUI(MacGenerator gen) {
		this.gen = gen;
	}

	TreeItem lastParent;
	TreeItem addChild (Node node, TreeItem superItem) {
		if (node.getNodeType() == Node.TEXT_NODE) return null;
		String name = node.getNodeName();
		TreeItem parentItem = null;
		if (lastParent != null && !lastParent.isDisposed() && lastParent.getParentItem() == superItem && name.equals(lastParent.getData())) {
			parentItem = lastParent;
		} else {
			for (TreeItem item : superItem.getItems()) {
				if (name.equals(item.getData())) {
					parentItem = item;
					break;
				}
			}
			if (parentItem == null) {
				parentItem = new TreeItem(superItem, SWT.NONE);
				parentItem.setData(name);
				parentItem.setText(getPrettyText(name));
			}
			lastParent = parentItem;
		}
		TreeItem item = new TreeItem(parentItem, SWT.NONE);
		Node idAttrib = gen.getIDAttribute(node);
		item.setText(idAttrib != null ? idAttrib.getNodeValue() : name);
		item.setData(node);
		checkItem(node, item);
		NodeList childNodes = node.getChildNodes();
		if (childNodes.getLength() > 0) new TreeItem(item, SWT.NONE);
		return item;
	}
	
	void checkPath(TreeItem item, boolean checked, boolean grayed) {
		if (item == null) return;
		if (grayed) {
			checked = true;
		} else {
			int index = 0;
			TreeItem[] items = item.getItems();
			while (index < items.length) {
				TreeItem child = items[index];
				if (child.getGrayed() || checked != child.getChecked()) {
					checked = grayed = true;
					break;
				}
				index++;
			}
		}
		item.setChecked(checked);
		item.setGrayed(grayed);
		updateGenAttribute(item);
		checkPath(item.getParentItem(), checked, grayed);
	}
	
	void checkItem(Node node, TreeItem item) {
		NamedNodeMap attributes = node.getAttributes();
		Node gen = attributes.getNamedItem("swt_gen");
		if (gen != null) {
			String value = gen.getNodeValue();
			boolean grayed = value.equals("mixed");
			boolean checked = grayed || value.equals("true");
			item.setChecked(checked);
			item.setGrayed(grayed);
		}
	}
	
	boolean getEditable(TableItem item, int column) {
		if (!(item.getData() instanceof Node)) return false;
		if (column == 0) return false;
		String attribName = item.getText();
		return attribName.startsWith("swt_") || item.getData("swt_") != null;
	}

	String getPrettyText(String text) {
		if (text.equals("class")) return "Classes";
		if (text.equals("depends_on")) return "Depends_on";
		return text.substring(0, 1).toUpperCase() + text.substring(1) + "s";
	}

	void checkChildren(TreeItem item) {
		TreeItem dummy;
		if (item.getItemCount() == 1 && (dummy = item.getItem(0)).getData() == null) {
			dummy.dispose();
			Node node = (Node)item.getData();
			NodeList childNodes = node.getChildNodes();
			for (int i = 0, length = childNodes.getLength(); i < length; i++) {
				addChild(childNodes.item(i), item);
			}
			/* Figure out categories state */
			for (TreeItem child : item.getItems()) {
				TreeItem[] children = child.getItems();
				int checkedCount = 0;
				for (TreeItem element : children) {
					if (element.getChecked()) checkedCount++;
					if (element.getGrayed()) break;
				}
				child.setChecked(checkedCount != 0);
				child.setGrayed(checkedCount != children.length);
			}
		}
	}
	
	void checkItems(TreeItem item, boolean checked) {
		item.setGrayed(false);
		item.setChecked(checked);
		updateGenAttribute(item);
		TreeItem[] items = item.getItems();
		if (items.length == 1 && items[0].getData() == null) {
			/* Update model only if view is not created */
			Node node = (Node)item.getData();
			NodeList childNodes = node.getChildNodes();
			for (int i = 0, length = childNodes.getLength(); i < length; i++) {
				checkNodes(childNodes.item(i), checked);
			}
		} else {
			for (TreeItem item2 : items) {
				checkItems(item2, checked);
			}
		}
	}
	
	void checkNodes(Node node, boolean checked) {
		if (node instanceof Element) {
			if (checked) {
				((Element)node).setAttribute("swt_gen", "true");
			} else {
				((Element)node).removeAttribute("swt_gen");
			}
		}
		NodeList childNodes = node.getChildNodes();
		for (int i = 0, length = childNodes.getLength(); i < length; i++) {
			checkNodes(childNodes.item(i), checked);
		}
	}
	
	void cleanup() {
	}

	Composite createSignaturesPanel(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginLeft = 5;
		layout.marginWidth = 0;
		comp.setLayout(layout);
		
		Label label = new Label(comp, SWT.NONE);
		label.setText("Signatures:");
		
		final Text search = new Text(comp, SWT.BORDER | SWT.SINGLE | SWT.SEARCH);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		search.setLayoutData(data);
		search.setText(".*");
		search.addListener(SWT.DefaultSelection, arg0 -> searchFor(search.getText()));
		search.addListener(SWT.KeyDown, event -> {
			if (event.keyCode == SWT.F6) {
				searchFor(search.getText());					
			}
		});
		
		nodesTree = new Tree(comp, SWT.SINGLE | SWT.CHECK | SWT.BORDER | SWT.FULL_SELECTION);
		data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 2;
		nodesTree.setLayoutData(data);
		
		nodesTree.addListener(SWT.Selection, event -> {
			TreeItem item = (TreeItem)event.item;
			if (item == null) return;
			if (event.detail != SWT.CHECK) {
				selectChild(item);
				return;
			}
			boolean checked = item.getChecked();
			item.getParent().setRedraw(false);
			checkItems(item, checked);
			checkPath(item.getParentItem(), checked, false);
			item.getParent().setRedraw(true);
		});
		nodesTree.addListener(SWT.Expand, event -> checkChildren((TreeItem)event.item));
		
		return comp;
	}
	
	Composite createPropertiesPanel(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		if (!actions) layout.marginRight = 5;
		comp.setLayout(layout);
		
		Label label = new Label(comp, SWT.NONE);
		label.setText("Properties:");
		
		attribTable = new Table(comp, SWT.BORDER | SWT.FULL_SELECTION);
		GridData data = new GridData(GridData.FILL_BOTH);
		attribTable.setLayoutData(data);
		attribTable.setLinesVisible(true);
		attribTable.setHeaderVisible(true);
		TableColumn nameColumn = new TableColumn(attribTable, SWT.NONE);
		nameColumn.setText("Name");
		nameColumn.pack();
		TableColumn valueColumn = new TableColumn(attribTable, SWT.NONE);
		valueColumn.setText("Value");
		valueColumn.pack();
		
		final Text editorTx = new Text(attribTable, SWT.SINGLE);
		final TableEditor editor = new TableEditor(attribTable);
		editor.grabHorizontal = true;
		editor.setEditor(editorTx);
		Listener textListener = e -> {
			if (e.type == SWT.KeyDown) {
				if (e.keyCode != SWT.F6) return;
			}
			if (e.type == SWT.Traverse) {
				switch (e.detail) {
					case SWT.TRAVERSE_ESCAPE:
						editor.setItem(null);
						break;
					default:
						return;
				}
			}
			editorTx.setVisible(false);
			TableItem item = editor.getItem();
			if (item == null) return;
			int column = editor.getColumn();
			String value = editorTx.getText();
			item.setText(column, value);
			Element node = (Element)item.getData();
			String name = item.getText();
			if (!name.startsWith("swt_")) {
				name = "swt_" + name;
			}
			if (value.length() != 0) {
				node.setAttribute(name, value);
			} else {
				node.removeAttribute(name);
			}
		};
		editorTx.addListener(SWT.DefaultSelection, textListener);
//		editorTx.addListener(SWT.FocusOut, textListener);
		editorTx.addListener(SWT.KeyDown, textListener);
		editorTx.addListener(SWT.Traverse, textListener);
		attribTable.addListener(SWT.MouseDown, e -> e.display.asyncExec (new Runnable () {
			@Override
			public void run () {
				if (attribTable.isDisposed ()) return;
				if (e.button != 1) return;
				Point pt = new Point(e.x, e.y);
				TableItem item = attribTable.getItem(pt);
				if (item == null) return;
				int column = -1;
				for (int i = 0; i < attribTable.getColumnCount(); i++) {
					if (item.getBounds(i).contains(pt)) {
						column = i;
						break;
					}				
				}
				if (column == -1) return;
				if (!getEditable(item, column)) return;
				editor.setColumn(column);
				editor.setItem(item);
				editorTx.setText(item.getText(column));
				editorTx.selectAll();
				editorTx.setVisible(true);
				editorTx.setFocus();
			}
		}));
		
		return comp;
	}
	
	Composite createActionsPanel(Composite parent) {
		Composite panel = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		panel.setLayout(layout);
		
		Button generate = new Button(panel, SWT.PUSH);
		generate.setText("Generate");
		generate.addListener(SWT.Selection, event -> generate(null));
		return panel;
	}
	
	public void generate(ProgressMonitor progress) {
		gen.generate(progress);
	}
	
	public boolean getActionsVisible() {
		return actions;
	}
	
	public void open(Composite parent) {
		FormLayout layout = new FormLayout();
		parent.setLayout(layout);
		
		Composite signaturePanel = createSignaturesPanel(parent);
		final Sash sash = new Sash(parent, SWT.SMOOTH | SWT.VERTICAL);
		Composite propertiesPanel = createPropertiesPanel(parent);
		
		Composite actionsPanel = null;
		if (actions) {
			actionsPanel = createActionsPanel(parent);
		}

		FormData data;
		
		data = new FormData();		
		data.left = new FormAttachment(0, 0);
		data.top = new FormAttachment(0, 0);
		data.right = new FormAttachment(sash, 0);
		data.bottom = new FormAttachment(100, 0);
		signaturePanel.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(null, Math.max(200, parent.getSize().x / 2));
		data.top = new FormAttachment(0, 0);
		data.bottom = new FormAttachment(100, 0);
		sash.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(sash, sash.computeSize(SWT.DEFAULT, SWT.DEFAULT).x);
		data.top = new FormAttachment(0, 0);
		data.right = actionsPanel != null ? new FormAttachment(actionsPanel, 0) : new FormAttachment(100, 0);
		data.bottom = new FormAttachment(100, 0);
		propertiesPanel.setLayoutData(data);

		if (actionsPanel != null) {
			data = new FormData();
			data.top = new FormAttachment(0, 0);
			data.right = new FormAttachment(100, 0);
			data.bottom = new FormAttachment(100, 0);
			actionsPanel.setLayoutData(data);
		}
		
		sash.addListener(SWT.Selection, event -> {
			Composite parent1 = sash.getParent();
			Rectangle rect = parent1.getClientArea();
			event.x = Math.min (Math.max (event.x, 60), rect.width - 60);
			if (event.detail != SWT.DRAG) {
				FormData data1 = (FormData)sash.getLayoutData();
				data1.left.offset = event.x;
				parent1.layout(true);
			}
		});

		updateNodes();
	}

	public void dispose() {
		cleanup();
	}
	
	ArrayList<Node> flatNodes;
	void searchFor(String name) {
		TreeItem[] selection = nodesTree.getSelection();
		Node node = null;
		if (selection.length != 0) {
			if (selection[0].getData() instanceof Node) {
				node = (Node)selection[0].getData();
			} else {
				if (selection[0].getItemCount() > 0 && selection[0].getItem(0).getData() instanceof Node) {
					node = (Node)selection[0].getItem(0).getData();
				}
			}
		}
		Document[] documents = gen.getDocuments();
		if (node == null && documents.length > 0) {
			int index = 0;
			while (index < documents.length && (node = documents[index]) == null) index++;
		}
		if (flatNodes == null) {
			flatNodes = new ArrayList<>();
			for (Document document : documents) {
				if (document != null) addNodes(document, flatNodes);
			}
		}
		int index = 0;
		while (flatNodes.get(index++) != node){}		
		int start = index;
		while (index < flatNodes.size()) {
			Node child = flatNodes.get(index);
			Node attribName = gen.getIDAttribute(child);
			if (attribName != null && attribName.getNodeValue().matches(name)) {
				selectNode(child);
				return;
			}
			index++;
		}
		index = 0;
		while (index < start) {
			Node child = flatNodes.get(index);
			Node attribName = gen.getIDAttribute(child);
			if (attribName != null && attribName.getNodeValue().matches(name)) {
				selectNode(child);
				return;
			}
			index++;
		}
		nodesTree.getDisplay().beep();
	}
	
	void selectNode(Node node) {
		ArrayList<Node> path = new ArrayList<>();
		do {
			path.add(node);
			node = node.getParentNode();
		} while (node != null);
		TreeItem[] items = nodesTree.getItems();
		Collections.reverse(path);
		path.remove(0);
		while (true) {
			TreeItem item = findItem(items, path.remove(0));
			if (item == null) return;
			if (path.isEmpty()) {
				nodesTree.setSelection(item);
				selectChild(item);
				return;
			}
			items = item.getItems();
		}
	}
	
	TreeItem findItem(TreeItem[] items, Node node) {
		for (TreeItem item : items) {
			checkChildren(item);
			if (item.getData() == node) return item;
		}
		for (TreeItem item : items) {
			TreeItem child = findItem(item.getItems(), node);
			if (child != null) return child;
		}
		return null;
	}
	
	void addNodes(Node node, ArrayList<Node> list) {
		if (node.getNodeType() == Node.TEXT_NODE) return;
		list.add(node);
		NodeList children = node.getChildNodes();
		for (int i = 0, length = children.getLength(); i < length; i++) {
			Node child = children.item(i);
			addNodes(child, list);
		}	
	}
	
	void selectChild(TreeItem item) {
		attribTable.removeAll();
		if (!(item.getData() instanceof Node)) return;
		Node node = (Node)item.getData();
		NamedNodeMap attributes = node.getAttributes();
		for (String extraAttrib : gen.getExtraAttributeNames(node)) {
			TableItem attribItem = new TableItem(attribTable, SWT.NONE);
			String attribName = extraAttrib;
			if (!SHOW_SWT_PREFIX && attribName.startsWith("swt_")) {
				attribName = attribName.substring("swt_".length(), attribName.length());
				attribItem.setData("swt_", "swt_");
			}
			attribItem.setText(attribName);
			attribItem.setData(node);
			attribItem.setForeground(item.getDisplay().getSystemColor(SWT.COLOR_BLUE));
			Node attrib = attributes.getNamedItem(extraAttrib);
			if (attrib != null) {
				attribItem.setText(1, attrib.getNodeValue());
			}
			
		}
		checkItem(node, item);
		for (int i = 0, length = attributes.getLength(); i < length; i++) {
			Node attrib = attributes.item(i);
			String attribName = attrib.getNodeName();
			if (attribName.startsWith("swt_")) continue;
			TableItem attribItem = new TableItem(attribTable, SWT.NONE);
			attribItem.setText(attribName);
			attribItem.setText(1, attrib.getNodeValue());
		}
		attribTable.getColumn(0).pack();
		attribTable.getColumn(1).setWidth(500);
	}
	
	void updateGenAttribute (TreeItem item) {
		if (item.getData() instanceof Element) {
			Element node = (Element)item.getData();
			if (item.getChecked()) {
				if (item.getGrayed()) {
					node.setAttribute("swt_gen", "mixed");
				} else {
					node.setAttribute("swt_gen", "true");
				}
			} else {
				node.removeAttribute("swt_gen");
			}
		}
	}
	
	void updateNodes() {
		String[] xmls = gen.getXmls();
		if (xmls == null) return;
		Document[] documents = gen.getDocuments();
		for (int x = 0; x < xmls.length; x++) {
			String xmlPath = xmls[x];
			Document document = documents[x];
			if (document == null) {
				System.out.println("Could not find: " + xmlPath);
				continue;
			}
			TreeItem item = new TreeItem(nodesTree, SWT.NONE);
			String fileName = gen.getFileName(xmlPath);
			if (fileName.endsWith("Full.bridgesupport")) {
				fileName =  fileName.substring(0, fileName.length() - "Full.bridgesupport".length());
			}
			item.setText(fileName);
			Node node = document.getDocumentElement();
			item.setData(node);
			checkItem(node, item);
			new TreeItem(item, SWT.NONE);
		}
		for (TreeColumn column : nodesTree.getColumns()) {
			column.pack();
		}
	}
	
	public void refresh () {
		if (nodesTree == null) return;
		gen.setXmls(null);
		flatNodes = null;
		nodesTree.getDisplay().asyncExec(() -> {
			if (nodesTree == null || nodesTree.isDisposed()) return;
			nodesTree.removeAll();
			attribTable.removeAll();
			updateNodes();
		});
	}
	
	public void setActionsVisible(boolean visible) {
		this.actions = visible;
	}
	
	public void setFocus() {
		nodesTree.setFocus();
	}

	public static void main(String[] args) {
		String mainClass = args.length > 0 ? args[0] : "org.eclipse.swt.internal.cocoa.OS";
		String outputDir = args.length > 1 ? args[1] : "../org.eclipse.swt/Eclipse SWT PI/cocoa/";
		String selectorEnumClass = args.length > 2 ? args[2] : "org.eclipse.swt.internal.cocoa.Selector";
		String[] xmls = {};
		if (args.length > 3) {
			xmls = new String[args.length - 3];
			System.arraycopy(args, 3, xmls, 0, xmls.length);
		}
		try {
			Display display = new Display();
			Shell shell = new Shell(display);
			MacGenerator gen = new MacGenerator();
			gen.setXmls(xmls);
			gen.setOutputDir(outputDir);
			gen.setMainClass(mainClass);
			gen.setSelectorEnum(selectorEnumClass);
			MacGeneratorUI ui = new MacGeneratorUI(gen);
			ui.open(shell);
			shell.open();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}
			ui.dispose();
			display.dispose();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
