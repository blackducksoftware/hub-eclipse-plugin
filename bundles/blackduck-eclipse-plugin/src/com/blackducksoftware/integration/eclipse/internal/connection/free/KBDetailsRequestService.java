package com.blackducksoftware.integration.eclipse.internal.connection.free;

import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_API;
import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_COMPONENTS;

import java.util.Arrays;
import java.util.List;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.model.response.ComponentSearchResultResponse;
import com.blackducksoftware.integration.hub.model.view.ComponentVersionView;

public class KBDetailsRequestService {
	private static final List<String> COMPONENT_SEGMENTS = Arrays.asList(SEGMENT_API, SEGMENT_COMPONENTS);

	public List<ComponentSearchResultResponse> getAllComponents(final String namespace, final String groupId, final String artifactId, final String version)
			throws IntegrationException {
		final String componentQuery = String.format("id:%s|%s|%s|%s", namespace, groupId, artifactId, version);
		//final HubPagedRequest hubPagedRequest = getHubRequestFactory().createPagedRequest(COMPONENT_SEGMENTS, componentQuery);

		//final List<ComponentSearchResultResponse> allComponents = getAllItems(hubPagedRequest, ComponentSearchResultResponse.class);
		return null;
	}

	public KBDetailsResultResponse getExactComponentMatch(final String namespace, final String groupId, final String artifactId, final String version)
			throws IntegrationException {
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

	public ComponentVersionView getItem(final String versionUrl, final Class<ComponentVersionView> class1) {
		// TODO Auto-generated method stub
		return null;
	}
}
