package com.here.platform.common;

import static io.restassured.RestAssured.given;

import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.steps.api.OnboardingSteps;
import com.here.platform.common.config.Conf;
import com.here.platform.ns.dto.Containers;
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
        createBaseProvidersIfNecessary();
        createBaseContainersIfNecessary();
        createBaseCMApplicationIfNecessary();
    }

    public static void createBaseProvidersIfNecessary() {
        Stream.of(Providers.values())
                .filter(providers -> !providers.equals(Providers.NOT_EXIST))
                .forEach(providers -> Steps.createRegularProvider(providers.getProvider()));
    }

    public static void createBaseContainersIfNecessary() {
        Arrays.stream(Containers.values()).forEach(containers ->
                Steps.createRegularContainer(containers.getContainer())
        );
    }

    public static void createBaseCMApplicationIfNecessary() {
        String providerId = Providers.BMW_TEST.getName();
        String consumerId = Conf.mpUsers().getMpConsumer().getRealm();
        new OnboardingSteps(providerId, consumerId)
                .onboardTestProviderApplicationForScope(ConsentRequestContainers.BMW_MILEAGE);
    }


    public static void createPoliciesForProviderGroup() {
        new AaaCall().addGroupToPolicy("GROUP-4cd9f1a8-114d-4dd7-bd82-730b01c01479",
                "POLICY-4657709a-4b14-424f-937b-55c5d5f99f6d");
        new AaaCall().addGroupToPolicy("GROUP-f905202a-b38c-46ec-b0a1-5ebda7b18389",
                "POLICY-7e671f5d-d786-4b4a-be97-0c03ae8dd608");
    }

    public static void setVehicleTokenForDaimler() {
        String token = Users.DAIMLER.getToken().split(":")[0];
        String refresh = Users.DAIMLER.getToken().split(":")[1];
        for (String vin : Arrays.asList(Vehicle.validVehicleIdLong, Vehicle.validVehicleId)) {
            String url = Conf.ns().getNsUrlBaseAccess() + Conf.ns().getAuthUrlAccess() + "token?" +
                    "access_token=" + token +
                    "&refresh_token=" + refresh +
                    "&client_id=88440bf1-2fff-42b6-8f99-0510b6b5e6f8" +
                    "&client_secret=2d839912-c5e6-4cfb-8543-9a1bed38efe6" +
                    "&vehicle_id=" + vin +
                    "&scope=mb:user:pool:reader mb:vehicle:status:general";
            given()
                    .headers("Content-Type", "application/json",
                            "Authorization", Users.PROVIDER.getToken())
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
