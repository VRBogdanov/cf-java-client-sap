package com.sap.cloudfoundry.client.facade;

import java.net.URL;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import org.cloudfoundry.AbstractCloudFoundryException;
import org.cloudfoundry.client.v3.Metadata;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;

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
import com.sap.cloudfoundry.client.facade.rest.CloudControllerRestClient;
import com.sap.cloudfoundry.client.facade.rest.CloudControllerRestClientFactory;
import com.sap.cloudfoundry.client.facade.rest.ImmutableCloudControllerRestClientFactory;

/**
 * A Java client to exercise the Cloud Foundry API.
 *
 */
public class CloudControllerClientImpl implements CloudControllerClient {

    private CloudControllerRestClient delegate;

    /**
     * Construct client without a default organization and space.
     */

    public CloudControllerClientImpl(URL controllerUrl, CloudCredentials credentials) {
        this(controllerUrl, credentials, null, false);
    }

    public CloudControllerClientImpl(URL controllerUrl, CloudCredentials credentials, boolean trustSelfSignedCerts) {
        this(controllerUrl, credentials, null, trustSelfSignedCerts);
    }

    public CloudControllerClientImpl(URL controllerUrl, CloudCredentials credentials, CloudSpace target, boolean trustSelfSignedCerts) {
        Assert.notNull(controllerUrl, "URL for cloud controller cannot be null");
        CloudControllerRestClientFactory restClientFactory = ImmutableCloudControllerRestClientFactory.builder()
                                                                                                      .shouldTrustSelfSignedCertificates(trustSelfSignedCerts)
                                                                                                      .build();
        this.delegate = restClientFactory.createClient(controllerUrl, credentials, target);
    }

    /**
     * Construct a client with a pre-configured CloudControllerClient
     */
    public CloudControllerClientImpl(CloudControllerRestClient delegate) {
        this.delegate = delegate;
    }

    @Override
    public void addDomain(String domainName) {
        handleExceptions(() -> delegate.addDomain(domainName));
    }

    @Override
    public void addRoute(String host, String domainName, String path) {
        handleExceptions(() -> delegate.addRoute(host, domainName, path));
    }

    @Override
    public Optional<String> bindServiceInstance(String applicationName, String serviceInstanceName) {
        return handleExceptions(() -> delegate.bindServiceInstance(applicationName, serviceInstanceName));
    }

    @Override
    public Optional<String> bindServiceInstance(String applicationName, String serviceInstanceName, Map<String, Object> parameters,
                                                ApplicationServicesUpdateCallback updateServicesCallback) {
        try {
            return handleExceptions(() -> delegate.bindServiceInstance(applicationName, serviceInstanceName, parameters));
        } catch (CloudOperationException e) {
            updateServicesCallback.onError(e, applicationName, serviceInstanceName);
        }
        return Optional.empty();
    }

    @Override
    public void createApplication(String applicationName, Staging staging, Integer disk, Integer memory, Metadata metadata,
                                  Set<CloudRoute> routes) {
        handleExceptions(() -> delegate.createApplication(applicationName, staging, disk, memory, metadata, routes));
    }

    @Override
    public void createServiceInstance(CloudServiceInstance serviceInstance) {
        handleExceptions(() -> delegate.createServiceInstance(serviceInstance));
    }

    @Override
    public String createServiceBroker(CloudServiceBroker serviceBroker) {
        return handleExceptions(() -> delegate.createServiceBroker(serviceBroker));
    }

    @Override
    public CloudServiceKey createAndFetchServiceKey(CloudServiceKey keyModel, String serviceInstanceName) {
        return handleExceptions(() -> delegate.createAndFetchServiceKey(keyModel, serviceInstanceName));
    }

    @Override
    public Optional<String> createServiceKey(CloudServiceKey keyModel, String serviceInstanceName) {
        return handleExceptions(() -> delegate.createServiceKey(keyModel, serviceInstanceName));
    }

    @Override
    public Optional<String> createServiceKey(String serviceInstanceName, String serviceKeyName, Map<String, Object> parameters) {
        return handleExceptions(() -> delegate.createServiceKey(serviceInstanceName, serviceKeyName, parameters));
    }

    @Override
    public void createUserProvidedServiceInstance(CloudServiceInstance serviceInstance) {
        handleExceptions(() -> delegate.createUserProvidedServiceInstance(serviceInstance));
    }

    @Override
    public void deleteAllApplications() {
        handleExceptions(() -> delegate.deleteAllApplications());
    }

    @Override
    public void deleteAllServiceInstances() {
        handleExceptions(() -> delegate.deleteAllServiceInstances());
    }

    @Override
    public void deleteApplication(String applicationName) {
        handleExceptions(() -> delegate.deleteApplication(applicationName));
    }

    @Override
    public void deleteDomain(String domainName) {
        handleExceptions(() -> delegate.deleteDomain(domainName));
    }

    @Override
    public void deleteOrphanedRoutes() {
        handleExceptions(() -> delegate.deleteOrphanedRoutes());
    }

    @Override
    public void deleteRoute(String host, String domainName, String path) {
        handleExceptions(() -> delegate.deleteRoute(host, domainName, path));
    }

    @Override
    public void deleteServiceInstance(String serviceInstanceName) {
        handleExceptions(() -> delegate.deleteServiceInstance(serviceInstanceName));
    }

    @Override
    public void deleteServiceInstance(CloudServiceInstance serviceInstance) {
        handleExceptions(() -> delegate.deleteServiceInstance(serviceInstance));
    }

    @Override
    public String deleteServiceBroker(String name) {
        return handleExceptions(() -> delegate.deleteServiceBroker(name));
    }

    @Override
    public CloudServiceBinding getServiceBinding(UUID serviceBindingId) {
        return handleExceptions(() -> delegate.getServiceBinding(serviceBindingId));
    }

    @Override
    public Optional<String> deleteServiceBinding(String serviceInstanceName, String serviceKeyName) {
        return handleExceptions(() -> delegate.deleteServiceBinding(serviceInstanceName, serviceKeyName));
    }

    @Override
    public Optional<String> deleteServiceBinding(UUID bindingGuid, ServiceBindingOperationCallback serviceBindingOperationCallback) {
        try {
            return handleExceptions(() -> delegate.deleteServiceBinding(bindingGuid));
        } catch (CloudOperationException e) {
            serviceBindingOperationCallback.onError(e, bindingGuid);
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> deleteServiceBinding(UUID bindingGuid) {
        return handleExceptions(() -> delegate.deleteServiceBinding(bindingGuid));
    }

    @Override
    public CloudApplication getApplication(String applicationName) {
        return handleExceptions(() -> delegate.getApplication(applicationName));
    }

    @Override
    public CloudApplication getApplication(String applicationName, boolean required) {
        return handleExceptions(() -> delegate.getApplication(applicationName, required));
    }

    @Override
    public UUID getApplicationGuid(String applicationName) {
        return handleExceptions(() -> delegate.getApplicationGuid(applicationName));
    }

    @Override
    public String getApplicationName(UUID applicationGuid) {
        return handleExceptions(() -> delegate.getApplicationName(applicationGuid));
    }

    @Override
    public Map<String, String> getApplicationEnvironment(UUID applicationGuid) {
        return handleExceptions(() -> delegate.getApplicationEnvironment(applicationGuid));
    }

    @Override
    public Map<String, String> getApplicationEnvironment(String applicationName) {
        return handleExceptions(() -> delegate.getApplicationEnvironment(applicationName));
    }

    @Override
    public List<CloudEvent> getApplicationEvents(String applicationName) {
        return handleExceptions(() -> delegate.getApplicationEvents(applicationName));
    }

    @Override
    public List<CloudEvent> getEventsByActee(UUID uuid) {
        return handleExceptions(() -> delegate.getEventsByTarget(uuid));
    }

    @Override
    public InstancesInfo getApplicationInstances(CloudApplication app) {
        return handleExceptions(() -> delegate.getApplicationInstances(app));
    }

    @Override
    public InstancesInfo getApplicationInstances(UUID applicationGuid) {
        return handleExceptions(() -> delegate.getApplicationInstances(applicationGuid));
    }

    @Override
    public CloudProcess getApplicationProcess(UUID applicationGuid) {
        return handleExceptions(() -> delegate.getApplicationProcess(applicationGuid));
    }

    @Override
    public List<CloudRoute> getApplicationRoutes(UUID applicationGuid) {
        return handleExceptions(() -> delegate.getApplicationRoutes(applicationGuid));
    }

    @Override
    public boolean getApplicationSshEnabled(UUID applicationGuid) {
        return handleExceptions(() -> delegate.getApplicationSshEnabled(applicationGuid));
    }

    @Override
    public List<CloudApplication> getApplications() {
        return handleExceptions(() -> delegate.getApplications());
    }

    @Override
    public List<CloudApplication> getApplicationsByMetadataLabelSelector(String labelSelector) {
        return handleExceptions(() -> delegate.getApplicationsByMetadataLabelSelector(labelSelector));
    }

    @Override
    public URL getCloudControllerUrl() {
        return delegate.getControllerUrl();
    }

    @Override
    public CloudDomain getDefaultDomain() {
        return handleExceptions(() -> delegate.getDefaultDomain());
    }

    @Override
    public List<CloudDomain> getDomains() {
        return handleExceptions(() -> delegate.getDomains());
    }

    @Override
    public List<CloudDomain> getDomainsForOrganization() {
        return handleExceptions(() -> delegate.getDomainsForOrganization());
    }

    @Override
    public List<CloudEvent> getEvents() {
        return handleExceptions(() -> delegate.getEvents());
    }

    @Override
    public CloudOrganization getOrganization(String organizationName) {
        return handleExceptions(() -> delegate.getOrganization(organizationName));
    }

    @Override
    public CloudOrganization getOrganization(String organizationName, boolean required) {
        return handleExceptions(() -> delegate.getOrganization(organizationName, required));
    }

    @Override
    public List<CloudOrganization> getOrganizations() {
        return handleExceptions(() -> delegate.getOrganizations());
    }

    @Override
    public List<CloudDomain> getPrivateDomains() {
        return handleExceptions(() -> delegate.getPrivateDomains());
    }

    @Override
    public List<ApplicationLog> getRecentLogs(String applicationName, LocalDateTime offset) {
        return handleExceptions(() -> delegate.getRecentLogs(applicationName, offset));
    }

    @Override
    public List<ApplicationLog> getRecentLogs(UUID applicationGuid, LocalDateTime offset) {
        return handleExceptions(() -> delegate.getRecentLogs(applicationGuid, offset));
    }

    @Override
    public List<CloudRoute> getRoutes(String domainName) {
        return handleExceptions(() -> delegate.getRoutes(domainName));
    }

    @Override
    public CloudServiceBroker getServiceBroker(String name) {
        return handleExceptions(() -> delegate.getServiceBroker(name));
    }

    @Override
    public CloudServiceBroker getServiceBroker(String name, boolean required) {
        return handleExceptions(() -> delegate.getServiceBroker(name, required));
    }

    @Override
    public List<CloudServiceBroker> getServiceBrokers() {
        return handleExceptions(() -> delegate.getServiceBrokers());
    }

    @Override
    public UUID getRequiredServiceInstanceGuid(String name) {
        return handleExceptions(() -> delegate.getRequiredServiceInstanceGuid(name));
    }

    @Override
    public CloudServiceInstance getServiceInstance(String serviceInstanceName) {
        return handleExceptions(() -> delegate.getServiceInstance(serviceInstanceName));
    }

    @Override
    public CloudServiceInstance getServiceInstance(String serviceInstanceName, boolean required) {
        return handleExceptions(() -> delegate.getServiceInstance(serviceInstanceName, required));
    }

    @Override
    public String getServiceInstanceName(UUID serviceInstanceGuid) {
        return handleExceptions(() -> delegate.getServiceInstanceName(serviceInstanceGuid));
    }

    @Override
    public CloudServiceInstance getServiceInstanceWithoutAuxiliaryContent(String serviceInstanceName) {
        return handleExceptions(() -> delegate.getServiceInstanceWithoutAuxiliaryContent(serviceInstanceName));
    }

    @Override
    public CloudServiceInstance getServiceInstanceWithoutAuxiliaryContent(String serviceInstanceName, boolean required) {
        return handleExceptions(() -> delegate.getServiceInstanceWithoutAuxiliaryContent(serviceInstanceName, required));
    }

    @Override
    public List<CloudServiceBinding> getServiceAppBindings(UUID serviceInstanceGuid) {
        return handleExceptions(() -> delegate.getServiceAppBindings(serviceInstanceGuid));
    }

    @Override
    public List<CloudServiceBinding> getAppBindings(UUID applicationGuid) {
        return handleExceptions(() -> delegate.getAppBindings(applicationGuid));
    }

    @Override
    public CloudServiceBinding getServiceBindingForApplication(UUID applicationId, UUID serviceInstanceGuid) {
        return handleExceptions(() -> delegate.getServiceBindingForApplication(applicationId, serviceInstanceGuid));
    }

    @Override
    public Map<String, Object> getServiceInstanceParameters(UUID guid) {
        return handleExceptions(() -> delegate.getServiceInstanceParameters(guid));
    }

    @Override
    public Map<String, Object> getUserProvidedServiceInstanceParameters(UUID guid) {
        return handleExceptions(() -> delegate.getUserProvidedServiceInstanceParameters(guid));
    }

    @Override
    public Map<String, Object> getServiceBindingParameters(UUID guid) {
        return handleExceptions(() -> delegate.getServiceBindingParameters(guid));
    }

    @Override
    public CloudServiceKey getServiceKey(String serviceInstanceName, String serviceKeyName) {
        return handleExceptions(() -> delegate.getServiceKey(serviceInstanceName, serviceKeyName));
    }

    @Override
    public List<CloudServiceKey> getServiceKeys(String serviceInstanceName) {
        return handleExceptions(() -> delegate.getServiceKeys(serviceInstanceName));
    }

    @Override
    public List<CloudServiceKey> getServiceKeysWithCredentials(String serviceInstanceName) {
        return handleExceptions(() -> delegate.getServiceKeysWithCredentials(serviceInstanceName));
    }

    @Override
    public List<CloudServiceKey> getServiceKeys(CloudServiceInstance serviceInstance) {
        return handleExceptions(() -> delegate.getServiceKeys(serviceInstance));
    }

    @Override
    public List<CloudServiceKey> getServiceKeysWithCredentials(CloudServiceInstance serviceInstance) {
        return handleExceptions(() -> delegate.getServiceKeysWithCredentials(serviceInstance));
    }

    @Override
    public List<CloudServiceOffering> getServiceOfferings() {
        return handleExceptions(() -> delegate.getServiceOfferings());
    }

    @Override
    public List<CloudServiceInstance> getServiceInstances() {
        return handleExceptions(() -> delegate.getServiceInstances());
    }

    @Override
    public List<CloudServiceInstance> getServiceInstancesByMetadataLabelSelector(String labelSelector) {
        return handleExceptions(() -> delegate.getServiceInstancesByMetadataLabelSelector(labelSelector));
    }

    @Override
    public List<CloudServiceInstance> getServiceInstancesWithoutAuxiliaryContentByMetadataLabelSelector(String labelSelector) {
        return handleExceptions(() -> delegate.getServiceInstancesWithoutAuxiliaryContentByMetadataLabelSelector(labelSelector));
    }

    @Override
    public List<CloudDomain> getSharedDomains() {
        return handleExceptions(() -> delegate.getSharedDomains());
    }

    @Override
    public CloudSpace getSpace(UUID spaceGuid) {
        return handleExceptions(() -> delegate.getSpace(spaceGuid));
    }

    @Override
    public CloudSpace getSpace(String organizationName, String spaceName) {
        return handleExceptions(() -> delegate.getSpace(organizationName, spaceName));
    }

    @Override
    public CloudSpace getSpace(String organizationName, String spaceName, boolean required) {
        return handleExceptions(() -> delegate.getSpace(organizationName, spaceName, required));
    }

    @Override
    public CloudSpace getSpace(String spaceName) {
        return handleExceptions(() -> delegate.getSpace(spaceName));
    }

    @Override
    public CloudSpace getSpace(String spaceName, boolean required) {
        return handleExceptions(() -> delegate.getSpace(spaceName, required));
    }

    @Override
    public List<CloudSpace> getSpaces() {
        return handleExceptions(() -> delegate.getSpaces());
    }

    @Override
    public List<CloudSpace> getSpaces(String organizationName) {
        return handleExceptions(() -> delegate.getSpaces(organizationName));
    }

    @Override
    public CloudStack getStack(String name) {
        return handleExceptions(() -> delegate.getStack(name));
    }

    @Override
    public CloudStack getStack(String name, boolean required) {
        return handleExceptions(() -> delegate.getStack(name, required));
    }

    @Override
    public List<CloudStack> getStacks() {
        return handleExceptions(() -> delegate.getStacks());
    }

    @Override
    public OAuth2AccessTokenWithAdditionalInfo login() {
        return handleExceptions(() -> delegate.login());
    }

    @Override
    public void logout() {
        handleExceptions(() -> delegate.logout());
    }

    @Override
    public void rename(String applicationName, String newName) {
        handleExceptions(() -> delegate.rename(applicationName, newName));
    }

    @Override
    public void restartApplication(String applicationName) {
        handleExceptions(() -> delegate.restartApplication(applicationName));
    }

    @Override
    public void startApplication(String applicationName) {
        handleExceptions(() -> delegate.startApplication(applicationName));
    }

    @Override
    public void stopApplication(String applicationName) {
        handleExceptions(() -> delegate.stopApplication(applicationName));
    }

    @Override
    public Optional<String> unbindServiceInstance(String applicationName, String serviceInstanceName,
                                                  ApplicationServicesUpdateCallback applicationServicesUpdateCallback) {
        try {
            return handleExceptions(() -> delegate.unbindServiceInstance(applicationName, serviceInstanceName));
        } catch (CloudOperationException e) {
            applicationServicesUpdateCallback.onError(e, applicationName, serviceInstanceName);
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> unbindServiceInstance(String applicationName, String serviceInstanceName) {
        return handleExceptions(() -> delegate.unbindServiceInstance(applicationName, serviceInstanceName));
    }

    @Override
    public Optional<String> unbindServiceInstance(UUID applicationGuid, UUID serviceInstanceGuid) {
        return handleExceptions(() -> delegate.unbindServiceInstance(applicationGuid, serviceInstanceGuid));
    }

    @Override
    public void updateApplicationDiskQuota(String applicationName, int disk) {
        handleExceptions(() -> delegate.updateApplicationDiskQuota(applicationName, disk));
    }

    @Override
    public void updateApplicationEnv(String applicationName, Map<String, String> env) {
        handleExceptions(() -> delegate.updateApplicationEnv(applicationName, env));
    }

    @Override
    public void updateApplicationInstances(String applicationName, int instances) {
        handleExceptions(() -> delegate.updateApplicationInstances(applicationName, instances));
    }

    @Override
    public void updateApplicationMetadata(UUID guid, Metadata metadata) {
        handleExceptions(() -> delegate.updateApplicationMetadata(guid, metadata));
    }

    @Override
    public List<CloudServiceInstance> getServiceInstancesWithoutAuxiliaryContentByNames(List<String> names) {
        return handleExceptions(() -> delegate.getServiceInstancesWithoutAuxiliaryContentByNames(names));
    }

    @Override
    public void updateApplicationMemory(String applicationName, int memory) {
        handleExceptions(() -> delegate.updateApplicationMemory(applicationName, memory));
    }

    @Override
    public void updateApplicationStaging(String applicationName, Staging staging) {
        handleExceptions(() -> delegate.updateApplicationStaging(applicationName, staging));
    }

    @Override
    public void updateApplicationRoutes(String applicationName, Set<CloudRoute> routes) {
        handleExceptions(() -> delegate.updateApplicationRoutes(applicationName, routes));
    }

    @Override
    public void updateServiceInstanceMetadata(UUID guid, Metadata metadata) {
        handleExceptions(() -> delegate.updateServiceInstanceMetadata(guid, metadata));
    }

    @Override
    public String updateServiceBroker(CloudServiceBroker serviceBroker) {
        return handleExceptions(() -> delegate.updateServiceBroker(serviceBroker));
    }

    @Override
    public void updateServicePlanVisibilityForBroker(String name, ServicePlanVisibility visibility) {
        handleExceptions(() -> delegate.updateServicePlanVisibilityForBroker(name, visibility));
    }

    @Override
    public void updateServicePlan(String serviceName, String planName) {
        handleExceptions(() -> delegate.updateServicePlan(serviceName, planName));
    }

    @Override
    public void updateServiceParameters(String serviceName, Map<String, Object> parameters) {
        handleExceptions(() -> delegate.updateServiceParameters(serviceName, parameters));
    }

    @Override
    public void updateServiceTags(String serviceName, List<String> tags) {
        handleExceptions(() -> delegate.updateServiceTags(serviceName, tags));
    }

    @Override
    public void updateServiceSyslogDrainUrl(String serviceName, String syslogDrainUrl) {
        handleExceptions(() -> delegate.updateServiceSyslogDrainUrl(serviceName, syslogDrainUrl));
    }

    @Override
    public CloudPackage asyncUploadApplication(String applicationName, Path file) {
        return handleExceptions(() -> delegate.asyncUploadApplication(applicationName, file, null));
    }

    @Override
    public CloudPackage asyncUploadApplication(String applicationName, Path file, UploadStatusCallback callback) {
        return handleExceptions(() -> delegate.asyncUploadApplication(applicationName, file, callback));
    }

    @Override
    public Upload getUploadStatus(UUID packageGuid) {
        return handleExceptions(() -> delegate.getUploadStatus(packageGuid));
    }

    @Override
    public CloudBuild createBuild(UUID packageGuid) {
        return handleExceptions(() -> delegate.createBuild(packageGuid));
    }

    @Override
    public CloudBuild getBuild(UUID buildGuid) {
        return handleExceptions(() -> delegate.getBuild(buildGuid));
    }

    @Override
    public CloudTask getTask(UUID taskGuid) {
        return handleExceptions(() -> delegate.getTask(taskGuid));
    }

    @Override
    public List<CloudTask> getTasks(String applicationName) {
        return handleExceptions(() -> delegate.getTasks(applicationName));
    }

    @Override
    public CloudTask runTask(String applicationName, CloudTask task) {
        return handleExceptions(() -> delegate.runTask(applicationName, task));
    }

    @Override
    public CloudTask cancelTask(UUID taskGuid) {
        return handleExceptions(() -> delegate.cancelTask(taskGuid));
    }

    @Override
    public void bindDropletToApp(UUID dropletGuid, UUID applicationGuid) {
        handleExceptions(() -> delegate.bindDropletToApp(dropletGuid, applicationGuid));
    }

    @Override
    public List<CloudBuild> getBuildsForApplication(UUID applicationGuid) {
        return handleExceptions(() -> delegate.getBuildsForApplication(applicationGuid));
    }

    @Override
    public List<CloudBuild> getBuildsForPackage(UUID packageGuid) {
        return handleExceptions(() -> delegate.getBuildsForPackage(packageGuid));
    }

    @Override
    public DropletInfo getCurrentDropletForApplication(UUID applicationGuid) {
        return handleExceptions(() -> delegate.getCurrentDropletForApplication(applicationGuid));
    }

    @Override
    public CloudPackage getPackage(UUID packageGuid) {
        return handleExceptions(() -> delegate.getPackage(packageGuid));
    }

    @Override
    public List<CloudPackage> getPackagesForApplication(UUID applicationGuid) {
        return handleExceptions(() -> delegate.getPackagesForApplication(applicationGuid));
    }

    @Override
    public List<UserRole> getUserRolesBySpaceAndUser(UUID spaceGuid, UUID userGuid) {
        return handleExceptions(() -> delegate.getUserRolesBySpaceAndUser(spaceGuid, userGuid));
    }

    @Override
    public CloudPackage createDockerPackage(UUID applicationGuid, DockerInfo dockerInfo) {
        return handleExceptions(() -> delegate.createDockerPackage(applicationGuid, dockerInfo));
    }

    @Override
    public CloudAsyncJob getAsyncJob(String jobId) {
        return handleExceptions(() -> delegate.getAsyncJob(jobId));
    }

    private void handleExceptions(Runnable runnable) {
        handleExceptions(() -> {
            runnable.run();
            return null;
        });
    }

    private <T> T handleExceptions(Supplier<T> runnable) {
        try {
            return runnable.get();
        } catch (AbstractCloudFoundryException e) {
            throw convertV3ClientException(e);
        }
    }

    private CloudOperationException convertV3ClientException(AbstractCloudFoundryException e) {
        HttpStatus httpStatus = HttpStatus.valueOf(e.getStatusCode());
        return new CloudOperationException(httpStatus, httpStatus.getReasonPhrase(), e.getMessage(), e);
    }

}
