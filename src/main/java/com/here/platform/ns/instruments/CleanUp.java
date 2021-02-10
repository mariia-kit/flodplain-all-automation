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
import com.here.platform.ns.helpers.Steps;
import com.here.platform.ns.restEndPoints.external.AaaCall;
import io.restassured.response.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
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
        List<Container> containerList = new ArrayList<>();

        List<String> doNotTouchProvider = List.of("reference_provider",
                "daimler_experimental_svt", "daimler_experimental_mp", "daimler_experimental_ns", "daimler_experimental_cm",
                "here-reference-dev", "here-reference-sit",
                "Daimler", "daimleR_experimental", "daimleR",
                "test-bmw", "bmw", "daimler", "exelsior", "daimler_experimental");
        List<String> doNotTouchContainer = List.of("manual-testing-mp-sit-1","manual-testing-mp-sit-2",
                "manual-testing-mp-sit-3","manual-testing-mp-sit-4","manual-testing-mp-sit-5",
                "manual-testing-mp-sit-6","manual-testing-mp-sit-7",
                "cypress-automation-yes-pii", "cypress-automation-no-pii",
                "manual-testing-marketplace-1", "e2e-sit-marketplace-testing-nopii",
                "cypress-automation-no-pii", "manual-testing-dev-denis", "manual-testing-mp-dev-1", "manual-testing-mp-dev-2",
                "e2e-prod-marketplace-testing-nopii", "manual-testing-mp-prod-1", "manual-testing-mp-prod-2",
                "odometer", "fuel", "tires", "doors", "location", "stateofcharge", "connectedvehicle", "payasyoudrive",
                "electricvehicle", "fuelstatus", "vehiclelockstatus", "vehiclestatus", "doorsstatus");
        providerList.parallelStream().forEach(provider -> {
            String providerName = provider.get("name").toString();
            System.out.println("Proceed provider " + providerName);
            //if (providerName.toLowerCase().contains(Providers.getDataProviderNamePrefix().toLowerCase())) {
                if(!doNotTouchProvider.contains(providerName)) {
                Response delete = new ProviderController().withToken(token).deleteProvider(providerName);
                if (delete.getStatusCode() == HttpStatus.SC_CONFLICT) {
                    System.out.println("Conflict detected during deletion of data provider:"
                            + providerName);
                    List<Container> failed = cleanContainers(providerName);
                    containerList.addAll(failed);
                    cleanResources(providerName);
                    Response finalDelete = new ProviderController().withToken(token).deleteProvider(providerName);
                    if (finalDelete.getStatusCode() != HttpStatus.SC_NO_CONTENT) {
                        System.out.println( "Error deleting " + providerName + " after resolving conflict!");
                    }
                }
            } else {
                //deleteAllTestContainersForProvider(new DataProvider(providerName, provider.get("url").toString()));
                    Response allContainers = new ContainerController().withToken(token).getContainersList(providerName);
                    if (allContainers.getStatusCode() == HttpStatus.SC_OK) {
                        Arrays.stream(allContainers.getBody().as(Container[].class))
                                .parallel()
                                .filter(container -> !doNotTouchContainer.contains(container.getId()))
                                .filter(container -> !container.getId().contains("bmwcardata_"))
                                .filter(cont -> !bruteCleanContainer(cont))
                                .forEach(containerList::add);
                    }
            }
        });

        System.out.println("del list (" + containerList.stream().map(c -> c.getId()).collect(Collectors.joining(",")) + ")");
    }

    private boolean bruteCleanContainer(Container container) {
        String token = "Bearer " + PROVIDER.getToken();
        System.out.println("Delete container " + container.getName() + " for " + container.getDataProviderName());
        Response delete = new ContainerController().withToken(token).deleteContainer(container);
        if (delete.getStatusCode() == 409) {
            new AaaCall().wipeAllPolicies(container.getId());
            Response finalDelete = new ContainerController().withToken(token).deleteContainer(container);
            if (delete.getStatusCode() == 409) {
                System.out.println( "Container " + container.getName() + " final deletion result:" + finalDelete.getBody().print());
                return false;
            }
        }
        return true;
    }

    public List<Container> cleanContainers(String providerName) {
        String token = "Bearer " + PROVIDER.getToken();
        Response allContainers = new ContainerController().withToken(token).getContainersList(providerName);
        System.out.println("Containers of " + providerName + ": " + allContainers.getBody().print());
        List<Container> containerList = new ArrayList<>();
        if (allContainers.getStatusCode() == HttpStatus.SC_OK) {
            Arrays.stream(allContainers.getBody().as(Container[].class))
                    .filter(cont -> !bruteCleanContainer(cont))
                    .forEach(containerList::add);
        }
        return containerList;
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
                    .forEach(this::bruteCleanContainer);
        }
    }

    public void deleteAllTestContainersForProvider2(DataProvider provider) {
        //new CleanUp().deleteProvider("exelsior");
//new AaaCall().wipeAllPolicies("automatedtestcontainer-35141g0bkvi13");
//"automatedtestcontainer-67861fv5h96131"
//Steps.removeRegularContainer(new Container("automatedtestcontainer-67861fv5h96131", "automatedtestcontainer-67861fv5h96131 n", "daimler_experimental", "String description",
//        "", true, ""));
        Response allContainers = new ContainerController().withToken(PROVIDER).getContainersList(provider.getId());
        if (allContainers.getStatusCode() == HttpStatus.SC_OK) {
            Arrays.stream(allContainers.getBody().as(Container[].class))
                    .filter(container -> container.getId().contains(Containers.getContainerNamePrefix()))
                    .forEach(cont -> Steps.removeRegularContainer(cont));
        }
    }

    public void deleteProvider(String providerName) {
        Response allContainers = new ContainerController().withToken(PROVIDER).getContainersList(providerName);
        if (allContainers.getStatusCode() == HttpStatus.SC_OK) {
            Arrays.stream(allContainers.getBody().as(Container[].class))
                    .parallel()
                    .forEach(container -> {
                        Response delete = new ContainerController().withToken(PROVIDER).deleteContainer(container);
                        System.out.println("Delete container " + container.getName() + " for " + providerName +
                                " > " + delete.getBody().print());
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
