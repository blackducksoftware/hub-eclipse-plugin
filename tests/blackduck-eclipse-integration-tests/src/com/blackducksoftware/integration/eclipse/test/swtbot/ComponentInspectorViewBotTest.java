/**
 * blackduck-eclipse-integration-tests
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
package com.blackducksoftware.integration.eclipse.test.swtbot;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRootMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.blackducksoftware.integration.eclipse.test.TestConstants;
import com.blackducksoftware.integration.eclipse.test.swtbot.utils.BlackDuckBotUtils;
import com.blackducksoftware.integration.eclipse.views.ComponentInspectorView;
import com.blackducksoftware.integration.eclipse.views.widgets.ComponentTableStatusCLabel;

@RunWith(SWTBotJunit4ClassRunner.class)
public class ComponentInspectorViewBotTest {
    private static final String COMMONS_FILEUPLOAD = "commons-fileupload";

    private final String[] testMavenComponents = { "commons-fileupload  1.0 ", "junit  3.8.1 " };
    private final String filterBoxMessage = "type filter text";
    private static BlackDuckBotUtils botUtils;

    @BeforeClass
    public static void setUpWorkspaceBot() throws IOException {
        botUtils = new BlackDuckBotUtils();
        botUtils.closeWelcomeView();
        botUtils.preferences().openHubPreferencesFromEclipseMenu();
        botUtils.preferences().hubSettings().enterValidCredentials();
        botUtils.preferences().pressApply();
        botUtils.preferences().inspectorSettings().openComponentInspectorPreferences();
        botUtils.preferences().inspectorSettings().setInspectNewByDefaultTrue();
        botUtils.preferences().pressApplyAndClose();
        botUtils.workbench().createProject().createMavenProject(TestConstants.TEST_MAVEN_GROUP, TestConstants.TEST_MAVEN_ARTIFACT);
        botUtils.workbench().createProject().createMavenProject(TestConstants.TEST_MAVEN_GROUP, TestConstants.TEST_MAVEN_COMPONENTS_ARTIFACT);
        botUtils.workbench().copyPomToProject(TestConstants.TEST_MAVEN_COMPONENTS_ARTIFACT_POM_PATH, TestConstants.TEST_MAVEN_COMPONENTS_ARTIFACT);
        botUtils.workbench().updateMavenProject(TestConstants.TEST_MAVEN_COMPONENTS_ARTIFACT);
        botUtils.workbench().createProject().createGeneralProject(TestConstants.TEST_NON_JAVA_PROJECT_NAME);
        botUtils.workbench().createProject().createMavenProject(TestConstants.TEST_MAVEN_GROUP, TestConstants.TEST_MAVEN_EMPTY_ARTIFACT);
    }

    @Test
    public void testVulnerabilityViewOpensFromWindowMenu() {
        botUtils.workbench().openComponentInspectorView();
        final SWTWorkbenchBot bot = botUtils.bot();
        assertNotNull(bot.viewByTitle(TestConstants.COMPONENT_INSPECTOR_VIEW_NAME));
        assertNotNull(bot.viewById(ComponentInspectorView.VIEW_ID));
        bot.viewByTitle(TestConstants.COMPONENT_INSPECTOR_VIEW_NAME).close();
    }

    @Test
    public void testInspectionResults() {
        botUtils.workbench().openComponentInspectorView();
        final SWTBotTreeItem projectNode = botUtils.workbench().getProject(TestConstants.TEST_MAVEN_COMPONENTS_ARTIFACT);
        final SWTBotRootMenu rootMenu = projectNode.select().contextMenu();
        final SWTBotMenu blackDuckMenu = rootMenu.contextMenu(TestConstants.CONTEXT_MENU_BLACK_DUCK_CATEGORY);
        final SWTBotMenu inspectProject = blackDuckMenu.contextMenu(TestConstants.CONTEXT_MENU_INSPECT_PROJECT_ACTION);
        inspectProject.click();
        botUtils.componentInspector().waitUntilInspectionResultsTableHasRows(testMavenComponents.length);
        final SWTBotTable table = botUtils.componentInspector().getInspectionResultsTable();
        for (final String componentName : testMavenComponents) {
            assertTrue(table.containsItem(componentName));
        }
    }

    @Test
    public void testConnectionOK() {
        botUtils.preferences().openHubPreferencesFromEclipseMenu();
        botUtils.preferences().hubSettings().enterValidCredentials();
        botUtils.preferences().pressApplyAndClose();
        final SWTBotTreeItem projectNode = botUtils.workbench().getProject(TestConstants.TEST_MAVEN_COMPONENTS_ARTIFACT);
        botUtils.workbench().openComponentInspectorView();
        projectNode.select();
        botUtils.componentInspector().getInspectionResultsTable().setFocus();
        assertNotNull(botUtils.componentInspector().getInspectionStatus(ComponentTableStatusCLabel.HUB_CONNECTION_OK_STATUS));
    }

    @Test
    public void testConnectionOKNoComponents() {
        botUtils.preferences().openHubPreferencesFromEclipseMenu();
        botUtils.preferences().hubSettings().enterValidCredentials();
        botUtils.preferences().pressApplyAndClose();
        final SWTBotTreeItem projectNode = botUtils.workbench().getProject(TestConstants.TEST_MAVEN_EMPTY_ARTIFACT);
        botUtils.workbench().openComponentInspectorView();
        projectNode.select();
        botUtils.componentInspector().getInspectionResultsTable().setFocus();
        assertNotNull(botUtils.componentInspector().getInspectionStatus(ComponentTableStatusCLabel.HUB_CONNECTION_OK_NO_COMPONENTS_STATUS));
    }

    @Test
    public void testProjectNotSupported() {
        botUtils.workbench().openComponentInspectorView();
        final SWTBotTreeItem projectNode = botUtils.workbench().getProject(TestConstants.TEST_NON_JAVA_PROJECT_NAME);
        botUtils.componentInspector().getComponentInspectorView();
        projectNode.select();
        botUtils.componentInspector().getInspectionResultsTable().setFocus();
        assertNotNull(botUtils.componentInspector().getInspectionStatus(ComponentTableStatusCLabel.PROJECT_NOT_SUPPORTED_STATUS));
    }

    @Test
    public void testInspectionDeactivated() {
        botUtils.preferences().openHubPreferencesFromEclipseMenu();
        botUtils.preferences().inspectorSettings().openComponentInspectorPreferences();
        botUtils.preferences().inspectorSettings().unmarkProjectForInspection(TestConstants.TEST_MAVEN_COMPONENTS_ARTIFACT);
        botUtils.preferences().inspectorSettings().pressApplyAndClose();
        try {
            final SWTBotTreeItem projectNode = botUtils.workbench().getProject(TestConstants.TEST_MAVEN_COMPONENTS_ARTIFACT);
            botUtils.workbench().openComponentInspectorView();
            projectNode.select();
            botUtils.componentInspector().getInspectionResultsTable().setFocus();
            assertNotNull(botUtils.componentInspector().getInspectionStatus(ComponentTableStatusCLabel.PROJECT_NOT_MARKED_FOR_INSPECTION_STATUS));
        } finally {
            botUtils.preferences().openHubPreferencesFromEclipseMenu();
            botUtils.preferences().inspectorSettings().openComponentInspectorPreferences();
            botUtils.preferences().inspectorSettings().markProjectForInspection(TestConstants.TEST_MAVEN_COMPONENTS_ARTIFACT);
            botUtils.preferences().inspectorSettings().pressApplyAndClose();
        }
    }

    @Test
    public void testFiltering() {
        botUtils.workbench().openComponentInspectorView();
        final SWTBotTreeItem projectNode = botUtils.workbench().getProject(TestConstants.TEST_MAVEN_COMPONENTS_ARTIFACT);
        projectNode.select();
        botUtils.componentInspector().getInspectionResultsTable().setFocus();
        final SWTBot viewBot = botUtils.componentInspector().getComponentInspectorView();
        final SWTBotText filterbox = viewBot.textWithMessage(filterBoxMessage);
        filterbox.typeText(COMMONS_FILEUPLOAD);
        try {
            final SWTBotTable table = botUtils.componentInspector().getInspectionResultsTable();
            for (final String componentName : testMavenComponents) {
                if (componentName.equals(testMavenComponents[0])) {
                    assertTrue(table.containsItem(componentName));
                } else {
                    assertFalse(table.containsItem(componentName));
                }
            }
        } finally {
            filterbox.setText("");
        }
    }

    @Test
    public void testSwitchHubInstance() {
        botUtils.preferences().openHubPreferencesFromEclipseMenu();
        botUtils.preferences().hubSettings().enterValidCredentials();
        botUtils.preferences().pressApplyAndClose();
        botUtils.workbench().openComponentInspectorView();
        final SWTBotTreeItem projectNode = botUtils.workbench().getProject(TestConstants.TEST_MAVEN_COMPONENTS_ARTIFACT);
        projectNode.select();
        assertNotNull(botUtils.componentInspector().getInspectionStatusIfCompleteOrInProgress());
        botUtils.preferences().openHubPreferencesFromEclipseMenu();
        botUtils.preferences().hubSettings().enterAlternateValidCredentials();
        botUtils.preferences().pressApplyAndClose();
        assertNotNull(botUtils.componentInspector().getInspectionStatusIfCompleteOrInProgress());
    }

    @AfterClass
    public static void tearDownWorkspaceBot() {
        botUtils.workbench().deleteProjectFromDisk(TestConstants.TEST_MAVEN_ARTIFACT);
        botUtils.workbench().deleteProjectFromDisk(TestConstants.TEST_MAVEN_COMPONENTS_ARTIFACT);
        botUtils.workbench().deleteProjectFromDisk(TestConstants.TEST_MAVEN_EMPTY_ARTIFACT);
        botUtils.workbench().deleteProjectFromDisk(TestConstants.TEST_NON_JAVA_PROJECT_NAME);
        botUtils.bot().resetWorkbench();
    }

}
