package com.here.platform.ns.restEndPoints.external;

import static com.here.platform.ns.dto.Users.PROVIDER;

import com.here.platform.ns.helpers.resthelper.RestHelper;
import com.here.platform.ns.restEndPoints.BaseRestControllerNS;
import com.here.platform.ns.utils.NS_Config;
import io.restassured.response.Response;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;


public class MarketplaceNSGetContainerInfoCall extends
        BaseRestControllerNS<MarketplaceNSGetContainerInfoCall> {

    public MarketplaceNSGetContainerInfoCall(String containerHrn) {
        callMessage = String
                .format("Perform MP call to gather NS Containers info");
        setDefaultUser(PROVIDER);
        String urlEncoded;
        try {
            urlEncoded = URLEncoder.encode(containerHrn, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("Error encoding hrn for api request", ex.getCause());
        }
        endpointUrl =
                NS_Config.URL_EXTERNAL_MARKETPLACE + "/neutral_server/containers/" + urlEncoded;
    }

    @Override
    public Supplier<Response> defineCall() {
        return () -> RestHelper.get(this);
    }

}