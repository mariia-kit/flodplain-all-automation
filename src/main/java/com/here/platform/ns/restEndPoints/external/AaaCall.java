package com.here.platform.ns.restEndPoints.external;

import static com.here.platform.ns.dto.Users.MP_PROVIDER;
import static io.restassured.RestAssured.given;

import com.fasterxml.jackson.databind.JsonNode;
import com.here.platform.common.config.Conf;
import com.here.platform.ns.dto.Container;
import com.here.platform.ns.dto.ProviderResource;
import com.here.platform.ns.dto.Users;
import com.here.platform.ns.helpers.AllureRestAssuredCustom;
import com.here.platform.ns.helpers.CleanUpHelper;
import io.qameta.allure.Step;
import io.restassured.path.json.JsonPath;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.http.HttpStatus;


public class AaaCall {

    private static final String prodPolicy = "POLICY-c6be1029-573c-4d66-903e-e6be57178c45";

    public List<MutablePair<String, String>> getAllContainersPolicy() {
        String url = Conf.ns().getAuthUrlBase() + "/search/policy?serviceId=" + Conf.nsUsers().getAaService().getAppId();

        List<MutablePair<String, String>> policy = new ArrayList<>();

        JsonPath res = given()
                .headers("Content-Type", "application/json",
                        "Authorization", "Bearer " + Users.AAA.getToken())
                .when().get(url)
                .then()
                .extract().response().getBody().jsonPath();

        int total = res.getInt("total");
        int iterations = total / 100 + (total % 100 > 0 ? 1 : 0);
        parseData(res, policy);

        for (int i = 1; i <= iterations; i++) {
            res = given()
                    .headers("Content-Type", "application/json",
                            "Authorization", "Bearer " + Users.AAA.getToken())
                    .when().get(url + "&startIndex=" + (i * 100))
                    .then()
                    .extract().response().getBody().jsonPath();
            parseData(res, policy);
        }

        policy.forEach(p -> System.out.println(">" + p.getLeft() + " - " + p.getRight()));
        return policy;
    }

    private void parseData(JsonPath res, List<MutablePair<String, String>> policy) {
        List<JsonNode> data = res.getList("data", JsonNode.class);
        data.forEach(p -> {
            String policyId = p.get("id").toString().replace("\"", "");
            p.get("permissions").findValues("resource").stream()
                    .map(val -> val.toString().replace("\"", ""))
                    .forEach(policyRes ->
                            policy.add(new MutablePair(policyId, policyRes)));
        });
    }

    public void deletePolicy(String id) {
        String url = Conf.ns().getAuthUrlBase() + "/policy/" + id;
        given()
                .headers("Content-Type", "application/json",
                        "Authorization", "Bearer " + Users.AAA.getToken())
                .when().delete(url)
                .then()
                .extract().response();
    }

    public void wipeAllPolicies(String query) {
        String groupId = Conf.nsUsers().getConsumerGroupId();
        List<MutablePair<String, String>> policy = getAllContainersPolicy();
        Map<String, String> pLinks = getAllPolicyLink(groupId);
        policy.stream()
                .filter(p -> p.getRight().contains("hrn:here-dev:neutral::" + Users.MP_PROVIDER.getUser().getRealm()))
                .filter(p -> p.getRight().toLowerCase().contains(query.toLowerCase()))
                .parallel()
                .forEach(p -> {
                    String policyLink = pLinks.get(p.getLeft());
                    System.out.println("try policy " + p.getLeft() + " -> " + p.getRight());
                    if (policyLink != null) {
                        removeGroupFromPolicy(groupId, policyLink);
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.println("delete policy " + p.getLeft() + " -> " + p.getRight());
                        deletePolicy(p.getLeft());
                    } else {
                        System.out.println("HARD delete policy required " + p.getLeft() + " -> " + p.getRight());
                        deletePolicy(p.getLeft());
                    }

                });
    }

    public void deletePolicyForContainer(Container container) {
        List<MutablePair<String, String>> policy = getAllContainersPolicy();
        policy.stream()
                .filter(e -> e.getRight().contains(container.getName()))
                .findFirst()
                .ifPresent(id -> new AaaCall().deletePolicy(id.getLeft()));
    }

    public void deletePolicyForHrn(String hrn) {
        List<MutablePair<String, String>> policy = getAllContainersPolicy();
        policy.stream()
                .filter(e -> e.getRight().contains(hrn))
                .findFirst()
                .ifPresent(id -> new AaaCall().deletePolicy(id.getLeft()));
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
                        "Authorization", "Bearer " + Users.AAA.getToken())
                .filter(new AllureRestAssuredCustom("Add group to Policy:" + policyId))
                .when()
                .body(body)
                .post(url)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().response().jsonPath().get("policies.find {it.policyId == '" + policyId + "'}.id");
    }

    public void removeGroupFromPolicy(String groupId, String policyLinkId) {
        String url = Conf.ns().getAuthUrlBase() + "/group/" + groupId + "/policies/" + policyLinkId;
        given()

                .headers("Authorization", "Bearer " + Users.AAA.getToken())
                .when()
                .delete(url)
                .then()
                .extract().response();
    }

    public void createResourcePermission(Container container) {
        createResourcePermission(container.generateHrn(Conf.ns().getRealm(), MP_PROVIDER.getUser().getRealm()));
    }

    public void createResourcePermissionForResource(ProviderResource providerResource, String providerName) {
        createResourcePermission(providerResource.generateHrn(providerName));
    }

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
        CleanUpHelper.getArtificialPolicy().put(policyId, policyLink);
        try {
            Thread.sleep(60000);
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

    @Step("Create policy for {container.name} with resource {res.name}")
    public void createContainerPolicyWithRes(Container container, ProviderResource res) {
        createResourcePermissionForResource(res, container.getDataProviderName());
    }

    @Step("Update policy for {container.name}")
    public void createContainerPolicyWithGeneralAccess(Container container) {
        String resHrn = new ProviderResource("general").generateGeneralResourceHrn(container.getDataProviderName());
        createResourcePermission(resHrn);
    }

    public String createPolicy(String resHrn) {
        String url = Conf.ns().getAuthUrlBase() + "/policy";
        String body = "{\n"
                + "    \"name\": \"NS_CONTAINER Policy\",\n"
                + "    \"scope\": {\n"
                + "        \"serviceId\": \"" + Conf.nsUsers().getAaService().getAppId() + "\"\n"
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
                        "Authorization", "Bearer " + Users.AAA.getToken())
                .filter(new AllureRestAssuredCustom("Create AA Policy " + resHrn))
                .when()
                .body(body)
                .post(url)
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract().response().jsonPath().get("id");
    }

    public String createAdminPolicy() {
        String url = Conf.ns().getAuthUrlBase() + "/policy";
        String body = "{\n"
                + "    \"name\": \"NS Provider Admin Policy\",\n"
                + "    \"scope\": {\n"
                + "        \"serviceId\": \"" + Conf.nsUsers().getAaService().getAppId() + "\"\n"
                + "    },\n"
                + "    \"permissions\": [\n"
                + "      {\n"
                + "         \"effect\": \"allow\",\n"
                + "         \"action\": \"admin\"\n"
                + "      }\n"
                + "    ],\n"
                + "    \"allowUpdates\": true\n"
                + "}";
        return given()

                .headers("Content-Type", "application/json",
                        "Authorization", "Bearer " + Users.AAA.getToken())
                .when()
                .body(body)
                .post(url)
                .then()
                .extract().response().jsonPath().get("id");
    }

    public String getPolicy(String policyId) {
        String url = Conf.ns().getAuthUrlBase() + "/policy/" + policyId;

        return given()
                .headers(
                        "Content-Type", "application/json",
                        "Authorization", "Bearer " + Users.AAA.getToken()
                )
                .when()
                .get(url)
                .asString();
    }

    public String getPolicyLink(String groupId, String policyId) {
        String url = Conf.ns().getAuthUrlBase() + "/group/" + groupId;
        return given()

                .headers("Content-Type", "application/json",
                        "Authorization", "Bearer " + Users.AAA.getToken())
                .when()
                .get(url)
                .then()
                .extract().response().jsonPath().get("policies.find {it.policyId == '" + policyId + "'}.id");
    }

    public Map<String, String> getAllPolicyLink(String groupId) {
        String url = Conf.ns().getAuthUrlBase() + "/group/" + groupId;
        JsonPath responce = given()

                .headers("Content-Type", "application/json",
                        "Authorization", "Bearer " + Users.AAA.getToken())
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
            if (serviceId.equals(Conf.nsUsers().getAaService().getAppId())) {
                res.put(policyId, linkId);
            }
        });
        return res;
    }

}
