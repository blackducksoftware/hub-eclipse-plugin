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
package com.blackducksoftware.integration.eclipse.preferencepages;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.blackducksoftware.integration.eclipse.BlackDuckHubPluginActivator;
import com.blackducksoftware.integration.eclipse.common.services.WorkspaceInformationService;
import com.blackducksoftware.integration.eclipse.common.services.inspector.ComponentInspectorPreferencesService;

public class ComponentInspectorPreferences extends PreferencePage implements IWorkbenchPreferencePage {
	public static final String SUPPORTED_PROJECT_ADDED_ACTION_LABEL = "When supported projects are added to workspace...";

	public static final String INSPECT_AUTOMATICALLY_LABEL = "Inspect them automatically";

	public static final String DO_NOT_INSPECT_AUTOMATICALLY_LABEL = "Do not inspect them automatically";

	public static final String MARKED_PROJECTS_LABEL = "Projects Marked for Inspection";

	public static final String CHECK_ALL_BUTTON_LABEL = "Check All";

	public static final String UNCHECK_ALL_BUTTON_LABEL = "Uncheck All";

	private final String[][] DEFAULT_ACTIVATION_LABELS_AND_VALUES = new String[][] {
		new String[] { INSPECT_AUTOMATICALLY_LABEL, "true" },
		new String[] { DO_NOT_INSPECT_AUTOMATICALLY_LABEL, "false" }
	};

	private List<BooleanFieldEditor> projectCheckboxes;

	private RadioGroupFieldEditor inspectByDefault;

	private Composite defaultsComposite;

	private Composite inspectedProjectsComposite;

	private ComponentInspectorPreferencesService inspectorPreferencesService;

	private WorkspaceInformationService workspaceInformationService;

	private Button uncheckAllButton;

	private Button checkAllButton;

	@Override
	public void init(final IWorkbench workbench) {
		this.noDefaultButton();
		this.setPreferenceStore(BlackDuckHubPluginActivator.getDefault().getPreferenceStore());
		inspectorPreferencesService = new ComponentInspectorPreferencesService();
		workspaceInformationService = new WorkspaceInformationService();

	}

	@Override
	protected Control createContents(final Composite parent) {
		defaultsComposite = new Composite(parent, SWT.LEFT);
		defaultsComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		defaultsComposite.setLayout(new GridLayout());
		inspectByDefault = new RadioGroupFieldEditor(ComponentInspectorPreferencesService.INSPECT_BY_DEFAULT,
				SUPPORTED_PROJECT_ADDED_ACTION_LABEL, 1, DEFAULT_ACTIVATION_LABELS_AND_VALUES, defaultsComposite);
		inspectByDefault.setPreferenceStore(this.getPreferenceStore());
		inspectByDefault.load();
		final Label spacer = new Label(defaultsComposite, SWT.HORIZONTAL);
		spacer.setVisible(false); // Not visible, but takes up a grid slot
		final Label activeProjectsLabel = new Label(defaultsComposite, SWT.HORIZONTAL);
		activeProjectsLabel.setText(MARKED_PROJECTS_LABEL);
		activeProjectsLabel.setFont(inspectByDefault.getLabelControl(defaultsComposite).getFont());
		final GridData indentGrid = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 2);
		indentGrid.horizontalIndent = ((GridData) inspectByDefault.getRadioBoxControl(defaultsComposite).getLayoutData()).horizontalIndent;
		inspectedProjectsComposite = new Composite(defaultsComposite, SWT.LEFT);
		inspectedProjectsComposite.setLayoutData(indentGrid);
		inspectedProjectsComposite.setLayout(new GridLayout());
		this.reloadActiveProjects();
		return defaultsComposite;
	}

	@Override
	protected void contributeButtons(final Composite parent) {
		((GridLayout) parent.getLayout()).numColumns+=2;
		checkAllButton = new Button(parent, SWT.PUSH);
		checkAllButton.setText(CHECK_ALL_BUTTON_LABEL);
		checkAllButton.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetDefaultSelected(final SelectionEvent arg0) {
				markAllProjects();
			}

			@Override
			public void widgetSelected(final SelectionEvent arg0) {
				markAllProjects();
			}
		});
		uncheckAllButton = new Button(parent, SWT.PUSH);
		uncheckAllButton.setText(UNCHECK_ALL_BUTTON_LABEL);
		uncheckAllButton.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetDefaultSelected(final SelectionEvent arg0) {
				unmarkAllProjects();
			}

			@Override
			public void widgetSelected(final SelectionEvent arg0) {
				unmarkAllProjects();
			}
		});
	}

	protected void markAllProjects() {
		projectCheckboxes.forEach(projectCheckBox -> {
			inspectorPreferencesService.activateProject(projectCheckBox.getPreferenceName());
			projectCheckBox.load();
		});
	}

	protected void unmarkAllProjects() {
		projectCheckboxes.forEach(projectCheckBox -> {
			inspectorPreferencesService.deactivateProject(projectCheckBox.getPreferenceName());
			projectCheckBox.load();
		});
	}

	@Override
	public void performApply() {
		this.storeValues();
		this.updateApplyButton();
	}

	@Override
	public boolean performOk() {
		this.storeValues();
		return super.performOk();
	}

	private void storeValues() {
		inspectByDefault.store();
		projectCheckboxes.forEach(projectCheckBox -> projectCheckBox.store());
	}

	public void reloadActiveProjects(final String... newProjects) {
		final List<String> names = workspaceInformationService.getSupportedProjectNames();
		if (projectCheckboxes != null) {
			for (final Iterator<BooleanFieldEditor> iterator = projectCheckboxes.iterator(); iterator.hasNext();) {
				final BooleanFieldEditor projectCheckBox = iterator.next();
				projectCheckBox.dispose();
				iterator.remove();
			}
		} else {
			projectCheckboxes = new ArrayList<>();
		}
		for (final String name : names) {
			final BooleanFieldEditor projectCheckBox = addProject(name);
			for (final String newProjectName : newProjects) {
				if (name.equals(newProjectName)) {
					projectCheckBox.loadDefault();
				}
			}
		}
	}

	private BooleanFieldEditor addProject(final String projectName) {
		inspectorPreferencesService.addProject(projectName);
		final BooleanFieldEditor projectCheckBox = new BooleanFieldEditor(projectName, projectName, inspectedProjectsComposite);
		projectCheckBox.setPage(this);
		projectCheckBox.setPreferenceStore(this.getPreferenceStore());
		projectCheckBox.load();
		projectCheckboxes.add(projectCheckBox);
		return projectCheckBox;
	}
}
