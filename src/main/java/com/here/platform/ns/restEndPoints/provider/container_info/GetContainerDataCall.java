package com.here.platform.ns.restEndPoints.provider.container_info;

import static com.here.platform.ns.dto.Users.PROVIDER;

import com.here.platform.ns.dto.CallsUrl;
import com.here.platform.ns.dto.Container;
import com.here.platform.ns.helpers.resthelper.RestHelper;
import com.here.platform.ns.restEndPoints.BaseRestControllerNS;
import io.restassured.response.Response;
import java.util.function.Supplier;


public class GetContainerDataCall extends BaseRestControllerNS<GetContainerDataCall> {

    public GetContainerDataCall(String containerName, String providerName) {
        callMessage = String
                .format("Request to receive content of Container for provider '%s' with name '%s'",
                        providerName, containerName);
        setDefaultUser(PROVIDER);
        endpointUrl = CallsUrl.GET_CONTAINER.builder()
                .withProviderName(providerName)
                .withContainerName(containerName)
                .getUrl();
    }

    public GetContainerDataCall(Container container) {
        this(container.getId(), container.getDataProviderName());
    }

    @Override
    public Supplier<Response> defineCall() {
        return () -> RestHelper.get(this);
    }

}
