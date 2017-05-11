package com.blackducksoftware.integration.eclipse.internal.connection.free.dataservices;

import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.integration.eclipse.internal.connection.free.KBDetailsRequestService;
import com.blackducksoftware.integration.eclipse.internal.connection.free.KBDetailsResponse;
import com.blackducksoftware.integration.eclipse.internal.connection.free.model.KBLicenseView;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.model.enumeration.ComplexLicenseCodeSharingEnum;
import com.blackducksoftware.integration.hub.model.enumeration.ComplexLicenseOwnershipEnum;
import com.blackducksoftware.integration.hub.model.view.ComplexLicenseView;
import com.blackducksoftware.integration.hub.model.view.LicenseView;

public class KBLicenseDataService {
	private final KBDetailsRequestService kbDetailsRequestService;

	public KBLicenseDataService(final KBDetailsRequestService kbDetailsRequestService) {
		this.kbDetailsRequestService = kbDetailsRequestService;
	}

	public ComplexLicenseView getComplexLicenseViewFromComponent(final String namespace, final String groupId, final String artifactId, final String version)
			throws IntegrationException {
		final KBDetailsResponse kbDetails = kbDetailsRequestService.getKBDetailsFromComponentVersion(namespace, groupId, artifactId, version);
		final KBLicenseView license = kbDetails.license;
		try {
			return this.transformKBLicenseViewToComplexLicenseView(license);
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			return null;
		}
	}

	private ComplexLicenseView transformKBLicenseViewToComplexLicenseView(final KBLicenseView license) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException{
		final ComplexLicenseView wrappingComplexLicenseView = new ComplexLicenseView();
		wrappingComplexLicenseView.getClass().getDeclaredField("type").set(wrappingComplexLicenseView, license.type);
		final List<ComplexLicenseView> licenses = new ArrayList<>();
		for(final LicenseView licenseView: license.detail.values()) {
			final ComplexLicenseView wrappedComplexLicenseView = new ComplexLicenseView();
			wrappingComplexLicenseView.getClass().getDeclaredField("codesharing").set(wrappingComplexLicenseView, ComplexLicenseCodeSharingEnum.valueOf(licenseView.getCodeSharing().toString()));
			wrappedComplexLicenseView.getClass().getDeclaredField("name").set(wrappedComplexLicenseView, licenseView.getName());
			wrappedComplexLicenseView.getClass().getDeclaredField("ownership").set(wrappedComplexLicenseView, ComplexLicenseOwnershipEnum.valueOf(licenseView.getOwnership().toString()));
			licenses.add(wrappedComplexLicenseView);
		}
		wrappingComplexLicenseView.getClass().getDeclaredField("licenses").set(wrappingComplexLicenseView, licenses);
		return wrappingComplexLicenseView;
	}
}
