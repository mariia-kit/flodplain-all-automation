package com.here.platform.cm.consentStatus.accessToken;

import com.here.platform.cm.consentStatus.BaseConsentStatusTests;
import com.here.platform.cm.controllers.AccessTokenController;
import com.here.platform.cm.controllers.ConsentStatusController;
import com.here.platform.cm.enums.CMErrorResponse;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.annotations.CMFeatures.GetAccessToken;
import com.here.platform.common.annotations.ErrorHandler;
import com.here.platform.ns.dto.User;
import com.here.platform.ns.dto.Users;
import java.util.stream.Stream;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.util.StringUtils;


@GetAccessToken
@DisplayName("Getting of access tokens for consents")
public class AccessTokenErrorHandlerTests extends BaseConsentStatusTests {

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
        User mpConsumer = Users.MP_CONSUMER.getUser();
        final var accessTokenResponse = new AccessTokenController()
                .withConsumerToken()
                .getAccessToken(crid, vin, mpConsumer.getRealm());

        if (StringUtils.isEmpty(crid)) {
            new ResponseAssertion(accessTokenResponse)
                    .statusCodeIsEqualTo(StatusCode.BAD_REQUEST)
                    .expectedErrorResponse(CMErrorResponse.PARAMETER_VALIDATION);
        } else {
            new ResponseAssertion(accessTokenResponse)
                    .statusCodeIsEqualTo(StatusCode.NOT_FOUND)
                    .expectedErrorResponse(
                            CMErrorResponse.CONSENT_REQUEST_NOT_FOUND);
        }
    }

    @ParameterizedTest(name = "Is not possible to get consent status by crid: {0}, vin: {1}, should cause: {2}")
    @ErrorHandler
    @MethodSource("consentRequestIdAndVins")
    void getConsentStatusByCridAndVinErrorHandlerTest(String crid, String vin, String cause) {
        var consentStatusResponse = new ConsentStatusController()
                .withConsumerToken()
                .getConsentStatusByIdAndVin(crid, vin);

        new ResponseAssertion(consentStatusResponse)
                .statusCodeIsEqualTo(StatusCode.BAD_REQUEST)
                .expectedErrorResponse(CMErrorResponse.PARAMETER_VALIDATION);
    }


}
