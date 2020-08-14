package com.here.platform.ns.restEndPoints.external;

import static com.here.platform.ns.dto.Users.MP_PROVIDER;

import com.here.platform.ns.helpers.resthelper.RestHelper;
import com.here.platform.ns.restEndPoints.BaseRestControllerNS;
import com.here.platform.ns.utils.NS_Config;
import io.restassured.response.Response;
import java.util.function.Supplier;


public class MarketplaceNSGetContainerCall extends BaseRestControllerNS<MarketplaceNSGetContainerCall> {

    public MarketplaceNSGetContainerCall(String providerName) {
        callMessage = "Perform MP call to gather NS Containers list info";
        setDefaultUser(MP_PROVIDER);
        endpointUrl = NS_Config.URL_EXTERNAL_MARKETPLACE + "/neutral_server/providers/" + providerName
                + "/containers";
    }

    @Override
    public Supplier<Response> defineCall() {
        return () -> RestHelper.get(this);
    }

}
