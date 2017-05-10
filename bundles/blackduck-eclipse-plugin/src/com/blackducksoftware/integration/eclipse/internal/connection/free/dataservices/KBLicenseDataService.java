package com.blackducksoftware.integration.eclipse.internal.connection.free.dataservices;

import com.blackducksoftware.integration.eclipse.internal.connection.free.KBDetailsRequestService;
import com.blackducksoftware.integration.eclipse.internal.connection.free.KBDetailsResultResponse;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.model.view.ComplexLicenseView;
import com.blackducksoftware.integration.hub.model.view.ComponentVersionView;

public class KBLicenseDataService {
	private final KBDetailsRequestService kbDetailsRequestService;

	public KBLicenseDataService(final KBDetailsRequestService kbDetailsRequestService) {
		this.kbDetailsRequestService = kbDetailsRequestService;
	}

	public ComplexLicenseView getComplexLicenseViewFromComponent(final String namespace, final String groupId, final String artifactId, final String version)
			throws IntegrationException {
		final KBDetailsResultResponse componentDetails = kbDetailsRequestService.getExactComponentMatch(namespace, groupId, artifactId, version);
		final String versionUrl = componentDetails.getLicenseId();
		final ComponentVersionView componentVersion = kbDetailsRequestService.getItem(versionUrl, ComponentVersionView.class);
		return componentVersion.getLicense();
	}
}
