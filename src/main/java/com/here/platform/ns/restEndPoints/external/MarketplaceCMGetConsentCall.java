package com.here.platform.ns.restEndPoints.external;

import static com.here.platform.ns.dto.Users.CONSUMER;

import com.here.platform.common.config.Conf;
import com.here.platform.ns.helpers.resthelper.RestHelper;
import com.here.platform.ns.restEndPoints.BaseRestControllerNS;
import io.restassured.response.Response;
import java.util.function.Supplier;


public class MarketplaceCMGetConsentCall extends BaseRestControllerNS<MarketplaceCMGetConsentCall> {

    public MarketplaceCMGetConsentCall(String subsId) {
        callMessage = "Perform MP call to get CM ConsentRequest data";
        setDefaultUser(CONSUMER);
        endpointUrl = Conf.mp().getMarketplaceUrl() + "/consent/subscriptions/" + subsId + "/request";
    }

    @Override
    public Supplier<Response> defineCall() {
        return () -> RestHelper.get(this);
    }

}
