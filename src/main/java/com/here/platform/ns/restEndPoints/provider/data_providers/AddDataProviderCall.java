package com.here.platform.ns.restEndPoints.provider.data_providers;

import static com.here.platform.ns.dto.Users.PROVIDER;

import com.here.platform.ns.dto.CallsUrl;
import com.here.platform.ns.dto.DataProvider;
import com.here.platform.ns.helpers.resthelper.RestHelper;
import com.here.platform.ns.restEndPoints.BaseRestControllerNS;
import io.restassured.response.Response;
import java.util.function.Supplier;


public class AddDataProviderCall extends BaseRestControllerNS<AddDataProviderCall> {

    private DataProvider provider;

    public AddDataProviderCall(DataProvider provider) {
        this.provider = provider;
        callMessage = String
                .format("Create new Data Provider '%s' with url '%s'", provider.getName(),
                        provider.getUrl());
        setDefaultUser(PROVIDER);
        endpointUrl = CallsUrl.ADD_PROVIDER.builder()
                .withProviderName(provider.getName())
                .getUrl();
    }

    @Override
    public Supplier<Response> defineCall() {
        return () -> RestHelper.put(callMessage, endpointUrl, getToken(), provider.generateBody());
    }

}
