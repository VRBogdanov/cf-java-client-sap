package com.sap.cloudfoundry.client.facade.rest;

import java.net.URL;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.cloudfoundry.client.v3.Metadata;
import org.springframework.web.reactive.function.client.WebClient;

import com.sap.cloudfoundry.client.facade.UploadStatusCallback;
import com.sap.cloudfoundry.client.facade.domain.ApplicationLog;
import com.sap.cloudfoundry.client.facade.domain.CloudApplication;
import com.sap.cloudfoundry.client.facade.domain.CloudAsyncJob;
import com.sap.cloudfoundry.client.facade.domain.CloudBuild;
import com.sap.cloudfoundry.client.facade.domain.CloudDomain;
import com.sap.cloudfoundry.client.facade.domain.CloudEvent;
import com.sap.cloudfoundry.client.facade.domain.CloudOrganization;
import com.sap.cloudfoundry.client.facade.domain.CloudPackage;
import com.sap.cloudfoundry.client.facade.domain.CloudProcess;
import com.sap.cloudfoundry.client.facade.domain.CloudRoute;
import com.sap.cloudfoundry.client.facade.domain.CloudServiceBinding;
import com.sap.cloudfoundry.client.facade.domain.CloudServiceBroker;
import com.sap.cloudfoundry.client.facade.domain.CloudServiceInstance;
import com.sap.cloudfoundry.client.facade.domain.CloudServiceKey;
import com.sap.cloudfoundry.client.facade.domain.CloudServiceOffering;
import com.sap.cloudfoundry.client.facade.domain.CloudSpace;
import com.sap.cloudfoundry.client.facade.domain.CloudStack;
import com.sap.cloudfoundry.client.facade.domain.CloudTask;
import com.sap.cloudfoundry.client.facade.domain.DockerInfo;
import com.sap.cloudfoundry.client.facade.domain.DropletInfo;
import com.sap.cloudfoundry.client.facade.domain.InstancesInfo;
import com.sap.cloudfoundry.client.facade.domain.ServicePlanVisibility;
import com.sap.cloudfoundry.client.facade.domain.Staging;
import com.sap.cloudfoundry.client.facade.domain.Upload;
import com.sap.cloudfoundry.client.facade.domain.UserRole;
import com.sap.cloudfoundry.client.facade.oauth2.OAuth2AccessTokenWithAdditionalInfo;
import com.sap.cloudfoundry.client.facade.oauth2.OAuthClient;

/**
 * Interface defining operations available for the cloud controller REST client implementations
 *
 */
public interface CloudControllerRestClient {

    void addDomain(String domainName);

    void addRoute(String host, String domainName, String path);

    Optional<String> bindServiceInstance(String applicationName, String serviceInstanceName);

    Optional<String> bindServiceInstance(String applicationName, String serviceInstanceName, Map<String, Object> parameters);

    void createApplication(String applicationName, Staging staging, Integer disk, Integer memory, Metadata metadata,
                           Set<CloudRoute> routes);

    void createServiceInstance(CloudServiceInstance serviceInstance);

    String createServiceBroker(CloudServiceBroker serviceBroker);

    CloudServiceKey createAndFetchServiceKey(CloudServiceKey keyModel, String serviceInstanceName);

    Optional<String> createServiceKey(CloudServiceKey keyModel, String serviceInstanceName);

    Optional<String> createServiceKey(String serviceInstanceName, String serviceKeyName, Map<String, Object> parameters);

    void createUserProvidedServiceInstance(CloudServiceInstance serviceInstance);

    void deleteAllApplications();

    void deleteAllServiceInstances();

    void deleteApplication(String applicationName);

    void deleteDomain(String domainName);

    void deleteOrphanedRoutes();

    void deleteRoute(String host, String domainName, String path);

    void deleteServiceInstance(String serviceInstanceName);

    void deleteServiceInstance(CloudServiceInstance serviceInstance);

    String deleteServiceBroker(String name);

    Optional<String> deleteServiceBinding(String serviceInstanceName, String serviceKeyName);

    Optional<String> deleteServiceBinding(UUID bindingGuid);

    CloudApplication getApplication(String applicationName);

    CloudApplication getApplication(String applicationName, boolean required);

    UUID getApplicationGuid(String applicationName);

    String getApplicationName(UUID applicationGuid);

    Map<String, String> getApplicationEnvironment(UUID applicationGuid);

    Map<String, String> getApplicationEnvironment(String applicationName);

    List<CloudEvent> getApplicationEvents(String applicationName);

    List<CloudEvent> getEventsByTarget(UUID uuid);

    InstancesInfo getApplicationInstances(CloudApplication app);

    InstancesInfo getApplicationInstances(UUID applicationGuid);

    CloudProcess getApplicationProcess(UUID applicationGuid);

    List<CloudRoute> getApplicationRoutes(UUID applicationGuid);

    boolean getApplicationSshEnabled(UUID applicationGuid);

    List<CloudApplication> getApplications();

    URL getControllerUrl();

    CloudDomain getDefaultDomain();

    List<CloudDomain> getDomains();

    List<CloudDomain> getDomainsForOrganization();

    List<CloudEvent> getEvents();

    CloudOrganization getOrganization(String organizationName);

    CloudOrganization getOrganization(String organizationName, boolean required);

    List<CloudOrganization> getOrganizations();

    List<CloudDomain> getPrivateDomains();

    List<ApplicationLog> getRecentLogs(String applicationName, LocalDateTime offset);

    List<ApplicationLog> getRecentLogs(UUID applicationGuid, LocalDateTime offset);

    List<CloudRoute> getRoutes(String domainName);

    UUID getRequiredServiceInstanceGuid(String name);

    CloudServiceInstance getServiceInstance(String serviceInstanceName);

    CloudServiceInstance getServiceInstance(String serviceInstanceName, boolean required);

    String getServiceInstanceName(UUID serviceInstanceGuid);

    CloudServiceInstance getServiceInstanceWithoutAuxiliaryContent(String serviceInstanceName);

    CloudServiceInstance getServiceInstanceWithoutAuxiliaryContent(String serviceInstanceName, boolean required);

    CloudServiceBinding getServiceBinding(UUID serviceBindingGuid);

    List<CloudServiceBinding> getServiceAppBindings(UUID serviceInstanceGuid);

    List<CloudServiceBinding> getAppBindings(UUID applicationGuid);

    CloudServiceBinding getServiceBindingForApplication(UUID applicationId, UUID serviceInstanceGuid);

    CloudServiceBroker getServiceBroker(String name);

    CloudServiceBroker getServiceBroker(String name, boolean required);

    List<CloudServiceBroker> getServiceBrokers();

    CloudServiceKey getServiceKey(String serviceInstanceName, String serviceKeyName);

    List<CloudServiceKey> getServiceKeys(String serviceInstanceName);

    List<CloudServiceKey> getServiceKeysWithCredentials(String serviceInstanceName);

    List<CloudServiceKey> getServiceKeys(CloudServiceInstance serviceInstance);

    List<CloudServiceKey> getServiceKeysWithCredentials(CloudServiceInstance serviceInstance);

    List<CloudServiceOffering> getServiceOfferings();

    List<CloudServiceInstance> getServiceInstances();

    List<CloudDomain> getSharedDomains();

    CloudSpace getSpace(UUID spaceGuid);

    CloudSpace getSpace(String organizationName, String spaceName);

    CloudSpace getSpace(String organizationName, String spaceName, boolean required);

    CloudSpace getSpace(String spaceName);

    CloudSpace getSpace(String spaceName, boolean required);

    List<CloudSpace> getSpaces();

    List<CloudSpace> getSpaces(String organizationName);

    CloudStack getStack(String name);

    CloudStack getStack(String name, boolean required);

    List<CloudStack> getStacks();

    OAuth2AccessTokenWithAdditionalInfo login();

    void logout();

    void rename(String applicationName, String newName);

    void restartApplication(String applicationName);

    void startApplication(String applicationName);

    void stopApplication(String applicationName);

    Optional<String> unbindServiceInstance(String applicationName, String serviceInstanceName);

    Optional<String> unbindServiceInstance(UUID applicationGuid, UUID serviceInstanceGuid);

    void updateApplicationDiskQuota(String applicationName, int disk);

    void updateApplicationEnv(String applicationName, Map<String, String> env);

    void updateApplicationInstances(String applicationName, int instances);

    void updateApplicationMemory(String applicationName, int memory);

    void updateApplicationStaging(String applicationName, Staging staging);

    void updateApplicationRoutes(String applicationName, Set<CloudRoute> routes);

    String updateServiceBroker(CloudServiceBroker serviceBroker);

    void updateServicePlanVisibilityForBroker(String name, ServicePlanVisibility visibility);

    void updateServicePlan(String serviceName, String planName);

    void updateServiceParameters(String serviceName, Map<String, Object> parameters);

    void updateServiceSyslogDrainUrl(String serviceName, String syslogDrainUrl);

    void updateServiceTags(String serviceName, List<String> tags);

    CloudPackage asyncUploadApplication(String applicationName, Path file, UploadStatusCallback callback);

    Upload getUploadStatus(UUID packageGuid);

    CloudTask getTask(UUID taskGuid);

    List<CloudTask> getTasks(String applicationName);

    CloudTask runTask(String applicationName, CloudTask task);

    CloudTask cancelTask(UUID taskGuid);

    CloudBuild createBuild(UUID packageGuid);

    CloudBuild getBuild(UUID packageGuid);

    void bindDropletToApp(UUID dropletGuid, UUID applicationGuid);

    List<CloudBuild> getBuildsForApplication(UUID applicationGuid);

    WebClient getWebClient();

    OAuthClient getOAuthClient();

    Map<String, Object> getServiceInstanceParameters(UUID guid);

    Map<String, Object> getUserProvidedServiceInstanceParameters(UUID guid);

    Map<String, Object> getServiceBindingParameters(UUID guid);

    List<CloudBuild> getBuildsForPackage(UUID packageGuid);

    List<CloudApplication> getApplicationsByMetadataLabelSelector(String labelSelector);

    List<CloudServiceInstance> getServiceInstancesWithoutAuxiliaryContentByNames(List<String> names);

    List<CloudServiceInstance> getServiceInstancesByMetadataLabelSelector(String labelSelector);

    List<CloudServiceInstance> getServiceInstancesWithoutAuxiliaryContentByMetadataLabelSelector(String labelSelector);

    void updateApplicationMetadata(UUID guid, Metadata metadata);

    void updateServiceInstanceMetadata(UUID guid, Metadata metadata);

    DropletInfo getCurrentDropletForApplication(UUID applicationGuid);

    CloudPackage getPackage(UUID packageGuid);

    List<CloudPackage> getPackagesForApplication(UUID applicationGuid);

    List<UserRole> getUserRolesBySpaceAndUser(UUID spaceGuid, UUID userGuid);

    CloudPackage createDockerPackage(UUID applicationGuid, DockerInfo dockerInfo);

    CloudAsyncJob getAsyncJob(String jobId);

}
