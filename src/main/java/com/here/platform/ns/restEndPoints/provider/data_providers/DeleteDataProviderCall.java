package com.here.platform.ns.restEndPoints.provider.data_providers;

import static com.here.platform.ns.dto.Users.PROVIDER;

import com.here.platform.ns.dto.CallsUrl;
import com.here.platform.ns.helpers.resthelper.RestHelper;
import com.here.platform.ns.restEndPoints.BaseRestControllerNS;
import io.restassured.response.Response;
import java.util.function.Supplier;


public class DeleteDataProviderCall extends BaseRestControllerNS<DeleteDataProviderCall> {

    public DeleteDataProviderCall(String providerName) {
        callMessage = String.format("Delete Data Provider '%s'", providerName);
        setDefaultUser(PROVIDER);
        endpointUrl = CallsUrl.DELETE_PROVIDER.builder()
                .withProviderName(providerName)
                .getUrl();
    }

    @Override
    public Supplier<Response> defineCall() {
        return () -> RestHelper.delete(callMessage, endpointUrl, getToken());
    }

}
