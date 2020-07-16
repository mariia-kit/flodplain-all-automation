package com.here.platform.ns.restEndPoints.external;

import static com.here.platform.ns.dto.Users.CONSUMER;

import com.here.platform.ns.dto.Container;
import com.here.platform.ns.helpers.resthelper.RestHelper;
import com.here.platform.ns.restEndPoints.BaseRestControllerNS;
import com.here.platform.ns.utils.NS_Config;
import io.restassured.response.Response;
import java.util.function.Supplier;


public class MarketplaceCMCreateConsentCall extends
        BaseRestControllerNS<MarketplaceCMCreateConsentCall> {

    private Container container;

    public MarketplaceCMCreateConsentCall(String subsId, Container container) {
        this.container = container;
        callMessage = String
                .format("Perform MP call to create CM ConsentRequest");
        setDefaultUser(CONSUMER);
        endpointUrl = NS_Config.URL_EXTERNAL_MARKETPLACE + "/consent/subscriptions/" + subsId + "/request";
    }

    @Override
    public Supplier<Response> defineCall() {

        String body = "{\n"
                + "  \"title\": \"" + container.getName() + " request\",\n"
                + "  \"purpose\": \"Test Consent for " + container.getName() + "\",\n"
                + "  \"privacyPolicy\":\"tratata\",\n"
                + "  \"additionalLinks\":["
                + "  {\"title\":\"title1\",\"url\":\"link1\"},\n"
                + "  {\"title\":\"title2\",\"url\":\"link2\"},\n"
                + "  {\"title\":\"title3\",\"url\":\"link3\"},\n"
                + "  {\"title\":\"title4\",\"url\":\"link4\"},\n"
                + "  {\"title\":\"title5\",\"url\":\"link5\"},\n"
                + "  {\"title\":\"title6\",\"url\":\"link6\"},\n"
                + "  {\"title\":\"title7\",\"url\":\"link7\"},\n"
                + "  {\"title\":\"title8\",\"url\":\"link8\"},\n"
                + "  {\"title\":\"title9\",\"url\":\"link9\"}]"
                + "}";
        return () -> RestHelper.post(this, body);
    }
}
