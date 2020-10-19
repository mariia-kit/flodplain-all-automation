package com.here.platform.ns.restEndPoints.external;


import com.here.platform.common.config.Conf;
import com.here.platform.ns.dto.Container;
import com.here.platform.ns.dto.ContainerResources;
import com.here.platform.ns.dto.Containers;
import com.here.platform.ns.dto.Providers;
import com.here.platform.ns.helpers.Steps;
import com.here.platform.ns.helpers.resthelper.RestHelper;
import io.restassured.response.Response;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;


public class ReferenceProviderCall {

    public static void wipeAllData() {
        String token = StringUtils.EMPTY;
        String url = Conf.ns().getRefProviderUrl() + "/admin/wipe";
        Response resp = RestHelper.get("Wipe all reference provider data", url, token);
        Assertions.assertEquals(HttpStatus.SC_OK, resp.getStatusCode(),
                "Wipe reference provider response not as expected!");
        Container manual = Containers.generateNew(Providers.REFERENCE_PROVIDER.getName())
                .withName("manual-testing-marketplace-1")
                .withId("manual-testing-marketplace-1")
                .withResourceNames(ContainerResources.FUEL.getResource().getName())
                .withConsentRequired(true);
        createContainer(manual);
    }

    public static void regNewToken(String vinNumber, String token) {
        String url = Conf.ns().getRefProviderUrl() + "/admin/tokens";
        String authToken = StringUtils.EMPTY;
        String body = "{\n"
                + "    \"tokenId\": \"" + token + "\",\n"
                + "    \"refreshTokenId\": \"" + UUID.randomUUID().toString() + "\",\n"
                + "    \"expiresIn\": 0,\n"
                + "    \"vin\": \"" + vinNumber + "\",\n"
                + "    \"scope\": \"general\"\n"
                + "  }";
        Response r = RestHelper.post("Register new token in reference provider for " + vinNumber, url, authToken, body);
        Assertions.assertEquals(HttpStatus.SC_OK, r.getStatusCode(),
                "Register new token in reference provider response not as expected!");
    }

    public static void regNewToken(String vinNumber, String token, Container container) {
        String url = Conf.ns().getRefProviderUrl() + "/admin/tokens";
        String authToken = StringUtils.EMPTY;
        String body = "{\n"
                + "    \"tokenId\": \"" + token + "\",\n"
                + "    \"refreshTokenId\": \"" + UUID.randomUUID().toString() + "\",\n"
                + "    \"expiresIn\": 0,\n"
                + "    \"vin\": \"" + vinNumber + "\",\n"
                + "    \"scope\": " + container.getName() + " " + container.getResourceNames() + "\n"
                + "  }";
        Response r = RestHelper.post("Register new token in reference provider for " + vinNumber, url, authToken, body);
        Assertions.assertEquals(HttpStatus.SC_OK, r.getStatusCode(),
                "Register new token in reference provider response not as expected!");
    }

    public static void createContainer(Container container) {
        String url = Conf.ns().getRefProviderUrl() + "/admin/containers";
        String authToken = StringUtils.EMPTY;
        String body = "{\n"
                + "    \"name\": \"" + container.getId() + "\",\n"
                + "    \"consentRequired\": " + container.getConsentRequired() + ",\n"
                + "    \"resourceNames\": [\n" + Arrays.stream(container.getResourceNames().split(","))
                .map(item -> "\"" + item + "\"").collect(Collectors.joining(",\n")) + "\n"
                + "    ]\n"
                + "  }";
        Response r = RestHelper
                .post("Create new Container on Reference Provider side:" + container.getName(), url, authToken, body);
        Assertions.assertEquals(HttpStatus.SC_OK, r.getStatusCode(),
                "Create new Container on Reference Provider side response not as expected!");
    }

}
