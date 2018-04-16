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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.blackducksoftware.integration.eclipse.preferencepages.hub.HubPreferences;
import com.blackducksoftware.integration.eclipse.test.TestConstants;
import com.blackducksoftware.integration.eclipse.test.swtbot.utils.BlackDuckBotUtils;
import com.blackducksoftware.integration.eclipse.test.swtbot.utils.HubPreferencesBotUtils;

@RunWith(SWTBotJunit4ClassRunner.class)
public class BlackDuckHubPreferencesBotTest {

    public static BlackDuckBotUtils botUtils;

    @BeforeClass
    public static void setUpWorkspace() {
        botUtils = new BlackDuckBotUtils();
        botUtils.closeWelcomeView();
        botUtils.preferences().openHubPreferencesFromEclipseMenu();
        botUtils.preferences().inspectorSettings().openComponentInspectorPreferences();
        botUtils.preferences().inspectorSettings().setInspectNewByDefaultTrue();
        botUtils.preferences().inspectorSettings().pressApplyAndClose();
        botUtils.workbench().createProject().createMavenProject(TestConstants.TEST_MAVEN_GROUP, TestConstants.TEST_MAVEN_ARTIFACT);
    }

    @After
    public void tearDown() {
        botUtils.setSWTBotTimeoutDefault();
    }

    @Test
    public void testAppearsInEclipseMenu() {
        botUtils.preferences().openHubPreferencesFromEclipseMenu();
        final SWTWorkbenchBot bot = botUtils.bot();
        final SWTBotTree preferencesTree = bot.tree();
        final SWTBotTreeItem blackDuckNode = preferencesTree.getTreeItem(TestConstants.HUB_PREFERENCE_PAGE_NAME);
        assertNotNull(blackDuckNode);
        blackDuckNode.expand();
        assertNotNull(blackDuckNode.getNode(TestConstants.COMPONENT_INSPECTOR_PREFERENCE_PAGE_NAME));
        botUtils.closeActiveShellIfExists();
    }

    @Test
    public void testValidHubConfiguration() {
        botUtils.preferences().openHubPreferencesFromEclipseMenu();
        botUtils.preferences().hubSettings().enterValidCredentials();
        botUtils.preferences().hubSettings().testCurrentCredentials();
        final SWTWorkbenchBot bot = botUtils.bot();
        assertNotNull(bot.text(HubPreferences.LOGIN_SUCCESS_MESSAGE));
        botUtils.closeActiveShellIfExists();
    }

    @Test
    public void testInvalidHubURL() {
        botUtils.preferences().openHubPreferencesFromEclipseMenu();
        botUtils.preferences().hubSettings().enterCredentials(HubPreferencesBotUtils.VALID_HUB_USERNAME, HubPreferencesBotUtils.VALID_HUB_PASSWORD,
                HubPreferencesBotUtils.INVALID_STRING, HubPreferencesBotUtils.VALID_HUB_TIMEOUT);
        botUtils.preferences().hubSettings().testCurrentCredentials();
        final SWTWorkbenchBot bot = botUtils.bot();
        try {
            botUtils.setSWTBotTimeoutShort();
            assertNull(bot.text(HubPreferences.LOGIN_SUCCESS_MESSAGE));
        } catch (final WidgetNotFoundException e) {
            // Expected
        } finally {
            botUtils.closeActiveShellIfExists();
        }
    }

    @Test
    public void testInvalidHubUsername() {
        botUtils.preferences().openHubPreferencesFromEclipseMenu();
        botUtils.preferences().hubSettings().enterCredentials(HubPreferencesBotUtils.INVALID_STRING, HubPreferencesBotUtils.VALID_HUB_PASSWORD,
                HubPreferencesBotUtils.VALID_HUB_URL, HubPreferencesBotUtils.VALID_HUB_TIMEOUT);
        botUtils.preferences().hubSettings().testCurrentCredentials();
        final SWTWorkbenchBot bot = botUtils.bot();
        try {
            botUtils.setSWTBotTimeoutShort();
            assertNull(bot.text(HubPreferences.LOGIN_SUCCESS_MESSAGE));
        } catch (final WidgetNotFoundException e) {
            // Expected
        } finally {
            botUtils.closeActiveShellIfExists();
        }
    }

    @Test
    public void testInvalidHubPassword() {
        botUtils.preferences().openHubPreferencesFromEclipseMenu();
        botUtils.preferences().hubSettings().enterCredentials(HubPreferencesBotUtils.VALID_HUB_USERNAME, HubPreferencesBotUtils.INVALID_STRING,
                HubPreferencesBotUtils.VALID_HUB_URL, HubPreferencesBotUtils.VALID_HUB_TIMEOUT);
        botUtils.preferences().hubSettings().testCurrentCredentials();
        final SWTWorkbenchBot bot = botUtils.bot();
        try {
            botUtils.setSWTBotTimeoutShort();
            assertNull(bot.text(HubPreferences.LOGIN_SUCCESS_MESSAGE));
        } catch (final WidgetNotFoundException e) {
            // Expected
        } finally {
            botUtils.closeActiveShellIfExists();
        }
    }

    @Test
    public void testInvalidHubTimeout() {
        botUtils.preferences().openHubPreferencesFromEclipseMenu();
        botUtils.preferences().hubSettings().enterCredentials(HubPreferencesBotUtils.VALID_HUB_USERNAME, HubPreferencesBotUtils.VALID_HUB_PASSWORD,
                HubPreferencesBotUtils.VALID_HUB_URL, HubPreferencesBotUtils.INVALID_STRING);
        botUtils.preferences().hubSettings().testCurrentCredentials();
        final SWTWorkbenchBot bot = botUtils.bot();
        try {
            botUtils.setSWTBotTimeoutShort();
            assertNull(bot.text(HubPreferences.LOGIN_SUCCESS_MESSAGE));
        } catch (final WidgetNotFoundException e) {
            // Expected
        } finally {
            botUtils.closeActiveShellIfExists();
        }
    }

    @Test
    public void testApplyChanges() {
        botUtils.workbench().openComponentInspectorView();
        botUtils.preferences().openHubPreferencesFromEclipseMenu();
        botUtils.preferences().hubSettings().enterValidCredentials();
        botUtils.preferences().pressApply();
        botUtils.closeActiveShell();
        final SWTBotTreeItem node = botUtils.workbench().getProject(TestConstants.TEST_MAVEN_ARTIFACT);
        node.click();
        try {
            assertNotNull(botUtils.componentInspector().getInspectionStatusIfCompleteOrInProgress());
        } finally {
            botUtils.preferences().openHubPreferencesFromEclipseMenu();
            botUtils.preferences().hubSettings().resetCredentials();
        }
    }

    @Test
    public void testOK() {
        botUtils.workbench().openComponentInspectorView();
        botUtils.preferences().openHubPreferencesFromEclipseMenu();
        botUtils.preferences().hubSettings().enterValidCredentials();
        botUtils.preferences().pressApplyAndClose();
        final SWTBotTreeItem node = botUtils.workbench().getProject(TestConstants.TEST_MAVEN_ARTIFACT);
        node.click();
        try {
            assertNotNull(botUtils.componentInspector().getInspectionStatusIfCompleteOrInProgress());
        } finally {
            botUtils.preferences().openHubPreferencesFromEclipseMenu();
            botUtils.preferences().hubSettings().resetCredentials();
        }
    }

    @Test
    public void testCancel() {
        botUtils.workbench().openComponentInspectorView();
        botUtils.preferences().openHubPreferencesFromEclipseMenu();
        botUtils.preferences().hubSettings().enterInvalidCredentials();
        botUtils.preferences().pressApplyAndClose();
        botUtils.preferences().openHubPreferencesFromEclipseMenu();
        botUtils.preferences().hubSettings().enterValidCredentials();
        botUtils.preferences().pressCancel();
        final SWTBotTreeItem node = botUtils.workbench().getProject(TestConstants.TEST_MAVEN_ARTIFACT);
        node.click();
        try {
            assertNull(botUtils.componentInspector().getInspectionStatusIfCompleteOrInProgress());
        } catch (final WidgetNotFoundException e) {
            // Expected
        }
    }

    @AfterClass
    public static void tearDownWorkspace() {
        botUtils.workbench().deleteProjectFromDisk(TestConstants.TEST_MAVEN_ARTIFACT);
        botUtils.bot().resetWorkbench();
    }
}
