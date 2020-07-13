package com.here.platform.common;

import com.here.platform.cm.enums.CMErrorResponse;
import com.here.platform.cm.rest.model.ErrorResponse;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;


public class ResponseAssertion {

    public static String[] timeFieldsToIgnore = {
            "approveTime", "createTime", "expiredTime",
            "finishedAt", "startedAt", "vinHash"
    };
    private final Response targetResponse;

    public ResponseAssertion(Response targetResponse) {
        this.targetResponse = targetResponse;
    }

    @Step("Verify status code of call is equal to \"{expectedStatusCode}\"")
    public ResponseAssertion statusCodeIsEqualTo(StatusCode expectedStatusCode) {
        var statusCodeAssertionMessage = new ResponseExpectMessages(targetResponse)
                .expectedStatuesCode(expectedStatusCode);

        Assertions.assertThat(targetResponse.statusCode())
                .withFailMessage(statusCodeAssertionMessage)
                .isEqualTo(expectedStatusCode.code);
        return this;
    }

    /**
     * Assert possibility response body binding to target class
     *
     * @return Binded response body to target object
     */
    public <T> T bindAs(Class<T> expectedClass) {
        T convertedObject;
        try {
            convertedObject = targetResponse.as(expectedClass);
        } catch (Exception ex) {
            var assertionMessage = new ResponseExpectMessages(targetResponse)
                    .expectedResponseBodyClass(expectedClass);
            throw new AssertionError(assertionMessage);
        }
        return convertedObject;
    }

    public <T> T[] bindAsListOf(Class<T[]> expectedClass) {
        return bindAs(expectedClass);
    }

    @Step("Verify response object is equal to \r\n\"{expectedObject}\"")
    public void responseIsEqualToObject(Object expectedObject) {
        var objectUnderTest = bindAs(expectedObject.getClass());

        Assertions.assertThat(objectUnderTest).isEqualTo(expectedObject);
    }

    @Step("Verify response object is equal to \r\n\"{expectedObject}\" ignoring time fields.")
    public void responseIsEqualToObjectIgnoringTimeFields(Object expectedObject) {
        var objectUnderTest = bindAs(expectedObject.getClass());

        Assertions.assertThat(objectUnderTest)
                .isEqualToIgnoringGivenFields(expectedObject, timeFieldsToIgnore);
    }

    @Step
    public ErrorResponse expectedErrorResponse(CMErrorResponse expectedErrorType) {
        var actualErrorResponse = bindAs(ErrorResponse.class);
        var expectedErrorResponse = new ErrorResponse()
                .action(expectedErrorType.getAction())
                .code(expectedErrorType.getCode())
                .title(expectedErrorType.getTitle());
        Assertions.assertThat(actualErrorResponse).isEqualToIgnoringGivenFields(
                expectedErrorResponse,
                "cause", "correlationId", "status"
        );
        return actualErrorResponse;
    }

    public void responseIsEmpty() {
        Assertions.assertThat(targetResponse.asString()).isEmpty();
    }

    public void responseIsEmptyArray() {
        Assertions.assertThat(targetResponse.asString()).isEqualTo("[]");
    }

}
