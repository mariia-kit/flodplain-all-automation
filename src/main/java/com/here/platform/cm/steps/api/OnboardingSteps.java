package com.here.platform.cm.steps.api;

import com.github.javafaker.Faker;
import com.here.platform.cm.controllers.ConsumerController;
import com.here.platform.cm.controllers.ProvidersController;
import com.here.platform.cm.enums.ConsentPageUrl;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.MPProviders;
import com.here.platform.cm.rest.model.Consumer;
import com.here.platform.cm.rest.model.Provider;
import com.here.platform.cm.rest.model.ProviderApplication;
import com.here.platform.ns.dto.Container;
import io.qameta.allure.Step;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;


public class OnboardingSteps {

    private final String providerId, consumerId;
    private final Faker faker = new Faker();
    private final ConsumerController consumerController = new ConsumerController();
    private final ProvidersController providersController = new ProvidersController();
    private String providerTypeName = MPProviders.DAIMLER.getName();
    private String providerAuthUrl = "https://api.secure.mercedes-benz.com/oidc10/auth/oauth/v2/authorize";
    private String providerTokenUrl = StringUtils.EMPTY;

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
            ConsentRequestContainers providerApplication
    ) {
        var onboardingSteps = new OnboardingSteps(providerId, consumerId);
        onboardingSteps.onboardTestProvider();
        onboardingSteps.onboardValidConsumer();
        onboardingSteps.onboardTestProviderApplication(providerApplication);
    }

    @Step
    public void onboardTestProviderApplication(ConsentRequestContainers container) {
        onboardTestProviderApplication(container.id, container.clientId, container.clientSecret);
    }

    @Step("Onboard application for current provide for {containerId}")
    public void onboardTestProviderApplication(String containerId, String clientId, String secret) {
        var testApplication = new ProviderApplication()
                .providerId(this.providerId)
                .consumerId(this.consumerId)
                .clientId(clientId)
                .clientSecret(secret)
                .container(containerId)
                .redirectUri(ConsentPageUrl.getDaimlerCallbackUrl());

        var applicationResponse = this.providersController.withConsumerToken().onboardApplication(testApplication);
        StepExpects.expectCREATEDStatusCode(applicationResponse);
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
        StepExpects.expectCREATEDStatusCode(providerResponse);
    }

    @Step("Onboard valid Consumer")
    public void onboardValidConsumer() {
        var testConsumer = new Consumer()
                .consumerId(this.consumerId)
                .consumerName(this.faker.commerce().department());

        var consumerResponse = this.consumerController
                .withConsumerToken()
                .onboardDataConsumer(testConsumer);
        StepExpects.expectOKStatusCode(consumerResponse);
    }

}
