package com.here.platform.ns.access.healthcheck;

import com.here.platform.ns.BaseNSTest;
import com.here.platform.ns.restEndPoints.neutralServer.healthcheck.AccessDeepHealthCheckCall;
import com.here.platform.ns.restEndPoints.neutralServer.healthcheck.AccessHealthCheckCall;
import com.here.platform.ns.restEndPoints.neutralServer.healthcheck.AccessVersionCall;
import com.here.platform.ns.utils.NS_Config;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


@DisplayName("Verify Access health check")
class AccessHealthCheckTest extends BaseNSTest {

    @Test
    @DisplayName("Verify Access service HealthCheck")
    @Tag("touch")
    void verifyProxyHealthCheck() {
        new AccessHealthCheckCall()
                .call()
                .expectedCode(HttpStatus.SC_OK)
                .expectedEquals("status", "ok",
                        "Health check 'status' attribute is not equals to 'ok'!");
    }

    @Test
    @DisplayName("Verify Access service Version")
    void verifyProxyVersionCheck() {
        new AccessVersionCall()
                .call()
                .expectedCode(HttpStatus.SC_OK)
                .expectedJsonTrue("apiVersion",
                        val -> val.matches(NS_Config.VERSION_PATTERN.toString()),
                        "App version is not as expected, or empty! Pattern "
                                + NS_Config.VERSION_PATTERN.toString());
    }

    @Test
    @DisplayName("Verify Access service Deep Health Check")
    @Tag("smoke_ns")
    void verifyProxyDeepHealthCheck() {
        new AccessDeepHealthCheckCall()
                .call()
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
