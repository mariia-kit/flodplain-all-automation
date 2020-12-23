package com.here.platform.cm.steps.api;

import com.github.javafaker.Faker;
import com.here.platform.cm.controllers.ConsumerController;
import com.here.platform.cm.controllers.ProvidersController;
import com.here.platform.cm.enums.ConsentPageUrl;
import com.here.platform.cm.enums.ConsentRequestContainer;
import com.here.platform.cm.enums.MPProviders;
import com.here.platform.cm.rest.model.Consumer;
import com.here.platform.cm.rest.model.Provider;
import com.here.platform.cm.rest.model.ProviderApplication;
import com.here.platform.cm.steps.remove.ConsentCollector;
import io.qameta.allure.Step;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;


public class OnboardingSteps {

    private final String providerId, consumerId;
    private final Faker faker = new Faker();
    private final ConsumerController consumerController = new ConsumerController();
    private final ProvidersController providersController = new ProvidersController();
    private final MPProviders targetProvider = MPProviders.DAIMLER;
    private String
            providerTypeName = targetProvider.getName(),
            providerAuthUrl = targetProvider.getAuthUrl(),
            providerTokenUrl = StringUtils.EMPTY;

    public OnboardingSteps(String providerId, String consumerId) {
        this.providerId = providerId;
        this.consumerId = consumerId;
    }

    public OnboardingSteps(MPProviders provider, String consumerId) {
        this.providerId = provider.getName();
        this.providerTypeName = provider.getType();
        this.providerAuthUrl = provider.getAuthUrl();
        this.providerTokenUrl = provider.getTokenUrl();
        this.consumerId = consumerId;
    }

    @Step
    public static void onboardApplicationProviderAndConsumer(
            String providerId,
            String consumerId,
            ConsentRequestContainer providerApplication
    ) {
        var onboardingSteps = new OnboardingSteps(providerId, consumerId);
        onboardingSteps.onboardTestProvider();
        onboardingSteps.onboardValidConsumer();
        onboardingSteps.onboardTestProviderApplication(providerApplication);
    }

    @Step
    public void onboardTestProviderApplication(ConsentRequestContainer container) {
        onboardTestProviderApplication(container.getId(), container.getClientId(), container.getClientSecret());
    }

    @Step("Onboard application for current provide for {containerId}")
    public void onboardTestProviderApplication(String containerId, String clientId, String secret) {
        var testApplication = new ProviderApplication()
                .providerId(this.providerId)
                .consumerId(this.consumerId)
                .clientId(clientId)
                .clientSecret(secret)
                .containerId(containerId)
                .redirectUri(ConsentPageUrl.getDaimlerCallbackUrl());

        var applicationResponse = this.providersController
                .withConsumerToken()
                .onboardApplication(testApplication);
        StatusCodeExpects.expectCREATEDStatusCode(applicationResponse);
    }

    @Step("Onboard test Provider")
    public void onboardTestProvider() {
        var providerProps = Map.of(
                "tokenUrl", providerTokenUrl,
                "authUrl", providerAuthUrl,
                "responseType", "code",
                "prompt", "consent login"
        );
        var testDataProvider = new Provider()
                .name(providerTypeName)
                .id(this.providerId)
                .properties(providerProps);

        var providerResponse = this.providersController
                .withConsumerToken()
                .onboardDataProvider(testDataProvider);
        ConsentCollector.addProvider(providerId);
        StatusCodeExpects.expectCREATEDStatusCode(providerResponse);
    }

    @Step("Onboard valid Consumer")
    public void onboardValidConsumer() {
        onboardConsumer(faker.commerce().department());
    }

    @Step("Onboard valid Consumer")
    public void onboardConsumer(String consumerName) {
        var testConsumer = new Consumer()
                .consumerId(this.consumerId)
                .consumerName(consumerName);

        var consumerResponse = this.consumerController
                .withConsumerToken()
                .onboardDataConsumer(testConsumer);
        ConsentCollector.addConsumer(consumerId);
        StatusCodeExpects.expectOKStatusCode(consumerResponse);
    }

}
