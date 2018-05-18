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
package com.blackducksoftware.integration.eclipse.preferencepages.hub;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.blackducksoftware.integration.eclipse.BlackDuckEclipseActivator;
import com.blackducksoftware.integration.eclipse.services.BlackDuckEclipseServicesFactory;
import com.blackducksoftware.integration.eclipse.services.connection.hub.HubPreferencesService;
import com.blackducksoftware.integration.eclipse.services.inspector.ComponentInspectorService;
import com.blackducksoftware.integration.eclipse.services.inspector.ComponentInspectorViewService;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.configuration.HubServerConfig;
import com.blackducksoftware.integration.hub.configuration.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.rest.CredentialsRestConnection;
import com.blackducksoftware.integration.log.IntBufferedLogger;

public class HubPreferences extends PreferencePage implements IWorkbenchPreferencePage {
    public static final String PREFERENCE_PAGE_ID = "com.blackducksoftware.integration.eclipse.preferencepages.hub.HubPreferences";
    public static final String TEST_HUB_CREDENTIALS_TEXT = "Test Connection";
    public static final String LOGIN_SUCCESS_MESSAGE = "Connection successful!";

    public static final String HUB_USERNAME_LABEL = "Username";
    public static final String HUB_PASSWORD_LABEL = "Password";
    public static final String HUB_URL_LABEL = "Instance URL";
    public static final String HUB_TIMEOUT_LABEL = "Timeout in Seconds";
    public static final String HUB_ALWAYS_TRUST_LABEL = "Always Trust Server Certificate";

    public static final String PROXY_USERNAME_LABEL = "Proxy Username";
    public static final String PROXY_PASSWORD_LABEL = "Proxy Password";
    public static final String PROXY_HOST_LABEL = "Proxy Host";
    public static final String PROXY_PORT_LABEL = "Proxy Port";

    private static final String INTEGER_FIELD_EDITOR_ERROR_STRING = "IntegerFieldEditor.errorMessage";
    private final int NUM_COLUMNS = 2;

    private HubPreferencesService hubPreferencesService;
    private StringFieldEditor hubUsernameField;
    private StringFieldEditor hubUrlField;
    private StringFieldEditor hubTimeoutField;
    private BooleanFieldEditor hubAlwaysTrustField;
    private StringFieldEditor proxyHostField;
    private StringFieldEditor proxyPortField;
    private StringFieldEditor proxyUsernameField;
    private Text hubPasswordField;
    private Text proxyPasswordField;
    private Button testHubCredentials;
    private Text connectionMessageText;

    private Set<String> hasChanges;

    @Override
    public void createControl(final Composite parent) {
        super.createControl(parent);
        this.getApplyButton().setEnabled(false);
    }

    @Override
    public void init(final IWorkbench workbench) {
        hubPreferencesService = BlackDuckEclipseServicesFactory.getInstance().getHubPreferencesService();
        this.setPreferenceStore(BlackDuckEclipseActivator.getDefault().getPreferenceStore());
        this.noDefaultButton();
    }

    @Override
    protected Control createContents(final Composite parent) {
        hasChanges = new HashSet<>();
        final Composite authComposite = new Composite(parent, SWT.LEFT);
        final GridLayout authCompositeLayout = new GridLayout();
        authCompositeLayout.numColumns = NUM_COLUMNS;
        authComposite.setLayout(authCompositeLayout);
        authComposite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_BEGINNING));
        authComposite.setFont(parent.getFont());
        hubUsernameField = createStringField(HubPreferencesService.HUB_USERNAME, HUB_USERNAME_LABEL, authComposite, false);
        hubPasswordField = createPasswordField(HubPreferencesService.HUB_PASSWORD, HUB_PASSWORD_LABEL, authComposite);
        hubUrlField = createStringField(HubPreferencesService.HUB_URL, HUB_URL_LABEL, authComposite, false);
        hubTimeoutField = createStringField(HubPreferencesService.HUB_TIMEOUT, HUB_TIMEOUT_LABEL, authComposite, true);
        hubAlwaysTrustField = createBooleanField(HubPreferencesService.HUB_ALWAYS_TRUST, HUB_ALWAYS_TRUST_LABEL, authComposite);
        proxyUsernameField = createStringField(HubPreferencesService.PROXY_USERNAME, PROXY_USERNAME_LABEL, authComposite, false);
        proxyPasswordField = createPasswordField(HubPreferencesService.PROXY_PASSWORD, PROXY_PASSWORD_LABEL, authComposite);
        proxyHostField = createStringField(HubPreferencesService.PROXY_HOST, PROXY_HOST_LABEL, authComposite, false);
        proxyPortField = createStringField(HubPreferencesService.PROXY_PORT, PROXY_PORT_LABEL, authComposite, true);
        final Composite connectionMessageComposite = new Composite(parent, SWT.LEFT);
        final GridLayout connectionMessageCompositeLayout = new GridLayout();
        connectionMessageCompositeLayout.numColumns = 1;
        connectionMessageComposite.setLayout(connectionMessageCompositeLayout);
        connectionMessageComposite.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.BEGINNING));
        final GridData textData = new GridData(GridData.FILL_BOTH);
        connectionMessageText = new Text(connectionMessageComposite, SWT.READ_ONLY | SWT.MULTI | SWT.WRAP);
        connectionMessageText.setBackground(connectionMessageText.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        connectionMessageText.setLayoutData(textData);
        return parent;
    }

    @Override
    public void performApply() {
        try {
            this.storeValues();
        } catch (final IntegrationException e) {
        }
    }

    @Override
    public boolean performOk() {
        if (this.getApplyButton().getEnabled()) {
            this.performApply();
        }
        return super.performOk();
    }

    @Override
    protected void contributeButtons(final Composite parent) {
        ((GridLayout) parent.getLayout()).numColumns++;
        testHubCredentials = new Button(parent, SWT.PUSH);
        testHubCredentials.setText(TEST_HUB_CREDENTIALS_TEXT);
        testHubCredentials.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(final SelectionEvent arg0) {
                attemptToConnect();
            }

            @Override
            public void widgetSelected(final SelectionEvent arg0) {
                attemptToConnect();
            }
        });
    }

    private StringFieldEditor createStringField(final String preferenceName, final String label, final Composite composite, final boolean integerValidation) {
        final StringFieldEditor editor;
        if (integerValidation) {
            // String field editor w/ integer validation, we can make this a separate class if we need to.
            editor = new StringFieldEditor(preferenceName, label, composite) {
                @Override
                protected boolean checkState() {
                    this.setErrorMessage(JFaceResources.getString(INTEGER_FIELD_EDITOR_ERROR_STRING));
                    final Text text = this.getTextControl();
                    if (text == null) {
                        return false;
                    }
                    final String intString = text.getText();
                    if (intString.isEmpty()) {
                        this.clearErrorMessage();
                        return true;
                    }
                    try {
                        Integer.valueOf(intString).intValue();
                    } catch (final NumberFormatException nfe) {
                        this.showErrorMessage();
                    }
                    return false;
                }
            };
        } else {
            editor = new StringFieldEditor(preferenceName, label, composite);
        }
        editor.setPage(this);
        editor.setPreferenceStore(this.getPreferenceStore());
        editor.load();
        editor.fillIntoGrid(composite, NUM_COLUMNS);
        editor.setPropertyChangeListener(new IPropertyChangeListener() {
            @Override
            public void propertyChange(final PropertyChangeEvent event) {
                if (hubPreferencesService.getPreference(preferenceName).equals(editor.getStringValue())) {
                    hasChanges.remove(preferenceName);
                } else {
                    hasChanges.add(preferenceName);
                }
                updateApplyButtonWithChanges();
            }
        });
        return editor;
    }

    private BooleanFieldEditor createBooleanField(final String preferenceName, final String label, final Composite composite) {
        final BooleanFieldEditor editor = new BooleanFieldEditor(preferenceName, label, composite);
        editor.setPage(this);
        editor.setPreferenceStore(this.getPreferenceStore());
        editor.load();
        editor.fillIntoGrid(composite, NUM_COLUMNS);
        editor.setPropertyChangeListener(new IPropertyChangeListener() {
            @Override
            public void propertyChange(final PropertyChangeEvent event) {
                if (Boolean.parseBoolean(hubPreferencesService.getPreference(preferenceName)) == (editor.getBooleanValue())) {
                    hasChanges.remove(preferenceName);
                } else {
                    hasChanges.add(preferenceName);
                }
                updateApplyButtonWithChanges();
            }
        });
        return editor;
    }

    private Text createPasswordField(final String preferenceName, final String labelText, final Composite composite) {
        final Label label = new Label(composite, SWT.WRAP);
        label.setText(labelText);
        label.setFont(composite.getFont());
        final Text passwordField = new Text(composite, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);
        String passwordFieldText = "";
        if (preferenceName.equals(HubPreferencesService.HUB_PASSWORD)) {
            passwordFieldText = hubPreferencesService.getHubPassword();
        } else if (preferenceName.equals(HubPreferencesService.PROXY_PASSWORD)) {
            passwordFieldText = hubPreferencesService.getHubProxyPassword();
        }
        passwordField.setText(passwordFieldText);
        passwordField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        passwordField.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(final ModifyEvent e) {
                if (preferenceName.equals(HubPreferencesService.HUB_PASSWORD)) {
                    if (hubPreferencesService.getHubPassword().equals(passwordField.getText())) {
                        hasChanges.remove(preferenceName);
                    } else {
                        hasChanges.add(preferenceName);
                    }
                } else if (preferenceName.equals(HubPreferencesService.PROXY_PASSWORD)) {
                    if (hubPreferencesService.getHubProxyPassword().equals(passwordField.getText())) {
                        hasChanges.remove(preferenceName);
                    } else {
                        hasChanges.add(preferenceName);
                    }
                }
                updateApplyButtonWithChanges();
            }
        });
        return passwordField;
    }

    private void attemptToConnect() {
        final HubServerConfigBuilder hubServerConfigBuilder = new HubServerConfigBuilder();
        hubServerConfigBuilder.setUsername(hubUsernameField.getStringValue());
        hubServerConfigBuilder.setPassword(hubPasswordField.getText());
        hubServerConfigBuilder.setHubUrl(hubUrlField.getStringValue());
        hubServerConfigBuilder.setTimeout(hubTimeoutField.getStringValue());
        hubServerConfigBuilder.setAlwaysTrustServerCertificate(hubAlwaysTrustField.getBooleanValue());
        hubServerConfigBuilder.setProxyHost(proxyHostField.getStringValue());
        hubServerConfigBuilder.setProxyPort(proxyPortField.getStringValue());
        hubServerConfigBuilder.setProxyUsername(proxyUsernameField.getStringValue());
        hubServerConfigBuilder.setProxyPassword(proxyPasswordField.getText());
        String message = LOGIN_SUCCESS_MESSAGE;
        try {
            final HubServerConfig config = hubServerConfigBuilder.build();
            final CredentialsRestConnection restConnection = config.createCredentialsRestConnection(new IntBufferedLogger());
            restConnection.connect();
        } catch (final Exception e) {
            message = e.getMessage();
        }
        final Display display = Display.getCurrent();
        if (message.equals(LOGIN_SUCCESS_MESSAGE)) {
            connectionMessageText.setText(message);
            connectionMessageText.setForeground(display.getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
        } else {
            connectionMessageText.setText(message);
            connectionMessageText.setForeground(display.getSystemColor(SWT.COLOR_RED));
        }
    }

    private void storeValues() throws IntegrationException {
        hasChanges = new HashSet<>();
        this.updateApplyButtonWithChanges();
        hubPreferencesService.saveHubUsername(hubUsernameField.getStringValue());
        hubPreferencesService.saveHubPassword(hubPasswordField.getText());
        hubPreferencesService.saveHubUrl(hubUrlField.getStringValue());
        hubPreferencesService.saveHubTimeout(hubTimeoutField.getStringValue());
        hubPreferencesService.saveHubAlwaysTrust(hubAlwaysTrustField.getBooleanValue());
        hubPreferencesService.saveHubProxyHost(proxyHostField.getStringValue());
        hubPreferencesService.saveHubProxyPort(proxyPortField.getStringValue());
        hubPreferencesService.saveHubProxyUsername(proxyUsernameField.getStringValue());
        hubPreferencesService.saveHubProxyPassword(proxyPasswordField.getText());
        final BlackDuckEclipseServicesFactory blackDuckEclipseServicesFactory = BlackDuckEclipseServicesFactory.getInstance();
        final ComponentInspectorViewService inspectorViewService = blackDuckEclipseServicesFactory.getComponentInspectorViewService();
        final ComponentInspectorService componentInspectorService = blackDuckEclipseServicesFactory.getComponentInspectorService();
        componentInspectorService.reloadConnection();
        inspectorViewService.resetDisplay();
    }

    public void updateApplyButtonWithChanges() {
        getApplyButton().setEnabled(!hasChanges.isEmpty());
    }

}
