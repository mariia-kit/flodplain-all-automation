package com.here.platform.ns.restEndPoints.provider.container_info;

import static com.here.platform.ns.dto.Users.PROVIDER;

import com.here.platform.ns.dto.CallsUrl;
import com.here.platform.ns.dto.Container;
import com.here.platform.ns.helpers.resthelper.RestHelper;
import com.here.platform.ns.restEndPoints.BaseRestControllerNS;
import io.restassured.response.Response;
import java.util.function.Supplier;


public class UpdateContainerCall extends BaseRestControllerNS<AddContainerCall> {

    private Container container;

    public UpdateContainerCall(String providerName, String containerName, Container container) {
        this.container = container;
        callMessage = String
                .format("Perform call to update Container for provider '%s' with name '%s' with data %s",
                        providerName, containerName, container.toString());
        setDefaultUser(PROVIDER);
        endpointUrl = CallsUrl.ADD_CONTAINER.builder()
                .withContainerName(containerName)
                .withProviderName(providerName)
                .getUrl();
    }

    @Override
    public Supplier<Response> defineCall() {
        return () -> RestHelper.put(
                callMessage,
                endpointUrl,
                getToken(),
                container.generateFullBody());
    }

}
