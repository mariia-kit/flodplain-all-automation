package com.here.platform.cm.consentStatus;

import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.annotations.CMFeatures.GetAccessToken;
import com.here.platform.common.annotations.ErrorHandler;
import com.here.platform.cm.controllers.AccessTokenController;
import com.here.platform.cm.controllers.ConsentStatusController;
import com.here.platform.cm.enums.CMErrorResponse;
import com.here.platform.cm.enums.MPConsumers;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;


@DisplayName("Getting of access tokens for consents")
@GetAccessToken
public class AccessTokenErrorHandlerTests extends BaseConsentStatusTests {

    private final ConsentStatusController consentStatusController = new ConsentStatusController()
            .withConsumerToken(MPConsumers.OLP_CONS_1);

    static Stream<Arguments> consentRequestIdAndVins() {
        return Stream.of(
                Arguments.of("", "", "consentRequestId"),
                Arguments.of("test", "", "vin"),
                Arguments.of("", "test", "consentRequestId")
        );
    }

    @ParameterizedTest(name = "Is not possible to get access token with crid: {0}, vin: {1}, should cause: {2}")
    @ErrorHandler
    @MethodSource("consentRequestIdAndVins")
    void accessTokenErrorHandlerTest(String crid, String vin, String cause) {
        var accessTokenController = new AccessTokenController();
        accessTokenController.withCMToken();
        final var accessTokenResponse = accessTokenController.getAccessToken(crid, vin, testConsumerId);

        new ResponseAssertion(accessTokenResponse)
                .statusCodeIsEqualTo(StatusCode.NOT_FOUND) //TODO should be StatusCode.BAD_REQUEST
                .expectedErrorResponse(
                        CMErrorResponse.CONSENT_REQUEST_NOT_FOUND); //TODO should be CMErrorResponse.PARAMETER_VALIDATION
    }

    @ParameterizedTest(name = "Is not possible to get consent status by crid: {0}, vin: {1}, should cause: {2}")
    @ErrorHandler
    @MethodSource("consentRequestIdAndVins")
    void getConsentStatusByCridAndVinErrorHandlerTest(String crid, String vin, String cause) {
        final var consentStatusResponse = consentStatusController.getConsentStatusByIdAndVin(crid, vin);

        new ResponseAssertion(consentStatusResponse)
                .statusCodeIsEqualTo(StatusCode.BAD_REQUEST)
                .expectedErrorResponse(CMErrorResponse.PARAMETER_VALIDATION);
    }


}
