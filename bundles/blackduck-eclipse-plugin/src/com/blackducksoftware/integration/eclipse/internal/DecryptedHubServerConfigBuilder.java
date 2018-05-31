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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.blackducksoftware.integration.exception.IntegrationCertificateException;
import com.blackducksoftware.integration.hub.configuration.HubServerConfig;
import com.blackducksoftware.integration.hub.configuration.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.configuration.HubServerConfigValidator;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.log.LogLevel;
import com.blackducksoftware.integration.log.PrintStreamIntLogger;
import com.blackducksoftware.integration.rest.credentials.Credentials;
import com.blackducksoftware.integration.rest.proxy.ProxyInfo;
import com.blackducksoftware.integration.rest.proxy.ProxyInfoBuilder;
import com.blackducksoftware.integration.validator.AbstractValidator;

public class DecryptedHubServerConfigBuilder extends HubServerConfigBuilder {
    private final HubServerConfigValidator validator;
    private final Map<Property, String> values = new HashMap<>();
    private IntLogger logger;

    public DecryptedHubServerConfigBuilder() {
        this(new DecryptedHubServerConfigValidator());
    }

    public DecryptedHubServerConfigBuilder(final DecryptedHubServerConfigValidator validator) {
        this.validator = validator;
        EnumSet.allOf(Property.class).forEach(property -> {
            values.put(property, null);
        });
        values.put(Property.TIMEOUT, String.valueOf(DEFAULT_TIMEOUT_SECONDS));
    }

    @Override
    public HubServerConfig build() throws IllegalStateException {
        try {
            return super.build();
        } catch (final IllegalStateException stateException) {
            if (!stateException.getMessage().contains("SunCertPathBuilderException")) {
                throw stateException;
            }
            throw new IntegrationCertificateException(String.format("Please import the certificate for %s into your Java keystore.", url()), stateException);
        }
    }

    @Override
    public HubServerConfig buildObject() {
        URL hubURL = null;
        try {
            String tempUrl = url();
            if (!tempUrl.endsWith("/")) {
                hubURL = new URL(tempUrl);
            } else {
                tempUrl = tempUrl.substring(0, tempUrl.length() - 1);
                hubURL = new URL(tempUrl);
            }
        } catch (final MalformedURLException e) {
        }

        final ProxyInfo proxyInfo = getHubProxyInfo();
        if (StringUtils.isNotBlank(apiToken())) {
            return new HubServerConfig(hubURL, timeoutSeconds(), apiToken(), proxyInfo, trustCert());
        } else {
            final Credentials credentials = getHubCredentials();
            return new HubServerConfig(hubURL, timeoutSeconds(), credentials, proxyInfo, trustCert());
        }
    }

    private Credentials getHubCredentials() {
        final DecryptedCredentialsBuilder credentialsBuilder = new DecryptedCredentialsBuilder();
        credentialsBuilder.setUsername(values.get(Property.USERNAME));
        credentialsBuilder.setPassword(values.get(Property.PASSWORD));
        return credentialsBuilder.buildObject();
    }

    private ProxyInfo getHubProxyInfo() {
        final ProxyInfoBuilder proxyBuilder = new ProxyInfoBuilder();
        proxyBuilder.setHost(values.get(Property.PROXY_HOST));
        proxyBuilder.setPort(values.get(Property.PROXY_PORT));
        proxyBuilder.setIgnoredProxyHosts(values.get(Property.IGNORED_PROXY_HOSTS));
        proxyBuilder.setUsername(values.get(Property.PROXY_USERNAME));
        proxyBuilder.setPassword(values.get(Property.PROXY_PASSWORD));
        proxyBuilder.setNtlmDomain(values.get(Property.PROXY_NTLM_DOMAIN));
        proxyBuilder.setNtlmWorkstation(values.get(Property.PROXY_NTLM_WORKSTATION));
        return proxyBuilder.buildObject();
    }

    @Override
    public AbstractValidator createValidator() {
        validator.setHubUrl(url());
        validator.setUsername(values.get(Property.USERNAME));
        validator.setPassword(values.get(Property.PASSWORD));
        validator.setApiToken(apiToken());
        validator.setTimeout(values.get(Property.TIMEOUT));
        validator.setProxyHost(values.get(Property.PROXY_HOST));
        validator.setProxyPort(values.get(Property.PROXY_PORT));
        validator.setIgnoredProxyHosts(values.get(Property.IGNORED_PROXY_HOSTS));
        validator.setProxyUsername(values.get(Property.PROXY_USERNAME));
        validator.setProxyPassword(values.get(Property.PROXY_PASSWORD));
        validator.setAlwaysTrustServerCertificate(trustCert());
        validator.setProxyNtlmDomain(values.get(Property.PROXY_NTLM_DOMAIN));
        validator.setProxyNtlmWorkstation(values.get(Property.PROXY_NTLM_WORKSTATION));
        return validator;
    }

    private String url() {
        return values.get(Property.URL);
    }

    private String apiToken() {
        return values.get(Property.API_TOKEN);
    }

    private int timeoutSeconds() {
        return NumberUtils.toInt(values.get(Property.TIMEOUT), DEFAULT_TIMEOUT_SECONDS);
    }

    private boolean trustCert() {
        return Boolean.parseBoolean(values.get(Property.TRUST_CERT));
    }

    @Override
    public IntLogger getLogger() {
        if (logger == null) {
            logger = new PrintStreamIntLogger(System.out, LogLevel.INFO);
        }
        return logger;
    }

    @Override
    public void setLogger(final IntLogger logger) {
        this.logger = logger;
    }

    // setters for the values of HubServerConfigBuilder
    @Override
    @Deprecated
    /**
     * @deprecated Please use setUrl(final String url) instead.
     */
    public void setHubUrl(final String hubUrl) {
        setUrl(hubUrl);
    }

    @Override
    @Deprecated
    /**
     * @deprecated Please use setTrustCert(final boolean trustCert) instead.
     */
    public void setAlwaysTrustServerCertificate(final boolean alwaysTrustServerCertificate) {
        setTrustCert(alwaysTrustServerCertificate);
    }

    @Override
    public void setUrl(final String url) {
        values.put(Property.URL, url);
    }

    @Override
    public void setUsername(final String username) {
        values.put(Property.USERNAME, username);
    }

    @Override
    public void setPassword(final String password) {
        values.put(Property.PASSWORD, password);
    }

    @Override
    public void setApiToken(final String apiToken) {
        values.put(Property.API_TOKEN, apiToken);
    }

    @Override
    public void setTimeout(final String timeout) {
        values.put(Property.TIMEOUT, timeout);
    }

    @Override
    public void setTimeout(final int timeout) {
        setTimeout(String.valueOf(timeout));
    }

    @Override
    public void setProxyHost(final String proxyHost) {
        values.put(Property.PROXY_HOST, proxyHost);
    }

    @Override
    public void setProxyPort(final String proxyPort) {
        values.put(Property.PROXY_PORT, proxyPort);
    }

    @Override
    public void setProxyPort(final int proxyPort) {
        setProxyPort(String.valueOf(proxyPort));
    }

    @Override
    public void setIgnoredProxyHosts(final String ignoredProxyHosts) {
        values.put(Property.IGNORED_PROXY_HOSTS, ignoredProxyHosts);
    }

    @Override
    public void setProxyUsername(final String proxyUsername) {
        values.put(Property.PROXY_USERNAME, proxyUsername);
    }

    @Override
    public void setProxyPassword(final String proxyPassword) {
        values.put(Property.PROXY_PASSWORD, proxyPassword);
    }

    @Override
    public void setProxyNtlmDomain(final String proxyNtlmDomain) {
        values.put(Property.PROXY_NTLM_DOMAIN, proxyNtlmDomain);
    }

    @Override
    public void setProxyNtlmWorkstation(final String proxyNtlmWorkstation) {
        values.put(Property.PROXY_NTLM_WORKSTATION, proxyNtlmWorkstation);
    }

    @Override
    public void setTrustCert(final String trustCert) {
        values.put(Property.TRUST_CERT, trustCert);
    }

    @Override
    public void setTrustCert(final boolean trustCert) {
        setTrustCert(String.valueOf(trustCert));
    }

}
