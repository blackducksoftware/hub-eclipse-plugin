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
package com.blackducksoftware.integration.eclipse.test.swtbot.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;

import com.blackducksoftware.integration.eclipse.preferencepages.hub.HubPreferences;
import com.blackducksoftware.integration.eclipse.test.TestConstants;

public class HubPreferencesBotUtils extends AbstractPreferenceBotUtils {
    private final Properties validHubCredentials;
    private final Properties alternateValidHubCredentials;

    public HubPreferencesBotUtils(final BlackDuckBotUtils botUtils) throws FileNotFoundException, IOException {
        super(botUtils);
        validHubCredentials = new Properties();
        validHubCredentials.load(new FileInputStream("resources/alternateValidHubCredentials.properties"));
        alternateValidHubCredentials = new Properties();
        alternateValidHubCredentials.load(new FileInputStream("resources/validHubCredentials.properties"));
    }

    public void enterValidCredentials() {
        this.enterCredentials(validHubCredentials.getProperty(TestConstants.HUB_USERNAME_KEY),
                validHubCredentials.getProperty(TestConstants.HUB_PASSWORD_KEY),
                validHubCredentials.getProperty(TestConstants.HUB_URL_KEY),
                validHubCredentials.getProperty(TestConstants.HUB_TIMEOUT_KEY));
    }

    public void enterAlternateValidCredentials() {
        this.enterCredentials(alternateValidHubCredentials.getProperty(TestConstants.HUB_USERNAME_KEY),
                alternateValidHubCredentials.getProperty(TestConstants.HUB_PASSWORD_KEY),
                alternateValidHubCredentials.getProperty(TestConstants.HUB_URL_KEY),
                alternateValidHubCredentials.getProperty(TestConstants.HUB_TIMEOUT_KEY));
    }

    public void enterInvalidCredentials() {
        this.enterCredentials(TestConstants.INVALID_STRING, TestConstants.INVALID_STRING, TestConstants.INVALID_STRING, TestConstants.INVALID_STRING);
    }

    public void enterCredentials(final String hubUsername, final String hubPassword, final String hubUrl, final String hubTimeout) {
        this.enterCredentials(hubUsername, hubPassword, hubUrl, hubTimeout, "", "", "", "");
    }

    public void enterCredentials(final String hubUsername, final String hubPassword, final String hubUrl, final String hubTimeout,
            final String proxyUsername, final String proxyPassword, final String proxyHost, final String proxyPort) {
        enterUsername(hubUsername);
        enterPassword(hubPassword);
        enterUrl(hubUrl);
        enterTimeout(hubTimeout);
        final SWTBotText proxyUsernameField = bot.textWithLabel(HubPreferences.PROXY_USERNAME_LABEL);
        proxyUsernameField.setText(proxyUsername);
        final SWTBotText proxyPasswordField = bot.textWithLabel(HubPreferences.PROXY_PASSWORD_LABEL);
        proxyPasswordField.setText(proxyPassword);
        final SWTBotText proxyHostField = bot.textWithLabel(HubPreferences.PROXY_HOST_LABEL);
        proxyHostField.setText(proxyHost);
        final SWTBotText proxyPortField = bot.textWithLabel(HubPreferences.PROXY_PORT_LABEL);
        proxyPortField.setText(proxyPort);
    }

    public void enterUsername(final String hubUsername) {
        final SWTBotText usernameField = bot.textWithLabel(HubPreferences.HUB_USERNAME_LABEL);
        usernameField.setText(hubUsername);
    }

    public void enterPassword(final String hubPassword) {
        final SWTBotText passwordField = bot.textWithLabel(HubPreferences.HUB_PASSWORD_LABEL);
        passwordField.setText(hubPassword);
    }

    public void enterUrl(final String hubUrl) {
        final SWTBotText urlField = bot.textWithLabel(HubPreferences.HUB_URL_LABEL);
        urlField.setText(hubUrl);
    }

    public void enterTimeout(final String hubTimeout) {
        final SWTBotText timeoutField = bot.textWithLabel(HubPreferences.HUB_TIMEOUT_LABEL);
        timeoutField.setText(hubTimeout);
    }

    public void resetCredentials() {
        this.enterCredentials("", "", "", "", "", "", "", "");
        this.pressApplyAndClose();
    }

    public void testCurrentCredentials() {
        final SWTBot pageBot = bot.activeShell().bot();
        this.pressButton(pageBot, HubPreferences.TEST_HUB_CREDENTIALS_TEXT);
    }
}
