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
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;

import com.blackducksoftware.integration.eclipse.test.swtbot.utils.conditions.TreeItemIsExpandedCondition;

public class ProjectCreationBotUtils extends AbstractBotUtils {
    public static final String MENU_FILE = "File";

    public static final String MENU_FILE_NEW = "New";

    public static final String MENU_FILE_NEW_PROJECT = "Project...";

    public static final String NEW_PROJECT_WINDOW_TITLE = "New Project";

    public static final String FINISH_BUTTON = "Finish";

    public static final String NEXT_BUTTON = "Next >";

    public static final String PROJECT_NAME_FIELD = "Project name:";

    public static final String PROJECT_NAME_FIELD_GRADLE = "Project name";

    public static final String OPEN_ASSOCIATED_PERSPECTIVE_WINDOW_TITLE = "Open Associated Perspective?";

    public static final String OPEN_ASSOCIATED_PERSPECTIVE_NO_BUTTON = "No";

    public static final String PROJECT_TYPE_JAVA = "Java";

    public static final String PROJECT_TYPE_GRADLE = "Gradle";

    public static final String PROJECT_TYPE_MAVEN = "Maven";

    public static final String PROJECT_TYPE_GENERAL = "General";

    public static final String JAVA_PROJECT = "Java Project";

    public static final String GRADLE_PROJECT = "Gradle Project";

    public static final String MAVEN_PROJECT = "Maven Project";

    public static final String GENERAL_PROJECT = "Project";

    public ProjectCreationBotUtils(final BlackDuckBotUtils botUtils) {
        super(botUtils);
    }

    public void createJavaProject(final String projectName) {
        this.openNewProjectWindow();
        final SWTBotTree optionTree = bot.tree();
        final SWTBotTreeItem javaNode = optionTree.expandNode(PROJECT_TYPE_JAVA);
        bot.waitUntil(new TreeItemIsExpandedCondition(javaNode));
        javaNode.expandNode(JAVA_PROJECT).select();
        this.pressButton(bot, NEXT_BUTTON);
        bot.textWithLabel(PROJECT_NAME_FIELD).setText(projectName);
        this.finishAndOpenAssociatedPerspectiveIfNotOpen();
    }

    public void createGradleProject(final String projectName) {
        this.openNewProjectWindow();
        final SWTBotTree optionTree = bot.tree();
        final SWTBotTreeItem javaNode = optionTree.expandNode(PROJECT_TYPE_GRADLE);
        bot.waitUntil(new TreeItemIsExpandedCondition(javaNode));
        javaNode.expandNode(GRADLE_PROJECT).select();
        this.pressButton(bot, NEXT_BUTTON);
        try {
            this.pressButton(bot, NEXT_BUTTON);
        } catch (final WidgetNotFoundException e) {
            // For the welcome screen
        }
        bot.textWithLabel(PROJECT_NAME_FIELD_GRADLE).setText(projectName);
        this.finishAndOpenAssociatedPerspectiveIfNotOpen();
    }

    public void createGeneralProject(final String projectName) {
        this.openNewProjectWindow();
        final SWTBotTree optionTree = bot.tree();
        final SWTBotTreeItem generalNode = optionTree.expandNode(PROJECT_TYPE_GENERAL);
        bot.waitUntil(new TreeItemIsExpandedCondition(generalNode));
        generalNode.expandNode(GENERAL_PROJECT).select();
        this.pressButton(bot, NEXT_BUTTON);
        bot.textWithLabel(PROJECT_NAME_FIELD).setText(projectName);
        this.finishAndOpenAssociatedPerspectiveIfNotOpen();
    }

    public void createMavenProject(final String groupId, final String artifactId) {
        this.openNewProjectWindow();
        final SWTBotTree optionTree = bot.tree();
        final SWTBotTreeItem mavenNode = optionTree.expandNode(PROJECT_TYPE_MAVEN);
        bot.waitUntil(new TreeItemIsExpandedCondition(mavenNode));
        mavenNode.expandNode(MAVEN_PROJECT).select();
        this.pressButton(bot, NEXT_BUTTON);
        bot.checkBox("Create a simple project (skip archetype selection)").select();
        this.pressButton(bot, NEXT_BUTTON);
        bot.comboBox(0).setText(groupId);
        bot.comboBox(1).setText(artifactId);
        this.finishAndOpenAssociatedPerspectiveIfNotOpen();
    }

    private void openNewProjectWindow() {
        final SWTBotMenu fileMenu = bot.menu(MENU_FILE);
        final SWTBotMenu projectMenu = fileMenu.menu(MENU_FILE_NEW);
        final SWTBotMenu newMenu = projectMenu.menu(MENU_FILE_NEW_PROJECT);
        newMenu.click();
        bot.waitUntil(Conditions.shellIsActive(NEW_PROJECT_WINDOW_TITLE));
    }

    private void finishAndOpenAssociatedPerspectiveIfNotOpen() {
        final SWTBot pageBot = bot.activeShell().bot();
        this.pressButton(pageBot, FINISH_BUTTON);
        try {
            bot.waitUntil(Conditions.shellIsActive(OPEN_ASSOCIATED_PERSPECTIVE_WINDOW_TITLE));
            this.pressButton(bot, OPEN_ASSOCIATED_PERSPECTIVE_NO_BUTTON);
        } catch (final TimeoutException e) {
        } finally {
            try {
                bot.waitUntil(Conditions.shellCloses(bot.activeShell()));
            } catch (final TimeoutException e) {

            }
        }
    }

}
