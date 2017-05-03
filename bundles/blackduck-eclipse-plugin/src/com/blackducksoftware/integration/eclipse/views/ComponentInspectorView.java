/**
 * com.blackducksoftware.integration.eclipse.plugin
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.blackducksoftware.integration.eclipse.views;

import java.util.List;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import com.blackducksoftware.integration.eclipse.BlackDuckHubPluginActivator;
import com.blackducksoftware.integration.eclipse.internal.ComponentModel;
import com.blackducksoftware.integration.eclipse.views.widgets.ComponentTableStatusCLabel;

public class ComponentInspectorView extends ViewPart {
	public static final String DUCKY_PNG_PATH = "resources/icons/ducky.png";

	public static final String DISCONNECT_PNG_PATH = "resources/icons/disconnect_co.gif";

	public static final String WAITING_PNG_PATH = "resources/icons/waiting.gif";

	public static final String WARNING_PNG_PATH = "resources/icons/warning.gif";

	private ComponentTableStatusCLabel tableStatus;

	private String lastSelectedProjectName = "";

	private TableViewer tableViewer;

	@Override
	public void createPartControl(final Composite arg0) {
	}

	@Override
	public void setFocus() {
	}

	public String getLastSelectedProjectName() {
		return lastSelectedProjectName;
	}

	public void refreshInput() {
		this.setLastSelectedProjectName(lastSelectedProjectName);
	}

	public void setLastSelectedProjectName(final String projectName) {
		lastSelectedProjectName = projectName;
		this.setTableInput(projectName);
		tableStatus.updateStatus(projectName);
	}

	private void setTableInput(final String projectName){
		ComponentModel[] results = new ComponentModel[]{};
		final List<ComponentModel> componentModels = BlackDuckHubPluginActivator.getDefault().getInspectorService().getProjectComponents(projectName);
		if (componentModels != null) {
			results = componentModels.toArray(new ComponentModel[componentModels.size()]);
		}
		tableViewer.setInput(results);
	}

	public void refreshStatus() {
		tableStatus.updateStatus(lastSelectedProjectName);
	}

}
