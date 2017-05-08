package com.blackducksoftware.integration.eclipse.services;

import com.blackducksoftware.integration.eclipse.BlackDuckEclipseActivator;
import com.blackducksoftware.integration.eclipse.services.inspector.ComponentInspectorCacheService;
import com.blackducksoftware.integration.eclipse.services.inspector.ComponentInspectorPreferencesService;
import com.blackducksoftware.integration.eclipse.services.inspector.ComponentInspectorService;
import com.blackducksoftware.integration.eclipse.services.inspector.ComponentInspectorViewService;

public class BlackDuckEclipseServicesFactory {
	protected static BlackDuckEclipseServicesFactory instance;

	protected IConnectionService connectionService;

	protected ComponentInspectorViewService componentInspectorViewService;

	protected ComponentInformationService componentInformationService;

	protected ProjectInformationService projectInformationService;

	protected WorkspaceInformationService workspaceInformationService;

	protected ComponentLookupService componentLookupService;

	protected BlackDuckPreferencesService blackDuckPreferencesService;

	protected ComponentInspectorPreferencesService componentInspectorPreferencesService;

	protected ComponentInspectorService componentInspectorService;

	protected ComponentInspectorCacheService componentInspectorCacheService;

	protected BlackDuckEclipseServicesFactory(){
		instance = this;
		//TODO: Implement some default connection service?
		this.connectionService = null;
		this.componentInspectorViewService = new ComponentInspectorViewService();
		this.componentInformationService = new ComponentInformationService();
		this.projectInformationService = new ProjectInformationService(componentInformationService);
		this.workspaceInformationService = new WorkspaceInformationService(projectInformationService);
		this.componentLookupService = new ComponentLookupService(connectionService);
		this.blackDuckPreferencesService = new BlackDuckPreferencesService(BlackDuckEclipseActivator.getDefault());
		this.componentInspectorPreferencesService = new ComponentInspectorPreferencesService(blackDuckPreferencesService);
		this.componentInspectorService = new ComponentInspectorService(componentInspectorViewService, connectionService);
		this.componentInspectorCacheService = new ComponentInspectorCacheService(componentInspectorViewService, componentLookupService);
	}

	public static BlackDuckEclipseServicesFactory getInstance(){
		return instance;
	}

	public IConnectionService getConnectionService() {
		return connectionService;
	}

	public BlackDuckPreferencesService getBlackDuckPreferencesService() {
		return blackDuckPreferencesService;
	}

	public ComponentInformationService getComponentInformationService() {
		return componentInformationService ;
	}

	public WorkspaceInformationService getWorkspaceInformationService() {
		return workspaceInformationService;
	}

	public ProjectInformationService getProjectInformationService() {
		return projectInformationService;
	}

	public ComponentInspectorService getComponentInspectorService() {
		return componentInspectorService;
	}

	public ComponentInspectorViewService getComponentInspectorViewService() {
		return componentInspectorViewService;
	}

	public ComponentInspectorPreferencesService getComponentInspectorPreferencesService() {
		return componentInspectorPreferencesService;
	}

	public ComponentLookupService getComponentLookupService() {
		return componentLookupService;
	}

	public ComponentInspectorCacheService getComponentInspectorCacheService() {
		return componentInspectorCacheService;
	}

}
