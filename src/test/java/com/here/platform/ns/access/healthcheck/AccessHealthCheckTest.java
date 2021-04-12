package com.here.platform.ns.access.healthcheck;

import com.here.platform.common.config.Conf;
import com.here.platform.ns.BaseNSTest;
import com.here.platform.ns.controllers.access.AccessServiceController;
import com.here.platform.ns.restEndPoints.NeutralServerResponseAssertion;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


@DisplayName("Verify Access health check")
@Disabled
class AccessHealthCheckTest extends BaseNSTest {

    @Test
    @DisplayName("Verify Access service HealthCheck")
    @Tag("touch")
    void verifyProxyHealthCheck() {
        var verify = new AccessServiceController().getHealth();
        new NeutralServerResponseAssertion(verify)
                .expectedCode(HttpStatus.SC_OK)
                .expectedEquals("status", "ok",
                        "Health check 'status' attribute is not equals to 'ok'!");
    }

    @Test
    @DisplayName("Verify Access service Version")
    void verifyProxyVersionCheck() {
        var verify = new AccessServiceController().getVersion();
        new NeutralServerResponseAssertion(verify)
                .expectedCode(HttpStatus.SC_OK)
                .expectedJsonTrue("apiVersion",
                        val -> val.matches(Conf.ns().getVersionPattern()),
                        "App version is not as expected, or empty! Pattern "
                                + Conf.ns().getVersionPattern());
    }

    @Test
    @DisplayName("Verify Access service Deep Health Check")
    @Tag("smoke_ns")
    void verifyProxyDeepHealthCheck() {
        var verify = new AccessServiceController().getHealthDeep();
        new NeutralServerResponseAssertion(verify)
                .expectedCode(HttpStatus.SC_OK)
                .expectedEquals("isHealthy", "true",
                        "Health check 'isHealthy' attribute is not equals to 'true'!")
                .expectedEquals("serviceName", "access",
                        "Health check 'serviceName' attribute is not equals to 'access'!")
                .expectedEquals("downstreamChecks.isHealthy[0]", "true",
                        "Health check 'PostgreSQL.isHealthy' attribute is not equals to 'true'!")
                .expectedEquals("downstreamChecks.isHealthy[1]", "true",
                        "Health check 'daimler_host.isHealthy' attribute is not equals to 'true'!");
    }

}
