package com.here.platform.common.extension;

import com.here.platform.cm.controllers.ConsumerController;
import com.here.platform.cm.controllers.ProvidersController;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.rest.model.ConsentRequestData;
import com.here.platform.cm.rest.model.Consumer;
import com.here.platform.cm.rest.model.Provider;
import com.here.platform.cm.steps.api.OnboardingSteps;
import lombok.Builder;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;


@Builder
public class OnboardApplicationExtension implements BeforeEachCallback {

    private final ConsentRequestData consentRequestData;

    @Override
    public void beforeEach(ExtensionContext context) {
        OnboardingSteps.onboardApplicationProviderAndConsumer(
                consentRequestData.getProviderId(),
                consentRequestData.getConsumerId(),
                ConsentRequestContainers.getById(consentRequestData.getContainerId())
        );
    }

    public Consumer getOnboardedConsumer() {
        return new ConsumerController().getConsumerById(consentRequestData.getConsumerId()).as(Consumer.class);
    }

    public Provider getOnboardedProvider() {
        return new ProvidersController().getProviderById(consentRequestData.getProviderId()).as(Provider.class);
    }

}
