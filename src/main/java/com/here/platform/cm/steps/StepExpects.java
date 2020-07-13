package com.here.platform.cm.steps;

import static org.hamcrest.Matchers.equalTo;

import com.here.platform.common.ResponseExpectMessages;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import io.restassured.response.Response;
import lombok.experimental.UtilityClass;
import org.hamcrest.Matchers;


@UtilityClass
class StepExpects {

    private void expectStatusCodeFor(StatusCode statusCode, Response targetResponse) {
        if (targetResponse == null) {
            return;
        }
        var errorMessage = new ResponseExpectMessages(targetResponse).expectedStatuesCode(statusCode);
        targetResponse.then().assertThat().statusCode(Matchers.describedAs(errorMessage, equalTo(statusCode.code)));
    }

    void expectCREATEDStatusCode(Response targetResponse) {
        expectStatusCodeFor(StatusCode.CREATED, targetResponse);
    }

    void expectOKStatusCode(Response targetResponse) {
        expectStatusCodeFor(StatusCode.OK, targetResponse);
    }

    void expectNOCONSTENTStatusCode(Response targetResponse) {
        expectStatusCodeFor(StatusCode.NO_CONTENT, targetResponse);
    }

}
