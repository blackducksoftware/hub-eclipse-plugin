/**
 * blackduck-eclipse-integration-tests
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
package com.blackducksoftware.integration.eclipse.test.swtbot;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.blackducksoftware.integration.eclipse.test.TestConstants;
import com.blackducksoftware.integration.eclipse.test.swtbot.utils.BlackDuckBotUtils;

@RunWith(SWTBotJunit4ClassRunner.class)
public class ComponentInspectorPreferencesBotTest {
    private static BlackDuckBotUtils botUtils;

    @BeforeClass
    public static void setUpWorkspace() {
        botUtils = new BlackDuckBotUtils();
        botUtils.closeWelcomeView();
        botUtils.preferences().openHubPreferencesFromEclipseMenu();
        botUtils.preferences().hubSettings().enterValidCredentials();
        botUtils.preferences().hubSettings().pressOK();
        botUtils.workbench().createProject().createGradleProject(TestConstants.TEST_GRADLE_PROJECT_NAME);
        botUtils.workbench().createProject().createGeneralProject(TestConstants.TEST_NON_JAVA_PROJECT_NAME);
    }

    @After
    public void cleanUp() {
        botUtils.setSWTBotTimeoutShort();
        try {
            botUtils.bot().shell("Preferences").close();
        } catch (WidgetNotFoundException e) {
            // Do nothing, because shell is closed
        }
        botUtils.setSWTBotTimeoutDefault();
        botUtils.workbench().deleteProjectFromDisk(TestConstants.TEST_MAVEN_ARTIFACT);
    }

    @Test
    public void testThatAllJavaProjectsShow() {
        botUtils.workbench().createProject().createMavenProject(TestConstants.TEST_MAVEN_GROUP, TestConstants.TEST_MAVEN_ARTIFACT);
        botUtils.preferences().openHubPreferencesFromEclipseMenu();
        botUtils.preferences().inspectorSettings().openComponentInspectorPreferences();
        final SWTBot pageBot = botUtils.bot().activeShell().bot();
        assertNotNull(pageBot.checkBox(TestConstants.TEST_GRADLE_PROJECT_NAME));
        assertNotNull(pageBot.checkBox(TestConstants.TEST_MAVEN_ARTIFACT));
        try {
            pageBot.checkBox(TestConstants.TEST_NON_JAVA_PROJECT_NAME);
            fail();
        } catch (final WidgetNotFoundException e) {
        }
    }

    @Test
    public void testAnalyzeByDefault() {
        botUtils.preferences().openHubPreferencesFromEclipseMenu();
        botUtils.preferences().inspectorSettings().openComponentInspectorPreferences();
        botUtils.preferences().inspectorSettings().setAnalyzeByDefaultTrue();
        botUtils.preferences().inspectorSettings().pressOK();
        botUtils.workbench().createProject().createMavenProject(TestConstants.TEST_MAVEN_GROUP, TestConstants.TEST_MAVEN_ARTIFACT);
        botUtils.preferences().openHubPreferencesFromEclipseMenu();
        botUtils.preferences().inspectorSettings().openComponentInspectorPreferences();
        final SWTBot pageBot = botUtils.bot().activeShell().bot();
        assertNotNull(pageBot.checkBox(TestConstants.TEST_MAVEN_ARTIFACT));
        assertTrue(pageBot.checkBox(TestConstants.TEST_MAVEN_ARTIFACT).isChecked());
    }

    @Test
    public void testDoNotAnalyzeByDefault() {
        botUtils.preferences().openHubPreferencesFromEclipseMenu();
        botUtils.preferences().inspectorSettings().openComponentInspectorPreferences();
        botUtils.preferences().inspectorSettings().setAnalyzeByDefaultFalse();
        botUtils.preferences().pressOK();
        botUtils.workbench().createProject().createMavenProject(TestConstants.TEST_MAVEN_GROUP, TestConstants.TEST_MAVEN_ARTIFACT);
        botUtils.preferences().openHubPreferencesFromEclipseMenu();
        botUtils.preferences().inspectorSettings().openComponentInspectorPreferences();
        final SWTBot pageBot = botUtils.bot().activeShell().bot();
        assertNotNull(pageBot.checkBox(TestConstants.TEST_MAVEN_ARTIFACT));
        assertFalse(pageBot.checkBox(TestConstants.TEST_MAVEN_ARTIFACT).isChecked());
    }

    @Test
    public void testActivateProject() {
        botUtils.preferences().openHubPreferencesFromEclipseMenu();
        botUtils.preferences().inspectorSettings().openComponentInspectorPreferences();
        botUtils.preferences().inspectorSettings().setAnalyzeByDefaultFalse();
        botUtils.preferences().pressOK();
        botUtils.workbench().createProject().createMavenProject(TestConstants.TEST_MAVEN_GROUP, TestConstants.TEST_MAVEN_ARTIFACT);
        botUtils.workbench().openComponentInspectorView();
        final SWTBotTreeItem project = botUtils.workbench().getProject(TestConstants.TEST_MAVEN_ARTIFACT);
        project.select();
        botUtils.setSWTBotTimeoutShort();
        try {
            assertNull(botUtils.componentInspector().getInspectionStatusIfCompleteOrInProgress());
        } catch (final WidgetNotFoundException e) {
            // Do nothing, this is expected
        }
        botUtils.setSWTBotTimeoutDefault();
        botUtils.preferences().openHubPreferencesFromEclipseMenu();
        botUtils.preferences().inspectorSettings().openComponentInspectorPreferences();
        final SWTBot pageBot = botUtils.bot().activeShell().bot();
        final SWTBotCheckBox mavenBox = pageBot.checkBox(TestConstants.TEST_MAVEN_ARTIFACT);
        mavenBox.click();
        botUtils.preferences().inspectorSettings().pressOK();
        assertNotNull(botUtils.componentInspector().getInspectionStatusIfCompleteOrInProgress());
    }

    @Test
    public void testDeactivateProject() {
        botUtils.preferences().openHubPreferencesFromEclipseMenu();
        botUtils.preferences().inspectorSettings().openComponentInspectorPreferences();
        botUtils.preferences().inspectorSettings().setAnalyzeByDefaultTrue();
        botUtils.preferences().pressOK();
        botUtils.workbench().createProject().createMavenProject(TestConstants.TEST_MAVEN_GROUP, TestConstants.TEST_MAVEN_ARTIFACT);
        botUtils.workbench().openComponentInspectorView();
        final SWTBotTreeItem project = botUtils.workbench().getProject(TestConstants.TEST_MAVEN_ARTIFACT);
        project.select();
        assertNotNull(botUtils.componentInspector().getInspectionStatusIfCompleteOrInProgress());
        botUtils.preferences().openHubPreferencesFromEclipseMenu();
        botUtils.preferences().inspectorSettings().openComponentInspectorPreferences();
        final SWTBot pageBot = botUtils.bot().activeShell().bot();
        final SWTBotCheckBox mavenBox = pageBot.checkBox(TestConstants.TEST_MAVEN_ARTIFACT);
        mavenBox.click();
        botUtils.preferences().inspectorSettings().pressOK();
        botUtils.setSWTBotTimeoutShort();
        try {
            assertNull(botUtils.componentInspector().getInspectionStatusIfCompleteOrInProgress());
        } catch (final WidgetNotFoundException e) {
            // Do nothing, this is expected
        }
        botUtils.setSWTBotTimeoutDefault();
    }

    @Test
    public void testCancel() {
        botUtils.preferences().openHubPreferencesFromEclipseMenu();
        botUtils.preferences().inspectorSettings().openComponentInspectorPreferences();
        botUtils.preferences().inspectorSettings().setAnalyzeByDefaultFalse();
        botUtils.preferences().inspectorSettings().pressOK();
        botUtils.workbench().createProject().createMavenProject(TestConstants.TEST_MAVEN_GROUP, TestConstants.TEST_MAVEN_ARTIFACT);
        botUtils.preferences().openHubPreferencesFromEclipseMenu();
        botUtils.preferences().inspectorSettings().openComponentInspectorPreferences();
        botUtils.preferences().inspectorSettings().activateProject(TestConstants.TEST_MAVEN_ARTIFACT);
        botUtils.preferences().inspectorSettings().pressCancel();
        botUtils.preferences().openHubPreferencesFromEclipseMenu();
        botUtils.preferences().inspectorSettings().openComponentInspectorPreferences();
        final SWTBot pageBot = botUtils.bot().activeShell().bot();
        assertNotNull(pageBot.checkBox(TestConstants.TEST_MAVEN_ARTIFACT));
        assertFalse(pageBot.checkBox(TestConstants.TEST_MAVEN_ARTIFACT).isChecked());
    }

    @Test
    public void testApply() {
        botUtils.preferences().openHubPreferencesFromEclipseMenu();
        botUtils.preferences().inspectorSettings().openComponentInspectorPreferences();
        botUtils.preferences().inspectorSettings().setAnalyzeByDefaultFalse();
        botUtils.preferences().inspectorSettings().pressOK();
        botUtils.workbench().createProject().createMavenProject(TestConstants.TEST_MAVEN_GROUP, TestConstants.TEST_MAVEN_ARTIFACT);
        botUtils.preferences().openHubPreferencesFromEclipseMenu();
        botUtils.preferences().inspectorSettings().openComponentInspectorPreferences();
        botUtils.preferences().inspectorSettings().activateProject(TestConstants.TEST_MAVEN_ARTIFACT);
        botUtils.preferences().inspectorSettings().pressApply();
        botUtils.preferences().inspectorSettings().pressCancel();
        botUtils.preferences().openHubPreferencesFromEclipseMenu();
        botUtils.preferences().inspectorSettings().openComponentInspectorPreferences();
        final SWTBot pageBot = botUtils.bot().activeShell().bot();
        assertNotNull(pageBot.checkBox(TestConstants.TEST_MAVEN_ARTIFACT));
        assertTrue(pageBot.checkBox(TestConstants.TEST_MAVEN_ARTIFACT).isChecked());
    }

    @AfterClass
    public static void tearDownWorkspace() {
        botUtils.workbench().deleteProjectFromDisk(TestConstants.TEST_GRADLE_PROJECT_NAME);
        botUtils.workbench().deleteProjectFromDisk(TestConstants.TEST_NON_JAVA_PROJECT_NAME);
        botUtils.bot().resetWorkbench();
    }

}
