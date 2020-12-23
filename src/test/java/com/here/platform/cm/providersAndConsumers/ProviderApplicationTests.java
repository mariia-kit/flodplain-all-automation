package com.here.platform.cm.providersAndConsumers;

import com.here.platform.cm.BaseCMTest;
import com.here.platform.cm.controllers.ConsumerController;
import com.here.platform.cm.enums.CMErrorResponse;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.MPProviders;
import com.here.platform.cm.rest.model.ConsentRequestData;
import com.here.platform.cm.rest.model.Consumer;
import com.here.platform.cm.rest.model.Provider;
import com.here.platform.cm.rest.model.ProviderApplication;
import com.here.platform.cm.steps.api.OnboardingSteps;
import com.here.platform.cm.steps.api.RemoveEntitiesSteps;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.annotations.Sentry;
import com.here.platform.common.config.Conf;
import com.here.platform.ns.dto.Users;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


@Feature("On-boarding")
@DisplayName("Onboard Application")
public class ProviderApplicationTests extends BaseCMTest {

    private String
            testDataProviderId = null,
            testDataConsumerId = null;

    @AfterEach
    void cleanUpProviderOrConsumer() {
        RemoveEntitiesSteps.removeProvider(testDataProviderId);
        RemoveEntitiesSteps.removeConsumer(testDataConsumerId);
    }

    @Test
    @DisplayName("Verify successful Data Provider Application on-boarding")
    @Tag("smoke_cm")
    void onboardDataProviderApplicationTest() {
        var testContainer = ConsentRequestContainers.getNextDaimlerExperimental();
        var targetConsumer = Users.MP_CONSUMER.getUser();
        new OnboardingSteps(testContainer.getProvider(), targetConsumer.getRealm())
                .onboardTestProvider();

        var testConsentRequest = new ConsentRequestData()
                .consumerId(targetConsumer.getRealm())
                .providerId(testContainer.provider.getName())
                .title(Conf.cm().getQaTestDataMarker() + faker.gameOfThrones().quote())
                .purpose(faker.commerce().productName())
                .privacyPolicy(faker.internet().url())
                .containerId(testContainer.id);

        var testApplication = new ProviderApplication()
                .providerId(testConsentRequest.getProviderId())
                .consumerId(testConsentRequest.getConsumerId())
                .clientId(crypto.sha1())
                .clientSecret(crypto.sha1())
                .containerId(crypto.md5())
                .redirectUri(faker.internet().url());

        var applicationResponse = providerController
                .withCMToken()
                .onboardApplication(testApplication);
        new ResponseAssertion(applicationResponse)
                .statusCodeIsEqualTo(StatusCode.CREATED)
                .responseIsEmpty();

        var providerApplications = new ResponseAssertion(providerController.getListOfApplications())
                .statusCodeIsEqualTo(StatusCode.OK)
                .bindAsListOf(ProviderApplication[].class);
        Assertions.assertThat(providerApplications).contains(testApplication);

        RemoveEntitiesSteps.removeProviderApplication(testApplication);

        providerApplications = new ResponseAssertion(providerController.getListOfApplications())
                .statusCodeIsEqualTo(StatusCode.OK)
                .bindAsListOf(ProviderApplication[].class);
        Assertions.assertThat(providerApplications).doesNotContain(testApplication);
    }

    @Test
    @DisplayName("Is not possible to onboard application without onboarded Data Consumer")
    @Issue("NS-2777")
    void isNotPossibleToOnboardApplicationWithoutOnboardedDataConsumer() {
        var testDataProvider = new Provider()
                .id(crypto.md5())
                .name(MPProviders.DAIMLER_EXPERIMENTAL.getName())
                .properties(Map.of());

        var onboardDataProvider = providerController
                .withConsumerToken()
                .onboardDataProvider(testDataProvider);
        new ResponseAssertion(onboardDataProvider)
                .statusCodeIsEqualTo(StatusCode.CREATED)
                .responseIsEmpty();

        testDataProviderId = testDataProvider.getId();

        var testProviderApplication = new ProviderApplication()
                .providerId(testDataProviderId)
                .clientId(crypto.md5())
                .clientSecret(crypto.md5())
                .containerId(crypto.sha256())
                .consumerId(crypto.sha512())
                .redirectUri(faker.internet().url());

        var onboardApplication = providerController
                .withCMToken()
                .onboardApplication(testProviderApplication);

        new ResponseAssertion(onboardApplication)
                .statusCodeIsEqualTo(StatusCode.NOT_FOUND);
    }

    @Test
    @DisplayName("Is not possible to onboard application without onboarded Data Provider")
    void isNotPossibleToOnboardApplicationWithoutOnboardedDataProvider() {
        var testDataConsumer = new Consumer()
                .consumerId(crypto.md5())
                .consumerName(faker.commerce().department());

        var onboardDataConsumer = new ConsumerController()
                .withConsumerToken()
                .onboardDataConsumer(testDataConsumer);
        new ResponseAssertion(onboardDataConsumer)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEmpty();

        testDataConsumerId = testDataConsumer.getConsumerId();

        var testProviderApplication = new ProviderApplication()
                .providerId(crypto.md5())
                .clientId(crypto.md5())
                .clientSecret(crypto.md5())
                .containerId(crypto.sha256())
                .consumerId(testDataConsumerId)
                .redirectUri(faker.internet().url());

        var onboardApplication = providerController
                .withCMToken()
                .onboardApplication(testProviderApplication);

        new ResponseAssertion(onboardApplication)
                .statusCodeIsEqualTo(StatusCode.NOT_FOUND)
                .expectedErrorResponse(CMErrorResponse.PROVIDER_NOT_FOUND);
    }

    @Test
    @Sentry
    @DisplayName("Is not possible to onboard Provider Application with empty Authorization")
    void isNotPossibleToOnboardProviderApplicationWithInvalidAuthorizationTokenValue() {
        var onboardApplication = providerController
                .withAuthorizationValue("")
                .onboardApplication(new ProviderApplication());

        new ResponseAssertion(onboardApplication)
                .statusCodeIsEqualTo(StatusCode.UNAUTHORIZED);
    }

    @Test
    @Sentry
    @DisplayName("Possible to onboard Provider Application with CM application token")
    void possibleToOnboardProviderApplicationWithCMAuthorizationTokenValue() {
        var onboardApplication = providerController
                .withCMToken()
                .onboardApplication(new ProviderApplication());

        new ResponseAssertion(onboardApplication)
                .statusCodeIsEqualTo(StatusCode.BAD_REQUEST)
                .expectedErrorResponse(CMErrorResponse.PARAMETER_VALIDATION);
    }

}
