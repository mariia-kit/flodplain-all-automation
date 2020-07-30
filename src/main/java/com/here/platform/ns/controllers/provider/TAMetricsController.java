package com.here.platform.ns.controllers.provider;

import com.here.platform.ns.controllers.BaseNeutralService;
import com.here.platform.ns.utils.NS_Config;
import io.qameta.allure.Step;
import io.restassured.response.Response;


public class TAMetricsController extends BaseNeutralService<TAMetricsController> {

    private final String metricsBasePath = NS_Config.SERVICE_PROVIDER + "ta/metrics";

    @Step
    public Response getTaMetrics(String date) {
        return neutralServerClient(metricsBasePath)
                .queryParam("date", date)
                .get();

    }

    @Step
    public Response getTaMetricsStatistics(String date) {
        return neutralServerClient(metricsBasePath)
                .queryParam("date", date)
                .get("/statistics");

    }

}
