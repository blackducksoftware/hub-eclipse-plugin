/**
 * com.blackducksoftware.integration.eclipse.hub.connector
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
package com.blackducksoftware.integration.eclipse.hub.contextmenu.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.handlers.HandlerUtil;

import com.blackducksoftware.integration.eclipse.hub.preferencepages.HubPreferences;
import com.blackducksoftware.integration.eclipse.preferencepages.ComponentInspectorPreferences;

public class OpenBlackDuckHubSettings extends AbstractHandler {
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final Shell activeShell = HandlerUtil.getActiveShell(event);
		final String[] pageIds = new String[] { HubPreferences.PREFERENCE_PAGE_ID, ComponentInspectorPreferences.PREFERENCE_PAGE_ID };
		final PreferenceDialog prefPage = PreferencesUtil.createPreferenceDialogOn(activeShell,
				HubPreferences.PREFERENCE_PAGE_ID, pageIds, null);
		prefPage.open();
		return null;
	}

}
