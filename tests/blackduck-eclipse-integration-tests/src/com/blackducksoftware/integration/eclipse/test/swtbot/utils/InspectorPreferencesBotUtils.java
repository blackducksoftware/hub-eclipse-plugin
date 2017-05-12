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
package com.blackducksoftware.integration.eclipse.test.swtbot.utils;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

import com.blackducksoftware.integration.eclipse.preferencepages.ComponentInspectorPreferences;
import com.blackducksoftware.integration.eclipse.test.TestConstants;
import com.blackducksoftware.integration.eclipse.test.swtbot.utils.conditions.TreeItemIsExpandedCondition;

public class InspectorPreferencesBotUtils extends AbstractPreferenceBotUtils {
	public InspectorPreferencesBotUtils(final BlackDuckBotUtils botUtils) {
		super(botUtils);
	}

	public void setAnalyzeByDefaultTrue() {
		final SWTBot pageBot = bot.activeShell().bot();
		pageBot.radio(ComponentInspectorPreferences.INSPECT_AUTOMATICALLY_LABEL).click();
	}

	public void setAnalyzeByDefaultFalse() {
		final SWTBot pageBot = bot.activeShell().bot();
		pageBot.radio(ComponentInspectorPreferences.DO_NOT_INSPECT_AUTOMATICALLY_LABEL).click();
	}

	public void activateProject(final String projectName) {
		final SWTBot pageBot = bot.activeShell().bot();
		pageBot.checkBox(projectName).select();
	}

	public void deactivateProject(final String projectName) {
		final SWTBot pageBot = bot.activeShell().bot();
		pageBot.checkBox(projectName).deselect();
	}

	public void openComponentInspectorPreferences() {
		final SWTBot pageBot = bot.activeShell().bot();
		final SWTBotTreeItem blackDuck = pageBot.tree().expandNode(TestConstants.HUB_PREFERENCE_PAGE_NAME);
		bot.waitUntil(new TreeItemIsExpandedCondition(blackDuck));
		blackDuck.getNode(TestConstants.COMPONENT_INSPECTOR_PREFERENCE_PAGE_NAME).click();
	}

}
