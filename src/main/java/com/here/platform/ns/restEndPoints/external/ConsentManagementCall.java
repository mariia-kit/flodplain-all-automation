package com.here.platform.ns.restEndPoints.external;

import static com.here.platform.ns.dto.Users.CM_CONSUMER;
import static com.here.platform.ns.dto.Users.CONSUMER;
import static com.here.platform.ns.dto.Users.MP_CONSUMER;
import static com.here.platform.ns.dto.Users.PROVIDER;

import com.here.platform.aaa.DaimlerTokenController;
import com.here.platform.aaa.ReferenceTokenController;
import com.here.platform.ns.dto.Container;
import com.here.platform.ns.dto.Containers;
import com.here.platform.ns.dto.Providers;
import com.here.platform.ns.dto.Vehicle;
import com.here.platform.ns.helpers.CleanUpHelper;
import com.here.platform.ns.helpers.ConsentManagerHelper;
import com.here.platform.ns.helpers.resthelper.RestHelper;
import com.here.platform.ns.utils.NS_Config;
import com.here.platform.ns.utils.PropertiesLoader;
import io.restassured.http.Header;
import io.restassured.response.Response;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Base64;
import org.junit.jupiter.api.Assertions;


public class ConsentManagementCall {

    public String initConsentRequestStrict(Container container, String cmConsumerId) {
        String token = "Bearer " + CM_CONSUMER.getUser().getToken();
        String url = NS_Config.URL_EXTERNAL_CM + "/consentRequests";
        String providerId = container.getDataProviderName();
        String body = "{\n"
                + "  \"consumerId\": \"" + cmConsumerId + "\",\n"
                + "  \"providerId\": \"" + providerId + "\",\n"
                + "  \"purpose\": \"qa_test_test_consent\",\n"
                + "  \"containerName\": \"" + container.getId() + "\",\n"
                + "  \"title\": \"qa_test_" + container.getId() + "\"\n"
                + "}";
        Response resp = RestHelper.post("Init Consent Campaign Id", url, token, body,
                new Header("X-Correlation-ID", "x-cons-init-" + container.getName()));
        Assertions.assertEquals(201, resp.getStatusCode(),
                "Error during init of consent for " + container.getName());
        String consentId = resp.getBody().jsonPath().get("consentRequestId").toString();
        CleanUpHelper.getConsentIdsList().add(consentId);
        return consentId;
    }

    public void addVinNumbers(String consentId, String vin) {
        String token = "Bearer " + MP_CONSUMER.getUser().getToken();
        String url = NS_Config.URL_EXTERNAL_CM + "/consentRequests/" + consentId + "/addDataSubjects";
        File file = new File("VIN_" + consentId + ".csv");

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(vin);
        } catch (IOException ex) {
            throw new RuntimeException("Error writing vins to file" + file.getAbsolutePath());
        }

        Response resp = RestHelper
                .putFile("Add vin number to Consent", url, token, file, "text/csv", "x-cons-addvins-" + consentId);
        CleanUpHelper.addToVinsList(consentId, vin);
        file.delete();
        Assertions.assertEquals(200, resp.getStatusCode(),
                "Error during adding vin to consent:" + vin);
    }

    public String addCMConsumer() {
        String url = NS_Config.URL_EXTERNAL_CM + "/consumers";
        String authToken = "Bearer " + CM_CONSUMER.getUser().getToken();
        String consumerId = "6be8790a2dda68105815cde7fbcf45b55062a779";
        String clientId = "88440bf1-2fff-42b6-8f99-0510b6b5e6f8";
        String clientSecret = "2d839912-c5e6-4cfb-8543-9a1bed38efe6";
        String body = "{\n"
                + "  \"clientId\": \"" + clientId + "\",\n"
                + "  \"clientSecret\": \"" + clientSecret + "\",\n"
                + "  \"consumerId\": \"" + consumerId + "\",\n"
                + "  \"consumerName\": \"Exelsior\",\n"
                + "  \"openIdAuthUrl\": \"https://api.secure.mercedes-benz.com/oidc10/auth/oauth/v2/authorize\",\n"
                + "  \"openIdTokenUrl\": \"https://api.secure.mercedes-benz.com/oidc10/auth/oauth/v2/token\",\n"
                + "  \"responseType\": \"id_token\"\n"
                + "}";
        Response r = RestHelper.put("Create CM Consumer", url, authToken, body);
        Assertions.assertEquals(200, r.getStatusCode(),
                "Create CM Consumer response not as expected");
        return consumerId;
    }

    public String addCMProvider() {
        String url = NS_Config.URL_EXTERNAL_CM + "/providers";
        String authToken = "Bearer " + PROVIDER.getUser().getToken();
        String providerId = "olp-sit-mrkt-p0";
        String clientId = "88440bf1-2fff-42b6-8f99-0510b6b5e6f8";
        String clientSecret = "2d839912-c5e6-4cfb-8543-9a1bed38efe6";
        String body = "{\n"
                + "  \"authUrl\": \"https://api.secure.mercedes-benz.com/oidc10/auth/oauth/v2/authorize\",\n"
                + "  \"clientId\": \"" + clientId + "\",\n"
                + "  \"clientSecret\": \"" + clientSecret + "\",\n"
                + "  \"providerId\": \"" + providerId + "\",\n"
                + "  \"providerName\": \"Industrial\",\n"
                + "  \"tokenUrl\": \"https://api.secure.mercedes-benz.com/oidc10/auth/oauth/v2/token\"\n"
                + "}";
        Response r = RestHelper.put("Create CM Provider", url, authToken, body);
        Assertions.assertEquals(201, r.getStatusCode(),
                "Create CM Consumer response not as expected");
        return providerId;
    }

    public Response approveConsentRequestNew(String consentRequestId, String vinNumber,
            String authToken, Container container) {
        String authCode = container.getDataProviderName().equals(Providers.REFERENCE_PROVIDER.getName()) || container
                .getDataProviderName().equals(
                        Providers.DAIMLER_REFERENCE.getName()) ?
                ReferenceTokenController
                        .produceConsentAuthCode(vinNumber, container.getId() + ":" + container.getResourceNames()) :
                DaimlerTokenController.produceConsentAuthCode(
                        PropertiesLoader.getInstance().mainProperties.getProperty("daimler.clientId"),
                        PropertiesLoader.getInstance().mainProperties.getProperty("daimler.clientSecret"),
                        new String(Base64.getDecoder().decode(NS_Config.DAIMLER_API_LOGIN.toString())),
                        new String(Base64.getDecoder().decode(NS_Config.DAIMLER_API_PASS.toString())),
                        PropertiesLoader.getInstance().mainProperties.getProperty("daimler.callbackurl"));

        String url = NS_Config.URL_EXTERNAL_CM + "/consents/approve";
        String body = "{\n"
                + "  \"authorizationCode\": \"" + authCode + "\",\n"
                + "  \"consentRequestId\": \"" + consentRequestId + "\",\n"
                + "  \"vinHash\": \"" + ConsentManagerHelper.getSHA512(vinNumber) + "\"\n"
                + "}";
        return RestHelper
                .put("Approve Consent Campaign Id:" + consentRequestId, url, authToken, body,
                        new Header("X-Correlation-ID", "x-cons-appr-" + consentRequestId));
    }

    public Response revokeConsentRequest(String consentRequestId, String vinNumber,
            String authToken) {
        String url = NS_Config.URL_EXTERNAL_CM +
                "/consents/revoke?consentRequestId=" + consentRequestId + "&vin=" + vinNumber;
        String body = "{\n"
                + "  \"consentRequestId\": \"" + consentRequestId + "\",\n"
                + "  \"vinHash\": \"" + ConsentManagerHelper.getSHA512(vinNumber) + "\"\n"
                + "}";
        return RestHelper.put(
                "Revoke Consent Campaign Id:" + consentRequestId + " for VIN:" + vinNumber,
                url, authToken, body, new Header("X-Correlation-ID", "x-cons-rev-" + consentRequestId));
    }

    public boolean isConsentApproved(String consentRequestId) {
        if (!NS_Config.CONSENT_MOCK.toString().equalsIgnoreCase("true")) {
            String token = "Bearer " + CM_CONSUMER.getUser().getToken();
            String url =
                    NS_Config.URL_EXTERNAL_CM + "/consentRequests/" + consentRequestId + "/status";
            Response res = RestHelper
                    .get("Get status of Consent Campaign Id:" + consentRequestId, url, token);
            return !res.getBody().jsonPath().getString("approved").equalsIgnoreCase("0");
        } else {
            return true;
        }
    }

    public Response deleteCMCache() {
        String token = "Bearer " + CM_CONSUMER.getUser().getToken();
        String url = NS_Config.URL_EXTERNAL_CM + "/cache/ns/clear";
        return RestHelper.delete("Clear scope cache on CM side", url, token);
    }

    public Response getOAuthState(String consentId) {
        String token = "Bearer " + CM_CONSUMER.getUser().getToken();
        String url = NS_Config.URL_EXTERNAL_CM + "/providers/oauth?consentRequestId=" + consentId;
        return RestHelper.get("Get OAuth state for consent", url, token);
    }

    public void addCMApplication(Container container, String providerId) {

        String authToken = "Bearer " + PROVIDER.getUser().getToken();
        String consumerId = CONSUMER.getUser().getRealm();
        String provider = providerId.equals(Providers.DAIMLER_REFERENCE.getName()) || providerId
                .equals(Providers.REFERENCE_PROVIDER.getName()) ? "exelsior" : "daimler";
        String clientId = PropertiesLoader.getInstance().mainProperties.getProperty(provider + ".clientId");
        String clientSecret = PropertiesLoader.getInstance().mainProperties.getProperty(provider + ".clientSecret");
        String redirectUri = PropertiesLoader.getInstance().mainProperties.getProperty(provider + ".callbackurl");
        String url = NS_Config.URL_EXTERNAL_CM + "/providers/applications";
        String body = "{\n"
                + "    \"providerId\": \"" + providerId + "\",\n"
                + "    \"consumerId\": \"" + consumerId + "\",\n"
                + "    \"container\": \"" + container.getId() + "\",\n"
                + "    \"clientId\": \"" + clientId + "\",\n"
                + "    \"clientSecret\": \"" + clientSecret + "\",\n"
                + "    \"redirectUri\": \"" + redirectUri + "\"\n"
                + "  }";
        Response r = RestHelper.post("Add CM Application for container " + container.getId(), url, authToken, body);
        Assertions.assertEquals(201, r.getStatusCode(),
                "Create CM Application response not as expected");
    }

    public void deleteConsentHard(String consentId) {
        String token = "Bearer " + MP_CONSUMER.getUser().getToken();
        String url = NS_Config.URL_EXTERNAL_CM + "/private/consentRequest/" + consentId;
        Response r = RestHelper.delete("Hard delete consent from CM " + consentId, url, token);
    }

    public void deleteApplicationHard(String providerId, String containerId) {
        if (containerId.contains(Containers.getContainerNamePrefix())) {
            String consumerId = CONSUMER.getUser().getRealm();
            String token = "Bearer " + MP_CONSUMER.getUser().getToken();
            String url = NS_Config.URL_EXTERNAL_CM + "/private/providerApplication?"
                    + "providerId=" + providerId
                    + "&consumerId=" + consumerId
                    + "&container=" + containerId;
            Response r = RestHelper
                    .delete("Hard delete Application from CM " + containerId, url, token);
        }
    }

    public void deleteVinsHard(String consentId, String vin) {
        String token = "Bearer " + MP_CONSUMER.getUser().getToken();
        String url = NS_Config.URL_EXTERNAL_CM + "/consentRequests/" + consentId + "/removeAllDataSubjects";
        File file = new File("VIN_" + consentId + ".csv");

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(vin);
        } catch (IOException ex) {
            throw new RuntimeException("Error writing vins to file" + file.getAbsolutePath());
        }

        Response resp = RestHelper
                .putFile("Hard delete vins from CM ", url, token, file, "text/csv", "x-corr-" + consentId);
        file.delete();

    }

    public void deleteAllAutomatedConsentsHard() {
        String token = "Bearer " + CM_CONSUMER.getUser().getToken();
        for (String vin : new String[]{Vehicle.validVehicleIdLong, Vehicle.validVehicleId}) {
            String url = NS_Config.URL_EXTERNAL_CM
                    + "/consentRequests?consumerId=" + CONSUMER.getUser().getRealm() + "&vin=" + vin;
            Response r = RestHelper.get("get all consent from CM for " + vin, url, token);
            r.jsonPath().getList(
                    "findAll { it.containerId.startsWith('automatedtestcontainer')}.consentRequestId",
                    String.class)
                    .forEach(id -> {
                        new ConsentManagementCall().deleteVinsHard(id, vin);
                        new ConsentManagementCall().deleteConsentHard(id);
                    });
        }
    }

    public Response getConsentInfo(String vin) {
        String url = NS_Config.URL_EXTERNAL_CM + "/consents/" + vin + "/info?state=PENDING";
        return RestHelper.get("Get consent info for " + vin, url, "");
    }

}
