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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalId;

public class ProjectInformationService {
    public static final String GRADLE_NATURE = "org.eclipse.buildship.core.gradleprojectnature";

    public static final String MAVEN_NATURE = "org.eclipse.m2e.core.maven2Nature";

    public static final String[] SUPPORTED_NATURES = {
            GRADLE_NATURE,
            MAVEN_NATURE
    };

    private final ComponentInformationService componentInformationService;

    public ProjectInformationService(final ComponentInformationService componentInformationService) {
        this.componentInformationService = componentInformationService;
    }

    public int getNumBinaryDependencies(final List<IPackageFragmentRoot> packageFragmentRoots) {
        int numBinary = 0;
        for (final IPackageFragmentRoot root : packageFragmentRoots) {
            try {
                if (root.getKind() == IPackageFragmentRoot.K_BINARY) {
                    numBinary++;
                }
            } catch (final JavaModelException e) {
                /*
                 * Occurs if root does not exist or an exception occurs while accessing resource. If this happens, assume root is not binary and therefore do not increment count
                 */
            }
        }
        return numBinary;
    }

    public List<ExternalId> getMavenExternalIdsFromFilepaths(final List<URL> mavenAndGradleFilePaths) {
        final List<ExternalId> gavs = new ArrayList<>();
        for (final URL filePath : mavenAndGradleFilePaths) {
            final ExternalId tempGav = componentInformationService.constructMavenExternalIdFromUrl(filePath);
            if (tempGav != null) {
                gavs.add(tempGav);
            }
        }
        return gavs;
    }

    public List<URL> getProjectComponentUrls(final String projectName) {
        final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
        try {
            if (project.hasNature(JavaCore.NATURE_ID)) {
                final IPackageFragmentRoot[] packageFragmentRoots = JavaCore.create(project).getPackageFragmentRoots();
                final List<URL> dependencyFilepaths = getBinaryDependencyFilepaths(Arrays.asList(packageFragmentRoots));
                return dependencyFilepaths;
            }
        } catch (final CoreException e) {
            // if exception thrown when getting filepaths to source and binary dependencies, assume
            // there are no dependencies
        }

        return Arrays.asList();
    }

    public List<URL> getBinaryDependencyFilepaths(final List<IPackageFragmentRoot> packageFragmentRoots) {
        final ArrayList<URL> dependencyFilepaths = new ArrayList<>();
        for (final IPackageFragmentRoot root : packageFragmentRoots) {
            final URL tempURL = getBinaryDependencyFilepath(root);
            if (tempURL != null) {
                dependencyFilepaths.add(tempURL);
            }
        }
        return dependencyFilepaths;
    }

    public URL getBinaryDependencyFilepath(final IPackageFragmentRoot packageFragmentRoot) {
        try {
            if (packageFragmentRoot.getKind() == IPackageFragmentRoot.K_BINARY) {
                return packageFragmentRoot.getPath().toFile().toURI().toURL();
            }
        } catch (final JavaModelException | MalformedURLException e) {
            /*
             * If root does not exist or exception occurs while accessing resource, do not add its filepath to the list of binary dependency filepaths
             */
        }
        return null;
    }

    public boolean isProjectSupported(final String projectName) {
        final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
        return this.isSupportedProject(project);
    }

    public boolean isSupportedProject(final IProject project) {
        try {
            if (project.hasNature(JavaCore.NATURE_ID)) {
                for (final String nature : SUPPORTED_NATURES) {
                    if (project.hasNature(nature)) {
                        return true;
                    }
                }
            }
        } catch (final CoreException e) {
            // CoreException means project is closed/ doesn't exist
        }
        return false;
    }

}
