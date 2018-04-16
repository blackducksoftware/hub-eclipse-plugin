package com.blackducksoftware.integration.eclipse.internal.utils.externalid;

import java.net.URL;

import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalId;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalIdFactory;

public class FilePathExternalIdExtractor {
    private final ExternalIdFactory externalIdFactory;

    public FilePathExternalIdExtractor() {
        externalIdFactory = new ExternalIdFactory();
    }

    public ExternalId getExternalIdFromLocalMavenUrl(final URL filePath, final URL localMavenRepoPath) {
        if (filePath == null || localMavenRepoPath == null) {
            return null;
        }
        String stringPath = filePath.getFile();
        stringPath = stringPath.replace(localMavenRepoPath.getFile(), "");
        final String[] pathArray = stringPath.split("/");
        final StringBuilder groupIdBuilder = new StringBuilder();
        for (int i = 0; i < pathArray.length - 3; i++) {
            groupIdBuilder.append(pathArray[i]);
            if (i != pathArray.length - 4) {
                groupIdBuilder.append(".");
            }
        }
        final String groupId = groupIdBuilder.toString();
        final String artifactId = pathArray[pathArray.length - 3];
        final String version = pathArray[pathArray.length - 2];

        return externalIdFactory.createMavenExternalId(groupId, artifactId, version);

    }

    public ExternalId getExternalIdFromLocalGradleUrl(final URL filePath) {
        if (filePath == null) {
            return null;
        }
        final String[] pathArray = filePath.getFile().split("/");

        if (pathArray.length < 5) {
            return null;
        }

        final String groupId = pathArray[pathArray.length - 5];
        final String artifactId = pathArray[pathArray.length - 4];
        final String version = pathArray[pathArray.length - 3];

        return externalIdFactory.createMavenExternalId(groupId, artifactId, version);
    }
}
