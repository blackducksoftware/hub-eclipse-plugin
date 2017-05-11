package com.blackducksoftware.integration.eclipse.internal.connection.free;

import static com.blackducksoftware.integration.eclipse.internal.connection.free.KBUrlConstants.SEGMENT_API;
import static com.blackducksoftware.integration.eclipse.internal.connection.free.KBUrlConstants.SEGMENT_BY_EXTERNAL_ID;
import static com.blackducksoftware.integration.eclipse.internal.connection.free.KBUrlConstants.SEGMENT_KB_DETAIL;
import static com.blackducksoftware.integration.eclipse.internal.connection.free.KBUrlConstants.SEGMENT_RELEASE;
import static com.blackducksoftware.integration.eclipse.internal.connection.free.KBUrlConstants.SEGMENT_V1;
import static com.blackducksoftware.integration.eclipse.internal.connection.free.KBUrlConstants.TOKEN_AUTHORIZATION;

import java.util.Arrays;
import java.util.List;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.request.HubRequest;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubResponseService;

public class KBDetailsRequestService extends HubResponseService{
	private static final List<String> DETAILS_SEGMENTS = Arrays.asList(SEGMENT_KB_DETAIL, SEGMENT_API, SEGMENT_V1, SEGMENT_RELEASE, SEGMENT_BY_EXTERNAL_ID);

	public KBDetailsRequestService(final RestConnection restConnection) {
		super(restConnection);
	}

	public KBDetailsResponse getKBDetailsFromComponentVersion(final String namespace, final String groupId, final String artifactId, final String version) throws IntegrationException {
		final List<String> requestUrlSegments = DETAILS_SEGMENTS;
		requestUrlSegments.add(namespace);
		requestUrlSegments.add(String.format("%s:%s:%s?authToken=%s", groupId, artifactId, version, TOKEN_AUTHORIZATION));
		final HubRequest kbRequest = getHubRequestFactory().createRequest(requestUrlSegments);
		final KBDetailsResponse kbDetails = getItem(kbRequest, KBDetailsResponse.class);
		return kbDetails;
	}
}

