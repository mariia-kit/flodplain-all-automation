package com.here.platform.proxy.helper;

import static io.restassured.RestAssured.given;

import com.fasterxml.jackson.databind.JsonNode;
import com.here.platform.cm.steps.remove.DataForRemoveCollector;
import com.here.platform.common.config.Conf;
import com.here.platform.ns.dto.Users;
import com.here.platform.ns.helpers.CleanUpHelper;
import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.path.json.exception.JsonPathException;
import io.restassured.response.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.http.HttpStatus;


public class ProxyAAController {

    public List<MutablePair<String, String>> getAllContainersPolicy() {
        String url =
                Conf.ns().getAuthUrlBase() + "/search/policy?serviceId=" + Conf.proxy().getProxyApp().getAppId();

        List<MutablePair<String, String>> policy = new ArrayList<>();

        JsonPath res = given()
                .headers("Content-Type", "application/json",
                        "Authorization", "Bearer " + Users.PROXY_APP.getToken())
                .when().get(url)
                .then()
                .extract().response().getBody().jsonPath();

        int total = res.getInt("total");
        int iterations = total / 100 + (total % 100 > 0 ? 1 : 0);
        parseData(res, policy);

        for (int i = 1; i <= iterations; i++) {
            res = given()
                    .headers("Content-Type", "application/json",
                            "Authorization", "Bearer " + Users.PROXY_APP.getToken())
                    .when().get(url + "&startIndex=" + (i * 100))
                    .then()
                    .extract().response().getBody().jsonPath();
            parseData(res, policy);
        }
        return policy;
    }

    private void parseData(JsonPath res, List<MutablePair<String, String>> policy) {
        List<JsonNode> data = new ArrayList<>();
        try {
            data = res.getList("data", JsonNode.class);
        } catch (JsonPathException ex) {
            System.out.println("Error parsing response " + res.toString());
            return;
        }
        data.forEach(p -> {
            String policyId = p.get("id").toString().replace("\"", "");
            p.get("permissions").findValues("resource").stream()
                    .map(val -> val.toString().replace("\"", ""))
                    .forEach(policyRes ->
                            policy.add(new MutablePair(policyId, policyRes)));
        });
    }

    @Step("Delete policy {id}")
    public Response deletePolicy(String id) {
        String url = Conf.ns().getAuthUrlBase() + "/policy/" + id;
        return given()
                .headers("Content-Type", "application/json",
                        "Authorization", "Bearer " + Users.PROXY_APP.getToken())
                .when()
                .filters(new AllureRestAssured())
                .delete(url)
                .then()
                .extract().response();
    }

    @Step("Create artificial policy for resource hrn {resHrn}")
    public void createResourcePermission(String resHrn) {
        String policyId = createPolicy(resHrn);
        try {
            Thread.sleep(8000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        CleanUpHelper.getArtificialPolicy().put(policyId, StringUtils.EMPTY);
        String groupId = Conf.nsUsers().getConsumerGroupId();
        String policyLink = addGroupToPolicy(groupId, policyId);
        DataForRemoveCollector.addPolicy(policyId, policyLink);
        try {
            Thread.sleep(90000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void removeResourcePermission(String policyId, String policyLink) {
        String groupId = Conf.nsUsers().getConsumerGroupId();
        removeGroupFromPolicy(groupId, policyLink);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        deletePolicy(policyId);
    }

    public String addGroupToPolicy(String groupId, String policyId) {
        String url = Conf.ns().getAuthUrlBase() + "/group/" + groupId + "/policies";
        String body = "{\n"
                + "    \"policies\": [\n"
                + "        {\n"
                + "            \"policyId\": \"" + policyId + "\"\n"
                + "        }\n"
                + "    ]\n"
                + "}";
        return given()

                .headers("Content-Type", "application/json",
                        "Authorization", "Bearer " + Users.PROXY_APP.getToken())
                .when()
                .body(body)
                .post(url)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().response().jsonPath().get("policies.find {it.policyId == '" + policyId + "'}.id");
    }

    @Step("Remove group from policy {groupId} {policyLinkId}")
    public void removeGroupFromPolicy(String groupId, String policyLinkId) {
        String url = Conf.ns().getAuthUrlBase() + "/group/" + groupId + "/policies/" + policyLinkId;
        given()
                .headers("Authorization", "Bearer " + Users.PROXY_APP.getToken())
                .filters(new AllureRestAssured())
                .when()
                .delete(url)
                .then()
                .extract().response();
    }

    public String createPolicy(String resHrn) {
        String url = Conf.ns().getAuthUrlBase() + "/policy";
        String body = "{\n"
                + "    \"name\": \"EXT_SVC Policy\",\n"
                + "    \"scope\": {\n"
                + "        \"serviceId\": \"" + Conf.proxy().getProxyApp().getAppId() + "\"\n"
                + "    },\n"
                + "    \"permissions\": [\n"
                + "        {\n"
                + "            \"effect\": \"allow\",\n"
                + "            \"action\": \"readResource\",\n"
                + "            \"resource\": \"" + resHrn + "\"\n"
                + "        }\n"
                + "    ],\n"
                + "    \"allowUpdates\": true\n"
                + "}";
        return given()

                .headers("Content-Type", "application/json",
                        "Authorization", "Bearer " + Users.PROXY_APP.getToken())
                .when()
                .body(body)
                .post(url)
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract().response().jsonPath().get("id");
    }

    public Map<String, String> getAllPolicyLink(String groupId) {
        String url = Conf.ns().getAuthUrlBase() + "/group/" + groupId;
        JsonPath responce = given()

                .headers("Content-Type", "application/json",
                        "Authorization", "Bearer " + Users.PROXY_APP.getToken())
                .when()
                .get(url)
                .then()
                .extract().response().jsonPath();
        List<JsonNode> data = responce.getList("policies", JsonNode.class);
        Map<String, String> res = new HashMap<>();
        data.forEach(p -> {
            String serviceId = p.get("serviceId").toString().replace("\"", "");
            String policyId = p.get("policyId").toString().replace("\"", "");
            String linkId = p.get("id").toString().replace("\"", "");
            res.put(policyId, linkId);
        });
        return res;
    }

    @Step("Delete policy for hrn:{hrn}")
    public void wipeAllPolicies(String hrn) {
        String groupId = Conf.nsUsers().getConsumerGroupId();
        List<MutablePair<String, String>> policy = getAllContainersPolicy();
        Map<String, String> pLinks = getAllPolicyLink(groupId);
        policy.stream()
                .filter(p -> p.getRight().contains(hrn))
                .parallel()
                .forEach(p -> {
                    String policyLink = pLinks.get(p.getLeft());
                    if (policyLink != null) {
                        removeGroupFromPolicy(groupId, policyLink);
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        deletePolicy(p.getLeft());
                    } else {
                        Response del = deletePolicy(p.getLeft());
                        System.out.println("Del Policy " + del.getBody().print());
                    }

                });
    }
}
