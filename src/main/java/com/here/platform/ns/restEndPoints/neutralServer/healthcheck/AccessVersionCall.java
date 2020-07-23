package com.here.platform.ns.restEndPoints.neutralServer.healthcheck;

import com.here.platform.ns.dto.CallsUrl;
import com.here.platform.ns.helpers.resthelper.RestHelper;
import com.here.platform.ns.restEndPoints.NeutralServerResponseAssertion;
import io.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;


public class AccessVersionCall {

    private String endpointUrl = CallsUrl.ACCESS_VERSION_CHECK.builder().getUrl();
    private String callMessage = "Call to get health check status for Access service";

    public NeutralServerResponseAssertion call() {
        Response response = RestHelper.get(callMessage, endpointUrl, StringUtils.EMPTY);
        return new NeutralServerResponseAssertion(response);
    }

}