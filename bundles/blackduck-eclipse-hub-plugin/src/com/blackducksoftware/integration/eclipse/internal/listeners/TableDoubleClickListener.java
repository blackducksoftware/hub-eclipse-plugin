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
package com.blackducksoftware.integration.eclipse.internal.listeners;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;

import com.blackducksoftware.integration.eclipse.hub.services.HubConnectionService;
import com.blackducksoftware.integration.eclipse.internal.ComponentModel;

public class TableDoubleClickListener implements IDoubleClickListener {
	private final HubConnectionService hubConnectionService;

	public TableDoubleClickListener(final HubConnectionService hubConnectionService){
		this.hubConnectionService = hubConnectionService;
	}

	@Override
	public void doubleClick(final DoubleClickEvent event) {
		final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		if (selection.getFirstElement() instanceof ComponentModel) {
			final ComponentModel selectedObject = (ComponentModel) selection.getFirstElement();
			if (!selectedObject.getComponentIsKnown()) {
				return;
			}
			hubConnectionService.openHubComponentPageInBrowser(selectedObject);
		}
	}

}
