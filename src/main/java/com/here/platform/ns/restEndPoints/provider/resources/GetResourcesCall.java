package com.here.platform.ns.restEndPoints.provider.resources;

import static com.here.platform.ns.dto.Users.PROVIDER;

import com.here.platform.ns.dto.CallsUrl;
import com.here.platform.ns.dto.DataProvider;
import com.here.platform.ns.helpers.resthelper.RestHelper;
import com.here.platform.ns.restEndPoints.BaseRestControllerNS;
import io.restassured.response.Response;
import java.util.function.Supplier;


public class GetResourcesCall extends BaseRestControllerNS<GetResourcesCall> {

    public GetResourcesCall(DataProvider provider) {
        this(provider.getName());
    }

    public GetResourcesCall(String providerId) {
        callMessage = String
                .format("Get resources for Data Provider '%s'", providerId);
        setDefaultUser(PROVIDER);
        endpointUrl = CallsUrl.GET_PROVIDER_RESOURCES.builder()
                .withProviderName(providerId)
                .getUrl();
    }

    @Override
    public Supplier<Response> defineCall() {
        return () -> RestHelper.get(callMessage, endpointUrl, getToken());
    }

}
