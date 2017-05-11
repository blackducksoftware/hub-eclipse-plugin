package com.blackducksoftware.integration.eclipse.internal.connection.free;

import java.util.Map;

import com.blackducksoftware.integration.eclipse.internal.connection.free.model.CVEVulnerabilityView;
import com.blackducksoftware.integration.eclipse.internal.connection.free.model.KBLicenseView;
import com.blackducksoftware.integration.eclipse.internal.connection.free.model.VulnDBVulnerabilityView;
import com.blackducksoftware.integration.hub.model.HubResponse;

public class KBDetailsResponse extends HubResponse{
	public Map<String, CVEVulnerabilityView> cves;

	public KBLicenseView license;

	public Map<String, VulnDBVulnerabilityView[]> vulns;

}
