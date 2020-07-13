package com.here.platform.ns.restEndPoints.provider.container_info;

import static com.here.platform.ns.dto.Users.PROVIDER;

import com.here.platform.ns.dto.CallsUrl;
import com.here.platform.ns.dto.Container;
import com.here.platform.ns.helpers.resthelper.RestHelper;
import com.here.platform.ns.restEndPoints.BaseRestControllerNS;
import io.restassured.response.Response;
import java.util.function.Supplier;


public class DeleteContainerCall extends BaseRestControllerNS<DeleteContainerCall> {


    public DeleteContainerCall(String containerName, String providerName) {
        callMessage = String
                .format("Request to delete Container for provider '%s' with name '%s'",
                        providerName,
                        containerName);
        setDefaultUser(PROVIDER);
        endpointUrl = CallsUrl.DELETE_CONTAINER.builder()
                .withContainerName(containerName)
                .withProviderName(providerName)
                .getUrl();
    }

    public DeleteContainerCall(Container container) {
        this(container.getId(), container.getDataProviderName());
    }

    @Override
    public Supplier<Response> defineCall() {
        return () -> RestHelper.delete(callMessage, endpointUrl, getToken());
    }

}
