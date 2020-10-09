package com.here.platform.ns.restEndPoints.external;

import static com.here.platform.ns.dto.Users.CM_CONSUMER;
import static com.here.platform.ns.dto.Users.CONSUMER;
import static com.here.platform.ns.dto.Users.MP_CONSUMER;
import static com.here.platform.ns.dto.Users.PROVIDER;

import com.here.platform.aaa.DaimlerTokenController;
import com.here.platform.aaa.ReferenceTokenController;
import com.here.platform.common.DaimlerApp;
import com.here.platform.common.ResponseExpectMessages;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.config.Conf;
import com.here.platform.ns.dto.Container;
import com.here.platform.ns.dto.Containers;
import com.here.platform.ns.dto.Providers;
import com.here.platform.ns.dto.Vehicle;
import com.here.platform.ns.helpers.CleanUpHelper;
import com.here.platform.ns.helpers.ConsentManagerHelper;
import com.here.platform.ns.helpers.resthelper.RestHelper;
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
        String url = Conf.ns().getConsentUrl() + "/consentRequests";
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
                new ResponseExpectMessages(resp).expectedStatuesCode(StatusCode.CREATED));
        String consentId = resp.getBody().jsonPath().get("consentRequestId").toString();
        CleanUpHelper.getConsentIdsList().add(consentId);
        return consentId;
    }

    public void addVinNumbers(String consentId, String vin) {
        String token = "Bearer " + MP_CONSUMER.getUser().getToken();
        String url = Conf.ns().getConsentUrl() + "/consentRequests/" + consentId + "/addDataSubjects";
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

    public Response approveConsentRequestNew(String consentRequestId, String vinNumber,
            String authToken, Container container) {
        String authCode = container.getDataProviderName().equals(Providers.REFERENCE_PROVIDER.getName()) || container
                .getDataProviderName().equals(
                        Providers.DAIMLER_REFERENCE.getName()) ?
                ReferenceTokenController
                        .produceConsentAuthCode(vinNumber, container.getId() + ":" + container.getResourceNames()) :
                DaimlerTokenController.produceConsentAuthCode(
                        Conf.ns().getDaimlerApp().getClientId(),
                        Conf.ns().getDaimlerApp().getClientSecret(),
                        new String(Base64.getDecoder().decode(Conf.nsUsers().getDaimlerUser().getEmail())),
                        new String(Base64.getDecoder().decode(Conf.nsUsers().getDaimlerUser().getPass())),
                        Conf.ns().getDaimlerApp().getCallBackUrl());

        String url = Conf.ns().getConsentUrl() + "/consents/approve";
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
        String url = Conf.ns().getConsentUrl() +
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
        if (!Conf.ns().isConsentMock()) {
            String token = "Bearer " + CM_CONSUMER.getUser().getToken();
            String url =
                    Conf.ns().getConsentUrl() + "/consentRequests/" + consentRequestId + "/status";
            Response res = RestHelper
                    .get("Get status of Consent Campaign Id:" + consentRequestId, url, token);
            return !res.getBody().jsonPath().getString("approved").equalsIgnoreCase("0");
        } else {
            return true;
        }
    }

    public Response deleteCMCache() {
        String token = "Bearer " + CM_CONSUMER.getUser().getToken();
        String url = Conf.ns().getConsentUrl() + "/cache/ns/clear";
        return RestHelper.delete("Clear scope cache on CM side", url, token);
    }

    public Response getOAuthState(String consentId) {
        String token = "Bearer " + CM_CONSUMER.getUser().getToken();
        String url = Conf.ns().getConsentUrl() + "/providers/oauth?consentRequestId=" + consentId;
        return RestHelper.get("Get OAuth state for consent", url, token);
    }

    public void addCMApplication(Container container, String providerId) {
        String authToken = "Bearer " + PROVIDER.getUser().getToken();
        String consumerId = CONSUMER.getUser().getRealm();
        DaimlerApp app = Conf.ns().getDaimlerApp();
        if (providerId.equals(Providers.DAIMLER_REFERENCE.getName()) || providerId
                .equals(Providers.REFERENCE_PROVIDER.getName())) {
            app = Conf.ns().getReferenceApp();
        } else if (providerId.equals(Providers.BMW_TEST.getName()) || providerId.equals(Providers.BMW.getName())) {
            app = Conf.ns().getBmwApp();
        }

        String url = Conf.ns().getConsentUrl() + "/providers/applications";
        String body = "{\n"
                + "    \"providerId\": \"" + providerId + "\",\n"
                + "    \"consumerId\": \"" + consumerId + "\",\n"
                + "    \"container\": \"" + container.getId() + "\",\n"
                + "    \"clientId\": \"" + app.getClientId() + "\",\n"
                + "    \"clientSecret\": \"" + app.getClientSecret() + "\",\n"
                + "    \"redirectUri\": \"" + app.getCallBackUrl() + "\"\n"
                + "  }";
        Response r = RestHelper.post("Add CM Application for container " + container.getId(), url, authToken, body);
        Assertions.assertEquals(201, r.getStatusCode(),
                "Create CM Application response not as expected");
    }

    public void deleteConsentHard(String consentId) {
        String token = "Bearer " + MP_CONSUMER.getUser().getToken();
        String url = Conf.ns().getConsentUrl() + "/private/consentRequest/" + consentId;
        Response r = RestHelper.delete("Hard delete consent from CM " + consentId, url, token);
    }

    public void deleteApplicationHard(String providerId, String containerId) {
        if (containerId.contains(Containers.getContainerNamePrefix())) {
            String consumerId = CONSUMER.getUser().getRealm();
            String token = "Bearer " + MP_CONSUMER.getUser().getToken();
            String url = Conf.ns().getConsentUrl() + "/private/providerApplication?"
                    + "providerId=" + providerId
                    + "&consumerId=" + consumerId
                    + "&container=" + containerId;
            Response r = RestHelper
                    .delete("Hard delete Application from CM " + containerId, url, token);
        }
    }

    public void deleteVinsHard(String consentId, String vin) {
        String token = "Bearer " + MP_CONSUMER.getUser().getToken();
        String url = Conf.ns().getConsentUrl() + "/consentRequests/" + consentId + "/removeAllDataSubjects";
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

    public Response getConsentInfo(String vin) {
        String url = Conf.ns().getConsentUrl() + "/consents/" + vin + "/info?state=PENDING";
        return RestHelper.get("Get consent info for " + vin, url, "");
    }

}
