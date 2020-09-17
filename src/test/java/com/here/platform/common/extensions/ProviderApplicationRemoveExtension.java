package com.here.platform.common.extensions;

import com.here.platform.cm.rest.model.ConsentRequestData;
import com.here.platform.cm.steps.api.RemoveEntitiesSteps;
import lombok.Builder;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;


@Builder
public class ProviderApplicationRemoveExtension implements AfterEachCallback {

    private final ConsentRequestData consentRequestData;

    @Override
    public void afterEach(ExtensionContext context) {
        RemoveEntitiesSteps.forceRemoveApplicationProviderConsumerEntities(consentRequestData);
    }

}
