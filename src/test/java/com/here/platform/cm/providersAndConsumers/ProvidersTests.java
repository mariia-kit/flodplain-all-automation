package com.here.platform.cm.providersAndConsumers;

import com.here.platform.cm.BaseCMTest;
import com.here.platform.cm.enums.CMErrorResponse;
import com.here.platform.cm.enums.ConsentManagementServiceUrl;
import com.here.platform.cm.enums.ConsentObject;
import com.here.platform.cm.enums.ConsentRequestContainer;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.MPProviders;
import com.here.platform.cm.rest.model.Provider;
import com.here.platform.cm.steps.api.ConsentRequestSteps;
import com.here.platform.cm.steps.api.RemoveEntitiesSteps;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.annotations.CMFeatures.OnBoardProvider;
import com.here.platform.ns.dto.User;
import com.here.platform.ns.dto.Users;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


@OnBoardProvider
@DisplayName("Onboard Provider")
class ProvidersTests extends BaseCMTest {

    @Test
    @Tag("smoke_cm")
    @DisplayName("Verify success on-boarding of a Data Provider")
    void onboardDataProvider() {
        var testDataProvider = new Provider()
                .name("daimler")
                .id(crypto.md5())
                .properties(Map.of());

        var onboardDataProvider = providerController
                .withCMToken()
                .onboardDataProvider(testDataProvider);

        new ResponseAssertion(onboardDataProvider)
                .statusCodeIsEqualTo(StatusCode.CREATED)
                .responseIsEmpty();

        var getProviderResponse = providerController
                .withConsumerToken()
                .getProviderById(testDataProvider.getId());
        new ResponseAssertion(getProviderResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObject(testDataProvider);

        RemoveEntitiesSteps.removeProvider(testDataProvider.getId());

        getProviderResponse = providerController
                .withConsumerToken()
                .getProviderById(testDataProvider.getId());
        new ResponseAssertion(getProviderResponse)
                .statusCodeIsEqualTo(StatusCode.NOT_FOUND)
                .expectedErrorResponse(CMErrorResponse.PROVIDER_NOT_FOUND);
    }

    @Test
    @DisplayName("Verify redirect to the Data Provider OAUTH")
    void dataProviderRedirectTest() {
        MPProviders provider = MPProviders.DAIMLER_EXPERIMENTAL;
        User mpConsumer = Users.MP_CONSUMER.getUser();
        ConsentRequestContainer targetContainer = ConsentRequestContainers.generateNew(provider);

        ConsentObject consentObj = new ConsentObject(mpConsumer, provider, targetContainer);
        var crid = new ConsentRequestSteps(consentObj)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .getId();

        var redirectToDataProviderResponse = providerController
                .redirectToDataProviderByRequestId(crid, ConsentManagementServiceUrl.getEnvUrl());

        new ResponseAssertion(redirectToDataProviderResponse)
                .statusCodeIsEqualTo(StatusCode.REDIRECT)
                .responseIsEmpty();
    }

    @Test
    @DisplayName("Verify redirect to the Data Provider OAUTH Reference")
    void dataProviderRedirectTestReference() {
        MPProviders provider = MPProviders.DAIMLER_REFERENCE;
        User mpConsumer = Users.MP_CONSUMER.getUser();
        ConsentRequestContainer targetContainer = ConsentRequestContainers.generateNew(provider);

        ConsentObject consentObj = new ConsentObject(mpConsumer, provider, targetContainer);
        var crid = new ConsentRequestSteps(consentObj)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .getId();

        var redirectToDataProviderResponse = providerController
                .redirectToDataProviderByRequestId(crid, ConsentManagementServiceUrl.getEnvUrl());

        new ResponseAssertion(redirectToDataProviderResponse)
                .statusCodeIsEqualTo(StatusCode.REDIRECT)
                .responseIsEmpty();
    }

    @Test
    @DisplayName("Verify on-boarding of a Data Provider with invalid name")
    void onboardDataProviderNotValidName() {
        var testDataProvider = new Provider()
                .name(crypto.md5())
                .id(crypto.md5())
                .properties(Map.of());

        var onboardDataProvider = providerController
                .withCMToken()
                .onboardDataProvider(testDataProvider);

        new ResponseAssertion(onboardDataProvider)
                .expectedErrorResponse(CMErrorResponse.PARAMETER_VALIDATION);
        new ResponseAssertion(onboardDataProvider)
                .expectedErrorCause("Property 'provider.name' has not supported provider name");

    }

}
