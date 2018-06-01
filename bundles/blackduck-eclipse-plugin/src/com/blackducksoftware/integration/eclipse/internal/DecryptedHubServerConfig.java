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
package com.blackducksoftware.integration.eclipse.internal;

import java.net.URL;

import com.blackducksoftware.integration.hub.configuration.HubServerConfig;
import com.blackducksoftware.integration.hub.rest.CredentialsRestConnection;
import com.blackducksoftware.integration.hub.rest.CredentialsRestConnectionBuilder;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.rest.credentials.Credentials;
import com.blackducksoftware.integration.rest.proxy.ProxyInfo;

public class DecryptedHubServerConfig extends HubServerConfig {
    private static final long serialVersionUID = -1747576304367551576L;

    public DecryptedHubServerConfig(final URL url, final int timeoutSeconds, final Credentials credentials, final ProxyInfo proxyInfo, final boolean alwaysTrustServerCertificate) {
        super(url, timeoutSeconds, credentials, proxyInfo, alwaysTrustServerCertificate);
    }

    public DecryptedHubServerConfig(final URL url, final int timeoutSeconds, final String apiToken, final ProxyInfo proxyInfo, final boolean alwaysTrustServerCertificate) {
        super(url, timeoutSeconds, apiToken, proxyInfo, alwaysTrustServerCertificate);
    }

    @Override
    public CredentialsRestConnection createCredentialsRestConnection(final IntLogger logger) throws IllegalStateException {
        final CredentialsRestConnectionBuilder builder = new CredentialsRestConnectionBuilder();
        builder.setLogger(logger);
        builder.setBaseUrl(getHubUrl().toString());
        builder.setTimeout(getTimeout());
        builder.setUsername(getGlobalCredentials().getUsername());
        builder.setPassword(getGlobalCredentials().getEncryptedPassword());
        builder.setAlwaysTrustServerCertificate(isAlwaysTrustServerCertificate());
        builder.applyProxyInfo(getProxyInfo());

        return builder.build();
    }

}
