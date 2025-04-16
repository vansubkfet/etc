/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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
package org.eclipse.swt.examples.controlexample;


import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Widget;

class CComboTab extends AlignableTab {

	/* Example widgets and groups that contain them */
	CCombo combo1;
	Group comboGroup;

	/* Style widgets added to the "Style" group */
	Button flatButton, readOnlyButton;

	static String [] ListData = {ControlExample.getResourceString("ListData1_0"),
								 ControlExample.getResourceString("ListData1_1"),
								 ControlExample.getResourceString("ListData1_2"),
								 ControlExample.getResourceString("ListData1_3"),
								 ControlExample.getResourceString("ListData1_4"),
								 ControlExample.getResourceString("ListData1_5"),
								 ControlExample.getResourceString("ListData1_6"),
								 ControlExample.getResourceString("ListData1_7"),
								 ControlExample.getResourceString("ListData1_8")};

	/**
	 * Creates the Tab within a given instance of ControlExample.
	 */
	CComboTab(ControlExample instance) {
		super(instance);
	}

	/**
	 * Creates the "Example" group.
	 */
	@Override
	void createExampleGroup () {
		super.createExampleGroup ();

		/* Create a group for the combo box */
		comboGroup = new Group (exampleGroup, SWT.NONE);
		comboGroup.setLayout (new GridLayout ());
		comboGroup.setLayoutData (new GridData (SWT.FILL, SWT.FILL, true, true));
		comboGroup.setText (ControlExample.getResourceString("Custom_Combo"));
	}

	/**
	 * Creates the "Example" widgets.
	 */
	@Override
	void createExampleWidgets () {

		/* Compute the widget style */
		int style = getDefaultStyle();
		if (flatButton.getSelection ()) style |= SWT.FLAT;
		if (readOnlyButton.getSelection ()) style |= SWT.READ_ONLY;
		if (borderButton.getSelection ()) style |= SWT.BORDER;
		if (leftButton.getSelection ()) style |= SWT.LEFT;
		if (centerButton.getSelection ()) style |= SWT.CENTER;
		if (rightButton.getSelection ()) style |= SWT.RIGHT;

		/* Create the example widgets */
		combo1 = new CCombo (comboGroup, style);
		combo1.setItems (ListData);
		if (ListData.length >= 3) {
			combo1.setText(ListData [2]);
		}
	}

	/**
	 * Creates the "Style" group.
	 */
	@Override
	void createStyleGroup () {
		super.createStyleGroup ();

		/* Create the extra widgets */
		readOnlyButton = new Button (styleGroup, SWT.CHECK);
		readOnlyButton.setText ("SWT.READ_ONLY");
		borderButton = new Button (styleGroup, SWT.CHECK);
		borderButton.setText ("SWT.BORDER");
		flatButton = new Button (styleGroup, SWT.CHECK);
		flatButton.setText ("SWT.FLAT");
	}

	/**
	 * Gets the "Example" widget children.
	 */
	@Override
	Widget [] getExampleWidgets () {
		return new Widget [] {combo1};
	}

	/**
	 * Returns a list of set/get API method names (without the set/get prefix)
	 * that can be used to set/get values in the example control(s).
	 */
	@Override
	String[] getMethodNames() {
		return new String[] {"Editable", "Items", "Selection", "Text", "TextLimit", "ToolTipText", "VisibleItemCount"};
	}

	/**
	 * Gets the text for the tab folder item.
	 */
	@Override
	String getTabText () {
		return "CCombo";
	}

	/**
	 * Sets the state of the "Example" widgets.
	 */
	@Override
	void setExampleWidgetState () {
		super.setExampleWidgetState ();
		flatButton.setSelection ((combo1.getStyle () & SWT.FLAT) != 0);
		readOnlyButton.setSelection ((combo1.getStyle () & SWT.READ_ONLY) != 0);
		borderButton.setSelection ((combo1.getStyle () & SWT.BORDER) != 0);
		leftButton.setSelection ((combo1.getStyle () & SWT.LEFT) != 0);
		centerButton.setSelection ((combo1.getStyle () & SWT.CENTER) != 0);
		rightButton.setSelection ((combo1.getStyle () & SWT.RIGHT) != 0);
	}

	@Override
	void setExampleWidgetAlignment() {
		int alignment = 0;
		if (leftButton.getSelection ()) alignment = SWT.LEFT;
		if (centerButton.getSelection ()) alignment = SWT.CENTER;
		if (rightButton.getSelection ()) alignment = SWT.RIGHT;
		combo1.setAlignment (alignment);
	}
}
