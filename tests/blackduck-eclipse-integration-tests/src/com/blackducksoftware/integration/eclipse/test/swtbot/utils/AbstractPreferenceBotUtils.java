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

import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;

public abstract class AbstractPreferenceBotUtils extends AbstractBotUtils {
    public static final String PREFERENCES_WINDOW_TITLE = "Preferences";

    public static final String PREFERENCES_FILTERED_WINDOW_TITLE = PREFERENCES_WINDOW_TITLE + " (Filtered)";

    public AbstractPreferenceBotUtils(final BlackDuckBotUtils botUtils) {
        super(botUtils);
    }

    public void pressApply() {
        this.pressButton("Apply");
    }

    public void pressOK() {
        this.pressButton("OK");
        try {
            bot.waitUntil(Conditions.shellCloses(bot.shell(PREFERENCES_WINDOW_TITLE)));
        } catch (final WidgetNotFoundException e) {
            // Do nothing because the window has already closed
        }
    }

    public void pressCancel() {
        this.pressButton("Cancel");
        try {
            bot.waitUntil(Conditions.shellCloses(bot.shell(PREFERENCES_WINDOW_TITLE)));
        } catch (final WidgetNotFoundException e) {
            // Do nothing because the window has already closed
        }
    }
}
