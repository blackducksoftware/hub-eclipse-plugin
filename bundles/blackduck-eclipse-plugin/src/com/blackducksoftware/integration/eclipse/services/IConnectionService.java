package com.blackducksoftware.integration.eclipse.services;

import com.blackducksoftware.integration.eclipse.internal.ComponentModel;

public interface IConnectionService {
	public void reloadConnection();

	public boolean hasActiveConnection();

	public void shutDown();

	public void displayExpandedComponentInformation(ComponentModel selectedObject);
}
