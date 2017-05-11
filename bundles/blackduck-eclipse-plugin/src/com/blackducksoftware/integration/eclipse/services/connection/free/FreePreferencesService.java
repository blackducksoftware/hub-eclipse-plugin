/**
 * com.blackducksoftware.integration.eclipse.plugin
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
package com.blackducksoftware.integration.eclipse.services.connection.free;

import java.net.URL;

import com.blackducksoftware.integration.eclipse.services.BlackDuckPreferencesService;
import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.hub.builder.HubCredentialsBuilder;
import com.blackducksoftware.integration.hub.builder.HubProxyInfoBuilder;
import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.global.HubCredentials;
import com.blackducksoftware.integration.hub.global.HubProxyInfo;
import com.blackducksoftware.integration.hub.global.HubServerConfig;

public class FreePreferencesService {
	private final BlackDuckPreferencesService blackDuckPreferencesService;

	public static final String HUB_USERNAME = "hubUsername";

	public static final String HUB_PASSWORD = "hubPassword";

	public static final String HUB_PASSWORD_LENGTH = "hubPasswordLength";

	public static final String HUB_URL = "hubURL";

	public static final String HUB_TIMEOUT = "hubTimeout";

	public static final String PROXY_USERNAME = "proxyUsername";

	public static final String PROXY_PASSWORD = "proxyPassword";

	public static final String PROXY_PASSWORD_LENGTH = "proxyPasswordLength";

	public static final String PROXY_HOST = "proxyHost";

	public static final String PROXY_PORT = "proxyPort";

	public static final String DEFAULT_HUB_TIMEOUT = "120";

	public FreePreferencesService(final BlackDuckPreferencesService blackDuckPreferenceService){
		this.blackDuckPreferencesService = blackDuckPreferenceService;
	}

	public String getPreference(final String preference){
		return blackDuckPreferencesService.getPreference(preference);
	}

	public String getHubUsername(){
		return this.getPreference(HUB_USERNAME);
	}

	public String getHubPassword(){
		final HubCredentialsBuilder hubCredentialsBuilder = new HubCredentialsBuilder();
		final String password = this.getPreference(HUB_PASSWORD);
		if(!password.trim().isEmpty()){
			hubCredentialsBuilder.setPassword(password);
			final String passwordLength = this.getPreference(HUB_PASSWORD_LENGTH);
			if(passwordLength != null && !passwordLength.equals("")){
				hubCredentialsBuilder.setPasswordLength(Integer.parseInt(passwordLength));
			}
			final HubCredentials hubCredentials = hubCredentialsBuilder.buildObject();
			try {
				return hubCredentials.getDecryptedPassword();
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
		return password;
	}

	public String getHubUrl(){
		return this.getPreference(HUB_URL);
	}

	public String getHubTimeout(){
		return this.getPreference(HUB_TIMEOUT);
	}

	public String getHubProxyUsername(){
		return this.getPreference(PROXY_USERNAME);
	}

	public String getHubProxyPort(){
		return this.getPreference(PROXY_PORT);
	}

	public String getHubProxyHost(){
		return this.getPreference(PROXY_HOST);
	}

	public String getHubProxyPassword(){
		final HubProxyInfoBuilder hubProxyInfoBuilder = new HubProxyInfoBuilder();
		final String proxyPassword = this.getPreference(PROXY_PASSWORD);
		if(!proxyPassword.trim().isEmpty()){
			hubProxyInfoBuilder.setPassword(proxyPassword);
			final String proxyPasswordLength = this.getPreference(PROXY_PASSWORD_LENGTH);
			if(proxyPasswordLength != null && !proxyPasswordLength.equals("")){
				hubProxyInfoBuilder.setPasswordLength(Integer.parseInt(proxyPasswordLength));
			}
			final HubProxyInfo hubProxyInfo = hubProxyInfoBuilder.buildObject();
			try {
				return hubProxyInfo.getDecryptedPassword();
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
		return proxyPassword;
	}

	public void savePreference(final String preference, final String preferenceValue){
		blackDuckPreferencesService.savePreference(preference, preferenceValue);
	}

	public void saveHubUsername(final String hubUsername){
		this.savePreference(HUB_USERNAME, hubUsername);
	}

	public void saveHubPassword(final String hubPassword){
		if(!hubPassword.trim().isEmpty()){
			final HubCredentialsBuilder hubCredentialsBuilder = new HubCredentialsBuilder();
			hubCredentialsBuilder.setPassword(hubPassword);
			final HubCredentials hubCredentials = hubCredentialsBuilder.buildObject();
			try{
				this.savePreference(HUB_PASSWORD, hubCredentials.getEncryptedPassword());
				this.savePreference(HUB_PASSWORD_LENGTH, String.valueOf(hubCredentials.getActualPasswordLength()));
			}catch(final Exception e){
				e.printStackTrace();
			}
		}
	}

	public void saveHubUrl(final String hubUrl){
		this.savePreference(HUB_URL, hubUrl);
	}

	public void saveHubTimeout(final String timeout){
		this.savePreference(HUB_TIMEOUT, timeout);
	}

	public void saveHubProxyUsername(final String proxyUsername){
		this.savePreference(PROXY_USERNAME, proxyUsername);
	}

	public void saveHubProxyPassword(final String proxyPassword){
		if(!proxyPassword.trim().isEmpty()){
			final HubProxyInfoBuilder hubProxyInfoBuilder = new HubProxyInfoBuilder();
			hubProxyInfoBuilder.setPassword(proxyPassword);
			final HubProxyInfo hubProxyInfo = hubProxyInfoBuilder.buildObject();
			try{
				this.savePreference(PROXY_PASSWORD, hubProxyInfo.getEncryptedPassword());
				this.savePreference(PROXY_PASSWORD_LENGTH, hubProxyInfo.getActualPasswordLength()+"");
			}catch(final Exception e){
				e.printStackTrace();
			}
		}
		this.savePreference(PROXY_PASSWORD, proxyPassword);
	}

	public void saveHubProxyHost(final String proxyHost){
		this.savePreference(PROXY_HOST, proxyHost);
	}

	public void saveHubProxyPort(final String proxyPort){
		this.savePreference(PROXY_PORT, proxyPort);
	}

	public HubServerConfig getHubServerConfig(){
		final HubServerConfigBuilder hubServerConfigBuilder = new HubServerConfigBuilder();
		final String username = this.getHubUsername();
		hubServerConfigBuilder.setUsername(username);
		final String password = this.getHubPassword();
		hubServerConfigBuilder.setPassword(password);
		final String hubUrl = this.getHubUrl();
		hubServerConfigBuilder.setHubUrl(hubUrl);
		final String timeout = this.getHubTimeout();
		hubServerConfigBuilder.setTimeout(timeout);
		final String proxyUsername = this.getHubProxyUsername();
		hubServerConfigBuilder.setProxyUsername(proxyUsername);
		final String proxyPassword = this.getHubProxyPassword();
		hubServerConfigBuilder.setProxyPassword(proxyPassword);
		final String proxyPort = this.getHubProxyPort();
		hubServerConfigBuilder.setProxyPort(proxyPort);
		final String proxyHost = this.getHubProxyHost();
		hubServerConfigBuilder.setProxyHost(proxyHost);
		try{
			return hubServerConfigBuilder.build();
		}catch(final Exception e){
			return null;
		}
	}

	public void saveHubServerConfig(final HubServerConfig hubServerConfig) throws EncryptionException{
		final HubCredentials hubCredentials = hubServerConfig.getGlobalCredentials();
		this.saveHubUsername(hubCredentials.getUsername());
		this.saveHubPassword(hubCredentials.getDecryptedPassword());
		final int timeout = hubServerConfig.getTimeout();
		this.saveHubTimeout(String.valueOf(timeout));
		final URL hubUrl = hubServerConfig.getHubUrl();
		this.saveHubUrl(hubUrl.toString());
		final HubProxyInfo hubProxyInfo = hubServerConfig.getProxyInfo();
		this.saveHubProxyUsername(hubProxyInfo.getUsername());
		this.saveHubProxyPassword(hubProxyInfo.getDecryptedPassword());
		this.saveHubProxyHost(hubProxyInfo.getHost());
		this.saveHubProxyPort(String.valueOf(hubProxyInfo.getPort()));
	}

}
