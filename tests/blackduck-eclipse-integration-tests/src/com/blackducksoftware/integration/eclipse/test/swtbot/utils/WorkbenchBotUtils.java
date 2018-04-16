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
import java.nio.file.StandardCopyOption;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRootMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

import com.blackducksoftware.integration.eclipse.test.TestConstants;

public class WorkbenchBotUtils extends AbstractBotUtils {
    public static final String PACKAGE_EXPLORER_VIEW = "Package Explorer";

    public static final String PROJECT_EXPLORER_VIEW = "Project Explorer";

    public static final String PROJECTS_VIEW = "Projects";

    public static final String MENU_WINDOW = "Window";

    public static final String MENU_WINDOW_SHOW_VIEW = "Show View";

    public static final String MENU_WINDOW_SHOW_VIEW_OTHER = "Other...";

    public static final String SHOW_VIEW_WINDOW_TITLE = "Show View";

    public static final String VIEW_TYPE_JAVA = "Java";

    public static final String VIEW_TYPE_JAVA_BROWSING = "Java Browsing";

    public static final String VIEW_TYPE_GENERAL = "General";

    private final ProjectCreationBotUtils projectCreationBotUtils;

    public WorkbenchBotUtils(final BlackDuckBotUtils botUtils) {
        super(botUtils);
        projectCreationBotUtils = new ProjectCreationBotUtils(botUtils);
    }

    public ProjectCreationBotUtils createProject() {
        return projectCreationBotUtils;
    }

    public SWTBotView getPackageExplorerView() {
        final SWTBotView view = bot.viewByTitle(PACKAGE_EXPLORER_VIEW);
        return view;
    }

    public SWTBotView getProjectExplorerView() {
        final SWTBotView view = bot.viewByTitle(PROJECT_EXPLORER_VIEW);
        return view;
    }

    public SWTBotView getProjectsView() {
        final SWTBotView view = bot.viewByTitle(PROJECTS_VIEW);
        return view;
    }

    public void openPackageExplorerView() {
        this.openShowViewDialog();
        final SWTBotTreeItem javaNode = this.expandSuperNode(VIEW_TYPE_JAVA);
        javaNode.expandNode(PACKAGE_EXPLORER_VIEW).select();
        final SWTBot pageBot = bot.activeShell().bot();
        this.pressButton(pageBot, "OK");
    }

    public void openProjectExplorerview() {
        this.openShowViewDialog();
        final SWTBotTreeItem generalNode = this.expandSuperNode(VIEW_TYPE_GENERAL);
        generalNode.expandNode(PROJECT_EXPLORER_VIEW).select();
        final SWTBot pageBot = bot.activeShell().bot();
        this.pressButton(pageBot, "OK");
    }

    public void openProjectsView() {
        this.openShowViewDialog();
        final SWTBotTreeItem javaBrowsingNode = this.expandSuperNode(VIEW_TYPE_JAVA_BROWSING);
        javaBrowsingNode.expandNode(PROJECTS_VIEW).select();
        final SWTBot pageBot = bot.activeShell().bot();
        this.pressButton(pageBot, "OK");
    }

    public void openComponentInspectorView() {
        this.openShowViewDialog();
        final SWTBotTreeItem blackDuckNode = this.expandSuperNode(TestConstants.BLACK_DUCK_CATEGORY_NAME);
        blackDuckNode.getNode(TestConstants.COMPONENT_INSPECTOR_VIEW_NAME).select();
        final SWTBot pageBot = bot.activeShell().bot();
        this.pressButton(pageBot, "OK");
    }

    private void openShowViewDialog() {
        final SWTBotMenu windowMenu = bot.menu(MENU_WINDOW);
        final SWTBotMenu showViewMenu = windowMenu.menu(MENU_WINDOW_SHOW_VIEW);
        final SWTBotMenu allViewsMenu = showViewMenu.menu(MENU_WINDOW_SHOW_VIEW_OTHER);
        allViewsMenu.click();
        bot.waitUntil(Conditions.shellIsActive(SHOW_VIEW_WINDOW_TITLE));
    }

    public void updateMavenProject(final String projectName) {
        final SWTBotTreeItem mavenProjectNode = this.getProject(projectName);
        final SWTBotMenu mavenMenu = mavenProjectNode.contextMenu().menu("Maven");
        mavenMenu.menu("Update Project...").click();
        bot.waitUntil(Conditions.shellIsActive("Update Maven Project"));
        final SWTBot pageBot = bot.activeShell().bot();
        this.pressButton(pageBot, "OK");
    }

    public void addMavenDependency(final String projectName, final String groupId, final String artifactId,
            final String version) {
        final SWTBotTreeItem mavenProjectNode = this.getProject(projectName);
        final SWTBotMenu mavenMenu = mavenProjectNode.contextMenu().menu("Maven");
        mavenMenu.menu("Add Dependency").click();
        bot.waitUntil(Conditions.shellIsActive("Add Dependency"));
        bot.text(0).setText(groupId);
        bot.text(1).setText(artifactId);
        bot.text(2).setText(version);
        this.pressButton(bot, "OK");
    }

    public void deleteProjectFromDisk(final String projectName) {
        final SWTBotTreeItem projectNode = this.getProjectInPackageExplorer(projectName);
        projectNode.contextMenu().menu("Delete").click();
        bot.waitUntil(Conditions.shellIsActive("Delete Resources"));
        bot.checkBox().select();
        this.pressButton(bot, "OK");
        try {
            bot.waitUntil(Conditions.shellCloses(bot.shell("Delete Resources")));
        } catch (final WidgetNotFoundException e) {
        }
    }

    public void deleteProjectFromWorkspace(final String projectName, final SWTWorkbenchBot bot) {
        this.openPackageExplorerView();
        final SWTBotTreeItem projectNode = this.getProjectInPackageExplorer(projectName);
        projectNode.contextMenu().menu("Delete").click();
        bot.waitUntil(Conditions.shellIsActive("Delete Resources"));
        this.pressButton(bot, "OK");
        try {
            bot.waitUntil(Conditions.shellCloses(bot.shell("Delete Resources")));
        } catch (final WidgetNotFoundException e) {
        }
    }

    public SWTBotTreeItem getProjectInPackageExplorer(final String projectName){
        this.openPackageExplorerView();
        final SWTBotView view = this.getPackageExplorerView();
        view.setFocus();
        final SWTBot viewBot = view.bot();
        final SWTBotTree tree = viewBot.tree();
        return tree.getTreeItem(projectName);
    }

    public SWTBotTreeItem getProject(final String projectName) {
        final SWTBotView view = botUtils.getSupportedProjectView();
        view.setFocus();
        final SWTBot viewBot = view.bot();
        final SWTBotTree tree = viewBot.tree();
        return tree.getTreeItem(projectName);
    }

    public void copyPomToProject(final String resourcePomPath, final String projectName) throws IOException {
        final File resourcePom = new File(resourcePomPath);
        final IWorkspace workspace = ResourcesPlugin.getWorkspace();
        final IWorkspaceRoot workspaceRoot = workspace.getRoot();
        IPath workspacePath = workspaceRoot.getLocation();
        final String projectPomRelativePath = String.format("/%s/pom.xml", projectName);
        workspacePath = workspacePath.append(projectPomRelativePath);
        final File projectPom = workspacePath.toFile();
        Files.copy(resourcePom.toPath(), projectPom.toPath(), StandardCopyOption.REPLACE_EXISTING);
        this.updateMavenProject(projectName);
    }

    public void closeProject(final String projectName) {
        final SWTBotTreeItem projectNode = getProject(projectName);
        final SWTBotRootMenu projectMenu = projectNode.contextMenu();
        final SWTBotMenu closeProject = projectMenu.menu("Close Project");
        closeProject.click();
    }
}
