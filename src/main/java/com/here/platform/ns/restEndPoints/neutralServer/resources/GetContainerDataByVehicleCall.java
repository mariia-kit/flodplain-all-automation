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


public class GetContainerDataByVehicleCall extends
        BaseRestControllerNS<GetContainerDataByVehicleCall> {

    public GetContainerDataByVehicleCall(String providerName, String vehicleId,
            String containerName) {
        callMessage = String
                .format("Get Container resource value for [%s][%s][%s] ", providerName, vehicleId,
                        containerName);
        setDefaultUser(CONSUMER);
        endpointUrl = CallsUrl.GET_CONTAINER_RESOURCE_BY_VEHICLE.builder()
                .withContainerName(containerName)
                .withProviderName("hrn:" + NS_Config.REALM.toString() + ":neutral::" + PROVIDER.getUser().getRealm() + ":"
                        + providerName)
                .withVehicleId(vehicleId)
                .getUrl();

    }

    public GetContainerDataByVehicleCall withCampaignId(String campaignId) {
        return withHeader(new Header("CampaignID", campaignId));
    }

    @Override
    public Supplier<Response> defineCall() {
        return () -> RestHelper.get(this);
    }

}
