package com.here.platform.cm.steps.remove;

import com.here.platform.cm.controllers.PrivateController;
import com.here.platform.cm.steps.api.RemoveEntitiesSteps;
import com.here.platform.cm.steps.api.UserAccountSteps;
import com.here.platform.ns.dto.Providers;
import com.here.platform.ns.helpers.Steps;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;


public class ConsentRequestRemoveExtension2 implements AfterEachCallback {

    @Override
    @Step("Clean Up test data")
    public void afterEach(ExtensionContext context) {
        String testId = Allure.getLifecycle().getCurrentTestCase().get();
        ConsentCollector
                .getAllConsentsWithVin(testId)
                .forEach(consentVin -> RemoveEntitiesSteps.forceRemoveConsentsFromConsentRequest(consentVin.getCrid(),
                        consentVin.getVin()));
        ConsentCollector
                .getAllConsents(testId)
                .forEach(RemoveEntitiesSteps::forceRemoveEmptyConsentRequest);
        ConsentCollector
                .getAllApplications(testId)
                .forEach(app -> new PrivateController()
                        .withCMToken()
                        .deleteProviderApplication(app));
        ConsentCollector.getConsumer(testId).forEach(RemoveEntitiesSteps::removeConsumer);
        ConsentCollector.getProvider(testId).forEach(RemoveEntitiesSteps::removeProvider);
        ConsentCollector
                .getNsContainers(testId)
                .forEach(Steps::removeRegularContainer);
        ConsentCollector
                .getNsProviders(testId)
                .stream().filter(prov -> prov.getName().contains(Providers.getDataProviderNamePrefix()))
                .forEach(Steps::removeRegularProvider);
        ConsentCollector
                .getHereAccounts(testId)
                .forEach(UserAccountSteps::removeHereAccount);
    }
}
