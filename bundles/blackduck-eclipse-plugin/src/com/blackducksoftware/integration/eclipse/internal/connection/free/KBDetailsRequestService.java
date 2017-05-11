/**
 * com.blackducksoftware.integration.eclipse.plugin
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.blackducksoftware.integration.eclipse.internal.connection.free;

import static com.blackducksoftware.integration.eclipse.internal.connection.free.KBUrlConstants.SEGMENT_API;
import static com.blackducksoftware.integration.eclipse.internal.connection.free.KBUrlConstants.SEGMENT_BY_EXTERNAL_ID;
import static com.blackducksoftware.integration.eclipse.internal.connection.free.KBUrlConstants.SEGMENT_KB_DETAIL;
import static com.blackducksoftware.integration.eclipse.internal.connection.free.KBUrlConstants.SEGMENT_RELEASE;
import static com.blackducksoftware.integration.eclipse.internal.connection.free.KBUrlConstants.SEGMENT_V1;
import static com.blackducksoftware.integration.eclipse.internal.connection.free.KBUrlConstants.TOKEN_AUTHORIZATION;

import java.util.ArrayList;
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
		final ArrayList<String> requestUrlSegments = new ArrayList<>();
		requestUrlSegments.addAll(DETAILS_SEGMENTS);
		requestUrlSegments.add(namespace);
		requestUrlSegments.add(String.format("%s:%s:%s", groupId, artifactId, version));
		final HubRequest kbRequest = getHubRequestFactory().createRequest(requestUrlSegments);
		kbRequest.addQueryParameter("authToken", TOKEN_AUTHORIZATION);
		final KBDetailsResponse kbDetails = getItem(kbRequest, KBDetailsResponse.class);
		return kbDetails;
	}
}

