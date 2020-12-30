package com.here.platform.cm.steps.remove;

import com.here.platform.cm.steps.api.RemoveEntitiesSteps;
import com.here.platform.cm.steps.api.UserAccountSteps;
import com.here.platform.ns.helpers.Steps;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;


public class ConsentRequestRemoveExtension2 implements AfterAllCallback {

    @Override
    public void afterAll(ExtensionContext context) {
        ConsentCollector
                .getAllConsentsWithVin()
                .forEach(consentVin -> RemoveEntitiesSteps.forceRemoveConsentsFromConsentRequest(consentVin.getCrid(),
                        consentVin.getVin()));
        ConsentCollector
                .getAllConsents()
                .forEach(RemoveEntitiesSteps::forceRemoveEmptyConsentRequest);
        ConsentCollector
                .getAllApplications()
                .forEach(RemoveEntitiesSteps::removeProviderApplication);
        ConsentCollector.getConsumer().forEach(RemoveEntitiesSteps::removeConsumer);
        ConsentCollector.getProvider().forEach(RemoveEntitiesSteps::removeProvider);
        ConsentCollector
                .getContainers()
                .forEach(Steps::removeRegularContainer);
    }
}
