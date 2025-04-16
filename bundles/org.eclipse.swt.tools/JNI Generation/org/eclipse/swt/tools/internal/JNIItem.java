/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
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

public interface JNIItem extends Flags {

public String[] getFlags();

public boolean getFlag(String flag);

public Object getParam(String key);

public boolean getGenerate();

public void setFlags(String[] flags);

public void setFlag(String flag, boolean value);

public void setGenerate(boolean value);

public void setParam(String key, Object value);
		
}
