package com.here.platform.cm.consentStatus;

import com.here.platform.cm.enums.CMErrorResponse;
import com.here.platform.cm.rest.model.ConsentInfo;
import com.here.platform.cm.rest.model.ConsentInfo.StateEnum;
import com.here.platform.cm.steps.api.RemoveEntitiesSteps;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.VIN;
import io.qameta.allure.Issue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


@DisplayName("Get Consent Info")
public class GetConsentInfoTests extends BaseConsentStatusTests {

    private String crid;

    @BeforeEach
    void beforeEach() {
        crid = createValidConsentRequest();
    }

    @AfterEach
    void cleanUp() {
        RemoveEntitiesSteps.cascadeForceRemoveConsentRequest(crid, testFileWithVINs, testConsentRequestData);
    }

    @Test
    @Issue("NS-1706")
    @DisplayName("Forbidden to get consent info without token")
    void getConsentInfoWithoutTokenTest() {
        var response = consentStatusController
                .getConsentRequestInfoByVinAndCrid(testVin, crid, "");

        new ResponseAssertion(response).statusCodeIsEqualTo(StatusCode.UNAUTHORIZED)
                .expectedErrorResponse(CMErrorResponse.TOKEN_VALIDATION);
    }

    @Test
    @Issue("NS-1706")
    @DisplayName("Get consent info")
    void getConsentInfoTest() {
        var privateBearer = vehicle.getBearerToken();
        var response = consentStatusController
                .getConsentRequestInfoByVinAndCrid(testVin, crid, privateBearer);

        new ResponseAssertion(response).statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObjectIgnoringTimeFields(new ConsentInfo()
                        .consentRequestId(crid)
                        .consumerName(mpConsumer.getConsumerName())
                        .containerName(testContainer.name)
                        .containerDescription(testContainer.containerDescription)
                        .resources(testContainer.resources) //for CONNECTED VEHICLE container
                        .purpose(testConsentRequestData.getPurpose())
                        .title(testConsentRequestData.getTitle())
                        .state(StateEnum.PENDING)
                        .vinLabel(new VIN(testVin).label()));
    }

}
