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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;

public class BlackDuckBotUtils extends AbstractBotUtils {
    private final ComponentInspectorBotUtils componentInspectorBotUtils;

    private final PreferenceBotUtils preferenceBotUtils;

    private final WorkbenchBotUtils workbenchBotUtils;

    public static String WELCOME_VIEW_TITLE = "Welcome";

    public BlackDuckBotUtils() {
        super(null);
        this.componentInspectorBotUtils = new ComponentInspectorBotUtils(this);
        this.preferenceBotUtils = new PreferenceBotUtils(this);
        this.workbenchBotUtils = new WorkbenchBotUtils(this);
    }

    public SWTWorkbenchBot bot() {
        return bot;
    }

    public ComponentInspectorBotUtils componentInspector() {
        return componentInspectorBotUtils;
    }

    public PreferenceBotUtils preferences() {
        return preferenceBotUtils;
    }

    public WorkbenchBotUtils workbench() {
        return workbenchBotUtils;
    }

    public void closeWelcomeView() {
        this.setSWTBotTimeoutShort();
        try {
            bot.viewByTitle(WELCOME_VIEW_TITLE).close();
        } catch (final RuntimeException e) {
        }
        this.setSWTBotTimeoutDefault();
    }

    public SWTBotView getSupportedProjectView() {
        for (final String viewTitle : Arrays.asList(WorkbenchBotUtils.PACKAGE_EXPLORER_VIEW, WorkbenchBotUtils.PROJECT_EXPLORER_VIEW)) {
            try {
                final SWTBotView view = bot.viewByTitle(viewTitle);
                return view;
            } catch (final WidgetNotFoundException e) {
            }
        }
        throw new WidgetNotFoundException(
                "Niether " + WorkbenchBotUtils.PACKAGE_EXPLORER_VIEW + " nor " + WorkbenchBotUtils.PROJECT_EXPLORER_VIEW + " was found");
    }

    public void closeActiveShell() {
        final SWTBotShell preferencesShell = bot.activeShell();
        preferencesShell.close();
    }

    public void closeActiveShellIfExists() {
        try {
            final SWTBotShell preferencesShell = bot.activeShell();
            preferencesShell.close();
        } catch (final Exception e) {
            // Do nothing
        }
    }

    public void addJarToProject(final String jarPath, final String projectName) throws IOException {
        final File resourceJar = new File(jarPath);
        final IWorkspace workspace = ResourcesPlugin.getWorkspace();
        final IWorkspaceRoot workspaceRoot = workspace.getRoot();
        IPath workspacePath = workspaceRoot.getLocation();
        final String projectPomRelativePath = String.format("/%s/target/%s", projectName, resourceJar.toPath().getFileName());
        workspacePath = workspacePath.append(projectPomRelativePath);
        final File projectLocation = workspacePath.toFile();
        Files.copy(resourceJar.toPath(), projectLocation.toPath());
    }
}
