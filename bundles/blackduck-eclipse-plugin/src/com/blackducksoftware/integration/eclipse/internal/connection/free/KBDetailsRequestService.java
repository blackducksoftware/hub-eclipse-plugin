package com.blackducksoftware.integration.eclipse.internal.connection.free;

import static com.blackducksoftware.integration.eclipse.internal.connection.free.KBUrlConstants.SEGMENT_API;
import static com.blackducksoftware.integration.eclipse.internal.connection.free.KBUrlConstants.SEGMENT_BY_EXTERNAL_ID;
import static com.blackducksoftware.integration.eclipse.internal.connection.free.KBUrlConstants.SEGMENT_KB_DETAIL;
import static com.blackducksoftware.integration.eclipse.internal.connection.free.KBUrlConstants.SEGMENT_RELEASE;
import static com.blackducksoftware.integration.eclipse.internal.connection.free.KBUrlConstants.SEGMENT_V1;

import java.util.Arrays;
import java.util.List;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.model.response.ComponentSearchResultResponse;
import com.blackducksoftware.integration.hub.request.HubPagedRequest;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubResponseService;

public class KBDetailsRequestService extends HubResponseService{
	private static final List<String> DETAILS_SEGMENTS = Arrays.asList(SEGMENT_KB_DETAIL, SEGMENT_API, SEGMENT_V1, SEGMENT_RELEASE, SEGMENT_BY_EXTERNAL_ID);

	public KBDetailsRequestService(final RestConnection restConnection) {
		super(restConnection);
	}

	public List<ComponentSearchResultResponse> getAllComponents(final String namespace, final String groupId, final String artifactId, final String version) throws IntegrationException {
		final String componentQuery = String.format("%s/%s:%s:%s?authToken=Eng_kb_testall", namespace, groupId, artifactId, version);
		final HubPagedRequest hubPagedRequest = getHubRequestFactory().createPagedRequest(DETAILS_SEGMENTS, componentQuery);
		final List<ComponentSearchResultResponse> allComponents = getAllItems(hubPagedRequest, ComponentSearchResultResponse.class);
		return null;
	}

	public KBDetailsResultResponse getExactComponentMatch(final String namespace, final String groupId, final String artifactId, final String version) throws IntegrationException {
		final List<ComponentSearchResultResponse> allComponents = getAllComponents(namespace, groupId, artifactId, version);
		for (final ComponentSearchResultResponse componentItem : allComponents) {
			if (componentItem.getOriginId() != null) {
				final String exactMatch = String.format("%s:%s:%s", groupId, artifactId, version);
				if (componentItem.getOriginId().equals(exactMatch)) {
					return null;
				}
			}
		}

		throw new HubIntegrationException("Couldn't find an exact component match.");
	}

}
