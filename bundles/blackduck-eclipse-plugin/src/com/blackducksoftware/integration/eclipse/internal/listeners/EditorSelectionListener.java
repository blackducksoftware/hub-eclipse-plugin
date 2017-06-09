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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import com.blackducksoftware.integration.eclipse.services.WorkspaceInformationService;
import com.blackducksoftware.integration.eclipse.services.inspector.ComponentInspectorViewService;

public class EditorSelectionListener implements ISelectionListener {
    private final ComponentInspectorViewService componentInspectorViewService;

    private final WorkspaceInformationService workspaceInformationService;

    public EditorSelectionListener(final ComponentInspectorViewService componentInspectorViewService, final WorkspaceInformationService workspaceInformationService){
        super();
        this.componentInspectorViewService = componentInspectorViewService;
        this.workspaceInformationService = workspaceInformationService;
    }

    @Override
    public void selectionChanged(final IWorkbenchPart part, final ISelection sel) {
        if (!(sel instanceof IStructuredSelection) && !(part instanceof IEditorPart)) {
            return;
        }
        IProject project = null;
        IResource resource = null;
        if(sel instanceof IStructuredSelection){
            final IStructuredSelection ss = (IStructuredSelection) sel;
            final Object element = ss.getFirstElement();
            if (element instanceof IProject) {
                project = (IProject) element;
            } else {
                if (element instanceof IResource) {
                    resource = ((IResource) element);
                } else if (element instanceof IAdaptable) {
                    final IAdaptable probableProject = ((IAdaptable) element);
                    resource = probableProject.getAdapter(IResource.class);
                } else {
                    resource = workspaceInformationService.getFileInputForActiveEditor(PlatformUI.getWorkbench());
                }
            }
        }else if (part instanceof IEditorPart){
            final IEditorInput input = ((IEditorPart)part).getEditorInput();
            if (!(input instanceof IFileEditorInput)) {
                return;
            }
            resource = ((IFileEditorInput)input).getFile();
        }
        if (resource != null && project == null) {
            project = resource.getProject();
        }
        String projectName = "";
        try {
            if (project != null && project.getDescription() != null) {
                projectName = project.getDescription().getName();
                componentInspectorViewService.setProject(projectName);
            }
        } catch (final CoreException e) {
            // Do nothing, we just don't set the project name
        }
    }

}
