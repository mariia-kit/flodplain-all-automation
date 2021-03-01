package com.here.platform.proxy.tunnel;

import com.here.platform.ns.restEndPoints.NeutralServerResponseAssertion;
import com.here.platform.proxy.BaseProxyTests;
import com.here.platform.proxy.conrollers.TunnelController;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


@Tag("Tunnel")
@DisplayName("Verify AccuWeather service")
public class AccuWeatherTest extends BaseProxyTests {

    @Test
    @DisplayName("Verify retrieve proxy AccuWeather data not exist")
    void verifyAccDataNotExist() {
        var response = new TunnelController()
                .withConsumerToken()
                .getData("dataservice.accuweather.mock", "/locations/v1/topcities/400");
        new NeutralServerResponseAssertion(response)
                .expectedCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    @DisplayName("Verify retrieve proxy AccuWeather data no Subscription")
    void verifyAccDataNoSubs() {
        var response = new TunnelController()
                .withConsumerToken()
                .getData("dataservice.accuweather.mock", "/locations/v1/topcities/100");
        new NeutralServerResponseAssertion(response)
                .expectedCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    @DisplayName("Verify retrieve proxy AccuWeather data top50")
    void verifyAccDataCanBeRetrieved() {
        var response = new TunnelController()
                .withConsumerToken()
                .getData("dataservice.accuweather.mock", "/locations/v1/topcities/50");
        new NeutralServerResponseAssertion(response)
                .expectedCode(HttpStatus.SC_OK);
    }

}
