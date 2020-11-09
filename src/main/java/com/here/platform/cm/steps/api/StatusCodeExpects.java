package com.here.platform.cm.steps.api;

import static org.hamcrest.Matchers.equalTo;

import com.here.platform.common.ResponseExpectMessages;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import io.restassured.response.Response;
import lombok.experimental.UtilityClass;
import org.hamcrest.Matchers;


@UtilityClass
public class StatusCodeExpects {

    private void expectStatusCodeFor(StatusCode statusCode, Response targetResponse) {
        //ignore if forbidden to execute the request, for example deleteProvider, dataConsumer
        if (targetResponse == null) {
            return;
        }
        var errorMessage = new ResponseExpectMessages(targetResponse).expectedStatuesCode(statusCode);
        targetResponse.then().assertThat().statusCode(Matchers.describedAs(errorMessage, equalTo(statusCode.code)));
    }

    public Response expectCREATEDStatusCode(Response targetResponse) {
        expectStatusCodeFor(StatusCode.CREATED, targetResponse);
        return targetResponse;
    }

    public Response expectOKStatusCode(Response targetResponse) {
        expectStatusCodeFor(StatusCode.OK, targetResponse);
        return targetResponse;
    }

    public Response expectNOCONSTENTStatusCode(Response targetResponse) {
        expectStatusCodeFor(StatusCode.NO_CONTENT, targetResponse);
        return targetResponse;
    }

}
