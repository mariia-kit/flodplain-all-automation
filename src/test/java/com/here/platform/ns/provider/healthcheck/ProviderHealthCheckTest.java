package com.here.platform.ns.provider.healthcheck;

import com.here.platform.common.config.Conf;
import com.here.platform.ns.BaseNSTest;
import com.here.platform.ns.controllers.provider.ProviderServiceController;
import com.here.platform.ns.restEndPoints.NeutralServerResponseAssertion;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


@DisplayName("Verify Provider health check")
class ProviderHealthCheckTest extends BaseNSTest {

    @Test
    @DisplayName("Verify Provider service HealthCheck")
    void verifyProviderHealthCheck() {
        var verify = new ProviderServiceController().getHealth();

        new NeutralServerResponseAssertion(verify)
                .expectedCode(HttpStatus.SC_OK)
                .expectedEquals("status", "ok",
                        "Health check 'status' attribute is not equals to 'ok'!");
    }

    @Test
    @DisplayName("Verify Provider service Version")
    void verifyProviderVersionCheck() {
        var verify = new ProviderServiceController().getVersion();

        new NeutralServerResponseAssertion(verify)
                .expectedCode(HttpStatus.SC_OK)
                .expectedJsonTrue("apiVersion",
                        val -> val.matches(Conf.ns().getVersionPattern()),
                        "App version is not as expected, or empty! Pattern "
                                + Conf.ns().getVersionPattern());
    }

    @Test
    @DisplayName("Verify Provider service Deep Health Check")
    @Tag("smoke_ns")
    void verifyProviderDeepHealthCheck() {
        var verify = new ProviderServiceController().getHealthDeep();

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
