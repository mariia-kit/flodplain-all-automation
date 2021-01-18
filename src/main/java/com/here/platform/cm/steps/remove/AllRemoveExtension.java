package com.here.platform.cm.steps.remove;

import com.here.platform.aaa.BearerAuthorization;
import com.here.platform.cm.controllers.PrivateController;
import com.here.platform.cm.steps.api.RemoveEntitiesSteps;
import com.here.platform.cm.steps.api.UserAccountSteps;
import com.here.platform.mp.steps.api.MarketplaceSteps;
import com.here.platform.ns.dto.Providers;
import com.here.platform.ns.helpers.CleanUpHelper;
import com.here.platform.ns.helpers.Steps;
import com.here.platform.ns.restEndPoints.external.AaaCall;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;


public class AllRemoveExtension implements AfterEachCallback {

    @Override
    @Step("Clean Up test data")
    public void afterEach(ExtensionContext context) {
        String testId = Allure.getLifecycle().getCurrentTestCase().get();
        DataForRemoveCollector
                .getAllConsentsWithVin(testId)
                .forEach(consentVin -> RemoveEntitiesSteps.forceRemoveConsentsFromConsentRequest(consentVin.getKey(),
                        consentVin.getValue()));
        DataForRemoveCollector
                .getAllConsents(testId)
                .forEach(RemoveEntitiesSteps::forceRemoveEmptyConsentRequest);
        String cmUserToken = BearerAuthorization.init().getCmUserToken();
        DataForRemoveCollector
                .getAllApplications(testId)
                .forEach(app -> new PrivateController()
                        .withAuthorizationValue(cmUserToken)
                        .deleteProviderApplication(app));
        DataForRemoveCollector.getConsumer(testId).forEach(RemoveEntitiesSteps::removeConsumer);
        DataForRemoveCollector.getProvider(testId).forEach(RemoveEntitiesSteps::removeProvider);

        DataForRemoveCollector
                .getAllMpSubs(testId)
                .forEach(id -> new MarketplaceSteps().beginCancellation(id));
        DataForRemoveCollector
                .getAllMpListings(testId)
                .forEach(id -> new MarketplaceSteps().deleteListing(id));

        DataForRemoveCollector
                .getAllArtificialPolicy(testId)
                .forEach(policy -> new AaaCall().removeResourcePermission(policy.getKey(), policy.getValue()));
        //TODO: remove after bam remove policy fix
        DataForRemoveCollector
                .getNsContainers(testId)
                .forEach(container -> new AaaCall().deletePolicyForContainer(container));

        DataForRemoveCollector
                .getNsContainers(testId)
                .forEach(Steps::removeRegularContainer);
        DataForRemoveCollector
                .getNsProviders(testId)
                .stream().filter(prov -> prov.getName().contains(Providers.getDataProviderNamePrefix()))
                .forEach(Steps::removeRegularProvider);
        DataForRemoveCollector
                .getHereAccounts(testId)
                .forEach(UserAccountSteps::removeHereAccount);
    }
}
