package com.here.platform.ns.restEndPoints.external;

import static com.here.platform.ns.dto.Users.MP_CONSUMER;
import static com.here.platform.ns.dto.Users.MP_PROVIDER;

import com.here.platform.ns.dto.Container;
import com.here.platform.ns.helpers.CleanUpHelper;
import com.here.platform.ns.helpers.resthelper.RestHelper;
import com.here.platform.ns.restEndPoints.NeutralServerResponseAssertion;
import com.here.platform.ns.utils.NS_Config;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import java.util.Random;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;


public class MarketplaceManageListingCall {

    public String createNewListing(Container container) {
        String RES_REALM = NS_Config.REALM.toString();
        NeutralServerResponseAssertion listing = createListing(container, RES_REALM)
                .expectedCode(HttpStatus.SC_OK);
        String hrn = listing.getResponse().getBody().jsonPath().get("hrn");
        String invite = inviteConsumer(hrn);
        inviteClicked(invite);
        return hrn;
    }

    public String subscribeListing(String listingHrn) {
        String subsId = subscribeStart(listingHrn);
        negotiate(subsId);
        ack_prov(subsId);
        ack_cons(subsId);
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

    @Step("Create marketplace listing for container {container.name}")
    public NeutralServerResponseAssertion createListing(Container container, String resRealm) {
        Random r = new Random();
        String containerTitle = String
                .format("[NS] Container Listing %s %s", container.getName(),
                        r.nextInt(10000));
        String providerToken = "Bearer " + MP_PROVIDER.getUser().getToken();
        String url = NS_Config.URL_EXTERNAL_MARKETPLACE + "/listings";
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
            String hrn = resp.getBody().jsonPath().get("hrn");
            CleanUpHelper.getListingList().put(hrn, container.getName());
        }
        return new NeutralServerResponseAssertion(resp);
    }

    @Step("Request access to subscription {subsId}")
    private Response req_access(String subsId) {
        String providerToken = "Bearer " + MP_CONSUMER.getUser().getToken();
        String url = NS_Config.URL_EXTERNAL_MARKETPLACE + "/subscriptions/" + subsId + "/access";
        String body = "{\"message\":\"asd\"}";
        return RestHelper
                .post("Request access on subscription " + subsId, url, providerToken, body);

    }

    @Step("Get listing by hrn {hrn}")
    private Response getListingByHRN(String hrn) {
        String providerToken = "Bearer " + MP_PROVIDER.getUser().getToken();
        String url = NS_Config.URL_EXTERNAL_MARKETPLACE + "/listings/" + hrn + "/as_provider";
        return RestHelper
                .get("Get listing by hrn " + hrn, url, providerToken);
    }

    @Step("Start subscription for listing {listingHrn}")
    public String subscribeStart(String listingHrn) {
        Response listing = getListingByHRN(listingHrn);
        String subsOptionId = listing.jsonPath().getString("subscriptionOptions[0].id");

        String providerToken = "Bearer " + MP_CONSUMER.getUser().getToken();
        String url = NS_Config.URL_EXTERNAL_MARKETPLACE + "/listings/" + listingHrn + "/access";
        String body = "{\"message\":\"asd\",\"subscriptionOptionId\":" + subsOptionId + "}";
        Response resp = RestHelper
                .post("Request subscription for hrn: " + listingHrn, url, providerToken, body);
        if (resp.getStatusCode() == 400) {
            resp = RestHelper
                    .post("Request subscription for hrn try 2: " + listingHrn, url, providerToken, body);
        }
        Assertions.assertEquals(200, resp.getStatusCode(), "Subscription to listing failed!");
        String subsId = resp.getBody().jsonPath().get("subscriptions[0].id").toString();
        CleanUpHelper.getSubsList().add(subsId);
        return subsId;
    }

    @Step("Negotiate for subscription {subsId}")
    private void negotiate(String subsId) {
        String providerToken = "Bearer " + MP_PROVIDER.getUser().getToken();
        String url = NS_Config.URL_EXTERNAL_MARKETPLACE + "/subscriptions/" + subsId + "/negotiate";
        String body = "{}";
        RestHelper.post("Negotiate for subscription : " + subsId, url, providerToken, body);
    }

    @Step("Acknowledge subscription {subsId} by Provider")
    private void ack_prov(String subsId) {
        String providerToken = "Bearer " + MP_PROVIDER.getUser().getToken();
        String url = NS_Config.URL_EXTERNAL_MARKETPLACE + "/subscriptions/" + subsId + "/ack";
        String body = "{}";

        Response resp = RestHelper
                .post("Acknowledge subscription by Provider : " + subsId, url, providerToken, body);
        Assertions
                .assertEquals(HttpStatus.SC_OK, resp.getStatusCode(), "Acknowledge subscription {subsId} by Provider");
    }

    @Step("Acknowledge subscription {subsId} by Consumer")
    private void ack_cons(String subsId) {
        String providerToken = "Bearer " + MP_CONSUMER.getUser().getToken();
        String url = NS_Config.URL_EXTERNAL_MARKETPLACE + "/subscriptions/" + subsId + "/ack";
        String body = "{}";
        Response resp = RestHelper.post("Acknowledge subscription terms by Consumer : "
                + subsId, url, providerToken, body);
        Assertions.assertEquals(HttpStatus.SC_OK, resp.getStatusCode(),
                "Acknowledge subscription {subsId} by Consumer failed!");
    }

    @Step("Begin cancellation of subscription {subsId}")
    public NeutralServerResponseAssertion beginCancellation(String subsId) {
        String providerToken = "Bearer " + MP_PROVIDER.getUser().getToken();
        String url = NS_Config.URL_EXTERNAL_MARKETPLACE + "/subscriptions/" + subsId
                + "/cancel";
        String body = "{}";
        Response response = RestHelper
                .post("Cancel subscription: " + subsId, url, providerToken, body);
        if (response.getStatusCode() == HttpStatus.SC_PRECONDITION_FAILED) {
            return rejectSubscribtion(subsId);
        }
        mpDelay();
        return new NeutralServerResponseAssertion(response);
    }

    @Step("Reject subscription {subsId}")
    public NeutralServerResponseAssertion rejectSubscribtion(String subsId) {
        String providerToken = "Bearer " + MP_PROVIDER.getUser().getToken();
        String url = NS_Config.URL_EXTERNAL_MARKETPLACE + "/subscriptions/" + subsId
                + "/reject/as_provider";
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
        String url = NS_Config.URL_EXTERNAL_MARKETPLACE + "/listings/" + listingHrn;
        Response response = RestHelper.delete("Delete listing: " + listingHrn, url, providerToken);
        CleanUpHelper.getListingList().remove(listingHrn);
        return new NeutralServerResponseAssertion(response);
    }

    @Step("Invite Consumer to Listing {listingHrn}")
    private String inviteConsumer(String listingHrn) {
        String providerToken = "Bearer " + MP_PROVIDER.getUser().getToken();
        String url = NS_Config.URL_EXTERNAL_MARKETPLACE + "/listings/invites";
        String body = "{\n"
                + "  \"listingHrn\": \"" + listingHrn + "\",\n"
                + "  \"deliveryMethod\": \"EMAIL\",\n"
                + "  \"emails\": [\n"
                + "    \"" + MP_CONSUMER.getUser().getEmail() + "\"\n"
                + "  ],\n"
                + "  \"callbackUrl\": \"" + NS_Config.URL_EXTERNAL_MARKETPLACE_UI + "/consumer\"\n"
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
        String url = NS_Config.URL_EXTERNAL_MARKETPLACE + "/listings/invites/" + inviteId + "/clicked";
        String body = "{}";
        Response resp = RestHelper
                .post("Consumer click invite with id " + inviteId, url, providerToken,
                        body);
        Assertions.assertEquals(HttpStatus.SC_OK, resp.getStatusCode(), "Submit of invitation failed!");
        mpDelay();
    }

    public void mpDelay() {
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Step("Delete all Provider data from MP.")
    public void providerCleanUp() {
        String providerToken = "Bearer " + MP_PROVIDER.getUser().getToken();
        String url = NS_Config.URL_EXTERNAL_MARKETPLACE + "/admin/provider_cleanup";
        RestHelper.post("Delete all MP data", url, providerToken, "");
    }

}
