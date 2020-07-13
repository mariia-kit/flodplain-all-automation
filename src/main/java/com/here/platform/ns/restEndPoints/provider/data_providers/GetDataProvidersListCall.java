package com.here.platform.ns.restEndPoints.provider.data_providers;

import static com.here.platform.ns.dto.Users.PROVIDER;

import com.here.platform.ns.dto.CallsUrl;
import com.here.platform.ns.helpers.resthelper.RestHelper;
import com.here.platform.ns.restEndPoints.BaseRestControllerNS;
import io.restassured.response.Response;
import java.util.function.Supplier;


public class GetDataProvidersListCall extends BaseRestControllerNS<GetDataProvidersListCall> {

    public GetDataProvidersListCall() {
        setDefaultUser(PROVIDER);
        callMessage = "Call to get list of all Data Providers";
        endpointUrl = CallsUrl.GET_PROVIDERS.builder()
                .getUrl();
    }

    @Override
    public Supplier<Response> defineCall() {
        return () -> RestHelper.get(this);
    }

}
