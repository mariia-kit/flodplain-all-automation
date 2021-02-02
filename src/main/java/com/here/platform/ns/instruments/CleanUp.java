package com.here.platform.ns.instruments;

import static com.here.platform.ns.dto.Users.PROVIDER;

import com.here.platform.ns.controllers.provider.ContainerController;
import com.here.platform.ns.controllers.provider.ProviderController;
import com.here.platform.ns.controllers.provider.ResourceController;
import com.here.platform.ns.dto.Container;
import com.here.platform.ns.dto.Containers;
import com.here.platform.ns.dto.DataProvider;
import com.here.platform.ns.dto.ProviderResource;
import com.here.platform.ns.dto.Providers;
import com.here.platform.ns.helpers.CleanUpHelper;
import com.here.platform.ns.restEndPoints.external.AaaCall;
import io.restassured.response.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.DisplayName;


@DisplayName("Clean test data before run")
public class CleanUp {

    private final static Logger logger = Logger.getLogger(CleanUp.class);

    public void deleteAllTestProvidersAndContainers() {
        String token = "Bearer " + PROVIDER.getToken();
        Response resp = new ProviderController().withToken(token).getProviderList();
        ArrayList<HashMap> providerList = resp.getBody().jsonPath().get();

        List<String> doNotTouchProvider = List.of("reference_provider",
                "daimler_experimental_svt", "daimler_experimental_mp", "daimler_experimental_ns", "daimler_experimental_cm", "here-reference-dev",
                "Daimler", "daimleR_experimental", "daimleR",
                "test-bmw", "bmw", "daimler", "exelsior", "daimler_experimental");

        providerList.parallelStream().forEach(provider -> {
            String providerName = provider.get("name").toString();
            System.out.println("Proceed provider " + providerName);
            if (providerName.toLowerCase().contains(Providers.getDataProviderNamePrefix().toLowerCase())) {
                Response delete = new ProviderController().withToken(token).deleteProvider(providerName);
                if (delete.getStatusCode() == HttpStatus.SC_CONFLICT) {
                    System.out.println("Conflict detected during deletion of data provider:"
                            + providerName);
                    cleanContainers(providerName);
                    cleanResources(providerName);
                    Response finalDelete = new ProviderController().withToken(token).deleteProvider(providerName);
                    if (finalDelete.getStatusCode() != HttpStatus.SC_NO_CONTENT) {
                        System.out.println( "Error deleting " + providerName + " after resolving conflict!");
                    }
                }
            } else {
                deleteAllTestContainersForProvider(new DataProvider(providerName, provider.get("url").toString()));
            }
        });
    }

    public void cleanContainers(String providerName) {
        Response allContainers = new ContainerController().withToken(PROVIDER).getContainersList(providerName);
        System.out.println("Containers of " + providerName + ": " + allContainers.getBody().print());
        if (allContainers.getStatusCode() == HttpStatus.SC_OK) {
            Arrays.stream(allContainers.getBody().as(Container[].class))
                    .forEach(container -> {
                        System.out.println("Delete container " + container.getName() + " for " + providerName);
                        Response delete = new ContainerController().withToken(PROVIDER).deleteContainer(container);
                    });
        }
    }

    public void cleanResources(String providerName) {
        Response allContainers = new ResourceController().withToken(PROVIDER).getResourceList(providerName);
        if (allContainers.getStatusCode() == HttpStatus.SC_OK) {
            Arrays.stream(allContainers.getBody().as(ProviderResource[].class))
                    .forEach(resource -> {
                        System.out.println("Delete resource " + resource.getName() + " for "
                                + providerName);
                        new ResourceController().withToken(PROVIDER).deleteResource(providerName, resource.getName());
                    });
        }
    }

    public void deleteAllTestContainersForProvider(DataProvider provider) {
        Response allContainers = new ContainerController().withToken(PROVIDER).getContainersList(provider.getName());
        System.out.println("Containers of " + provider.getName() + ": " + allContainers.getBody().print());
        if (allContainers.getStatusCode() == HttpStatus.SC_OK) {
            Arrays.stream(allContainers.getBody().as(Container[].class))
                    .parallel()
                    .filter(container -> container.getId().toLowerCase()
                            .contains(Containers.getContainerNamePrefix().toLowerCase()))
                    .forEach(container -> {
                        System.out.println("Delete container " + container.getName() + " for " + provider
                                .getName());
                        Response delete = new ContainerController().withToken(PROVIDER).deleteContainer(container);
                            System.out.println(
                                    "Container " + container.getName() + " deletion result:" + delete.getStatusCode());
                    });
        }
    }


    public void deleteProvider(String providerName) {
        Response allContainers = new ContainerController().withToken(PROVIDER).getContainersList(providerName);
        if (allContainers.getStatusCode() == HttpStatus.SC_OK) {
            Arrays.stream(allContainers.getBody().as(Container[].class))
                    .parallel()
                    .forEach(container -> {
                        logger.info("Delete container " + container.getName() + " for " + providerName);
                        Response delete = new ContainerController().withToken(PROVIDER).deleteContainer(container);
//                        if (delete.getStatusCode() == 409) {
//                            new AaaCall().wipeAllPolicies(container.getId());
//                            new DeleteContainerCall(container).call();
//                        }
                    });
        }
        Response allResources = new ResourceController().withToken(PROVIDER).getResourceList(providerName);
        Arrays.stream(allResources.getBody().as(ProviderResource[].class)).forEach(res -> {
            new ResourceController()
                    .withToken(PROVIDER)
                    .deleteResource(new DataProvider(providerName, ""), res);
        });
        new ProviderController()
                .withToken(PROVIDER)
                .deleteProvider(providerName);
    }

    public void deleteAllArtificialPolicies() {
        logger.info("Delete all artificial Policies!");
        CleanUpHelper.getArtificialPolicy().entrySet().forEach(policy -> {
            logger.info("Delete policy:" + policy.getKey() + " link:" + policy.getValue());
            new AaaCall().removeResourcePermission(policy.getKey(), policy.getValue());
        });
        CleanUpHelper.getArtificialPolicy().clear();
    }

    public void deleteAllArtificialPoliciesBrute() {
        logger.info("Delete all artificial Policies with prefix " + Containers.getContainerNamePrefix());
        new AaaCall().wipeAllPolicies(Containers.getContainerNamePrefix());
        new AaaCall().wipeAllPolicies("/resources");
    }

}
