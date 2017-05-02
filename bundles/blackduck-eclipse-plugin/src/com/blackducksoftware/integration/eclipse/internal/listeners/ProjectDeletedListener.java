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

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;

import com.blackducksoftware.integration.eclipse.common.services.inspector.ComponentInspectorService;

public class ProjectDeletedListener implements IResourceChangeListener {
	private final ComponentInspectorService componentInspectorService;

	public ProjectDeletedListener(final ComponentInspectorService componentInspectorService){
		this.componentInspectorService = componentInspectorService;
	}

	@Override
	public void resourceChanged(final IResourceChangeEvent event) {
		if (event != null && event.getResource() != null) {
			final String projectName = event.getResource().getName();
			componentInspectorService.removeProject(projectName);
		}
	}

}
