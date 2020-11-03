package com.here.platform.common;

import static io.restassured.RestAssured.given;

import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.steps.api.OnboardingSteps;
import com.here.platform.common.config.Conf;
import com.here.platform.ns.dto.Containers;
import com.here.platform.ns.dto.ProviderResource;
import com.here.platform.ns.dto.Providers;
import com.here.platform.ns.dto.Users;
import com.here.platform.ns.dto.Vehicle;
import com.here.platform.ns.helpers.Steps;
import com.here.platform.ns.restEndPoints.external.AaaCall;
import com.here.platform.ns.restEndPoints.external.ReferenceProviderCall;
import java.util.Arrays;
import java.util.stream.Stream;


public class TestDataGeneration {

    public static void main(String[] args) {
        if (!"prod".equalsIgnoreCase(System.getProperty("env"))) {
            createPoliciesForProviderGroup();
            createBaseProvidersIfNecessary();
            createBaseContainersIfNecessary();
            createBaseCMProvidersIfNecessary();
            createBaseCMApplicationIfNecessary();
        }
    }

    private static void createBaseProvidersIfNecessary() {
        Stream.of(Providers.values())
                .filter(providers -> !providers.equals(Providers.NOT_EXIST))
                .forEach(providers -> Steps.createRegularProvider(providers.getProvider()));
    }

    private static void createBaseContainersIfNecessary() {
        Arrays.stream(Containers.values()).forEach(containers ->
                Steps.createRegularContainer(containers.getContainer())
        );
    }

    private static void createBaseProvidersResourcesIfNecessary() {
        Arrays.stream(Containers.values()).forEach(containers ->
                Stream.of(containers.getContainer().getResourceNames().split(",")).parallel()
                        .forEach(res -> Steps.addResourceToProvider(
                                containers.getContainer().getDataProviderByName(),
                                new ProviderResource(res)
                                )
                        )
        );
    }

    private static void createBaseCMProvidersIfNecessary() {
        String consumerId = Conf.mpUsers().getMpConsumer().getRealm();
        Stream.of(ConsentRequestContainers.values()).forEach(containers ->
                new OnboardingSteps(containers.provider, consumerId)
                        .onboardTestProvider());
    }

    private static void createBaseCMApplicationIfNecessary() {
        String consumerId = Conf.mpUsers().getMpConsumer().getRealm();
        Stream.of(ConsentRequestContainers.values()).forEach(containers ->
                new OnboardingSteps(containers.provider.getName(), consumerId)
                        .onboardTestProviderApplication(containers.getConsentContainer()));
    }


    private static void createPoliciesForProviderGroup() {
        new AaaCall().addGroupToPolicy(Conf.nsUsers().getProviderGroupId(),
                Conf.nsUsers().getProviderPolicyId());
    }

    public static void setVehicleTokenForDaimler() {
        String token = Users.DAIMLER.getToken().split(":")[0];
        String refresh = Users.DAIMLER.getToken().split(":")[1];
        for (String vin : Arrays.asList(Vehicle.validVehicleIdLong, Vehicle.validVehicleId)) {
            String url = Conf.ns().getNsUrlBaseAccess() + Conf.ns().getNsUrlAccess() + "token?" +
                    "access_token=" + token +
                    "&refresh_token=" + refresh +
                    "&client_id=88440bf1-2fff-42b6-8f99-0510b6b5e6f8" +
                    "&client_secret=2d839912-c5e6-4cfb-8543-9a1bed38efe6" +
                    "&vehicle_id=" + vin +
                    "&scope=mb:user:pool:reader mb:vehicle:status:general";
            given()
                    .headers("Content-Type", "application/json",
                            "Authorization", "Bearer " + Users.PROVIDER.getToken())
                    .when()
                    .post(url)
                    .then()
                    .extract().response();
        }

        ReferenceProviderCall.regNewToken(Vehicle.validVehicleId, token);
        ReferenceProviderCall.regNewToken(Vehicle.validRefVehicleId, token);
        ReferenceProviderCall.regNewToken(Vehicle.validVehicleIdLong, token);
    }

}
