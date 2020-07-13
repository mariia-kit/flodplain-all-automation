package com.here.platform.ns.restEndPoints.neutralServer.resources;

import static com.here.platform.ns.dto.Users.CONSUMER;

import com.here.platform.ns.helpers.resthelper.RestHelper;
import com.here.platform.ns.restEndPoints.BaseRestControllerNS;
import io.restassured.http.Header;
import io.restassured.response.Response;
import java.util.function.Supplier;


public class GetResourceAsyncResultCall extends
        BaseRestControllerNS<GetResourceAsyncResultCall> {

    public GetResourceAsyncResultCall(String location) {
        callMessage = String
                .format("Get Resource values for vehicle Async by URL:", location);
        setDefaultUser(CONSUMER);
        endpointUrl = location;
    }

    public GetResourceAsyncResultCall withCampaignId(String campaignId) {
        return withHeader(new Header("CampaignID", campaignId));
    }

    @Override
    public Supplier<Response> defineCall() {
        return () -> RestHelper.get(this);
    }

}
