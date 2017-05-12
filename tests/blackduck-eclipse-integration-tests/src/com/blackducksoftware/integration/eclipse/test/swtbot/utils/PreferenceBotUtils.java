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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.blackducksoftware.integration.eclipse.test.TestConstants;

public class PreferenceBotUtils extends AbstractPreferenceBotUtils {
	public static final String OK_BUTTON_TEXT = "OK";

	public static final String APPLY_BUTTON_TEXT = "Apply";

	public static final String DEFAULTS_BUTTON_TEXT = "Restore Defaults";

	public static final String CANCEL_BUTTON_TEXT = "Cancel";

	private final HubPreferencesBotUtils hubPreferencesBotUtils;

	private final InspectorPreferencesBotUtils inspectorPreferencesBotUtils;

	public PreferenceBotUtils(final BlackDuckBotUtils botUtils) {
		super(botUtils);
		this.hubPreferencesBotUtils = new HubPreferencesBotUtils(botUtils);
		this.inspectorPreferencesBotUtils = new InspectorPreferencesBotUtils(botUtils);
	}

	public HubPreferencesBotUtils hubSettings() {
		return hubPreferencesBotUtils;
	}

	public InspectorPreferencesBotUtils inspectorSettings() {
		return inspectorPreferencesBotUtils;
	}

	public void openHubPreferencesFromContextMenu() {
		final SWTBotView view = botUtils.getSupportedProjectView();
		view.setFocus();
		final SWTBot viewBot = view.bot();
		final SWTBotTree tree = viewBot.tree();
		tree.setFocus();
		this.selectFromMenu(tree.contextMenu(), TestConstants.BLACK_DUCK_HUB_CATEGORY_NAME, TestConstants.CONTEXT_MENU_OPEN_HUB_SETTINGS_ACTION);
		bot.waitUntil(Conditions.shellIsActive(PREFERENCES_FILTERED_WINDOW_TITLE));
	}

	public void openHubPreferencesFromEclipseMenu() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		workbench.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				final IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
				if (window != null) {
					final Menu appMenu = workbench.getDisplay().getSystemMenu();
					for (final MenuItem item : appMenu.getItems()) {
						if (item.getText().startsWith(PREFERENCES_WINDOW_TITLE)) {
							final Event event = new Event();
							event.time = (int) System.currentTimeMillis();
							event.widget = item;
							event.display = workbench.getDisplay();
							item.setSelection(true);
							item.notifyListeners(SWT.Selection, event);
							break;
						}
					}
				}
			}
		});
		try {
			this.setSWTBotTimeoutShort();
			bot.waitUntil(Conditions.shellIsActive(PREFERENCES_WINDOW_TITLE));
			this.setSWTBotTimeoutDefault();
		} catch (final TimeoutException e1) {
			try {
				this.setSWTBotTimeoutDefault();
				bot.menu("Window").menu(PREFERENCES_WINDOW_TITLE).click();
				bot.waitUntil(Conditions.shellIsActive(PREFERENCES_WINDOW_TITLE));
			} catch (final WidgetNotFoundException e2) {
				this.setSWTBotTimeoutDefault();
				bot.activeShell().close();
				bot.menu("Window").menu(PREFERENCES_WINDOW_TITLE).click();
				bot.waitUntil(Conditions.shellIsActive(PREFERENCES_WINDOW_TITLE));
			}
		}
		final SWTBotTree tree = bot.tree();
		final SWTBotTreeItem blackDuckNode = tree.getTreeItem(TestConstants.HUB_PREFERENCE_PAGE_NAME);
		blackDuckNode.click();
	}

}
