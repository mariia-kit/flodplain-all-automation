package com.here.platform.ns.restEndPoints.provider.resources;

import static com.here.platform.ns.dto.Users.PROVIDER;

import com.here.platform.ns.dto.CallsUrl;
import com.here.platform.ns.dto.DataProvider;
import com.here.platform.ns.helpers.resthelper.RestHelper;
import com.here.platform.ns.restEndPoints.BaseRestControllerNS;
import io.restassured.response.Response;
import java.util.function.Supplier;


public class AddProviderResourceCall extends BaseRestControllerNS<AddProviderResourceCall> {

    private String resourceId;

    public AddProviderResourceCall(DataProvider provider, String resourceId) {
        this(provider.getName(), resourceId);
    }

    public AddProviderResourceCall(String providerId, String resourceId) {
        this.resourceId = resourceId;
        callMessage = String
                .format("Add resource data for Data Provider '%s': '%s'", providerId, resourceId);
        setDefaultUser(PROVIDER);
        endpointUrl = CallsUrl.PROVIDER_RESOURCE.builder()
                .withProviderName(providerId)
                .withResourceId(resourceId)
                .getUrl();
    }

    @Override
    public Supplier<Response> defineCall() {
        String body = "{\n"
                + "  \"name\": \"" + resourceId + "\"\n"
                + "}";
        return () -> RestHelper.put(callMessage, endpointUrl, getToken(), body);
    }

}
