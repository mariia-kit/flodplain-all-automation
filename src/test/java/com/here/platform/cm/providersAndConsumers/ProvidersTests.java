package com.here.platform.cm.providersAndConsumers;

import static com.here.platform.cm.steps.api.RemoveEntitiesSteps.forceRemoveApplicationProviderConsumerEntities;

import com.here.platform.cm.BaseCMTest;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.annotations.CMFeatures.OnBoardProvider;
import com.here.platform.cm.enums.ConsentManagementServiceUrl;
import com.here.platform.cm.enums.ConsentPageUrl;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.MPConsumers;
import com.here.platform.cm.enums.MPProviders;
import com.here.platform.cm.rest.model.ConsentRequestData;
import com.here.platform.cm.rest.model.ConsentRequestIdResponse;
import com.here.platform.cm.rest.model.Provider;
import com.here.platform.cm.rest.model.ProviderApplication;
import com.here.platform.cm.steps.api.OnboardingSteps;
import com.here.platform.common.JConvert;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import io.qameta.allure.TmsLink;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


@DisplayName("On-board data provider")
@OnBoardProvider
@Tag("smoke_cm")
class ProvidersTests extends BaseCMTest {

    private final ConsentRequestContainers testContainer = ConsentRequestContainers.CONNECTED_VEHICLE;
    private final ConsentRequestData testConsentRequest = new ConsentRequestData()
            .consumerId(MPConsumers.OLP_CONS_1.getRealm())
            .providerId(crypto.sha1())
            .title(faker.gameOfThrones().quote())
            .purpose(faker.commerce().productName())
            .privacyPolicy(faker.internet().url())
            .containerId(testContainer.id);

    @AfterEach
    void afterEach() {
        forceRemoveApplicationProviderConsumerEntities(testConsentRequest);
    }

    @Test
    @DisplayName("Verify onboard of Data Provider")
    @TmsLink("NS-1376")
    void onboardProviderPositiveTest() {
        var providerRealmId = testConsentRequest.getProviderId();
        var consumerRealmId = testConsentRequest.getConsumerId();

        var providerProps = Map.of(
                "authUrl", "https://api.secure.mercedes-benz.com/oidc10/auth/oauth/v2/authorize",
                "responseType", "code",
                "prompt", "consent login"
        );
        var dataProviderBody = new Provider()
                .name(MPProviders.DAIMLER_EXPERIMENTAL.getName())
                .id(providerRealmId)
                .properties(providerProps);

        var providerResponse = providerController.onboardDataProvider(dataProviderBody);
        new ResponseAssertion(providerResponse).statusCodeIsEqualTo(StatusCode.CREATED).responseIsEmpty();

        var testApplication = new ProviderApplication()
                .providerId(providerRealmId)
                .consumerId(consumerRealmId)
                .clientId(crypto.sha1())
                .clientSecret(crypto.sha1())
                .container(testContainer.id)
                .redirectUri(ConsentPageUrl.getDaimlerCallbackUrl());

        var applicationResponse = providerController.onboardApplication(testApplication);
        new ResponseAssertion(applicationResponse).statusCodeIsEqualTo(StatusCode.CREATED).responseIsEmpty();

        var providerApplications = new JConvert(providerController.getListOfApplications().body().asString())
                .toListOfObjects(ProviderApplication[].class);
        Assertions.assertThat(providerApplications).contains(testApplication);

        new ResponseAssertion(providerController.getProviderById(providerRealmId))
                .responseIsEqualToObject(dataProviderBody);
    }

    @Test
    @DisplayName("Verify Data Provider redirect")
    @TmsLink("NS-1377")
    void dataProviderRedirectTest() {
        testConsentRequest.providerId(MPProviders.DAIMLER_EXPERIMENTAL.getName());
        OnboardingSteps.onboardApplicationProviderAndConsumer(
                testConsentRequest.getProviderId(),
                testConsentRequest.getConsumerId(),
                testContainer
        );
        consentRequestController.withCMToken();
        var consentRequestResponse = consentRequestController.createConsentRequest(testConsentRequest);
        var crid = new ResponseAssertion(consentRequestResponse)
                .statusCodeIsEqualTo(StatusCode.CREATED)
                .bindAs(ConsentRequestIdResponse.class).getConsentRequestId();

        var redirectToDataProviderResponse = providerController
                .redirectToDataProviderByRequestId(crid, ConsentManagementServiceUrl.getEnvUrl());

        new ResponseAssertion(redirectToDataProviderResponse)
                .statusCodeIsEqualTo(StatusCode.REDIRECT)
                .responseIsEmpty();
    }

    //todo add tests for DAIMLER redirect and excelsior

}
