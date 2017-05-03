package com.blackducksoftware.integration.eclipse.views.widgets;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.blackducksoftware.integration.eclipse.BlackDuckHubPluginActivator;
import com.blackducksoftware.integration.eclipse.common.services.ProjectInformationService;
import com.blackducksoftware.integration.eclipse.common.services.inspector.ComponentInspectorPreferencesService;
import com.blackducksoftware.integration.eclipse.common.services.inspector.ComponentInspectorService;
import com.blackducksoftware.integration.eclipse.internal.ComponentModel;
import com.blackducksoftware.integration.eclipse.views.ComponentInspectorView;

public class ComponentTableStatusCLabel extends CLabel{
	private final ComponentInspectorPreferencesService componentInspectorPreferencesService;
	private final ProjectInformationService projectInformationService;
	private final TableViewer componentInspectorTableViewer;

	public ComponentTableStatusCLabel(final Composite parent, final int style, final TableViewer componentInspectorTableViewer) {
		super(parent, style);
		this.componentInspectorPreferencesService = new ComponentInspectorPreferencesService();
		this.projectInformationService = new ProjectInformationService();
		this.componentInspectorTableViewer = componentInspectorTableViewer;
	}

	public void updateStatus(final String projectName){
		final ComponentInspectorService componentInspectorService = BlackDuckHubPluginActivator.getDefault().getInspectorService();
		final boolean noComponents = ((ComponentModel[]) componentInspectorTableViewer.getInput()).length == 0;
		final boolean noProjectMapping = componentInspectorService.getProjectComponents(projectName) == null;
		if (projectName.equals("")) {
			this.setStatusMessage(ComponentInspectorService.NO_SELECTED_PROJECT_STATUS);
		}else{
			if(componentInspectorPreferencesService.isProjectMarkedForInspection(projectName)){
				if(BlackDuckHubPluginActivator.getDefault().getHubConnectionService().hasActiveHubConnection()){
					if (componentInspectorService.isProjectInspectionRunning(projectName)) {
						this.setStatusMessage(ComponentInspectorService.PROJECT_INSPECTION_RUNNING_STATUS);
					} else if (componentInspectorService.isProjectInspectionScheduled(projectName)) {
						this.setStatusMessage(ComponentInspectorService.PROJECT_INSPECTION_SCHEDULED_STATUS);
					} else if (noProjectMapping) {
						this.setStatusMessage(ComponentInspectorService.PROJECT_NEEDS_INSPECTION_STATUS);
					} else if (noComponents){
						this.setStatusMessage(ComponentInspectorService.CONNECTION_OK_NO_COMPONENTS_STATUS);
					} else{
						this.setStatusMessage(ComponentInspectorService.CONNECTION_OK_STATUS);
					}
				} else {
					this.setStatusMessage(ComponentInspectorService.CONNECTION_DISCONNECTED_STATUS);
				}
			} else {
				if (projectInformationService.isProjectSupported(projectName)) {
					this.setStatusMessage(ComponentInspectorService.PROJECT_NOT_MARKED_FOR_INSPECTION_STATUS);
				} else {
					this.setStatusMessage(ComponentInspectorService.PROJECT_NOT_SUPPORTED_STATUS);
				}
			}
		}
	}

	public void setStatusMessage(final String message) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (!isDisposed() && !message.equals(getText())) {
					ImageDescriptor newImageDescriptor = null;
					switch (message) {
					case ComponentInspectorService.PROJECT_INSPECTION_RUNNING_STATUS:
						newImageDescriptor = BlackDuckHubPluginActivator.imageDescriptorFromPlugin(BlackDuckHubPluginActivator.PLUGIN_ID, ComponentInspectorView.WAITING_PNG_PATH);
						break;
					case ComponentInspectorService.PROJECT_INSPECTION_SCHEDULED_STATUS:
						newImageDescriptor = BlackDuckHubPluginActivator.imageDescriptorFromPlugin(BlackDuckHubPluginActivator.PLUGIN_ID, ComponentInspectorView.WAITING_PNG_PATH);
						break;
					case ComponentInspectorService.CONNECTION_DISCONNECTED_STATUS:
						newImageDescriptor = BlackDuckHubPluginActivator.imageDescriptorFromPlugin(BlackDuckHubPluginActivator.PLUGIN_ID, ComponentInspectorView.DISCONNECT_PNG_PATH);
						break;
					case ComponentInspectorService.PROJECT_NEEDS_INSPECTION_STATUS:
						newImageDescriptor = BlackDuckHubPluginActivator.imageDescriptorFromPlugin(BlackDuckHubPluginActivator.PLUGIN_ID, ComponentInspectorView.WARNING_PNG_PATH);
						break;
					default:
						break;
					}
					Image newImage = null;
					if (newImageDescriptor != null) {
						newImage = newImageDescriptor.createImage();
					}
					setImage(newImage);
					setText(message);
				}
			}
		});
	}

}
