package com.here.platform.common.extensions;

import com.here.platform.cm.controllers.ConsumerController;
import com.here.platform.cm.controllers.ProvidersController;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.rest.model.ConsentRequestData;
import com.here.platform.cm.rest.model.Consumer;
import com.here.platform.cm.rest.model.Provider;
import com.here.platform.cm.steps.api.OnboardingSteps;
import com.here.platform.cm.steps.api.RemoveEntitiesSteps;
import lombok.Builder;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;


@Builder
public class OnboardAndRemoveApplicationExtension implements BeforeEachCallback, AfterEachCallback {

    private final ConsentRequestData consentRequestData;
    private final boolean cleanUpAfter;

    @Override
    public void beforeEach(ExtensionContext context) {
        OnboardingSteps.onboardApplicationProviderAndConsumer(
                consentRequestData.getProviderId(),
                consentRequestData.getConsumerId(),
                ConsentRequestContainers.getById(consentRequestData.getContainerId())
        );
    }

    @Override
    public void afterEach(ExtensionContext context) {
        if (!cleanUpAfter) {
            return;
        }
        RemoveEntitiesSteps.forceRemoveApplicationProviderConsumerEntities(consentRequestData);
    }

    public Consumer getOnboardedConsumer() {
        return new ConsumerController().getConsumerById(consentRequestData.getConsumerId()).as(Consumer.class);
    }

    public Provider getOnboardedProvider() {
        return new ProvidersController().getProviderById(consentRequestData.getProviderId()).as(Provider.class);
    }

}
