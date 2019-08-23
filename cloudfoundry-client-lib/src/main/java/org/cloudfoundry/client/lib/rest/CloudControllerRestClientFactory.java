/*
 * Copyright 2009-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cloudfoundry.client.lib.rest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.client.lib.CloudCredentials;
import org.cloudfoundry.client.lib.HttpProxyConfiguration;
import org.cloudfoundry.client.lib.adapters.CloudControllerV3ClientFactory;
import org.cloudfoundry.client.lib.adapters.ImmutableCloudControllerV3ClientFactory;
import org.cloudfoundry.client.lib.domain.CloudSpace;
import org.cloudfoundry.client.lib.domain.annotation.Nullable;
import org.cloudfoundry.client.lib.oauth2.OAuthClient;
import org.cloudfoundry.client.lib.util.RestUtil;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.reactor.ConnectionContext;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.immutables.value.Value;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

/**
 * Factory used to create cloud controller client implementations.
 *
 * @author Thgomas Risberg
 * @author Ramnivas Laddad
 */
@Value.Immutable
public abstract class CloudControllerRestClientFactory {

    private final Map<URL, Map<String, Object>> infoCache = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestUtil restUtil = new RestUtil();

    public abstract Optional<Integer> getClientConnectionPoolSize();

    public abstract Optional<Integer> getClientThreadPoolSize();

    @Nullable
    public abstract HttpProxyConfiguration getHttpProxyConfiguration();

    @Value.Default
    public boolean shouldTrustSelfSignedCertificates() {
        return false;
    }

    public abstract Optional<Duration> getClientConnectTimeout();

    @Value.Derived
    public CloudControllerV3ClientFactory getV3ClientFactory() {
        ImmutableCloudControllerV3ClientFactory.Builder builder = ImmutableCloudControllerV3ClientFactory.builder();
        getClientConnectTimeout().ifPresent(builder::clientConnectTimeout);
        getClientConnectionPoolSize().ifPresent(builder::clientConnectionPoolSize);
        getClientThreadPoolSize().ifPresent(builder::clientThreadPoolSize);
        return builder.build();
    }

    public RestTemplate getGeneralPurposeRestTemplate() {
        return restUtil.createRestTemplate(getHttpProxyConfiguration(), shouldTrustSelfSignedCertificates());
    }

    public CloudControllerRestClient createClient(URL controllerUrl, CloudCredentials credentials) {
        return createClient(controllerUrl, credentials, (CloudSpace) null);
    }

    private CloudControllerRestClient createClient(URL controllerUrl, CloudCredentials credentials, OAuthClient oAuthClient) {
        return createClient(controllerUrl, credentials, (CloudSpace) null, oAuthClient);
    }

    public CloudControllerRestClient createClient(URL controllerUrl, CloudCredentials credentials, String organizationName,
                                                  String spaceName) {
        return createClient(controllerUrl, credentials, organizationName, spaceName,
                            createOAuthClient(controllerUrl, credentials.getOrigin()));
    }

    public CloudControllerRestClient createClient(URL controllerUrl, CloudCredentials credentials, String organizationName,
                                                  String spaceName, OAuthClient oAuthClient) {
        CloudControllerRestClient clientWithoutTarget = createClient(controllerUrl, credentials, oAuthClient);
        CloudSpace target = clientWithoutTarget.getSpace(organizationName, spaceName);

        return createClient(controllerUrl, credentials, target, oAuthClient);
    }

    public CloudControllerRestClient createClient(URL controllerUrl, CloudCredentials credentials, CloudSpace target) {
        return createClient(controllerUrl, credentials, target, createOAuthClient(controllerUrl, credentials.getOrigin()));
    }

    public CloudControllerRestClient createClient(URL controllerUrl, CloudCredentials credentials, CloudSpace target,
                                                  OAuthClient oAuthClient) {
        RestTemplate restTemplate = createAuthorizationSettingRestTemplate(credentials, oAuthClient);
        CloudFoundryClient v3Client = getV3ClientFactory().createClient(controllerUrl, oAuthClient);
        CloudFoundryOperations v3OperationsClient = getV3ClientFactory().createOperationsClient(controllerUrl, v3Client, target);

        return new CloudControllerRestClientImpl(controllerUrl,
                                                 credentials,
                                                 restTemplate,
                                                 oAuthClient,
                                                 v3OperationsClient,
                                                 v3Client,
                                                 target);
    }

    private OAuthClient createOAuthClient(URL controllerUrl, String origin) {
        Map<String, Object> infoMap = getInfoMap(controllerUrl);
        URL authorizationEndpoint = getAuthorizationEndpoint(infoMap);
        if (StringUtils.isEmpty(origin)) {
            return restUtil.createOAuthClient(authorizationEndpoint, getHttpProxyConfiguration(), shouldTrustSelfSignedCertificates());
        }
        ConnectionContext connectionContext = getV3ClientFactory().getOrCreateConnectionContext(controllerUrl.getHost());
        return restUtil.createOAuthClient(authorizationEndpoint, getHttpProxyConfiguration(), shouldTrustSelfSignedCertificates(),
                                          connectionContext, origin);
    }

    private URL getAuthorizationEndpoint(Map<String, Object> infoMap) {
        String authorizationEndpoint = (String) infoMap.get("authorization_endpoint");
        try {
            return new URL(authorizationEndpoint);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(MessageFormat.format("Error creating authorization endpoint URL for endpoint {0}.",
                                                                    authorizationEndpoint),
                                               e);
        }
    }

    private Map<String, Object> getInfoMap(URL controllerUrl) {
        if (infoCache.containsKey(controllerUrl)) {
            return infoCache.get(controllerUrl);
        }

        String infoResponse = getGeneralPurposeRestTemplate().getForObject(controllerUrl + "/v2/info", String.class);
        try {
            return objectMapper.readValue(infoResponse, new TypeReference<Map<String, Object>>() {
            });
        } catch (IOException e) {
            throw new IllegalArgumentException("Error getting /v2/info from cloud controller.", e);
        }
    }

    private RestTemplate createAuthorizationSettingRestTemplate(CloudCredentials credentials, OAuthClient oAuthClient) {
        RestTemplate restTemplate = restUtil.createRestTemplate(getHttpProxyConfiguration(), shouldTrustSelfSignedCertificates());
        oAuthClient.init(credentials);
        setAuthorizingRequestFactory(restTemplate, credentials, oAuthClient);
        return restTemplate;
    }

    private void setAuthorizingRequestFactory(RestTemplate restTemplate, CloudCredentials credentials, OAuthClient oAuthClient) {
        ClientHttpRequestFactory requestFactory = restTemplate.getRequestFactory();
        if (!(requestFactory instanceof CloudControllerRestClientHttpRequestFactory)) {
            restTemplate.setRequestFactory(new CloudControllerRestClientHttpRequestFactory(requestFactory, credentials, oAuthClient));
        }
    }

}
