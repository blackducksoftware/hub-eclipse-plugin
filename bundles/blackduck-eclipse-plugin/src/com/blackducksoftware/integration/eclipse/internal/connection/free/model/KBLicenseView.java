package com.blackducksoftware.integration.eclipse.internal.connection.free.model;

import java.util.Map;

import com.blackducksoftware.integration.hub.model.HubView;
import com.blackducksoftware.integration.hub.model.enumeration.ComplexLicenseEnum;
import com.blackducksoftware.integration.hub.model.view.LicenseView;

public class KBLicenseView extends HubView{
	public Map<String, LicenseView> detail;

	public ComplexLicenseEnum type;

}
