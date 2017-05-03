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
package com.blackducksoftware.integration.eclipse.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class WorkspaceInformationService {
	private final ProjectInformationService projectInformationService;

	public WorkspaceInformationService(){
		this.projectInformationService = new ProjectInformationService();
	}

	public Set<IProject> getAllSupportedProjects() {
		final IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		final Set<IProject> supportedProjects = new HashSet<>();
		for (final IProject project : allProjects) {
			if(projectInformationService.isSupportedProject(project)){
				supportedProjects.add(project);
			}
		}
		return supportedProjects;
	}

	public List<String> getSupportedProjectNames() {
		final Set<IProject> projects = this.getAllSupportedProjects();
		final List<String> names = new ArrayList<>();
		for (final IProject project : projects) {
			try {
				final IProjectDescription projectDescription = project.getDescription();
				if (projectDescription != null) {
					final String projectName = projectDescription.getName();
					names.add(projectName);
				}
			} catch (final CoreException e) {
				/*
				 * If unsuccessful getting project description, means that project doesn't
				 * exist or is closed. In that case, do not add project
				 */
			}
		}
		Collections.sort(names, String.CASE_INSENSITIVE_ORDER);
		return names;
	}

	public String getSelectedProject() {
		final IStructuredSelection selection = getWorkspaceSelection();
		if (selection != null && selection.getFirstElement() != null) {
			final Object selected = selection.getFirstElement();
			if (selected instanceof IAdaptable) {
				return getSelectedProjectName((IAdaptable) selected);
			}
		}
		return "";
	}

	public List<String> getAllSelectedProjects() {
		final ArrayList<String> projectNames = new ArrayList<>();
		final IStructuredSelection selection = getWorkspaceSelection();
		if (selection != null && !selection.toList().isEmpty()) {
			final List<?> selectedObjects = selection.toList();
			for (final Object selected : selectedObjects) {
				if (selected instanceof IAdaptable) {
					final String projectName = getSelectedProjectName((IAdaptable) selected);
					if (!projectName.equals("")) {
						projectNames.add(projectName);
					}
				}
			}

		}
		return projectNames;
	}

	public IResource getFileInputForActiveEditor(final IWorkbench currentWorkbench) {
		try{
			final IEditorInput editorInput = currentWorkbench.getActiveWorkbenchWindow().getActivePage().getActiveEditor().getEditorInput();
			if(editorInput instanceof IFileEditorInput) {
				return ((IFileEditorInput) editorInput).getFile();
			}
		}catch(final NullPointerException e){
			//Do nothing. This happens if there is no active workbench, page, or editor
		}
		return null;
	}

	private IStructuredSelection getWorkspaceSelection() {
		final IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (activeWindow != null) {
			final ISelectionService selectionService = activeWindow.getSelectionService();
			if (selectionService != null) {
				return (IStructuredSelection) selectionService.getSelection();
			}
		}
		return null;
	}

	private String getSelectedProjectName(final IAdaptable selected) {
		final IProject project = selected.getAdapter(IProject.class);
		try {
			if (project != null && project.getDescription() != null) {
				return project.getDescription().getName();
			}
		} catch (final CoreException e) {
			// do nothing
		}
		return "";
	}

}
