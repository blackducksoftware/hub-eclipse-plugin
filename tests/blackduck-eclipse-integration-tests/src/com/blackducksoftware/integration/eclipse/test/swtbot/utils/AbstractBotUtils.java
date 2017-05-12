/**
 * hub-eclipse-plugin-test
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

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRootMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

import com.blackducksoftware.integration.eclipse.test.swtbot.utils.conditions.ButtonIsEnabledCondition;
import com.blackducksoftware.integration.eclipse.test.swtbot.utils.conditions.TreeItemIsExpandedCondition;

public abstract class AbstractBotUtils {
    protected final SWTWorkbenchBot bot;

    protected final BlackDuckBotUtils botUtils;

    public AbstractBotUtils(final BlackDuckBotUtils botUtils) {
        this.bot = new SWTWorkbenchBot();
        this.botUtils = botUtils;
    }

    protected SWTBotButton pressButton(final String buttonTitle) {
        final SWTBotButton target = bot.button(buttonTitle);
        bot.waitUntil(new ButtonIsEnabledCondition(target));
        return target.click();
    }

    protected SWTBotTreeItem expandSuperNode(final String nodeName) {
        final SWTBotTree optionTree = bot.tree();
        final SWTBotTreeItem node = optionTree.expandNode(nodeName);
        bot.waitUntil(new TreeItemIsExpandedCondition(node));
        return node;
    }

    protected SWTBotMenu selectFromMenu(final SWTBotRootMenu rootMenu, final String... menuLabels) {
        final SWTBotMenu currentMenu = rootMenu.menu(menuLabels);
        return currentMenu.click();
    }

    public void setSWTBotTimeoutShort() {
        SWTBotPreferences.TIMEOUT = 500;
    }

    public void setSWTBotTimeoutDefault() {
        SWTBotPreferences.TIMEOUT = 5000;
    }

}
