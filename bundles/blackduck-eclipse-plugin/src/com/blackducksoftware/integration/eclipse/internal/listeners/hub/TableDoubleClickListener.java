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
package com.blackducksoftware.integration.eclipse.internal.listeners.hub;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;

import com.blackducksoftware.integration.eclipse.internal.ComponentModel;
import com.blackducksoftware.integration.eclipse.services.hub.HubConnectionService;
import com.blackducksoftware.integration.eclipse.services.inspector.ComponentInspectorViewService;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.buildtool.Gav;
import com.blackducksoftware.integration.hub.dataservice.component.ComponentDataService;
import com.blackducksoftware.integration.hub.model.view.ComponentVersionView;

public class TableDoubleClickListener implements IDoubleClickListener {
	public static final String JOB_GENERATE_URL = "Opening component in the Hub...";
	private final ComponentInspectorViewService componentInspectorViewService;
	private final HubConnectionService hubConnectionService;

	public TableDoubleClickListener(final ComponentInspectorViewService componentInspectorViewService, final HubConnectionService hubConnectionService){
		this.componentInspectorViewService = componentInspectorViewService;
		this.hubConnectionService = hubConnectionService;
	}

	@Override
	public void doubleClick(final DoubleClickEvent event) {
		final ComponentDataService componentDataService = hubConnectionService.getComponentDataService();
		final IStructuredSelection selection = (IStructuredSelection) event.getSelection();

		if (selection.getFirstElement() instanceof ComponentModel) {
			final ComponentModel selectedObject = (ComponentModel) selection.getFirstElement();
			if (!selectedObject.getComponentIsKnown()) {
				return;
			}
			final Job job = new Job(JOB_GENERATE_URL) {
				@Override
				protected IStatus run(final IProgressMonitor arg0) {
					final Gav selectedGav = selectedObject.getGav();
					String link;
					try {
						final ComponentVersionView selectedComponentVersion = componentDataService.getExactComponentVersionFromComponent(
								selectedGav.getNamespace(), selectedGav.getGroupId(), selectedGav.getArtifactId(), selectedGav.getVersion());
						// Final solution, will work once the redirect is set up
						link = hubConnectionService.getMetaService().getHref(selectedComponentVersion);

						// But for now...
						final String versionID = link.substring(link.lastIndexOf("/") + 1);
						link = hubConnectionService.getRestConnection().hubBaseUrl.toString();
						link = link + "/#versions/id:" + versionID + "/view:overview";
						IWebBrowser browser;
						if (PlatformUI.getWorkbench().getBrowserSupport().isInternalWebBrowserAvailable()) {
							browser = PlatformUI.getWorkbench().getBrowserSupport().createBrowser("Hub-Eclipse-Browser");
						} else {
							browser = PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser();
						}
						browser.openURL(new URL(link));
					} catch (PartInitException | MalformedURLException | IntegrationException e) {
						componentInspectorViewService.openError("Could not open Component in Hub instance",
								String.format("Problem opening %1$s %2$s in %3$s, are you connected to your hub instance?",
										selectedGav.getArtifactId(),
										selectedGav.getVersion(),
										hubConnectionService.getRestConnection().hubBaseUrl),
								e);
						return Status.CANCEL_STATUS;
					}
					return Status.OK_STATUS;
				}

			};
			job.schedule();
		}
	}

}
