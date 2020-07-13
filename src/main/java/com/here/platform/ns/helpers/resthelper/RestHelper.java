package com.here.platform.ns.helpers.resthelper;

import static io.restassured.RestAssured.given;

import com.here.platform.ns.helpers.AllureRestAssuredCustom;
import com.here.platform.ns.restEndPoints.BaseRestControllerNS;
import io.qameta.allure.Step;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.http.HttpStatus;


public class RestHelper {

    private static ResponseSpecification responseSpec = new ResponseSpecBuilder()
            .log(LogDetail.ALL)
            .build();

    public static Headers generateHeaders(String token, List<Header> headers) {
        List<Header> list = new ArrayList<>();
        list.addAll(getBaseHeaders(token).asList());
        for (Header hd : headers) {
            list.add(hd);
        }
        return new Headers(list);
    }

    private static RequestSpecification getReqSpec(String requestName, String token) {
        return getReqSpec(requestName, getBaseHeaders(token));
    }

    private static RequestSpecification getReqSpec(String requestName, Headers headers) {
        return new RequestSpecBuilder()
                .log(LogDetail.ALL)
                .setUrlEncodingEnabled(false)
                .addHeaders(headers.asList().stream().collect(Collectors.toMap(
                        Header::getName, Header::getValue)))
                .addFilter(new AllureRestAssuredCustom(requestName))
                .build();
    }

    public static Headers getBaseHeaders(String token) {
        List<Header> list = new ArrayList<>();
        list.add(new Header("Content-Type", "application/json"));
        list.add(new Header("Authorization", token));
        list.add(new Header("Accept", "application/json"));
        return new Headers(list);
    }

    @Step("{requestName}")
    public static Response put(String requestName, String url, String token, String body, Header... header) {
        Headers additionalHeaders = new Headers(header);
        return RestHelper.gatewayWrapper(() -> given()
                .spec(getReqSpec(requestName, token))
                .headers(additionalHeaders)
                .when()
                .body(body)
                .put(url)
                .then()
                .spec(responseSpec)
                .extract().response());
    }

//    @Step("{requestName}")
//    public static Response put(String requestName, String url, String token, String body) {
//        return RestHelper.gatewayWrapper(() -> given()
//                .spec(getReqSpec(requestName, token))
//                .when()
//                .body(body)
//                .put(url)
//                .then()
//                .spec(responseSpec)
//                .extract().response());
//    }

    @Step("{requestName}")
    public static Response putFile(String requestName, String url, String token, File file, String mimeType, String xCorrId) {
        return RestHelper.gatewayWrapper(() -> given()
                .header("Content-Type","multipart/form-data")
                .header("Authorization", token)
                .header("X-Correlation-ID", xCorrId)
                .log().all()
                .multiPart("vins", file, mimeType)
                .filter(new AllureRestAssuredCustom(requestName))
                .when()
                .put(url)
                .then()
                .spec(responseSpec)
                .extract().response());
    }

    @Step("{call.callMessage}")
    public static Response put(BaseRestControllerNS call, String body) {
        return RestHelper.gatewayWrapper(() -> given()
                .spec(getReqSpec(call.getCallMessage(),
                        generateHeaders(call.getToken(), call.getHeaders())))
                .when()
                .body(body)
                .put(call.getEndpointUrl() + call.getQueryParams())
                .then()
                .spec(responseSpec)
                .extract().response());
    }

    @Step("{requestName}")
    public static Response get(String requestName, String url, String token) {
        return RestHelper.gatewayWrapper(() -> given()
                .spec(getReqSpec(requestName, token))
                .when()
                .redirects().follow(false)
                .get(url)
                .then()
                .spec(responseSpec)
                .extract().response());
    }

    @Step("{call.callMessage}")
    public static Response get(BaseRestControllerNS call) {
        return RestHelper.gatewayWrapper(() -> given()
                .spec(getReqSpec(call.getCallMessage(),
                        generateHeaders(call.getToken(), call.getHeaders())))
                .when()
                .redirects().follow(false)
                .get(call.getEndpointUrl() + call.getQueryParams())
                .then()
                .spec(responseSpec)
                .extract().response());
    }



    @Step("{requestName}")
    public static Response delete(String requestName, String url, String token) {
        return RestHelper.gatewayWrapper(() -> given()
                .spec(getReqSpec(requestName, token))
                .when().delete(url)
                .then()
                .spec(responseSpec)
                .extract().response());
    }

    @Step("{requestName}")
    public static Response post(String requestName, String url, String token, String body, Header... header) {
        Headers additionalHeaders = new Headers(header);
        return RestHelper.gatewayWrapper(() -> given()
                .spec(getReqSpec(requestName, token))
                .headers(additionalHeaders)
                .when()
                .body(body)
                .post(url)
                .then()
                .spec(responseSpec)
                .extract().response());
    }

    @Step("{call.callMessage}")
    public static Response post(BaseRestControllerNS call, String body) {
        return RestHelper.gatewayWrapper(() -> given()
                .spec(getReqSpec(call.getCallMessage(),
                        generateHeaders(call.getToken(), call.getHeaders())))
                .when()
                .body(body)
                .post(call.getEndpointUrl() + call.getQueryParams())
                .then()
                .spec(responseSpec)
                .extract().response());
    }

    @Step("{requestName}")
    public static Response patch(String requestName, String url, String token) {
        return given()
                .spec(getReqSpec(requestName, token))
                .when()
                .patch(url)
                .then()
                .spec(responseSpec)
                .extract().response();
    }


    public static Response gatewayWrapper(Supplier<Response> apiCall) {
        Response result = apiCall.get();
        if ((result.getStatusCode() == HttpStatus.SC_GATEWAY_TIMEOUT) ||
                result.getStatusCode() == HttpStatus.SC_BAD_GATEWAY) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return apiCall.get();
        }
        return result;
    }
}
