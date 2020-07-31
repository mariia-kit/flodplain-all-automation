package com.here.platform.cm.steps;

import com.github.javafaker.Faker;
import com.here.platform.cm.controllers.ConsumerController;
import com.here.platform.cm.controllers.ProvidersController;
import com.here.platform.cm.enums.ConsentPageUrl;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.MPProviders;
import com.here.platform.cm.rest.model.Consumer;
import com.here.platform.cm.rest.model.Provider;
import com.here.platform.cm.rest.model.ProviderApplication;
import io.qameta.allure.Step;
import java.util.Map;


public class OnboardingSteps {

    private final String providerId, consumerId;
    private final Faker faker = new Faker();
    private final ConsumerController consumerController = new ConsumerController();
    private final ProvidersController providersController = new ProvidersController();

    public OnboardingSteps(String providerId, String consumerId) {
        this.providerId = providerId;
        this.consumerId = consumerId;
    }

    @Step
    public static void onboardApplicationProviderAndConsumer(
            String providerId,
            String consumerId,
            ConsentRequestContainers scope
    ) {
        var onboardingSteps = new OnboardingSteps(providerId, consumerId);
        onboardingSteps.onboardTestProvider();
        onboardingSteps.onboardValidConsumer();
        onboardingSteps.onboardTestProviderApplicationForScope(scope);
    }

    @Step
    public void onboardTestProviderApplicationForScope(ConsentRequestContainers container) {
        var testApplication = new ProviderApplication()
                .providerId(this.providerId)
                .consumerId(this.consumerId)
                .clientId(container.clientId)
                .clientSecret(container.clientSecret)
                .container(container.id)
                .redirectUri(ConsentPageUrl.getDaimlerCallbackUrl());

        var applicationResponse = this.providersController.onboardApplication(testApplication);
        StepExpects.expectCREATEDStatusCode(applicationResponse);
    }

    @Step("Onboard test Provider")
    public void onboardTestProvider() {
        var providerProps = Map.of(
                "authUrl", "https://api.secure.mercedes-benz.com/oidc10/auth/oauth/v2/authorize",
                "responseType", "code",
                "prompt", "consent login"
        );
        var testDataProvider = new Provider()
                .name(MPProviders.DAIMLER.getName())
                .id(this.providerId)
                .properties(providerProps);

        var providerResponse = this.providersController.onboardDataProvider(testDataProvider);
        StepExpects.expectCREATEDStatusCode(providerResponse);
    }

    @Step("Onboard valid Consumer")
    public void onboardValidConsumer() {
        var testConsumer = new Consumer()
                .consumerId(this.consumerId)
                .consumerName(this.faker.commerce().department());

        var consumerResponse = this.consumerController.onboardConsumer(testConsumer);
        StepExpects.expectOKStatusCode(consumerResponse);
    }

}
