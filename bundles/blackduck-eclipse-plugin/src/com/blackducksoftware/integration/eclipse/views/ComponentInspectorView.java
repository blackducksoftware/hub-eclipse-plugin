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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

import com.blackducksoftware.integration.eclipse.BlackDuckEclipseActivator;
import com.blackducksoftware.integration.eclipse.internal.ComponentModel;
import com.blackducksoftware.integration.eclipse.internal.listeners.EditorSelectionListener;
import com.blackducksoftware.integration.eclipse.internal.listeners.TableDoubleClickListener;
import com.blackducksoftware.integration.eclipse.services.BlackDuckEclipseServicesFactory;
import com.blackducksoftware.integration.eclipse.services.WorkspaceInformationService;
import com.blackducksoftware.integration.eclipse.services.inspector.ComponentInspectorService;
import com.blackducksoftware.integration.eclipse.services.inspector.ComponentInspectorViewService;
import com.blackducksoftware.integration.eclipse.views.providers.ComponentTableContentProvider;
import com.blackducksoftware.integration.eclipse.views.providers.LicenseColumnLabelProvider;
import com.blackducksoftware.integration.eclipse.views.providers.NameColumnLabelProvider;
import com.blackducksoftware.integration.eclipse.views.providers.VulnerabilityCountColumnLabelProvider;
import com.blackducksoftware.integration.eclipse.views.widgets.ComponentModelFilter;
import com.blackducksoftware.integration.eclipse.views.widgets.ComponentTableStatusCLabel;

public class ComponentInspectorView extends ViewPart {
    public static final String VIEW_ID = "com.blackducksoftware.integration.eclipse.views.ComponentInspectorView";

    public static final String DUCKY_PNG_PATH = "resources/icons/ducky.png";

    public static final String DISCONNECT_PNG_PATH = "resources/icons/disconnect_co.gif";

    public static final String WAITING_PNG_PATH = "resources/icons/waiting.gif";

    public static final String WARNING_PNG_PATH = "resources/icons/warning.gif";

    private ComponentTableStatusCLabel tableStatus;

    private String lastSelectedProjectName = "";

    private TableViewer tableViewer;

    private Text filterBox;

    private ComponentTableContentProvider contentProvider;

    private EditorSelectionListener editorSelectionListener;

    private final ComponentInspectorViewService componentInspectorViewService;

    private final ComponentInspectorService componentInspectorService;

    private final WorkspaceInformationService workspaceInformationService;

    public ComponentInspectorView(){
        super();
        componentInspectorViewService = BlackDuckEclipseServicesFactory.getInstance().getComponentInspectorViewService();
        componentInspectorService = BlackDuckEclipseServicesFactory.getInstance().getComponentInspectorService();
        workspaceInformationService = BlackDuckEclipseServicesFactory.getInstance().getWorkspaceInformationService();
    }

    @Override
    public void createPartControl(final Composite parent) {
        componentInspectorViewService.registerComponentInspectorView(this);
        final GridLayout parentLayout = new GridLayout(1, false);
        parentLayout.marginWidth = 0;
        parentLayout.marginHeight = 0;
        parent.setLayout(parentLayout);
        lastSelectedProjectName = workspaceInformationService.getSelectedProject();
        this.setUpHeaderComposite(parent);
        final ComponentModelFilter componentFilter = new ComponentModelFilter(filterBox);
        tableViewer = new TableViewer(parent, (SWT.VIRTUAL | SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL));
        tableViewer.setUseHashlookup(true);
        tableViewer.getTable().setHeaderVisible(true);
        tableViewer.getTable().setLinesVisible(true);
        tableViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
        tableViewer.addDoubleClickListener(new TableDoubleClickListener());
        contentProvider = new ComponentTableContentProvider(tableViewer);
        contentProvider.addFilter(componentFilter);
        tableViewer.setContentProvider(contentProvider);
        editorSelectionListener = new EditorSelectionListener(componentInspectorViewService, workspaceInformationService);
        getSite().getPage().addPostSelectionListener(editorSelectionListener);
        this.createColumns();
        this.refreshInput();
        tableStatus = new ComponentTableStatusCLabel(parent, SWT.LEFT, tableViewer, componentInspectorService);
        tableStatus.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }

    @Override
    public Image getTitleImage() {
        final ImageDescriptor descriptor = BlackDuckEclipseActivator.imageDescriptorFromPlugin(BlackDuckEclipseActivator.PLUGIN_ID, DUCKY_PNG_PATH);
        return descriptor == null ? null : descriptor.createImage();
    }

    @Override
    public void dispose() {
        super.dispose();
        getSite().getPage().removePostSelectionListener(editorSelectionListener);
    }

    @Override
    public void setFocus() {
        tableViewer.getControl().setFocus();
    }

    private void createColumns() {
        final NameColumnLabelProvider nameColumnLabelProvider = new NameColumnLabelProvider();
        nameColumnLabelProvider.addColumnTo(tableViewer);
        final LicenseColumnLabelProvider licenseColumnLabelProvider = new LicenseColumnLabelProvider(300, SWT.LEFT);
        licenseColumnLabelProvider.addColumnTo(tableViewer);
        final VulnerabilityCountColumnLabelProvider vulnerabilityCountColumnLabelProvider = new VulnerabilityCountColumnLabelProvider(150, SWT.CENTER, contentProvider);
        vulnerabilityCountColumnLabelProvider.addColumnTo(tableViewer);
    }

    private Composite setUpHeaderComposite(final Composite parent) {
        final Composite headerComposite = new Composite(parent, SWT.NONE);
        final GridLayout headerLayout = new GridLayout(1, false);
        headerLayout.marginWidth = 0;
        headerLayout.marginHeight = 0;
        headerLayout.horizontalSpacing = 0;
        headerLayout.verticalSpacing = 0;
        headerComposite.setLayout(headerLayout);
        headerComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        final Label headerLabel = new Label(headerComposite, SWT.HORIZONTAL);
        headerLabel.setText("Project Components");
        final Label separator = new Label(headerComposite, SWT.HORIZONTAL | SWT.SEPARATOR);
        separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        setUpFilterComposite(headerComposite);
        return headerComposite;
    }

    private Composite setUpFilterComposite(final Composite parent) {
        final Composite filterComposite = new Composite(parent, SWT.NONE);
        final GridLayout filterLayout = new GridLayout(1, false);
        filterLayout.marginWidth = 1;
        filterLayout.marginHeight = 2;
        filterComposite.setLayout(filterLayout);
        filterComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        filterBox = new Text(filterComposite, SWT.SEARCH);
        filterBox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        filterBox.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(final ModifyEvent event) {
                refreshInput();
            }
        });
        filterBox.setMessage("type filter text");
        return filterComposite;
    }

    public String getLastSelectedProjectName() {
        return lastSelectedProjectName;
    }

    public void refreshInput() {
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                if (!tableViewer.getTable().isDisposed()) {
                    setTableInput(lastSelectedProjectName);
                    tableViewer.refresh();
                }
            }
        });
    }

    public void setLastSelectedProjectName(final String projectName) {
        lastSelectedProjectName = projectName;
        this.setTableInput(projectName);
        tableStatus.updateStatus(projectName);
    }

    private void setTableInput(final String projectName){
        ComponentModel[] results = new ComponentModel[]{};
        final List<ComponentModel> componentModels = componentInspectorService.getProjectComponents(projectName);
        if (componentModels != null) {
            results = componentModels.toArray(new ComponentModel[componentModels.size()]);
        }
        tableViewer.setItemCount(results.length);
        tableViewer.setInput(results);
    }

    public void refreshStatus() {
        tableStatus.updateStatus(lastSelectedProjectName);
    }

    public void openError(final String dialogTitle, final String message, final Throwable e) {
        ErrorDialog.openError(this.getSite().getShell(), dialogTitle, message, new Status(IStatus.ERROR, BlackDuckEclipseActivator.PLUGIN_ID, e.getMessage(), e));
    }

}
