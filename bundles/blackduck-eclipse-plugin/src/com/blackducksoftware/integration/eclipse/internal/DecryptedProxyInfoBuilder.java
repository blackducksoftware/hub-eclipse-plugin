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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.blackducksoftware.integration.rest.credentials.Credentials;
import com.blackducksoftware.integration.rest.proxy.ProxyInfo;
import com.blackducksoftware.integration.rest.proxy.ProxyInfoBuilder;
import com.blackducksoftware.integration.rest.proxy.ProxyInfoValidator;
import com.blackducksoftware.integration.validator.AbstractValidator;

public class DecryptedProxyInfoBuilder extends ProxyInfoBuilder {
    private String host;
    private String port;
    private String username;
    private String password;
    private String ignoredProxyHosts;
    private String ntlmDomain;
    private String ntlmWorkstation;

    @Override
    public ProxyInfo buildObject() throws IllegalArgumentException {
        ProxyInfo proxyInfo = null;
        final int proxyPort = NumberUtils.toInt(port);
        if (StringUtils.isNotBlank(password) && StringUtils.isNotBlank(username)) {

            final DecryptedCredentialsBuilder credBuilder = new DecryptedCredentialsBuilder();
            credBuilder.setUsername(username);
            credBuilder.setPassword(password);
            final Credentials credResult = credBuilder.build();

            proxyInfo = new ProxyInfo(host, proxyPort, credResult, ignoredProxyHosts, ntlmDomain, ntlmWorkstation);
        } else {
            // password is blank or already encrypted so we just pass in the
            // values given to us
            proxyInfo = new ProxyInfo(host, proxyPort, null, ignoredProxyHosts, ntlmDomain, ntlmWorkstation);
        }

        return proxyInfo;
    }

    @Override
    public AbstractValidator createValidator() {
        final ProxyInfoValidator validator = new ProxyInfoValidator();
        validator.setHost(getHost());
        validator.setPort(getPort());
        validator.setUsername(getUsername());
        validator.setPassword(getPassword());
        validator.setIgnoredProxyHosts(getIgnoredProxyHosts());
        validator.setNtlmDomain(ntlmDomain);
        validator.setNtlmWorkstation(ntlmWorkstation);
        return validator;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public void setHost(final String host) {
        this.host = host;
    }

    @Override
    public String getPort() {
        return port;
    }

    @Override
    public void setPort(final int port) {
        setPort(String.valueOf(port));
    }

    @Override
    public void setPort(final String port) {
        this.port = port;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(final String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(final String password) {
        this.password = password;
    }

    @Override
    public int getPasswordLength() {
        return 0;
    }

    @Override
    public String getIgnoredProxyHosts() {
        return ignoredProxyHosts;
    }

    @Override
    public void setIgnoredProxyHosts(final String ignoredProxyHosts) {
        this.ignoredProxyHosts = ignoredProxyHosts;
    }

    @Override
    public String getNtlmDomain() {
        return ntlmDomain;
    }

    @Override
    public void setNtlmDomain(final String ntlmDomain) {
        this.ntlmDomain = ntlmDomain;
    }

    @Override
    public String getNtlmWorkstation() {
        return ntlmWorkstation;
    }

    @Override
    public void setNtlmWorkstation(final String ntlmWorkstation) {
        this.ntlmWorkstation = ntlmWorkstation;
    }
}
