package com.here.platform.ns.provider.healthcheck;

import com.here.platform.ns.BaseNSTest;
import com.here.platform.ns.controllers.provider.ProviderHealthController;
import com.here.platform.ns.restEndPoints.NeutralServerResponseAssertion;
import com.here.platform.ns.utils.NS_Config;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


@DisplayName("Verify Provider health check")
class ProviderHealthCheckTest extends BaseNSTest {

    @Test
    @DisplayName("Verify Provider service HealthCheck")
    void verifyProviderHealthCheck() {
        var verify = new ProviderHealthController().getHealth();

        new NeutralServerResponseAssertion(verify)
                .expectedCode(HttpStatus.SC_OK)
                .expectedEquals("status", "ok",
                        "Health check 'status' attribute is not equals to 'ok'!");
    }

    @Test
    @DisplayName("Verify Provider service Version")
    void verifyProviderVersionCheck() {
        var verify = new ProviderHealthController().getVersion();

        new NeutralServerResponseAssertion(verify)
                .expectedCode(HttpStatus.SC_OK)
                .expectedJsonTrue("apiVersion",
                        val -> val.matches(NS_Config.VERSION_PATTERN.toString()),
                        "App version is not as expected, or empty! Pattern "
                                + NS_Config.VERSION_PATTERN.toString());
    }

    @Test
    @DisplayName("Verify Provider service Deep Health Check")
    @Tag("smoke_ns")
    void verifyProviderDeepHealthCheck() {
        var verify = new ProviderHealthController().getHealthDeep();

        new NeutralServerResponseAssertion(verify)
                .expectedCode(HttpStatus.SC_OK)
                .expectedEquals("isHealthy", "true",
                        "Health check 'isHealthy' attribute is not equals to 'true'!")
                .expectedEquals("serviceName", "provider",
                        "Health check 'serviceName' attribute is not equals to 'provider'!")
                .expectedEquals("downstreamChecks.isHealthy[0]", "true",
                        "Health check 'PostgreSQL.isHealthy' attribute is not equals to 'true'!");
    }

}
