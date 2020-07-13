package com.here.platform.ns.restEndPoints.neutralServer.resources;

import static com.here.platform.ns.dto.Users.CONSUMER;
import static com.here.platform.ns.dto.Users.PROVIDER;

import com.here.platform.ns.dto.CallsUrl;
import com.here.platform.ns.helpers.resthelper.RestHelper;
import com.here.platform.ns.restEndPoints.BaseRestControllerNS;
import com.here.platform.ns.utils.NS_Config;
import io.restassured.http.Header;
import io.restassured.response.Response;
import java.util.function.Supplier;


public class GetAllResourcesByVehicleCall extends
        BaseRestControllerNS<GetAllResourcesByVehicleCall> {

    public GetAllResourcesByVehicleCall(String providerName, String vehicleId) {
        callMessage = String
                .format("Get All Resource values for vehicle [%s][%s]", providerName, vehicleId);
        setDefaultUser(CONSUMER);
        endpointUrl = CallsUrl.GET_ALL_RESOURCES_BY_VEHICLE.builder()
                .withProviderName(
                        "hrn:" + NS_Config.REALM.toString() + ":neutral::" + PROVIDER.getUser()
                                .getRealm() + ":" + providerName)
                .withVehicleId(vehicleId)
                .getUrl();

    }

    public GetAllResourcesByVehicleCall withCampaignId(String campaignId) {
        return withHeader(new Header("CampaignID", campaignId));
    }

    @Override
    public Supplier<Response> defineCall() {
        return () -> RestHelper.get(this);
    }

}
