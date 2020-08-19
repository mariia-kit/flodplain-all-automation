package com.here.platform.ns.restEndPoints.external;

import static com.here.platform.ns.dto.Users.PROVIDER;

import com.here.platform.common.config.Conf;
import com.here.platform.ns.helpers.resthelper.RestHelper;
import com.here.platform.ns.restEndPoints.BaseRestControllerNS;
import io.restassured.response.Response;
import java.util.function.Supplier;


public class MarketplaceNSGetProvidersCall extends BaseRestControllerNS<MarketplaceNSGetProvidersCall> {

    public MarketplaceNSGetProvidersCall() {
        callMessage = "Perform MP call to gather NS Data Providers info";
        setDefaultUser(PROVIDER);
        endpointUrl = Conf.mp().getMarketplaceUrl() + "/neutral_server/providers";
    }

    @Override
    public Supplier<Response> defineCall() {
        return () -> RestHelper.get(this);
    }

}
