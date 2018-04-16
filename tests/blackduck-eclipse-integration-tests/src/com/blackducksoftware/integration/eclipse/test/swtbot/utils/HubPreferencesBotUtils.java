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

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;

import com.blackducksoftware.integration.eclipse.preferencepages.hub.HubPreferences;

public class HubPreferencesBotUtils extends AbstractPreferenceBotUtils {
    public static final String VALID_HUB_USERNAME = "sysadmin";
    public static final String VALID_HUB_PASSWORD = "blackduck";
    public static final String VALID_HUB_URL = "http://int-auto01.dc1.lan:9000";
    public static final String ALT_VALID_HUB_URL = "http://int-hub01.dc1.lan:8080";
    public static final String VALID_HUB_TIMEOUT = "120";
    public static final String INVALID_STRING = "INVALID";

    public HubPreferencesBotUtils(final BlackDuckBotUtils botUtils) {
        super(botUtils);
    }

    public void enterValidCredentials() {
        this.enterCredentials(VALID_HUB_USERNAME, VALID_HUB_PASSWORD, VALID_HUB_URL, VALID_HUB_TIMEOUT);
    }

    public void enterInvalidCredentials() {
        this.enterCredentials(INVALID_STRING, INVALID_STRING, INVALID_STRING, INVALID_STRING);
    }

    public void enterCredentials(final String hubUsername, final String hubPassword, final String hubUrl, final String hubTimeout) {
        this.enterCredentials(hubUsername, hubPassword, hubUrl, hubTimeout, "", "", "", "");
    }

    public void enterCredentials(final String hubUsername, final String hubPassword, final String hubUrl, final String hubTimeout,
            final String proxyUsername, final String proxyPassword, final String proxyHost, final String proxyPort) {
        final SWTBotText usernameField = bot.textWithLabel(HubPreferences.HUB_USERNAME_LABEL);
        usernameField.setText(hubUsername);
        final SWTBotText passwordField = bot.textWithLabel(HubPreferences.HUB_PASSWORD_LABEL);
        passwordField.setText(hubPassword);
        final SWTBotText urlField = bot.textWithLabel(HubPreferences.HUB_URL_LABEL);
        urlField.setText(hubUrl);
        final SWTBotText timeoutField = bot.textWithLabel(HubPreferences.HUB_TIMEOUT_LABEL);
        timeoutField.setText(hubTimeout);
        final SWTBotText proxyUsernameField = bot.textWithLabel(HubPreferences.PROXY_USERNAME_LABEL);
        proxyUsernameField.setText(proxyUsername);
        final SWTBotText proxyPasswordField = bot.textWithLabel(HubPreferences.PROXY_PASSWORD_LABEL);
        proxyPasswordField.setText(proxyPassword);
        final SWTBotText proxyHostField = bot.textWithLabel(HubPreferences.PROXY_HOST_LABEL);
        proxyHostField.setText(proxyHost);
        final SWTBotText proxyPortField = bot.textWithLabel(HubPreferences.PROXY_PORT_LABEL);
        proxyPortField.setText(proxyPort);
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
