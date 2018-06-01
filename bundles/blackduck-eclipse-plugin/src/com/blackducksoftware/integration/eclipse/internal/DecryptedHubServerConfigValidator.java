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
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.ApiTokenField;
import com.blackducksoftware.integration.hub.configuration.HubServerConfigFieldEnum;
import com.blackducksoftware.integration.hub.configuration.HubServerConfigValidator;
import com.blackducksoftware.integration.hub.service.model.HubServerVerifier;
import com.blackducksoftware.integration.rest.credentials.Credentials;
import com.blackducksoftware.integration.rest.credentials.CredentialsValidator;
import com.blackducksoftware.integration.rest.exception.IntegrationRestException;
import com.blackducksoftware.integration.rest.proxy.ProxyInfo;
import com.blackducksoftware.integration.rest.proxy.ProxyInfoField;
import com.blackducksoftware.integration.rest.proxy.ProxyInfoValidator;
import com.blackducksoftware.integration.validator.ValidationResult;
import com.blackducksoftware.integration.validator.ValidationResultEnum;
import com.blackducksoftware.integration.validator.ValidationResults;

public class DecryptedHubServerConfigValidator extends HubServerConfigValidator {
    private final HubServerVerifier hubServerVerifier;
    private String hubUrl;
    private String timeoutSeconds;
    private String username;
    private String password;
    private String apiToken;
    private String proxyHost;
    private String proxyPort;
    private String proxyUsername;
    private String proxyPassword;
    private String ignoredProxyHosts;
    private String proxyNtlmDomain;
    private String proxyNtlmWorkstation;
    private boolean alwaysTrustServerCertificate;
    private ProxyInfo proxyInfo;

    public DecryptedHubServerConfigValidator() {
        this.hubServerVerifier = new HubServerVerifier();
    }

    public DecryptedHubServerConfigValidator(final HubServerVerifier hubServerVerifier) {
        this.hubServerVerifier = hubServerVerifier;
    }

    @Override
    public ValidationResults assertValid() {
        final ValidationResults proxyResult = assertProxyValid();
        final ValidationResults credentialsOrApiTokenResult = assertCredentialsOrApiTokenValid();
        final ValidationResults result = new ValidationResults();
        result.addAllResults(proxyResult.getResultMap());
        result.addAllResults(credentialsOrApiTokenResult.getResultMap());
        validateHubUrl(result);
        validateTimeout(result, null);
        return result;
    }

    @Override
    public ValidationResults assertProxyValid() {
        final ProxyInfoValidator validator = new ProxyInfoValidator();
        validator.setHost(proxyHost);
        validator.setPort(proxyPort);
        validator.setIgnoredProxyHosts(ignoredProxyHosts);
        validator.setUsername(proxyUsername);
        validator.setPassword(proxyPassword);
        validator.setNtlmDomain(proxyNtlmDomain);
        validator.setNtlmWorkstation(proxyNtlmWorkstation);

        final ValidationResults results = validator.assertValid();
        if (results.isSuccess()) {
            if (validator.hasProxySettings()) {
                final int port = NumberUtils.toInt(proxyPort);
                if (validator.hasAuthenticatedProxySettings()) {
                    final DecryptedCredentialsBuilder credBuilder = new DecryptedCredentialsBuilder();
                    credBuilder.setUsername(proxyUsername);
                    credBuilder.setPassword(proxyPassword);
                    final Credentials credResult = credBuilder.build();

                    proxyInfo = new ProxyInfo(proxyHost, port, credResult, ignoredProxyHosts, proxyNtlmDomain, proxyNtlmWorkstation);

                } else {
                    // password is blank or already encrypted so we just pass in the
                    // values given to us
                    proxyInfo = new ProxyInfo(proxyHost, port, null, ignoredProxyHosts, proxyNtlmDomain, proxyNtlmWorkstation);
                }
            }
        }
        return results;
    }

    // you can specify either username/password OR apiToken
    @Override
    public ValidationResults assertCredentialsOrApiTokenValid() {
        final ValidationResults validationResults = new ValidationResults();

        if (StringUtils.isBlank(apiToken)) {
            final CredentialsValidator credentialsBuilder = new CredentialsValidator();
            credentialsBuilder.setUsername(username);
            credentialsBuilder.setPassword(password);
            final ValidationResults credentialsResults = credentialsBuilder.assertValid();
            validationResults.addAllResults(credentialsResults.getResultMap());

            if (validationResults.hasErrors()) {
                validationResults.addResult(ApiTokenField.API_TOKEN, new ValidationResult(ValidationResultEnum.ERROR, "No api token was found."));
            }
        }

        return validationResults;
    }

    @Override
    public void validateHubUrl(final ValidationResults result) {
        if (hubUrl == null) {
            result.addResult(HubServerConfigFieldEnum.HUBURL, new ValidationResult(ValidationResultEnum.ERROR, ERROR_MSG_URL_NOT_FOUND));
            return;
        }

        URL hubURL = null;
        try {
            hubURL = new URL(hubUrl);
            hubURL.toURI();
        } catch (final MalformedURLException | URISyntaxException e) {
            result.addResult(HubServerConfigFieldEnum.HUBURL, new ValidationResult(ValidationResultEnum.ERROR, ERROR_MSG_URL_NOT_VALID));
            return;
        }

        try {
            hubServerVerifier.verifyIsHubServer(hubURL, proxyInfo, alwaysTrustServerCertificate, NumberUtils.toInt(timeoutSeconds, 120));
        } catch (final IntegrationRestException e) {
            if (e.getHttpStatusCode() == 407) {
                result.addResult(ProxyInfoField.PROXYUSERNAME, new ValidationResult(ValidationResultEnum.ERROR, e.getHttpStatusMessage()));
            } else {
                result.addResult(HubServerConfigFieldEnum.HUBURL,
                        new ValidationResult(ValidationResultEnum.ERROR, ERROR_MSG_UNREACHABLE_PREFIX + hubUrl + ERROR_MSG_UNREACHABLE_CAUSE + e.getHttpStatusCode() + " : " + e.getHttpStatusMessage(), e));
            }
        } catch (final IntegrationException e) {
            result.addResult(HubServerConfigFieldEnum.HUBURL, new ValidationResult(ValidationResultEnum.ERROR, ERROR_MSG_UNREACHABLE_PREFIX + hubUrl + ERROR_MSG_UNREACHABLE_CAUSE + e.getMessage(), e));
        }
    }

    @Override
    public void validateTimeout(final ValidationResults result) {
        validateTimeout(result, null);
    }

    private void validateTimeout(final ValidationResults result, final Integer defaultTimeoutSeconds) {
        if (StringUtils.isBlank(timeoutSeconds)) {
            result.addResult(HubServerConfigFieldEnum.HUBTIMEOUT, new ValidationResult(ValidationResultEnum.ERROR, "No Hub Timeout was found."));
            return;
        }
        int timeoutToValidate = 0;
        try {
            timeoutToValidate = stringToInteger(timeoutSeconds);
        } catch (final IllegalArgumentException e) {
            result.addResult(HubServerConfigFieldEnum.HUBTIMEOUT, new ValidationResult(ValidationResultEnum.ERROR, e.getMessage(), e));
            return;
        }
        if (timeoutToValidate <= 0) {
            result.addResult(HubServerConfigFieldEnum.HUBTIMEOUT, new ValidationResult(ValidationResultEnum.ERROR, "The Timeout must be greater than 0."));
        }
    }

    @Override
    public void setTimeout(final String timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    @Override
    public String getTimeout() {
        return timeoutSeconds;
    }

    @Override
    public void setTimeout(final int timeoutSeconds) {
        setTimeout(String.valueOf(timeoutSeconds));
    }

    @Override
    public String getHubUrl() {
        return hubUrl;
    }

    @Override
    public void setHubUrl(final String hubUrl) {
        this.hubUrl = StringUtils.trimToNull(hubUrl);
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
    public String getApiToken() {
        return apiToken;
    }

    @Override
    public void setApiToken(final String apiToken) {
        this.apiToken = apiToken;
    }

    @Override
    public String getProxyHost() {
        return proxyHost;
    }

    @Override
    public void setProxyHost(final String proxyHost) {
        this.proxyHost = proxyHost;
    }

    @Override
    public String getProxyPort() {
        return proxyPort;
    }

    @Override
    public void setProxyPort(final String proxyPort) {
        this.proxyPort = proxyPort;
    }

    @Override
    public void setProxyPort(final int proxyPort) {
        setProxyPort(String.valueOf(proxyPort));
    }

    @Override
    public String getProxyUsername() {
        return proxyUsername;
    }

    @Override
    public void setProxyUsername(final String proxyUsername) {
        this.proxyUsername = proxyUsername;
    }

    @Override
    public String getProxyPassword() {
        return proxyPassword;
    }

    @Override
    public void setProxyPassword(final String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    @Override
    public int getProxyPasswordLength() {
        return 0;
    }

    @Override
    public String getProxyNtlmDomain() {
        return proxyNtlmDomain;
    }

    @Override
    public void setProxyNtlmDomain(final String ntlmDomain) {
        this.proxyNtlmDomain = ntlmDomain;
    }

    @Override
    public String getProxyNtlmWorkstation() {
        return proxyNtlmWorkstation;
    }

    @Override
    public void setProxyNtlmWorkstation(final String ntlmWorkstation) {
        this.proxyNtlmWorkstation = ntlmWorkstation;
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
    public boolean isAlwaysTrustServerCertificate() {
        return alwaysTrustServerCertificate;
    }

    @Override
    public void setAlwaysTrustServerCertificate(final boolean alwaysTrustServerCertificate) {
        this.alwaysTrustServerCertificate = alwaysTrustServerCertificate;
    }

}
