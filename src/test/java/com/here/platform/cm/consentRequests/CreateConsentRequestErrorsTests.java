package com.here.platform.cm.consentRequests;

import static org.assertj.core.api.Assertions.assertThat;

import com.here.platform.cm.BaseCMTest;
import com.here.platform.cm.enums.CMErrorResponse;
import com.here.platform.cm.enums.ConsentObject;
import com.here.platform.cm.enums.ConsentRequestContainer;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.MPProviders;
import com.here.platform.cm.rest.model.ConsentRequestData;
import com.here.platform.cm.steps.api.OnboardingSteps;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.annotations.CMFeatures.CreateConsentRequest;
import com.here.platform.common.annotations.ErrorHandler;
import com.here.platform.common.annotations.Sentry;
import com.here.platform.ns.dto.User;
import io.qameta.allure.Issue;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


@DisplayName("Create consent request")
@CreateConsentRequest
class CreateConsentRequestErrorsTests extends BaseCMTest {

    @Test
    @ErrorHandler
    @Issue("NS-3048")
    @DisplayName("Verify It Is Not Possible To Create ConsentRequest With Out Consumer")
    void isNotPossibleToCreateConsentRequestWithOutConsumer() {
        MPProviders provider = MPProviders.DAIMLER_REFERENCE;
        ConsentRequestContainer testContainer = ConsentRequestContainers.generateNew(provider);
        User mpConsumer = new User().withRealm(crypto.sha1());
        ConsentObject consentObj = new ConsentObject(mpConsumer, provider, testContainer);

        final var actualResponse = consentRequestController
                .withConsumerToken()
                .createConsentRequest(consentObj.getConsentRequestData());

        var actualCause = new ResponseAssertion(actualResponse)
                .statusCodeIsEqualTo(StatusCode.NOT_FOUND)
                .expectedErrorResponse(CMErrorResponse.PROVIDER_APPLICATION_NOT_FOUND)
                .getCause();
        assertThat(actualCause)
                .startsWith("Couldn't find provider application by id: ProviderApplicationPK")
                .contains(mpConsumer.getRealm())
                .contains(provider.getName())
                .contains(testContainer.getId());
    }

    @Test
    @Sentry
    @DisplayName("Verify Sentry Block ConsentRequest Creation")
    void sentryBlockConsentRequestCreationTest() {
        MPProviders provider = MPProviders.DAIMLER_REFERENCE;
        ConsentRequestContainer testContainer = ConsentRequestContainers.generateNew(provider);
        User mpConsumer = new User().withRealm(crypto.sha1());
        ConsentObject consentObj = new ConsentObject(mpConsumer, provider, testContainer);

        final var actualResponse = consentRequestController
                .createConsentRequest(consentObj.getConsentRequestData());

        new ResponseAssertion(actualResponse).statusCodeIsEqualTo(StatusCode.UNAUTHORIZED);
    }

    @Test
    @Sentry
    @DisplayName("Is  not possible to create consent request with application token")
    void isNotPossibleToCreateConsentRequestWithApplicationToken() {
        MPProviders provider = MPProviders.DAIMLER_REFERENCE;
        ConsentRequestContainer testContainer = ConsentRequestContainers.generateNew(provider);
        User mpConsumer = new User().withRealm(crypto.sha1());
        ConsentObject consentObj = new ConsentObject(mpConsumer, provider, testContainer);

        final var actualResponse = consentRequestController
                .withCMToken()
                .createConsentRequest(consentObj.getConsentRequestData());

        new ResponseAssertion(actualResponse).statusCodeIsEqualTo(StatusCode.FORBIDDEN);
    }

    @Test
    @ErrorHandler
    @DisplayName("Verify Create Empty ConsentRequest is forbidden")
    void createEmptyConsentRequestTest() {
        final var actualCreateConsentRequestResponse = consentRequestController
                .withConsumerToken()
                .createConsentRequest(new ConsentRequestData());

        new ResponseAssertion(actualCreateConsentRequestResponse)
                .statusCodeIsEqualTo(StatusCode.BAD_REQUEST)
                .expectedErrorResponse(CMErrorResponse.CONSENT_REQUEST_VALIDATION);
    }

    @Test
    @ErrorHandler
    @Issue("NS-3048")
    @DisplayName("Verify It Is Not Possible To Create ConsentRequest With Out Provider")
    void isNotPossibleToCreateConsentRequestWithOutProvider() {
        MPProviders provider = MPProviders.DAIMLER_REFERENCE;
        ConsentRequestContainer testContainer = ConsentRequestContainers.generateNew(provider);
        User mpConsumer = new User().withRealm(crypto.sha1());
        ConsentObject consentObj = new ConsentObject(mpConsumer, provider, testContainer);

        new OnboardingSteps(StringUtils.EMPTY, mpConsumer.getRealm()).onboardValidConsumer();

        final var actualResponse = consentRequestController
                .withConsumerToken()
                .createConsentRequest(consentObj.getConsentRequestData());

        var actualCause = new ResponseAssertion(actualResponse)
                .statusCodeIsEqualTo(StatusCode.NOT_FOUND)
                .expectedErrorResponse(CMErrorResponse.PROVIDER_APPLICATION_NOT_FOUND)
                .getCause();
        assertThat(actualCause)
                .startsWith("Couldn't find provider application by id: ProviderApplicationPK")
                .contains(mpConsumer.getRealm())
                .contains(provider.getName())
                .contains(testContainer.getId());
    }

}
