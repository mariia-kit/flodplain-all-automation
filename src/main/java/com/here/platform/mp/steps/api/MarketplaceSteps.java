package com.here.platform.mp.steps.api;

import static com.here.platform.ns.dto.Users.MP_CONSUMER;
import static com.here.platform.ns.dto.Users.MP_PROVIDER;

import com.here.platform.cm.steps.remove.DataForRemoveCollector;
import com.here.platform.common.config.Conf;
import com.here.platform.common.strings.SBB;
import com.here.platform.ns.dto.Container;
import com.here.platform.ns.helpers.CleanUpHelper;
import com.here.platform.ns.helpers.resthelper.RestHelper;
import com.here.platform.ns.restEndPoints.NeutralServerResponseAssertion;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import java.util.Random;
import java.util.function.Supplier;
import java.util.zip.DataFormatException;
import javax.xml.crypto.Data;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;


public class MarketplaceSteps {

    private static final String baseMpUrl = Conf.mp().getMarketplaceUrl();

    @Step("Create new Listing {container.name}")
    public String createNewListing(Container container) {
        String RES_REALM = Conf.ns().getRealm();
        NeutralServerResponseAssertion listing = createListing(container, RES_REALM)
                .expectedCode(HttpStatus.SC_OK);
        String hrn = listing.getResponse().getBody().jsonPath()
                .get("resourceId").toString();
        int taskId = listing.getResponse().getBody().jsonPath()
                .getInt("taskId");
        waitForAsyncTask(taskId, () -> "Bearer " + MP_PROVIDER.getUser().getToken());
        String invite = inviteConsumer(hrn);
        inviteClicked(invite);
        return hrn;
    }

    @Step("Create Subscription for listing:{listingHrn}")
    public String subscribeListing(String listingHrn) {
        String subsId = subscribeStart(listingHrn);
        negotiate(subsId);
        ack_prov(subsId);
        int taskId = ack_cons(subsId);
        waitForAsyncTask(taskId, () -> "Bearer " + MP_CONSUMER.getToken());
        mpDelay();
        return subsId;
    }

    public String fullFlowContainer(Container container) {
        String listingHrn = createNewListing(container);
        String subsId = subscribeListing(listingHrn);
        if (container.getName().contains("deactivated")) {
            beginCancellation(subsId);
        }
        return listingHrn;
    }

    @Step("Create new listing for container {container.id} of provider {container.dataProviderName}")
    public NeutralServerResponseAssertion createListing(Container container, String resRealm) {
        Random r = new Random();
        String containerTitle = String.format("[NS] Container Listing %s %s", container.getName(), r.nextInt(10000));
        String providerToken = "Bearer " + MP_PROVIDER.getUser().getToken();
        String url = baseMpUrl + "/listings";
        String body = "{\n" +
                "  \"visibility\": \"PRIVATE\",\n" +
                "  \"state\": \"ACTIVE\",\n" +
                "  \"title\": \"" + containerTitle + "\",\n" +
                "  \"description\": \"" + container.getName() + " resource items\",\n" +
                "  \"comment\": \"Test listing\",\n" +
                "  \"suggestedUse\": \"1\",\n" +
                "  \"additionalReference\": \"1\",\n" +
                "  \"topic\": \"AUTOMOTIVE\",\n" +
                "  \"tags\": [\n" +
                "    \"test\"\n" +
                "  ],\n" +
                "  \"type\": \"NEUTRAL_SERVER\",\n" +
                "  \"visibleCompanyName\": false,\n" +
                "  \"visibleCoverageMap\": false,\n" +
                "  \"visibleResourceFiles\": false,\n" +
                "  \"visibleDataProps\": false,\n" +
                "  \"listItems\": [\n" +
                "    {\n" +
                "      \"title\": \"\",\n" +
                "      \"summary\": \"\",\n" +
                "      \"description\": \"\",\n" +
                "      \"resourceHrn\": \"" + container.generateHrn(resRealm, MP_PROVIDER.getUser().getRealm()) + "\"\n"
                +
                "    }\n" +
                "  ],\n" +
                "  \"subscriptionOptions\":[\n"
                + "{\n"
                + "\"type\":\"CUSTOMIZED\",\n"
                + "\"name\":\"Contact us\",\n"
                + "\"resourceHrn\":\"" + container.generateHrn() + "\",\n"
                + "\"description\":\"Get a customized subscription that meets your needs. Terms are negotiated outside the Marketplace.\",\n"
                + "\"keyTerms\":[\n"
                + "\"Negotiable\"\n"
                + "],\n"
                + "\"termsLink\":\"https://aaa.com\",\n"
                + "\"lifecycleState\":\"ACTIVE\",\n"
                + "\"renewable\":false,\n"
                + "\"pricingType\":\"USAGE_BASED\"\n"
                + "}\n"
                + "]," +
                "\"notifications\":{\n"
                + "\"recipientType\":\"OWNER\"\n"
                + "}" +
                "}";
        Response resp = RestHelper
                .post("Create new Listing " + containerTitle, url, providerToken, body);
        if (resp.getStatusCode() == HttpStatus.SC_OK) {
            String hrn = resp.getBody().jsonPath().get("resourceId").toString();
            DataForRemoveCollector.addMpListing(hrn);
        }
        return new NeutralServerResponseAssertion(resp);
    }

    @Step("Request access to subscription {subsId}")
    private Response req_access(String subsId) {
        String providerToken = "Bearer " + MP_CONSUMER.getUser().getToken();
        String url = baseMpUrl + "/subscriptions/" + subsId + "/access";
        String body = "{\"message\":\"asd\"}";
        return RestHelper
                .post("Request access on subscription " + subsId, url, providerToken, body);

    }

    @Step("Get listing by hrn {hrn}")
    private Response getListingByHRN(String hrn) {
        String providerToken = "Bearer " + MP_PROVIDER.getUser().getToken();
        String url = baseMpUrl + "/listings/" + hrn + "/asProvider";
        return RestHelper
                .get("Get listing by hrn " + hrn, url, providerToken);
    }

    @Step("Start subscription for listing {listingHrn}")
    public String subscribeStart(String listingHrn) {
        Response listing = getListingByHRN(listingHrn);
        String subsOptionId = listing.jsonPath().getString("subscriptionOptions[0].id");

        String providerToken = "Bearer " + MP_CONSUMER.getUser().getToken();
        String url = baseMpUrl + "/listings/" + listingHrn + "/access";
        String body = "{\"message\":\"asd\",\"subscriptionOptionId\":" + subsOptionId + "}";
        Response resp = RestHelper
                .post("Request subscription for hrn: " + listingHrn, url, providerToken, body);
        Assertions.assertEquals(200, resp.getStatusCode(), "Subscription to listing failed!");
        String subsId = resp.getBody().jsonPath().get("resourceId").toString();
        DataForRemoveCollector.addMpSubs(subsId);
        return subsId;
    }

    @Step("Negotiate for subscription {subsId}")
    private void negotiate(String subsId) {
        String providerToken = "Bearer " + MP_PROVIDER.getUser().getToken();
        String url = baseMpUrl + "/subscriptions/" + subsId + "/negotiate";
        String body = "{}";
        RestHelper.post("Negotiate for subscription : " + subsId, url, providerToken, body);
    }

    @Step("Acknowledge subscription {subsId} by Provider")
    private void ack_prov(String subsId) {
        String providerToken = "Bearer " + MP_PROVIDER.getUser().getToken();
        String url = baseMpUrl + "/subscriptions/" + subsId + "/ack";
        String body = "{}";

        Response resp = RestHelper
                .post("Acknowledge subscription by Provider : " + subsId, url, providerToken, body);
        Assertions
                .assertEquals(HttpStatus.SC_OK, resp.getStatusCode(), "Acknowledge subscription {subsId} by Provider");
    }

    @Step("Acknowledge subscription {subsId} by Consumer")
    private int ack_cons(String subsId) {
        String providerToken = "Bearer " + MP_CONSUMER.getUser().getToken();
        String url = baseMpUrl + "/subscriptions/" + subsId + "/ack";
        String body = "{}";
        Response resp = RestHelper.post("Acknowledge subscription terms by Consumer : "
                + subsId, url, providerToken, body);
        Assertions.assertEquals(HttpStatus.SC_OK, resp.getStatusCode(),
                "Acknowledge subscription {subsId} by Consumer failed!");
        return resp.jsonPath().getInt("taskId");
    }

    @Step("Get State of subscription {subsId}")
    public Response getSubsState(String subsId) {
        String providerToken = "Bearer " + MP_PROVIDER.getUser().getToken();
        String url = baseMpUrl + "/subscriptions/" + subsId + "/asProvider";
        return RestHelper.get("Get subs state: " + subsId, url, providerToken);
    }

    @Step("Begin cancellation of subscription {subsId}")
    public NeutralServerResponseAssertion beginCancellation(String subsId) {
        String providerToken = "Bearer " + MP_PROVIDER.getUser().getToken();
        String url = baseMpUrl + "/subscriptions/" + subsId
                + "/beginCancellation?duration=1";
        String body = "{}";
        Response response = RestHelper
                .post("Cancel subscription: " + subsId, url, providerToken, body);
        if (response.getStatusCode() == HttpStatus.SC_PRECONDITION_FAILED) {
            return rejectSubscribtion(subsId);
        }
        waitForSubsStateCanceled(subsId);
        return new NeutralServerResponseAssertion(response);
    }

    @Step("Reject subscription {subsId}")
    public NeutralServerResponseAssertion rejectSubscribtion(String subsId) {
        String providerToken = "Bearer " + MP_PROVIDER.getUser().getToken();
        String url = baseMpUrl + "/subscriptions/" + subsId
                + "/reject";
        String body = "{\n"
                + "  \"message\": \"ManualProviderReject\"\n"
                + "}";
        Response response = RestHelper
                .post("Reject subscription: " + subsId, url, providerToken, body);
        return new NeutralServerResponseAssertion(response);
    }

    @Step("Delete Listing {listingHrn}")
    public NeutralServerResponseAssertion deleteListing(String listingHrn) {
        String providerToken = "Bearer " + MP_PROVIDER.getUser().getToken();
        String url = baseMpUrl + "/listings/" + listingHrn;
        Response response = RestHelper.delete("Delete listing: " + listingHrn, url, providerToken);
        return new NeutralServerResponseAssertion(response);
    }

    @Step("Invite Consumer to Listing {listingHrn}")
    private String inviteConsumer(String listingHrn) {
        String providerToken = "Bearer " + MP_PROVIDER.getUser().getToken();
        String url = baseMpUrl + "/listings/invites";
        String body = "{\n"
                + "  \"listingHrn\": \"" + listingHrn + "\",\n"
                + "  \"deliveryMethod\": \"EMAIL\",\n"
                + "  \"emails\": [\n"
                + "    \"" + MP_CONSUMER.getUser().getEmail() + "\"\n"
                + "  ],\n"
                + "  \"callbackUrl\": \"" + Conf.mp().getMarketplaceUiUrl() + "/consumer\"\n"
                + "}";
        Response resp = RestHelper
                .post("Invite Consumer " + MP_CONSUMER.getUser().getEmail() + " to Listing "
                                + listingHrn, url, providerToken,
                        body);
        return resp.getBody().jsonPath().get("[0].id");
    }

    @Step("Invite Consumer to Listing")
    private void inviteClicked(String inviteId) {
        String providerToken = "Bearer " + MP_CONSUMER.getUser().getToken();
        String url = baseMpUrl + "/listings/invites/" + inviteId + "/clicked";
        String body = "{}";
        Response resp = RestHelper
                .post("Consumer click invite with id " + inviteId, url, providerToken,
                        body);
        Assertions.assertEquals(HttpStatus.SC_OK, resp.getStatusCode(), "Submit of invitation failed!");
        mpDelayStep();
    }

    public void mpDelay() {
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void mpDelayStep() {
        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void mpDelayMinute() {
        try {
            Thread.sleep(1 * 60 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void waitForAsyncTask(int taskId, Supplier<String> token) {
        String url = baseMpUrl + "/tasks/" + taskId;
        int maxCount = 60;
        do {
            maxCount--;
            Response resp = RestHelper
                    .get("Wait for asynk task to complete " + taskId, url, token.get());
            if (resp.getStatusCode() == HttpStatus.SC_OK) {
                if (resp.jsonPath().getString("status").equals("SUCCESS")) {
                    return;
                }
                if (resp.jsonPath().getString("status").equals("FAILED")) {
                    throw new RuntimeException("Error creating subscription. Async status is FAILED");
                }
                try {
                    Thread.sleep(30 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                throw new RuntimeException(SBB.sbb("Error waiting for MP async task!").n()
                        .append(resp.getStatusCode()).n()
                        .append(resp.getBody().prettyPrint()).bld());
            }
        } while (maxCount > 1);
        throw new RuntimeException("Error waiting for MP async task " + taskId
                + " to complete! Status not success after wait for 30 min.");
    }

    @SneakyThrows
    public void waitForSubsStateCanceled(String subsId) {
        int maxCount = 30;
        do {
            maxCount--;
            Response resp = getSubsState(subsId);
            if (resp.getStatusCode() == HttpStatus.SC_OK) {
                if (resp.jsonPath().getString("subscription.state").equals("CANCELLED_BY_PROVIDER")) {
                    return;
                }
                Thread.sleep(10 * 1000);
            } else {
                throw new RuntimeException(SBB.sbb("Error waiting for MP cancel subs task!").n()
                        .append(resp.getStatusCode()).n()
                        .append(resp.getBody().prettyPrint()).bld());
            }
        } while (maxCount > 1);
        throw new RuntimeException("Error waiting for MP cancel subs task " + subsId
                + " to complete! Status not success after wait for 5 min.");
    }

    public int getIdOfAsyncTaskForList(String listingHrn, String token) {
        String url = baseMpUrl + "/tasks";
        Response resp = RestHelper
                .get("Get list of all async tasks on mp.", url, token);
        if (resp.getStatusCode() == HttpStatus.SC_OK) {
            Object idObj = resp.jsonPath().get("items.find {it.data.consumerSubscriptionResponse.listingHrn == '" + listingHrn + "'}.id");
            if (idObj != null && !StringUtils.isEmpty(idObj.toString())) {
                return Integer.parseInt(idObj.toString());
            }

        }
        throw new RuntimeException(String.format("Error getting list of async tasks. %s %s", listingHrn, resp.getStatusCode()));
    }

    public String getIdOfSubsFromAsyncTask(int taskId, String token) {
        String url = baseMpUrl + "/tasks/" + taskId;
        Response resp = RestHelper
                .get("Get list of all async tasks on mp.", url, token);
        if (resp.getStatusCode() == HttpStatus.SC_OK) {
            return resp.jsonPath().getString(
                    "data.consumerSubscriptionResponse.id");
        } else {
            throw new RuntimeException(String.format("Error getting async task. %s ", taskId));
        }
    }

    @Step("Delete all Provider data from MP.")
    public void providerCleanUp() {
        String providerToken = "Bearer " + MP_PROVIDER.getUser().getToken();
        String url = baseMpUrl + "/admin/providerCleanup";
        RestHelper.post("Delete all MP data", url, providerToken, "{\n"
                + "  \"resourcesToKeep\": []\n"
                + "}");
    }

}
