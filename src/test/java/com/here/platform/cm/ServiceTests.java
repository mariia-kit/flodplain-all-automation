package com.here.platform.cm;

import static org.assertj.core.api.Assertions.assertThat;

import com.here.platform.cm.controllers.ServiceController;
import com.here.platform.cm.controllers.ServiceController.ConsentManagementHealth;
import com.here.platform.cm.rest.model.Health;
import com.here.platform.cm.rest.model.Version;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.annotations.CMFeatures.BaseService;
import io.qameta.allure.Stories;
import io.qameta.allure.Story;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


@DisplayName("Service status")
@BaseService
@Tag("smoke_cm")
class ServiceTests extends BaseCMTest {

    private final ServiceController serviceController = new ServiceController();

    @Test
    @DisplayName("Verify health check call")
    void healthTest() {
        var actualHealthResponse = this.serviceController.health();

        new ResponseAssertion(actualHealthResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObject(new Health().status("true"));
    }

    @Test
    @DisplayName("Verify version call return valid version number")
    @Stories({@Story(value = "NS-826")})
    void versionTest() {
        var actualVersionResponse = this.serviceController.version();

        var versionResponse = new ResponseAssertion(actualVersionResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .bindAs(Version.class);

        assertThat(versionResponse.getApiVersion())
                .isInstanceOf(String.class)
                .isNotBlank();
    }

    @Test
    @DisplayName("Verify deep health check call")
    @Stories({@Story(value = "NS-825")})
    void deepHealthTest() {
        var actualHealthResponse = this.serviceController.deepHealth();

        var consentManagementHealth = new ResponseAssertion(actualHealthResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .bindAs(ConsentManagementHealth.class);

        makeCMBuildStatus(consentManagementHealth);

        assertThat(consentManagementHealth.getHealth()).isEqualTo(true);
    }

    private void makeCMBuildStatus(ConsentManagementHealth cmHealth) {
        var envStatusPropsPath = System.getProperty("user.dir") + "/build/allure-results/environment.properties";
        try (OutputStream output = new FileOutputStream(envStatusPropsPath)) {
            var props = new HashMap<>(Map.of(
                    "CM version", cmHealth.getServiceVersion() + "." + cmHealth.getServiceBuildNumber(),
                    "CM healthy", cmHealth.getHealth().toString()
            ));

            cmHealth.getServiceHealths().forEach(serviceHealth -> {
                props.putIfAbsent(serviceHealth.getName() + " healthy", serviceHealth.getHealth().toString());
                props.putIfAbsent(serviceHealth.getName() + " version", serviceHealth.getVersion());
            });

            var envProps = new Properties();
            envProps.putAll(props);
            envProps.store(output, null);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

}
