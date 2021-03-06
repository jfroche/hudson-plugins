/*
 * Copyright (C) 2010 JFrog Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jfrog.hudson;

import hudson.ProxyConfiguration;
import hudson.model.Hudson;
import hudson.util.Scrambler;
import org.apache.commons.lang.StringUtils;
import org.jfrog.build.api.util.NullLog;
import org.jfrog.build.client.ArtifactoryBuildInfoClient;
import org.jfrog.build.client.ArtifactoryHttpClient;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents an artifactory instance.
 *
 * @author Yossi Shaul
 */
public class ArtifactoryServer {
    private static final Logger log = Logger.getLogger(ArtifactoryServer.class.getName());

    private static final int DEFAULT_CONNECTION_TIMEOUT = 300;    // 5 Minutes

    private final String url;
    private final String userName;
    private final String password;    // base64 scrambled password
    // Network timeout in milliseconds to use both for connection establishment and for unanswered requests
    private int timeout = DEFAULT_CONNECTION_TIMEOUT;
    private boolean bypassProxy;

    /**
     * List of repository keys, last time we checked. Copy on write semantics.
     */
    private transient volatile List<String> repositories;

    private transient volatile List<String> virtualRepositories;

    @DataBoundConstructor
    public ArtifactoryServer(String url, String userName, String password, int timeout, boolean bypassProxy) {
        this.url = StringUtils.removeEnd(url, "/");
        this.userName = userName;
        this.password = Scrambler.scramble(password);
        this.timeout = timeout > 0 ? timeout : DEFAULT_CONNECTION_TIMEOUT;
        this.bypassProxy = bypassProxy;
    }

    public String getName() {
        return url;
    }

    public String getUrl() {
        return url;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return Scrambler.descramble(password);
    }

    public int getTimeout() {
        return timeout;
    }

    public boolean isBypassProxy() {
        return bypassProxy;
    }

    public List<String> getRepositoryKeys() {
        try {
            ArtifactoryBuildInfoClient client = createArtifactoryClient(userName, getPassword());
            repositories = client.getLocalRepositoriesKeys();
        } catch (IOException e) {
            log.log(Level.WARNING, "Failed to obtain list of local repositories: " + e.getMessage());
        }

        return repositories;
    }

    public List<String> getVirtualRepositoryKeys() {
        try {
            ArtifactoryBuildInfoClient client = createArtifactoryClient(userName, getPassword());
            virtualRepositories = client.getVirtualRepositoryKeys();
        } catch (IOException e) {
            log.log(Level.WARNING, "Failed to obtain list of virtual repositories: " + e.getMessage());
        }

        return virtualRepositories;
    }

    public boolean isPowerPack() {
        try {
            ArtifactoryHttpClient client = new ArtifactoryHttpClient(url, userName, getPassword(), new NullLog());
            ArtifactoryHttpClient.Version version = client.getVersion();
            return version.hasAddons();
        } catch (IOException e) {
            log.log(Level.WARNING, "Failed to obtain list of virtual repositories: " + e.getMessage());
        }
        return false;
    }

    public ArtifactoryBuildInfoClient createArtifactoryClient(String userName, String password) {
        ArtifactoryBuildInfoClient client = new ArtifactoryBuildInfoClient(url, userName, password, new NullLog());
        client.setConnectionTimeout(timeout);

        ProxyConfiguration proxyConfiguration = Hudson.getInstance().proxy;
        if (!bypassProxy && proxyConfiguration != null) {
            client.setProxyConfiguration(proxyConfiguration.name,
                    proxyConfiguration.port,
                    proxyConfiguration.getUserName(),
                    proxyConfiguration.getPassword());
        }

        return client;
    }
}
