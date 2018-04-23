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

import java.util.Arrays;

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCLabel;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;

import com.blackducksoftware.integration.eclipse.views.widgets.ComponentTableStatusCLabel;

public class ComponentInspectorBotUtils extends AbstractBotUtils {
    public ComponentInspectorBotUtils(final BlackDuckBotUtils parent) {
        super(parent);
    }

    public static final String COMPONENT_INSPECTOR_NAME = "Component Inspector";

    public SWTBot getComponentInspectorView() {
        final SWTBotView view = bot.viewByTitle(COMPONENT_INSPECTOR_NAME);
        return view.bot();
    }

    public SWTBotCLabel getInspectionStatus(final String status) {
        final SWTBot viewBot = this.getComponentInspectorView();
        this.setSWTBotTimeoutShort();
        final SWTBotCLabel clabel = viewBot.clabel(status);
        this.setSWTBotTimeoutDefault();
        return clabel;
    }

    public SWTBotCLabel getInspectionStatusIfCompleteOrInProgress() {
        final String[] expectedStatuses = { ComponentTableStatusCLabel.HUB_CONNECTION_OK_STATUS,
                ComponentTableStatusCLabel.PROJECT_INSPECTION_RUNNING_STATUS,
                ComponentTableStatusCLabel.PROJECT_INSPECTION_SCHEDULED_STATUS,
                ComponentTableStatusCLabel.HUB_CONNECTION_OK_NO_COMPONENTS_STATUS };

        final String[] remainingStatuses = { ComponentTableStatusCLabel.CONNECTION_DISCONNECTED_STATUS,
                ComponentTableStatusCLabel.NO_SELECTED_PROJECT_STATUS,
                ComponentTableStatusCLabel.PROJECT_NEEDS_INSPECTION_STATUS,
                ComponentTableStatusCLabel.PROJECT_NOT_MARKED_FOR_INSPECTION_STATUS,
                ComponentTableStatusCLabel.PROJECT_NOT_SUPPORTED_STATUS };

        final SWTBot viewBot = this.getComponentInspectorView();
        this.setSWTBotTimeoutShort();
        for (final String statusMessage : Arrays.asList(expectedStatuses)) {
            try {
                final SWTBotCLabel clabel = viewBot.clabel(statusMessage);
                this.setSWTBotTimeoutDefault();
                return clabel;
            } catch (final WidgetNotFoundException e) {
            }
        }
        this.setSWTBotTimeoutDefault();
        SWTBotCLabel badclabel = null;
        for (final String statusMessage : Arrays.asList(remainingStatuses)) {
            try {
                badclabel = viewBot.clabel(statusMessage);
                this.setSWTBotTimeoutDefault();
            } catch (final WidgetNotFoundException e) {
            }
        }
        if (badclabel == null) {
            throw new WidgetNotFoundException("Inspection status widget not found with any value.");
        } else {
            throw new WidgetNotFoundException(
                    String.format("Inspection status widget found with value '%s' when '%s', '%s', '%s', or '%s' was expected", badclabel.getText(), expectedStatuses[0], expectedStatuses[1], expectedStatuses[2], expectedStatuses[3]));
        }
    }

    public SWTBotTable getInspectionResultsTable() {
        final SWTBot viewBot = this.getComponentInspectorView();
        return viewBot.table();
    }

    public void waitUntilInspectionResultsTableHasRows(final int numberOfRows) {
        final SWTBot viewBot = this.getComponentInspectorView();
        viewBot.waitUntil(Conditions.tableHasRows(viewBot.table(), numberOfRows));
    }

    public String[][] getInspectionResults() {
        final SWTBotTable inspectorTable = this.getInspectionResultsTable();
        final String[][] inspectionResults = new String[inspectorTable.rowCount()][inspectorTable.columnCount()];
        for (int i = 0; i < inspectorTable.rowCount(); i++) {
            for (int j = 0; j < inspectorTable.columnCount(); j++) {
                inspectionResults[i][j] = inspectorTable.cell(i, j);
            }
        }
        return inspectionResults;
    }

    public void openComponent(final String componentText) {
        final SWTBotTable table = this.getInspectionResultsTable();
        final SWTBotTableItem tableItem = table.getTableItem(componentText);
        tableItem.doubleClick();
    }

}
