package com.here.platform.ns.restEndPoints.provider.resources;

import static com.here.platform.ns.dto.Users.PROVIDER;

import com.here.platform.ns.dto.CallsUrl;
import com.here.platform.ns.dto.DataProvider;
import com.here.platform.ns.helpers.resthelper.RestHelper;
import com.here.platform.ns.restEndPoints.BaseRestControllerNS;
import io.restassured.response.Response;
import java.util.function.Supplier;


public class DeleteProviderResourceCall extends BaseRestControllerNS<DeleteProviderResourceCall> {

    public DeleteProviderResourceCall(DataProvider provider, String resourceId) {
        this(provider.getName(), resourceId);
    }

    public DeleteProviderResourceCall(String providerId, String resourceId) {
        callMessage = String
                .format("Delete resource data for Data Provider '%s' with name:'%s'", providerId,
                        resourceId);
        setDefaultUser(PROVIDER);
        endpointUrl = CallsUrl.PROVIDER_RESOURCE.builder()
                .withProviderName(providerId)
                .withResourceId(resourceId)
                .getUrl();
    }

    @Override
    public Supplier<Response> defineCall() {
        return () -> RestHelper.delete(callMessage, endpointUrl, getToken());
    }

}