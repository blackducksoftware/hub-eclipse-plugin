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
package com.blackducksoftware.integration.eclipse.services.connection.hub;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.eclipse.services.BlackDuckPreferencesService;
import com.blackducksoftware.integration.encryption.PasswordDecrypter;
import com.blackducksoftware.integration.encryption.PasswordEncrypter;
import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.hub.configuration.HubServerConfig;
import com.blackducksoftware.integration.hub.configuration.HubServerConfigBuilder;

public class HubPreferencesService {
    private final Logger log = LoggerFactory.getLogger(HubPreferencesService.class);

    private final BlackDuckPreferencesService blackDuckPreferencesService;

    public static final String HUB_USERNAME = "hubUsername";
    public static final String HUB_PASSWORD = "hubPassword";
    public static final String HUB_PASSWORD_LENGTH = "hubPasswordLength";
    public static final String HUB_URL = "hubURL";
    public static final String HUB_TIMEOUT = "hubTimeout";
    public static final String HUB_ALWAYS_TRUST = "hubAlwaysTrustCert";
    public static final String PROXY_USERNAME = "proxyUsername";
    public static final String PROXY_PASSWORD = "proxyPassword";
    public static final String PROXY_PASSWORD_LENGTH = "proxyPasswordLength";
    public static final String PROXY_HOST = "proxyHost";
    public static final String PROXY_PORT = "proxyPort";

    public static final String DEFAULT_HUB_TIMEOUT = "120";
    public static final boolean DEFAULT_HUB_ALWAYS_TRUST = true;

    public HubPreferencesService(final BlackDuckPreferencesService blackDuckPreferenceService) {
        this.blackDuckPreferencesService = blackDuckPreferenceService;
        blackDuckPreferencesService.setPreferenceDefault(HUB_TIMEOUT, DEFAULT_HUB_TIMEOUT);
        blackDuckPreferencesService.setPreferenceDefault(HUB_ALWAYS_TRUST, DEFAULT_HUB_ALWAYS_TRUST);
    }

    public String getPreference(final String preference) {
        return blackDuckPreferencesService.getPreference(preference);
    }

    public String getHubUsername() {
        return this.getPreference(HUB_USERNAME);
    }

    public String getHubPassword() {
        String hubPassword = this.getPreference(HUB_PASSWORD);
        final String hubPasswordLength = this.getPreference(HUB_PASSWORD_LENGTH);

        if (StringUtils.isNotBlank(hubPassword) && StringUtils.isNotBlank(hubPasswordLength)) {
            hubPassword = decryptPassword(hubPassword, Integer.parseInt(hubPasswordLength));
        }

        return hubPassword;
    }

    public String getHubUrl() {
        return this.getPreference(HUB_URL);
    }

    public String getHubTimeout() {
        return this.getPreference(HUB_TIMEOUT);
    }

    public boolean getHubAlwaysTrust() {
        return Boolean.parseBoolean(this.getPreference(HUB_ALWAYS_TRUST));
    }

    public String getHubProxyUsername() {
        return this.getPreference(PROXY_USERNAME);
    }

    public String getHubProxyPort() {
        return this.getPreference(PROXY_PORT);
    }

    public String getHubProxyHost() {
        return this.getPreference(PROXY_HOST);
    }

    public String getHubProxyPassword() {
        String proxyPassword = this.getPreference(PROXY_PASSWORD);
        final String proxyPasswordLength = this.getPreference(PROXY_PASSWORD_LENGTH);

        if (StringUtils.isNotBlank(proxyPassword) && StringUtils.isNotBlank(proxyPasswordLength)) {
            proxyPassword = decryptPassword(proxyPassword, Integer.parseInt(proxyPasswordLength));
        }

        return proxyPassword;
    }

    public void savePreference(final String preference, final String preferenceValue) {
        blackDuckPreferencesService.savePreference(preference, preferenceValue);
    }

    public void saveHubUsername(final String hubUsername) {
        this.savePreference(HUB_USERNAME, hubUsername);
    }

    public void saveHubPassword(final String hubPassword) {
        String encryptedPassword = hubPassword;

        if (StringUtils.isNotBlank(hubPassword)) {
            encryptedPassword = encryptPassword(hubPassword);
        }

        if (!encryptedPassword.equals(hubPassword)) {
            this.savePreference(HUB_PASSWORD_LENGTH, Integer.toString(hubPassword.length()));
        }

        this.savePreference(HUB_PASSWORD, encryptedPassword);
    }

    public void saveHubUrl(final String hubUrl) {
        this.savePreference(HUB_URL, hubUrl);
    }

    public void saveHubTimeout(final String timeout) {
        this.savePreference(HUB_TIMEOUT, timeout);
    }

    public void saveHubAlwaysTrust(final boolean hubAlwaysTrust) {
        this.savePreference(HUB_ALWAYS_TRUST, Boolean.toString(hubAlwaysTrust));
    }

    public void saveHubProxyUsername(final String proxyUsername) {
        this.savePreference(PROXY_USERNAME, proxyUsername);
    }

    public void saveHubProxyPassword(final String proxyPassword) {
        String encryptedPassword = proxyPassword;

        if (StringUtils.isNotBlank(proxyPassword)) {
            encryptedPassword = encryptPassword(proxyPassword);
        }

        if (!encryptedPassword.equals(proxyPassword)) {
            this.savePreference(PROXY_PASSWORD_LENGTH, Integer.toString(proxyPassword.length()));
        }

        this.savePreference(PROXY_PASSWORD, encryptedPassword);
    }

    public void saveHubProxyHost(final String proxyHost) {
        this.savePreference(PROXY_HOST, proxyHost);
    }

    public void saveHubProxyPort(final String proxyPort) {
        this.savePreference(PROXY_PORT, proxyPort);
    }

    public HubServerConfig getHubServerConfig() throws IllegalStateException {
        final HubServerConfigBuilder hubServerConfigBuilder = new HubServerConfigBuilder();
        final String username = this.getHubUsername();
        hubServerConfigBuilder.setUsername(username);
        final String password = this.getHubPassword();
        hubServerConfigBuilder.setPassword(password);
        final String hubUrl = this.getHubUrl();
        hubServerConfigBuilder.setUrl(hubUrl);
        final String timeout = this.getHubTimeout();
        hubServerConfigBuilder.setTimeout(timeout);
        final boolean hubAlwaysTrust = this.getHubAlwaysTrust();
        hubServerConfigBuilder.setTrustCert(hubAlwaysTrust);
        final String proxyUsername = this.getHubProxyUsername();
        hubServerConfigBuilder.setProxyUsername(proxyUsername);
        final String proxyPassword = this.getHubProxyPassword();
        hubServerConfigBuilder.setProxyPassword(proxyPassword);
        final String proxyPort = this.getHubProxyPort();
        hubServerConfigBuilder.setProxyPort(proxyPort);
        final String proxyHost = this.getHubProxyHost();
        hubServerConfigBuilder.setProxyHost(proxyHost);
        return hubServerConfigBuilder.build();
    }

    private String decryptPassword(final String encryptedPassword, final int actualLength) {
        String decryptedPassword;
        if (encryptedPassword.length() == actualLength) {
            decryptedPassword = encryptedPassword;
        } else {
            try {
                decryptedPassword = PasswordDecrypter.decrypt(encryptedPassword);
            } catch (final Exception e) {
                log.error("Decryption Failed. Password decrypter encountered an error when decrypting the password: ", e);
                log.error("If this problem persists, simply re-enter your credentials.");
                decryptedPassword = encryptedPassword;
            }
        }
        return decryptedPassword;
    }

    private String encryptPassword(final String plainTextPassword) {
        String encryptedPassword;
        try {
            encryptedPassword = PasswordEncrypter.encrypt(plainTextPassword);
        } catch (final EncryptionException e) {
            log.warn("Encryption Failed. Password encryptor encountered an error when encrypting the password: ", e);
            encryptedPassword = plainTextPassword;
        }
        return encryptedPassword;
    }

}
