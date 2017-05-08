package com.blackducksoftware.integration.eclipse.hub.services;

import com.blackducksoftware.integration.eclipse.services.BlackDuckEclipseServicesFactory;

public class BlackDuckEclipseHubServicesFactory extends BlackDuckEclipseServicesFactory {
	private final HubPreferencesService hubPreferencesService;

	public BlackDuckEclipseHubServicesFactory(){
		super();
		this.connectionService = new HubConnectionService(componentInspectorViewService);
		this.componentLookupService = new HubComponentLookupService((HubConnectionService) connectionService);
		this.hubPreferencesService = new HubPreferencesService();
		instance = this;
	}

	public static BlackDuckEclipseHubServicesFactory getInstance(){
		return (BlackDuckEclipseHubServicesFactory) instance;
	}

	public HubPreferencesService getHubPreferencesService(){
		return hubPreferencesService;
	}

}
