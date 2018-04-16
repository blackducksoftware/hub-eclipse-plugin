/**
 * com.blackducksoftware.integration.eclipse.plugin
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
package com.blackducksoftware.integration.eclipse.services;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jdt.core.JavaCore;

import com.blackducksoftware.integration.eclipse.internal.utils.externalid.FilePathExternalIdExtractor;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalId;

public class ComponentInformationService {
    public static final String M2_REPO = "M2_REPO";

    private final FilePathExternalIdExtractor filePathExternalIdExtractor;

    public ComponentInformationService() {
        this.filePathExternalIdExtractor = new FilePathExternalIdExtractor();
    }

    public boolean isMavenDependency(final URL filePath) {
        URL m2Repo;
        try {
            m2Repo = JavaCore.getClasspathVariable(M2_REPO).toFile().toURI().toURL();
        } catch (final MalformedURLException e) {
            e.printStackTrace();
            return false;
        }
        final String[] m2RepoSegments = m2Repo.getFile().split("/");
        final String[] filePathSegments = filePath.getFile().split("/");
        if (filePathSegments.length < m2RepoSegments.length) {
            return false;
        }
        for (int i = 0; i < m2RepoSegments.length; i++) {
            if (!filePathSegments[i].equals(m2RepoSegments[i])) {
                return false;
            }
        }
        return true;
    }

    public boolean isGradleDependency(final URL filePath) {
        final String[] filePathSegments = filePath.getFile().split("/");
        if (filePathSegments.length < 3) {
            return false;
        }
        if (filePathSegments[filePathSegments.length - 3].equals("lib")
                || filePathSegments[filePathSegments.length - 2].equals("plugins")
                || filePathSegments[filePathSegments.length - 2].equals("lib")) {
            return false;
        }
        for (final String segment : filePathSegments) {
            if (segment.equals(".gradle")) {
                return true;
            }
        }
        return false;
    }

    public ExternalId constructMavenExternalIdFromUrl(final URL filePath) {
        try {
            if (this.isGradleDependency(filePath)) {
                return filePathExternalIdExtractor.getExternalIdFromLocalGradleUrl(filePath);
            } else if (this.isMavenDependency(filePath)) {
                final URL localMavenPath = JavaCore.getClasspathVariable(M2_REPO).toFile().toURI().toURL();
                return filePathExternalIdExtractor.getExternalIdFromLocalMavenUrl(filePath, localMavenPath);
            }
        } catch (final MalformedURLException e) {
            // Return null if we can't resolve the maven url
        }
        return null;
    }

}
