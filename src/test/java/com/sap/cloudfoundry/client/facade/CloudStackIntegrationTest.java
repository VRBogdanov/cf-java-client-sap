package com.sap.cloudfoundry.client.facade;

import static com.sap.cloudfoundry.client.facade.IntegrationTestConstants.APPLICATION_HOST;
import static com.sap.cloudfoundry.client.facade.IntegrationTestConstants.DEFAULT_DOMAIN;
import static com.sap.cloudfoundry.client.facade.IntegrationTestConstants.DISK_IN_MB;
import static com.sap.cloudfoundry.client.facade.IntegrationTestConstants.MEMORY_IN_MB;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sap.cloudfoundry.client.facade.domain.CloudApplication;
import com.sap.cloudfoundry.client.facade.domain.CloudRoute;
import com.sap.cloudfoundry.client.facade.domain.ImmutableCloudApplication;
import com.sap.cloudfoundry.client.facade.domain.ImmutableCloudDomain;
import com.sap.cloudfoundry.client.facade.domain.ImmutableCloudRoute;
import com.sap.cloudfoundry.client.facade.domain.ImmutableStaging;
import com.sap.cloudfoundry.client.facade.domain.Staging;

class CloudStackIntegrationTest extends CloudControllerClientIntegrationTest {

    private static final String DEFAULT_STACK_NAME = "cflinuxfs3";
    private static final String INCORRECT_STACK_NAME = "Non-existent-stack-name";
    private static final CloudRoute DEFAULT_ROUTE = ImmutableCloudRoute.builder()
                                                                       .host(APPLICATION_HOST)
                                                                       .domain(ImmutableCloudDomain.builder()
                                                                                                   .name(DEFAULT_DOMAIN)
                                                                                                   .build())
                                                                       .url(APPLICATION_HOST+"."+DEFAULT_DOMAIN)
                                                                       .build();

    @BeforeAll
    static void createDefaultDomain() {
        client.addDomain(DEFAULT_DOMAIN);
    }

    @AfterAll
    static void deleteDefaultDomain() {
        List<CloudRoute> routes = client.getRoutes(DEFAULT_DOMAIN);
        for (CloudRoute route : routes) {
            client.deleteRoute(route.getHost(), DEFAULT_DOMAIN, null);
        }
        client.deleteDomain(DEFAULT_DOMAIN);
    }

    @Test
    @DisplayName("Create application and verify default cloud stack")
    void createApplicationWithDefaultCloudStack() {
        String applicationName = "test-stack-app-1";
        Staging staging = ImmutableStaging.builder()
                                          .addBuildpack(IntegrationTestConstants.JAVA_BUILDPACK)
                                          .build();
        try {
            verifyExistenceOfDefaultStack(applicationName, staging, Set.of(DEFAULT_ROUTE));
        } finally {
            client.deleteApplication(applicationName);
        }
    }

    private void verifyExistenceOfDefaultStack(String applicationName, Staging staging, Set<CloudRoute> routes) {
        client.createApplication(applicationName, staging, DISK_IN_MB, MEMORY_IN_MB, null, routes);
        assertDefaultCloudStack(ImmutableCloudApplication.builder()
                                                         .name(applicationName)
                                                         .state(CloudApplication.State.STARTED)
                                                         .lifecycle(createLifecycle(staging))
                                                         .build());

    }

    private void assertDefaultCloudStack(CloudApplication application) {
        CloudApplication app = client.getApplication(application.getName());
        var appStack = app.getLifecycle()
                          .getData()
                          .get("stack");
        assertNotNull(appStack);
        assertEquals(DEFAULT_STACK_NAME, appStack);
    }

    @Test
    @DisplayName("Create application with incorrect stack and verify that exception is thrown")
    void createApplicationWithIncorrectStack() {
        String applicationName = "test-stack-app-2";
        Staging staging = ImmutableStaging.builder()
                                          .addBuildpack(IntegrationTestConstants.JAVA_BUILDPACK)
                                          .build();
        try {
            verifyIncorrectStack(applicationName, staging, Set.of(DEFAULT_ROUTE));
        } finally {
            client.deleteApplication(applicationName);
        }
    }

    private void verifyIncorrectStack(String applicationName, Staging staging, Set<CloudRoute> routes) {
        client.createApplication(applicationName, staging, DISK_IN_MB, MEMORY_IN_MB, null, routes);
        assertThrows(CloudOperationException.class, () -> client.getStack(INCORRECT_STACK_NAME, true));
    }

    @Test
    @DisplayName("Verify existence of at least one valid Cloud Stack")
    void getStackList() {
        assertNotNull(client.getStacks());
        assertFalse(client.getStacks()
                          .isEmpty());
    }
}