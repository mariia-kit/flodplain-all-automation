package com.here.platform.ns.instruments;

import static com.here.platform.ns.dto.Users.PROVIDER;
import static io.restassured.RestAssured.given;

import com.here.platform.ns.controllers.provider.ContainerController;
import com.here.platform.ns.controllers.provider.ProviderController;
import com.here.platform.ns.controllers.provider.ResourceController;
import com.here.platform.ns.dto.Containers;
import com.here.platform.ns.dto.DataProvider;
import com.here.platform.ns.dto.ProviderResource;
import com.here.platform.ns.dto.Providers;
import com.here.platform.ns.dto.Users;
import com.here.platform.ns.dto.Vehicle;
import com.here.platform.ns.helpers.CleanUpHelper;
import com.here.platform.ns.restEndPoints.NeutralServerResponseAssertion;
import com.here.platform.ns.restEndPoints.external.AaaCall;
import com.here.platform.ns.restEndPoints.external.MarketplaceManageListingCall;
import com.here.platform.ns.restEndPoints.external.ReferenceProviderCall;
import com.here.platform.ns.utils.NS_Config;
import java.util.Arrays;
import org.apache.http.HttpStatus;


public class TestDataGeneration {

    public void createAllRequiredSubscription() {
        Arrays.stream(Containers.values())
                .filter(containers -> Providers.DAIMLER_REFERENCE
                        .getName().equals(containers.getContainer().getDataProviderName()))
                .forEach(containers -> new MarketplaceManageListingCall().fullFlowContainer(containers.getContainer())
        );
        CleanUpHelper.getSubsList().clear();
        CleanUpHelper.getListingList().clear();
    }

    public void createBaseContainersIfNecessary() {
        DataProvider provider0 = Providers.DAIMLER_REAL.getProvider();
        var addDataProvider0 = new ProviderController()
                .withToken(PROVIDER)
                .addProvider(provider0);
        new NeutralServerResponseAssertion(addDataProvider0)
                .expectedCode(HttpStatus.SC_OK);
        DataProvider provider1 = Providers.DAIMLER_EXPERIMENTAL.getProvider();
        var addDataProvider1 = new ProviderController()
                .withToken(PROVIDER)
                .addProvider(provider1);
        new NeutralServerResponseAssertion(addDataProvider1)
                .expectedCode(HttpStatus.SC_OK);
        DataProvider provider2 = Providers.DAIMLER_REFERENCE.getProvider();
        var addDataProvider2 = new ProviderController()
                .withToken(PROVIDER)
                .addProvider(provider2);
        new NeutralServerResponseAssertion(addDataProvider2)
                .expectedCode(HttpStatus.SC_OK);
        DataProvider provider3 = Providers.REFERENCE_PROVIDER.getProvider();
        var addDataProvider3 = new ProviderController()
                .withToken(PROVIDER)
                .addProvider(provider3);
        new NeutralServerResponseAssertion(addDataProvider3)
                .expectedCode(HttpStatus.SC_OK);

        Arrays.stream(Containers.values())
                .forEach(container -> Arrays.asList(container.getContainer().getResourceNames().split(",")).forEach(resource ->
                        new ResourceController().withToken(PROVIDER)
                                .addResource(Providers.generateNew().withName(container.getContainer().getDataProviderName()),
                                        new ProviderResource(resource))));

        Arrays.stream(Containers.values()).forEach(containers ->
                new ContainerController()
                .withToken(PROVIDER)
                .addContainer(containers.getContainer())
        );

//        Arrays.stream(ContainerResources.values())
//                .forEach(res -> new AddProviderResourceCall(
//                        Providers.REFERENCE_PROVIDER.getProvider(),
//                        res.getResource().getName())
//                        .call()
//                        .expectedCode(HttpStatus.SC_OK));

    }

    public void createPoliciesForProviderGroup() {
        new AaaCall().addGroupToPolicy("GROUP-4cd9f1a8-114d-4dd7-bd82-730b01c01479",
                "POLICY-4657709a-4b14-424f-937b-55c5d5f99f6d");
        new AaaCall().addGroupToPolicy("GROUP-f905202a-b38c-46ec-b0a1-5ebda7b18389",
                "POLICY-7e671f5d-d786-4b4a-be97-0c03ae8dd608");
    }

    public void setVehicleTokenForDaimler() {
        String token = Users.DAIMLER.getToken().split(":")[0];
        String refresh = Users.DAIMLER.getToken().split(":")[1];
        for(String vin: Arrays.asList(Vehicle.validVehicleIdLong, Vehicle.validVehicleId)) {
            String url = NS_Config.URL_NS.toString() + NS_Config.SERVICE_ACCESS.toString() + "token?" +
                    "access_token=" + token +
                    "&refresh_token=" + refresh +
                    "&client_id=88440bf1-2fff-42b6-8f99-0510b6b5e6f8" +
                    "&client_secret=2d839912-c5e6-4cfb-8543-9a1bed38efe6" +
                    "&vehicle_id=" + vin +
                    "&scope=mb:user:pool:reader mb:vehicle:status:general";
            given()
                    .log().all()
                    .headers("Content-Type", "application/json",
                            "Authorization", Users.PROVIDER.getToken())
                    .when()
                    .post(url)
                    .then().log().all()
                    .extract().response();
        }

        ReferenceProviderCall.regNewToken(Vehicle.validVehicleId, token);
        ReferenceProviderCall.regNewToken(Vehicle.validRefVehicleId, token);
        ReferenceProviderCall.regNewToken(Vehicle.validVehicleIdLong, token);
    }

}
