package com.here.platform.ns.restEndPoints.provider.container_info;

import static com.here.platform.ns.dto.Users.PROVIDER;

import com.here.platform.ns.dto.CallsUrl;
import com.here.platform.ns.helpers.resthelper.RestHelper;
import com.here.platform.ns.restEndPoints.BaseRestControllerNS;
import io.restassured.response.Response;
import java.util.function.Supplier;


public class GetContainersListForProviderCall extends
        BaseRestControllerNS<GetContainersListForProviderCall> {

    public GetContainersListForProviderCall(String providerName) {
        callMessage = String
                .format("Request to receive list of all Containers for provider '%s'",
                        providerName);
        setDefaultUser(PROVIDER);
        endpointUrl = CallsUrl.GET_CONTAINER_LIST.builder()
                .withProviderName(providerName)
                .getUrl();
    }

    @Override
    public Supplier<Response> defineCall() {
        return () -> RestHelper.get(this);
    }

}
