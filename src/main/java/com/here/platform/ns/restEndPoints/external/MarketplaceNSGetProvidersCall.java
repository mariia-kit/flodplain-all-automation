package com.here.platform.ns.restEndPoints.external;

import static com.here.platform.ns.dto.Users.PROVIDER;

import com.here.platform.ns.helpers.resthelper.RestHelper;
import com.here.platform.ns.restEndPoints.BaseRestControllerNS;
import com.here.platform.ns.utils.NS_Config;
import io.restassured.response.Response;
import java.util.function.Supplier;


public class MarketplaceNSGetProvidersCall extends
        BaseRestControllerNS<MarketplaceNSGetProvidersCall> {

    public MarketplaceNSGetProvidersCall() {
        callMessage = String
                .format("Perform MP call to gather NS Data Providers info");
        setDefaultUser(PROVIDER);
        endpointUrl = NS_Config.URL_EXTERNAL_MARKETPLACE + "/neutral_server/providers";
    }

    @Override
    public Supplier<Response> defineCall() {
        return () -> RestHelper.get(this);
    }

}
