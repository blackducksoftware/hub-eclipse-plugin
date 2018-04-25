/**
 * com.blackducksoftware.integration.eclipse.plugin
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

import com.blackducksoftware.integration.eclipse.services.WorkspaceInformationService;
import com.blackducksoftware.integration.eclipse.services.inspector.ComponentInspectorViewService;

public class EditorSelectionListener implements ISelectionListener {
    private final ComponentInspectorViewService componentInspectorViewService;
    private final WorkspaceInformationService workspaceInformationService;

    public EditorSelectionListener(final ComponentInspectorViewService componentInspectorViewService, final WorkspaceInformationService workspaceInformationService) {
        super();
        this.componentInspectorViewService = componentInspectorViewService;
        this.workspaceInformationService = workspaceInformationService;
    }

    @Override
    public void selectionChanged(final IWorkbenchPart part, final ISelection sel) {
        String projectName = "";

        if (sel instanceof IStructuredSelection) {
            projectName = workspaceInformationService.getFirstProjectNameFromSelection((IStructuredSelection) sel);
        } else if (part instanceof IEditorPart) {
            projectName = workspaceInformationService.getProjectNameFromEditor((IEditorPart) part);
        }

        if (StringUtils.isNotBlank(projectName)) {
            componentInspectorViewService.setProject(projectName);
        }
    }

}
