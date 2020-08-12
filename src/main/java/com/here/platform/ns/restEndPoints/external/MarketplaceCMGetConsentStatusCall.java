package com.here.platform.ns.restEndPoints.external;

import static com.here.platform.ns.dto.Users.CONSUMER;

import com.here.platform.ns.helpers.resthelper.RestHelper;
import com.here.platform.ns.restEndPoints.BaseRestControllerNS;
import com.here.platform.ns.utils.NS_Config;
import io.restassured.response.Response;
import java.util.function.Supplier;


public class MarketplaceCMGetConsentStatusCall extends BaseRestControllerNS<MarketplaceCMGetConsentStatusCall> {

    public MarketplaceCMGetConsentStatusCall(String subsId) {
        callMessage = "Perform MP call to get CM ConsentRequest Status";
        setDefaultUser(CONSUMER);
        endpointUrl = NS_Config.URL_EXTERNAL_MARKETPLACE + "/consent/subscriptions/" + subsId + "/requestStatus";
    }

    @Override
    public Supplier<Response> defineCall() {
        return () -> RestHelper.get(this);
    }

}
